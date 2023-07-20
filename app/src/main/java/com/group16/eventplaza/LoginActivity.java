package com.group16.eventplaza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group16.eventplaza.databinding.ActivityLoginBinding;
import com.group16.eventplaza.utils.FirebaseUtil;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerButtonListener();
        getLocationPermission();
    }

    protected void registerButtonListener(){
        // Redirect  to previous activity
        loginBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Redirect to sign in activity
        loginBinding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        // Redirect to forgot password activity
        loginBinding.forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,PasswordActivity.class));
            }
        });

        // sign in user
        loginBinding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = String.valueOf(loginBinding.email.getText());
                password = String.valueOf(loginBinding.pwd.getText());

                if(!email.equals("") && !password.equals("")){
                    signIn(email,password);
                }
            }
        });
    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // set FirebaseUtil userId
                            FirebaseUtil.saveUserId(LoginActivity.this, mAuth.getCurrentUser().getUid());
                            // redirect to home page
                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Email/Password is incorrect",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.CAMERA
                    },
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}