package com.example.quizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private List<TestModel> testList;

    public TestAdapter(List<TestModel> testList) {
        this.testList = testList;
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.ViewHolder holder, int position) {
        String testID = testList.get(position).getId();
        holder.setData(position,testID,this);
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView testName;
        private ImageView delTestB;
        private Dialog dialogProgress;
        private TextView txtDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.catName);
            delTestB = itemView.findViewById(R.id.catDel);

            dialogProgress = new Dialog(itemView.getContext());
            dialogProgress.setContentView(R.layout.loading_dialog_layout);
            dialogProgress.setCancelable(false);
            dialogProgress.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void setData(final int pos, final String testID,TestAdapter testAdapter){
            testName.setText(testList.get(pos).getId());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TestActivity.selected_test_index = pos;

                    Intent intent = new Intent(itemView.getContext(),QuestionActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            delTestB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("DELETE CATEGORY")
                            .setMessage("Do you want to delete this test?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    deleteTest(pos,testID,itemView.getContext(),testAdapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0,0,50,0);
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);
                }
            });

        }
        private void deleteTest(int pos, String testID, Context context, TestAdapter testAdapter){
            dialogProgress.show();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Map<String, Object> testData = new ArrayMap<>();
            int index = 1;
            for (int i=0;i<testList.size();i++){
                if (i != pos){
                    testData.put("TEST" + String.valueOf(index) + "_ID",testList.get(i).getId());
                    testData.put("TEST" + String.valueOf(index) + "_TIME",testList.get(i).getTime());
                    index++;
                }
            }
            Map<String, Object> testDataCount = new ArrayMap<>();
            testDataCount.put("NO_OF_TEST",index-1);


            firebaseFirestore.collection("QUIZ").document(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId())
                    .collection("TESTS_LIST").document("TESTS_INFO")
                    .update(testData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Delete thành công",Toast.LENGTH_SHORT).show();

                            TestActivity.testID.remove(pos);

                            CategoryActivity.catList.get(CategoryActivity.selected_cat_index).setNoOfTest(TestActivity.testID.size());

                            testAdapter.notifyDataSetChanged();

                            dialogProgress.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                            dialogProgress.dismiss();
                        }
                    });
            firebaseFirestore.collection("QUIZ").document(CategoryActivity.catList.get(CategoryActivity.selected_cat_index).getId())
                    .update(testDataCount)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Cập nhật thành công",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }
}
