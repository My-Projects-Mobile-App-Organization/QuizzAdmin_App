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
import android.widget.Button;
import android.widget.EditText;
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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryModel> cat_list;

    public CategoryAdapter(List<CategoryModel> cat_list) {
        this.cat_list = cat_list;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
            String title = cat_list.get(position).getName();

            holder.setData(title,position,this);
    }

    @Override
    public int getItemCount() {
        return cat_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView delB;
        private Dialog dialogProgress, editDialog;
        private EditText edtUpdateName;
        private Button btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName = itemView.findViewById(R.id.catName);
            delB = itemView.findViewById(R.id.catDel);

            dialogProgress = new Dialog(itemView.getContext());
            dialogProgress.setContentView(R.layout.loading_dialog_layout);
            dialogProgress.setCancelable(false);
            dialogProgress.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_cat_dialog_layout);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            edtUpdateName = editDialog.findViewById(R.id.edtUpdateName);
            btnUpdate = editDialog.findViewById(R.id.btnUpdate);

        }

        private void setData(final String title,final int position,final CategoryAdapter adapter){
            catName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CategoryActivity.selected_cat_index=position;
                    Intent intent = new Intent(itemView.getContext(),TestActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    edtUpdateName.setText(cat_list.get(position).getName());
                    editDialog.show();

                    return false;
                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (edtUpdateName.getText().toString().isEmpty()){
                        edtUpdateName.setError("Mời nhập dữ liệu");
                        return;
                    }
                    updateCategory(edtUpdateName.getText().toString(), position, itemView.getContext(), adapter);
                }
            });

            delB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("DELETE CATEGORY")
                            .setMessage("Do you want to delete this category?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteCategory(position, itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    alertDialog.getButton(dialogProgress.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    alertDialog.getButton(dialogProgress.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0,0,50,0);
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);

                }
            });
        }

        private void updateCategory(final String catNewName,final int pos, Context context, CategoryAdapter adapter){
            editDialog.dismiss();

            dialogProgress.show();

            Map<String, Object> catData = new ArrayMap<>();
            catData.put("NAME", catNewName);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            firebaseFirestore.collection("QUIZ").document(cat_list.get(pos).getId())
                    .update(catData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Map<String, Object> catDoc = new ArrayMap<>();
                            catDoc.put("CAT"+ String.valueOf(pos+1) + "_NAME", catNewName);

                            firebaseFirestore.collection("QUIZ").document("Categorories")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Update thành công",Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(pos).setName(catNewName);
                                            adapter.notifyDataSetChanged();
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
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                            dialogProgress.dismiss();
                        }
                    });

        }

        private void deleteCategory(final int id, Context context, CategoryAdapter adapter){
            dialogProgress.show();

            FirebaseFirestore firebaseFirestore =  FirebaseFirestore.getInstance();

            Map<String, Object> catDoc = new ArrayMap<>();
            int index=1;
            for (int i=0;i<cat_list.size();i++){
                if (i != id){
                    catDoc.put("CAT" + String.valueOf(index) + "_ID", cat_list.get(i).getId()) ;
                    catDoc.put("CAT" + String.valueOf(index) + "_NAME", cat_list.get(i).getName()) ;
                    index++;
                }
            }
            catDoc.put("COUNT", index-1);

            firebaseFirestore.collection("QUIZ").document(CategoryActivity.catList.get(id).getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

            firebaseFirestore.collection("QUIZ").document("Categorories")
                    .set(catDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Xóa thành công",Toast.LENGTH_SHORT).show();
                            CategoryActivity.catList.remove(id);

                            adapter.notifyDataSetChanged();
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
        }
    }
}
