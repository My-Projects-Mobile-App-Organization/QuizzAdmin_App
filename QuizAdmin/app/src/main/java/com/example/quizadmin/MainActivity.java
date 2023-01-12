package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPass;
    private Button btnLogin;
    private FirebaseAuth firebaseAuth;

    private String taikhoan, matkhau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail = findViewById(R.id.email_ID);
        edtPass = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginB);

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taikhoan = edtEmail.getText().toString();
                matkhau = edtPass.getText().toString();
                if (taikhoan.isEmpty()){
                    edtEmail.setError("Mời nhập email đăng nhập");
                    return;
                } else {
                    edtEmail.setError(null);
                }
                if (matkhau.isEmpty()){
                    edtPass.setError("Mời nhập password đăng nhập");
                    return;
                } else {
                    edtPass.setError(null);
                }
                if (taikhoan.equals("admin") && matkhau.equals("123")){
                    Toast.makeText(MainActivity.this,"Login thành công",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,CategoryActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
                //fireBaseLogin();

            }
        });



    }

    private void fireBaseLogin() {
        firebaseAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                        } else {
                            // If sign in fails, display a message to the user.

                        }
                    }
                });
    }
}