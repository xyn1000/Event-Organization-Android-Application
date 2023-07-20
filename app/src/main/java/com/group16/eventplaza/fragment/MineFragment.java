package com.group16.eventplaza.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group16.eventplaza.EditProfileActivity;
import com.group16.eventplaza.LoginActivity;
import com.group16.eventplaza.PasswordActivity;
import com.group16.eventplaza.databinding.FragmentMineBinding;

import java.util.Map;
import java.util.Objects;


public class MineFragment extends Fragment {

    private com.group16.eventplaza.databinding.FragmentMineBinding fragmentMineBinding;

    private static final String TAG = "MineFragment_log---->";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentMineBinding = FragmentMineBinding.inflate(getLayoutInflater(), container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://android5216.appspot.com");
        storageRef = storage.getReference();
        /**
         * todo： 编辑按钮点击事件
         */
        fragmentMineBinding.myEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        currentUser = mAuth.getCurrentUser();
        initData();
        /**
         * todo： 退出按钮点击事件
         */
        fragmentMineBinding.myLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        /**
         * todo： 修改密码点击事件
         */
        fragmentMineBinding.myChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PasswordActivity.class));
            }
        });


        return fragmentMineBinding.getRoot();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
            initData();
    }



    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    /**
     * todo： 初始化数据显示
     */

    public void initData() {

        if (!TextUtils.isEmpty(currentUser.getEmail())) {
            fragmentMineBinding.myEmail.setText(currentUser.getEmail());
        }

        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        //todo 成功获取到数据  设置
                        assert data != null;
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("phone", "")).toString())) {
                            fragmentMineBinding.myPhone
                                    .setText(Objects.requireNonNull(data.getOrDefault("phone", "")).toString());
                        }

                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("displayName", "")).toString())) {
                            fragmentMineBinding.myName.setText(Objects.requireNonNull(data.getOrDefault("displayName", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("location", "")).toString())) {
                            fragmentMineBinding.myLocation
                                    .setText(Objects.requireNonNull(data.getOrDefault("location", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("age", "")).toString())) {
                            fragmentMineBinding.myAge
                                    .setText(Objects.requireNonNull(data.getOrDefault("age", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("urlString", "")).toString())) {
                            Glide
                                    .with(getActivity())
                                    .load(Objects.requireNonNull(data.getOrDefault("urlString", "")).toString())
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(fragmentMineBinding.myRound);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    public void reload() {
        if (currentUser!=null)
            initData();
    }
}
