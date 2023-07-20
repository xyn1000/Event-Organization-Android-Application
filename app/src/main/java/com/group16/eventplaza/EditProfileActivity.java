package com.group16.eventplaza;

import static com.google.android.gms.common.util.IOUtils.copyStream;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group16.eventplaza.databinding.ActivityInfoBinding;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CODE2 = 2;
    private static String TAG = "EditProfileActivity";
    private FirebaseUser currentUser;
    private ActivityInfoBinding infoBinding;
    private FirebaseFirestore db;
    private String urlString = "";
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageReference;
    private final static int REQUEST_CODE = 1;
    public String photoFileName = "photo.jpg";
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoBinding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(infoBinding.getRoot());
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://android5216.appspot.com");
        storageRef = storage.getReference();

        if (!TextUtils.isEmpty(currentUser.getDisplayName())) {
            infoBinding.displayName
                    .setText(currentUser.getDisplayName());
        }
        /**
         * todo： 返回
         */
        infoBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /**
         * todo： 跳转到地图选择界面
         */
        infoBinding.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(EditProfileActivity.this, MapActivity.class), REQUEST_CODE);
            }
        });

        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        /**
         * todo： 查询当前登录的用户的信息
         */
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        //todo 成功获取到数据  设置显示
                        assert data != null;
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("phone", "")).toString())) {
                            infoBinding.phone
                                    .setText(Objects.requireNonNull(data.getOrDefault("phone", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("location", "")).toString())) {
                            infoBinding.location
                                    .setText(Objects.requireNonNull(data.getOrDefault("location", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("age", "")).toString())) {
                            infoBinding.ageEdit
                                    .setText(Objects.requireNonNull(data.getOrDefault("age", "")).toString());
                        }
                        if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("urlString", "")).toString())) {
                            Glide
                                    .with(EditProfileActivity.this)
                                    .load(Objects.requireNonNull(data.getOrDefault("urlString", "")).toString())
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(infoBinding.myRound);
                            urlString = Objects.requireNonNull(data.getOrDefault("urlString", "")).toString();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        /**
         * todo： 上传按钮的点击事件
         */
        infoBinding.btnUpdateUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String displayName = infoBinding.displayName.getText().toString();
                String phone = infoBinding.phone.getText().toString();
                String location = infoBinding.location.getText().toString();
                String age = infoBinding.ageEdit.getText().toString();
                if (TextUtils.isEmpty(displayName) || TextUtils.isEmpty(location)
                        || TextUtils.isEmpty(phone) || TextUtils.isEmpty(age) || TextUtils.isEmpty(urlString)) {
                    return;
                }
                UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName);
                if (!TextUtils.isEmpty(urlString))
                    builder.setPhotoUri(Uri.parse(urlString));
                UserProfileChangeRequest build = builder.build();
                currentUser.updateProfile(build);  //将头像路径及名称更新到user对象中
                Map<String, Object> user = new HashMap<>();
                user.put("location", location);// 位置
                user.put("age", age); //年龄
                user.put("phone", phone); // 电话号码
                user.put("displayName", displayName);  //名称
                if (!TextUtils.isEmpty(urlString))
                    user.put("urlString", urlString); //图片路径

                /**
                 * todo： 判断信息完整后将用户信息存入users表中
                 * <p>
                 */
                db.collection("users")
                        .document(currentUser.getUid())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /**
                                 * todo： 上传成功
                                 * <p>
                                 */
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                Toast.makeText(EditProfileActivity.this, "DocumentSnapshot successfully written!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                /**
                                 * todo： 上传失败
                                 */
                                Log.w(TAG, "Error writing document", e);
                                Toast.makeText(EditProfileActivity.this, "Error writing document!", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        infoBinding.myRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * todo： 头像点击事件  点击后进入头像选择和拍照选择界面
                 * <p>
                 *
                 */
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE2);
            }
        });

    }

    public Uri getFileUri(String fileName) {
        Uri fileUri = null;
        try {
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d("TAG", "failed to create directory");
            }
            file = new File(mediaStorageDir, fileName);
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(
                        this.getApplicationContext(),
                        "com.group16.eventplaza", file);
            } else {
                fileUri = Uri.fromFile(mediaStorageDir);
            }
        } catch (Exception ex) {
            Log.e("getFileUri", ex.getStackTrace().toString());
        }
        return fileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * todo： 选择定位后回调 设置位置信息为所选择位置
         * <p>
         *
         */
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            infoBinding.location.setText(data.getExtras().getString("location"));
        }
        /**
         * todo： 选择图片的回调  设置图片显示及文件上传
         * <p>
         *
         */
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE2) {
            Uri photoUri = data.getData();
            Bitmap selectedImage;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), photoUri);
                Glide.with(EditProfileActivity.this)
                        .load(selectedImage)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(infoBinding.myRound);
                File appDir = new File(Environment.getExternalStorageDirectory(), "tempfile");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = "IMG_" + System.currentTimeMillis() + ".jpeg";
                File file = new File(appDir, fileName);
                try {
                    InputStream inputStream = EditProfileActivity.this.getContentResolver().openInputStream(photoUri);
                    if (inputStream == null) return;
                    OutputStream outputStream = new FileOutputStream(file);
                    copyStream(inputStream, outputStream);
                    inputStream.close();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                doUpload(file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * todo： 上传指定路径的图片到storage
     * <p>
     *
     * @param string 文件路径
     */
    private void doUpload(String string) {
        Uri file = Uri.fromFile(new File(string));
        storageReference = storageRef.child("images/" + file.getLastPathSegment());
        UploadTask uploadTask = storageReference.putFile(file);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.child("images/" + file.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                urlString = uri.toString();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(EditProfileActivity.this, "Upload img err", Toast.LENGTH_SHORT).show();
        }));
    }


}