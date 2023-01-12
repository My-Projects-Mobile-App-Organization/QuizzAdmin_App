package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private EditText ques,optionA,optionB,optionC,optionD, answerIp;
    private int answer;
    private Button btnaddQ,btnCancelQ;
    private String quesStr, opAStr,opBStr,opCStr,opDStr,ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore firebaseFirestore;
    private String action;
    private int quesID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Toolbar toolbar = findViewById(R.id.qa_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Question"+String.valueOf(QuestionActivity.quesList.size()+1));

        firebaseFirestore =FirebaseFirestore.getInstance();
        anhXa();
        loadingDialog = new Dialog(QuestionDetailActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog_layout);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        action = getIntent().getStringExtra("ACTION");

        if (action.compareTo("EDIT") == 0){
            quesID = getIntent().getIntExtra("Q_ID",0);
            loadData(quesID);
            getSupportActionBar().setTitle("Question" + String.valueOf(quesID + 1));

            btnaddQ.setText("UPDATE");
        }
        else {
            getSupportActionBar().setTitle("Question" + String.valueOf(QuestionActivity.quesList.size() + 1));
            btnaddQ.setText("ADD");
        }
        btnCancelQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionDetailActivity.this.finish();
            }
        });

        btnaddQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quesStr = ques.getText().toString();
                opAStr = optionA.getText().toString();
                opBStr = optionB.getText().toString();
                opCStr = optionC.getText().toString();
                opDStr = optionD.getText().toString();
                ansStr = answerIp.getText().toString();

                if (quesStr.isEmpty()){
                    ques.setError("Nhap question");
                    return;
                }
                if (opAStr.isEmpty()){
                    ques.setError("Nhap opA");
                    return;
                }
                if (opBStr.isEmpty()){
                    ques.setError("Nhap opB");
                    return;
                }if (opCStr.isEmpty()){
                    ques.setError("Nhap opC");
                    return;
                }
                if (opDStr.isEmpty()){
                    ques.setError("Nhap opD");
                    return;
                }
                if (ansStr.isEmpty()){
                    ques.setError("Nhap answer");
                    return;
                }
                answer = Integer.valueOf(ansStr);

                if (action.compareTo("EDIT") == 0){
                    editQues();
                } else {
                    addNewQues();
                }


            }
        });
    }

    private void loadData(int id) {
        ques.setText(QuestionActivity.quesList.get(id).getQuestion());
        optionA.setText(QuestionActivity.quesList.get(id).getOptionA());
        optionB.setText(QuestionActivity.quesList.get(id).getOptionB());
        optionC.setText(QuestionActivity.quesList.get(id).getOptionC());
        optionD.setText(QuestionActivity.quesList.get(id).getOptionD());
        answerIp.setText(String.valueOf(QuestionActivity.quesList.get(id).getCorrectAns()));
    }

    private void editQues(){
        loadingDialog.show();
        Toast.makeText(QuestionDetailActivity.this, quesID+"", Toast.LENGTH_SHORT).show();
        Toast.makeText(QuestionDetailActivity.this, QuestionActivity.quesList.get(quesID).getQuesID(), Toast.LENGTH_SHORT).show();
        Toast.makeText(QuestionDetailActivity.this, CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId(), Toast.LENGTH_SHORT).show();
        Toast.makeText(QuestionDetailActivity.this, TestActivity.testID.get(TestActivity.selected_test_index).getId(), Toast.LENGTH_SHORT).show();
        Map<String, Object> quesData1 = new ArrayMap<>();


        answer = Integer.valueOf(answerIp.getText().toString());

//        quesData1.put("quesID",QuestionActivity.quesList.get(quesID).getQuesID());
//        quesData1.put("QUESTION",quesStr);
//        quesData1.put("A",opAStr);
//        quesData1.put("B",opBStr);
//        quesData1.put("C",opCStr);
//        quesData1.put("D",opDStr);
//        quesData1.put("ANSWER",answer);
//        quesData1.put("CATEGORY",CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId());
//        quesData1.put("TEST",TestActivity.testID.get(TestActivity.selected_test_index).getId());
        quesData1.put("quesID",QuestionActivity.quesList.get(quesID).getQuesID());
        quesData1.put("QUESTION",quesStr);
        quesData1.put("A",opAStr);
        quesData1.put("B",opBStr);
        quesData1.put("C",opCStr);
        quesData1.put("D",opDStr);
        quesData1.put("ANSWER",answer);
        quesData1.put("CATEGORY",CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId());
        quesData1.put("TEST",TestActivity.testID.get(TestActivity.selected_test_index).getId());

        firebaseFirestore.collection("Questions").document(QuestionActivity.quesList.get(quesID).getQuesID())
            .update(quesData1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(QuestionDetailActivity.this, "Update thanh cong", Toast.LENGTH_SHORT).show();

                        QuestionActivity.quesList.get(quesID).setQuestion(quesStr);
                        QuestionActivity.quesList.get(quesID).setOptionA(opAStr);
                        QuestionActivity.quesList.get(quesID).setOptionB(opBStr);
                        QuestionActivity.quesList.get(quesID).setOptionC(opCStr);
                        QuestionActivity.quesList.get(quesID).setOptionD(opDStr);
                        QuestionActivity.quesList.get(quesID).setCorrectAns(answer);
                        loadingDialog.dismiss();
                        QuestionDetailActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(QuestionDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void addNewQues() {
        loadingDialog.show();
        final String ques_ID =firebaseFirestore.collection("Questions").document().getId();

        Map<String, Object> quesData = new ArrayMap<>();

        quesData.put("quesID",ques_ID);
        quesData.put("QUESTION",quesStr);
        quesData.put("A",opAStr);
        quesData.put("B",opBStr);
        quesData.put("C",opCStr);
        quesData.put("D",opDStr);
        quesData.put("ANSWER",answer);
        quesData.put("CATEGORY",CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId());
        quesData.put("TEST",TestActivity.testID.get(TestActivity.selected_test_index).getId());

        firebaseFirestore.collection("Questions").document(ques_ID)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        QuestionActivity.quesList.add(new QuestionModel(
                                ques_ID,
                                quesStr,opAStr,opBStr,opCStr,opDStr,answer
                        ));

                        Toast.makeText(QuestionDetailActivity.this, "Add thanh cong", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        QuestionDetailActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(QuestionDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void anhXa() {

        ques = findViewById(R.id.edtNameQues);
        optionA = findViewById(R.id.edtOptionA);
        optionB = findViewById(R.id.edtOptionB);
        optionC = findViewById(R.id.edtOptionC);
        optionD = findViewById(R.id.edtOptionD);
        answerIp = findViewById(R.id.edtAnswer);
        btnaddQ = findViewById(R.id.btnAddQuesDetail);
        btnCancelQ = findViewById(R.id.btnCancelAddQuesDetail);

    }
}