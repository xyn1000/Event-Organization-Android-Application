package com.group16.eventplaza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group16.eventplaza.databinding.ActivityRegisterBinding;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static  String TAG = "RegisterActivity_log====>";
    private ActivityRegisterBinding registerBinding;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private String name;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(registerBinding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerButtonListener();
        db = FirebaseFirestore.getInstance();
    }



    /**
     * todo： signup
     */
    protected void registerUser(String email,String password,String name){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        // set FirebaseUtil userId
                        FirebaseUtil.saveUserId(RegisterActivity.this, mAuth.getCurrentUser().getUid());
                        // set display name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();
                        user.updateProfile(profileUpdate);


                        Map<String, Object> usermap = new HashMap<>();
                        usermap.put("displayName", name);
                        /**
                         * todo： signup success, save user information to users table
                         */
                        db.collection("users")
                                .document(user.getUid())
                                .set(usermap)
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> {
                                });
                        /**
                         * todo： jump to Home page
                         */
                        Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
                        startActivity(intent);
                    }else{

                        try {
                            throw  task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            Toast.makeText(RegisterActivity.this, "Email was taken",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
            }
        });
    }


    /**
     * Register onClick listener
     */
    private void registerButtonListener(){

        // Redirect to login activity
        registerBinding.btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        // Redirect to previous activity
        registerBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        /**
         * todo： signup button listener
         */
        registerBinding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = String.valueOf(registerBinding.email.getText());
                password = String.valueOf(registerBinding.pwd.getText());
                name = String.valueOf(registerBinding.name.getText());

                if(!email.equals("") && !password.equals("")&& !name.equals("")){
                    registerUser(email,password,name);
                }
            }
        });
    }
}