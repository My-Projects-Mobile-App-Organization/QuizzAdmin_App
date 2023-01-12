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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private RecyclerView testView;
    private Button btnaddTest,btnCancelAddSet;
    public static List<TestModel> testID = new ArrayList<>();
    private TestAdapter testAdapter;
    private FirebaseFirestore firebaseFirestore;
    private Dialog loadingDialog, addTestDialog;
    public static int noOfTest=0;
    private EditText edtTestName;
    private EditText edtTestTime;
    private Button btnAddTestDialog,btnCancelAddTestDialog;

    public static int selected_test_index=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        testView = findViewById(R.id.ta_rcl_test);
        btnaddTest = findViewById(R.id.btnAddSet);
        btnCancelAddSet=findViewById(R.id.btnCancelAddSet);

        Toolbar toolbar = findViewById(R.id.ta_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tests");

        loadingDialog = new Dialog(TestActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog_layout);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addTestDialog = new Dialog(TestActivity.this);
        addTestDialog.setContentView(R.layout.add_test_dialog_layout);
        addTestDialog.setCancelable(true);
        addTestDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        edtTestName = addTestDialog.findViewById(R.id.edtTestName);
        edtTestTime = addTestDialog.findViewById(R.id.edtTestTime);
        btnAddTestDialog = addTestDialog.findViewById(R.id.btnAddTest);
        btnCancelAddTestDialog = addTestDialog.findViewById(R.id.btnCancelAddTest);



        btnCancelAddSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestActivity.this.finish();
            }
        });

        btnaddTest.setText("Add New Test");
        btnCancelAddTestDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTestDialog.cancel();
            }
        });

        btnAddTestDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int testTimedl = Integer.parseInt(edtTestTime.getText().toString());
                addNewTest(edtTestName.getText().toString(),testTimedl);
            }
        });

        btnaddTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtTestName.getText().clear();
                edtTestTime.getText().clear();
                addTestDialog.show();

            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        testView.setLayoutManager(layoutManager);

        loadTest();
    }

    private void addNewTest(final String testNameAdd,final int testTimeAdd) {
        loadingDialog.show();
        addTestDialog.cancel();

        String curr_cat_id = CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId();
        int curr_counter = CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getSetCounter();
        Map<String, Object> qData = new ArrayMap<>();
        qData.put("TEST" + String.valueOf(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getSetCounter()) + "_ID",
                testNameAdd);
        qData.put("TEST" + String.valueOf(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getSetCounter()) + "_TIME",
                testTimeAdd);
        //Toast.makeText(TestActivity.this,CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getSetCounter() + "",Toast.LENGTH_SHORT).show();
        firebaseFirestore.collection("QUIZ").document(curr_cat_id)
                .collection("TESTS_LIST").document("TESTS_INFO")
                .set(qData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("COUNTER",curr_counter + 1);
                        catDoc.put("NO_OF_TEST",testID.size()+1);

                        firebaseFirestore.collection("QUIZ").document(curr_cat_id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(TestActivity.this,"Thêm test thành công",Toast.LENGTH_SHORT).show();

                                        testID.add(new TestModel(testNameAdd,testTimeAdd));
                                        CategoryActivity.catList.get(CategoryActivity.selected_cat_index)
                                                .setNoOfTest(testID.size());
                                        CategoryActivity.catList.get(CategoryActivity.selected_cat_index)
                                                .setSetCounter(curr_counter+1);

                                        testAdapter.notifyItemInserted(testID.size());
                                        loadingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismiss();
                                        Toast.makeText(TestActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(TestActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTest() {
        testID.clear();

        loadingDialog.show();

        firebaseFirestore.collection("QUIZ").document(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        noOfTest = documentSnapshot.getLong("NO_OF_TEST").intValue();
                        CategoryActivity.catList.get(CategoryActivity.selected_cat_index).setNoOfTest(noOfTest);
                        CategoryActivity.catList.get(CategoryActivity.selected_cat_index).setSetCounter(documentSnapshot.getLong("COUNTER").intValue());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


        firebaseFirestore.collection("QUIZ").document(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        for (int i=1; i<=CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getNoOfTest();i++){

                            testID.add(new TestModel(documentSnapshot.getString("TEST" + String.valueOf(i) + "_ID"),
                                    documentSnapshot.getLong("TEST" + String.valueOf(i) + "_TIME").intValue()));
                        }

                        testAdapter = new TestAdapter(testID);
                        testView.setAdapter(testAdapter);
                        loadingDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(TestActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });



    }
}