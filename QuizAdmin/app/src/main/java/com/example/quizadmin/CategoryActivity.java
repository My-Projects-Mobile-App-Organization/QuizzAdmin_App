package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rclCat;
    private Button btnAdd;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index=0;

    private FirebaseFirestore firebaseFirestore;
    private CategoryAdapter categoryAdapter;
    private Dialog dialogProgress, addDialog;
    private TextView txtDialog;
    private Button btnDialogAdd;
    private EditText edtAddName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        rclCat = findViewById(R.id.rcl_cat);
        btnAdd = findViewById(R.id.btnAddCat);

        firebaseFirestore = FirebaseFirestore.getInstance();

        dialogProgress = new Dialog(CategoryActivity.this);
        dialogProgress.setContentView(R.layout.loading_dialog_layout);
        dialogProgress.setCancelable(false);
        dialogProgress.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        txtDialog = (TextView) dialogProgress.findViewById(R.id.dialog_text);
        txtDialog.setText("Loading.....");

        addDialog = new Dialog(CategoryActivity.this);
        addDialog.setContentView(R.layout.add_cat_dialog_layout);
        addDialog.setCancelable(true);
        addDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        edtAddName = addDialog.findViewById(R.id.edtName);
        btnDialogAdd = addDialog.findViewById(R.id.btnAdd);


        loadData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                dialogProgress.dismiss();
            }

            @Override
            public void onFailure() {
                dialogProgress.dismiss();
                Toast.makeText(CategoryActivity.this,"Không có cat nào trong db",Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rclCat.setLayoutManager(layoutManager);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtAddName.getText().clear();
                addDialog.show();
            }
        });

        btnDialogAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCategory(edtAddName.getText().toString());
            }
        });
    }

    private void loadData(MyCompleteListener myCompleteListener){
        catList.clear();

        dialogProgress.show();
        firebaseFirestore.collection("QUIZ").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            docList.put(doc.getId(), doc);
                        }
                        QueryDocumentSnapshot catListDoc = docList.get("Categorories");

                        long catCount = catListDoc.getLong("COUNT");

                        for (int i=1;i<=catCount;i++){
                            String catID = catListDoc.getString("CAT" + String.valueOf(i) +"_ID");
                            String catName = catListDoc.getString("CAT" + String.valueOf(i) + "_NAME");

                            catList.add(new CategoryModel(catID, catName, 0,1));
                        }
                        categoryAdapter = new CategoryAdapter(catList);
                        rclCat.setAdapter(categoryAdapter);

                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });


    }

    private void addNewCategory(String catName){
        addDialog.dismiss();
        dialogProgress.show();

        Map<String, Object> catData = new ArrayMap<>();

        final String doc_ID = firebaseFirestore.collection("QUIZ").document().getId();
        catData.put("CAT_ID",doc_ID);
        catData.put("NAME",catName);
        catData.put("NO_OF_TEST",0);
        catData.put("COUNTER",1);

        firebaseFirestore.collection("QUIZ").document(doc_ID)
                .set(catData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1) + "_ID", doc_ID);
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1) + "_NAME", catName);
                        catDoc.put("COUNT", catList.size() + 1);

                        firebaseFirestore.collection("QUIZ").document("Categorories")
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(CategoryActivity.this,"Update thành công",Toast.LENGTH_SHORT).show();

                                        catList.add(new CategoryModel(doc_ID,catName,0,1));

                                        categoryAdapter.notifyItemInserted(catList.size());

                                        dialogProgress.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        dialogProgress.dismiss();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        dialogProgress.dismiss();
                    }
                });
    }
}