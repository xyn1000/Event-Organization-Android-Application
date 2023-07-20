package com.group16.eventplaza;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.group16.eventplaza.databinding.ActivityCodeBinding;

public class PasswordActivity extends AppCompatActivity {

    private ActivityCodeBinding codeBinding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        codeBinding = ActivityCodeBinding.inflate(getLayoutInflater());
        setContentView(codeBinding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /**
         * todo： 发送邮件的点击事件
         */
        codeBinding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(codeBinding.email.getText().toString())) {
                    Toast.makeText(PasswordActivity.this, "Error email", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * todo： 发送邮件
                 */
                mAuth.sendPasswordResetEmail(codeBinding.email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        codeBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


}