package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView quesView;
    private Button btnAddQues,btnCancelAddQues;
    public static List<QuestionModel> quesList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private Dialog loadingDialog;
    private QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Toolbar toolbar = findViewById(R.id.qa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Question");

        quesView = findViewById(R.id.rcl_ques);
        btnAddQues = findViewById(R.id.btnAddQues);
        btnCancelAddQues=findViewById(R.id.btnCancelAddQues);


        firebaseFirestore = FirebaseFirestore.getInstance();

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog_layout);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        btnCancelAddQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionActivity.this.finish();
            }
        });

        btnAddQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionActivity.this,QuestionDetailActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        quesView.setLayoutManager(layoutManager);

        loadQuestion();
    }

    private void loadQuestion() {
        quesList.clear();

        loadingDialog.show();

        firebaseFirestore.collection("Questions")
                .whereEqualTo("CATEGORY",CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId())
                .whereEqualTo("TEST",TestActivity.testID.get(TestActivity.selected_test_index).getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            quesList.add(new QuestionModel(
                                    documentSnapshot.getString("quesID"),
                                    documentSnapshot.getString("QUESTION"),
                                    documentSnapshot.getString("A"),
                                    documentSnapshot.getString("B"),
                                    documentSnapshot.getString("C"),
                                    documentSnapshot.getString("D"),
                                    documentSnapshot.getLong("ANSWER").intValue()

                            ));
                        }
                        questionAdapter = new QuestionAdapter(quesList);

                        quesView.setAdapter(questionAdapter);

                        loadingDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });


    }


    @Override
    protected void onResume() {
        super.onResume();

        if (questionAdapter != null){
            questionAdapter.notifyDataSetChanged();
        }
    }
}