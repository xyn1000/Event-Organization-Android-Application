package com.group16.eventplaza;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group16.eventplaza.databinding.ActivityEditBinding;
import com.group16.eventplaza.databinding.ActivityPostBinding;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.fragment.DateFragment;
import com.group16.eventplaza.fragment.TimeFragment;
import com.group16.eventplaza.utils.DateUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditEventActivity extends AppCompatActivity {
    private ActivityEditBinding editBinding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String mGeoHash;
    private double mLng;
    private double mLat;
    private String eventId;
    private String userId;
    private long startDateSecond;
    private long startTimeSecond;
    private long endDateSecond;
    private long endTimeSecond;

    private Date startDateData;
    private Date endDateData;
    private static final int REQUEST_CODE2 = 2;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageReference;
    private Uri file;
    private String urlString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editBinding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(editBinding.getRoot());

        // Initialize variables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        storage = FirebaseStorage.getInstance("gs://android5216.appspot.com");
        storageRef = storage.getReference();

        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        db.collection("events")
                .document( eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                Map<String, Object> data = document.getData();
                                assert data != null;
                                eventId = document.getId();
                                userId = data.getOrDefault("userId", "").toString();
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("title", "")).toString())) {
                                    editBinding.editTitle
                                            .setText(Objects.requireNonNull(data.getOrDefault("title", "")).toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("startDate", "")).toString())) {
                                    Timestamp startDateSeconds = (Timestamp) data.getOrDefault("startDate",0);
                                    startDateData =startDateSeconds.toDate();

                                    String startTime = DateUtil.convertToString(startDateData,"HH:mm");
                                    String startDate = DateUtil.convertToString(startDateData,"dd-MM-yyyy");


                                    editBinding.editStartTime.setText(startTime);
                                    editBinding.editStartDate.setText(startDate);


                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("endDate", "")).toString())) {
                                   Timestamp endDateTimestamp = (Timestamp) data.getOrDefault("endDate",0);
                                    endDateData =endDateTimestamp.toDate();

                                    String endTime = DateUtil.convertToString(endDateData,"HH:mm");
                                    String endDate = DateUtil.convertToString(endDateData,"dd-MM-yyyy");
                                    editBinding.editEndTime.setText(endTime);
                                    editBinding.editEndDate.setText(endDate);


                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("location", "")).toString())) {
                                    editBinding.editLocation
                                            .setText(Objects.requireNonNull(data.getOrDefault("location", "")).toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("description", "")).toString())) {
                                    editBinding.editDescription
                                            .setText(Objects.requireNonNull(data.getOrDefault("description", "")).toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("maxParticipantsNumber", "")).toString())) {
                                    editBinding.editNum
                                            .setText(Objects.requireNonNull(data.getOrDefault("maxParticipantsNumber", "")).toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("image", "")).toString())) {
                                    Picasso.get().load(Objects.requireNonNull(data.getOrDefault("image", "")).toString()).into(editBinding.editImage);
                                    urlString = data.getOrDefault("image", "").toString();
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("geohash", "")).toString())) {
                                    mGeoHash = data.getOrDefault("geohash", "").toString();
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("lat", "")).toString())) {
                                    mLat = Double.parseDouble(data.getOrDefault("lat", "").toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("lng", "")).toString())) {
                                    mLng = Double.parseDouble(data.getOrDefault("lng", "").toString());
                                }
                            }
                        }
                    }
                });

        editBinding.editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(EditEventActivity.this);
                startActivityForResult(intent,1);
            }
        });

        // Select Start Date
        editBinding.editStartDate.setOnClickListener(new View.OnClickListener() {
            String date = "";
            String time = "";
            @Override
            public void onClick(View view) {

                DialogFragment dateFragment = new DateFragment(null,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month +1;
                        date = day+"-"+month+"-"+year;
                        DialogFragment timeFragment = new TimeFragment(new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                                time = date + " "+hour+":"+min;
                                try {
                                    startDateData = DateUtil.convertToDate(time,"dd-MM-yyyy hh:mm");
                                    String date = DateUtil.convertToString(startDateData,"dd-MM-yyyy");
                                    String time = DateUtil.convertToString(startDateData,"HH:mm");
                                    editBinding.editStartDate.setText(date);
                                    editBinding.editStartTime.setText(time);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        timeFragment.show(getSupportFragmentManager(),"Start Time");
                    }
                });
                dateFragment.show(getSupportFragmentManager(), "Start Date");
            }
        });

    editBinding.editBack.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    });
        // Set end date
        editBinding.editEndDate.setOnClickListener(new View.OnClickListener() {
            String date = "";
            String time = "";
            @Override
            public void onClick(View view) {
                if(startDateData != null){
                    Date start;
                    String startDate = DateUtil.convertToString(startDateData,"dd-MM-yyyy");
                    try {
                        start = DateUtil.convertToDate(startDate,"dd-MM-yyyy");
                        DialogFragment newFragment = new DateFragment(start,new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month +1;
                                date = day+"-"+month+"-"+year;
                                DialogFragment newFragment = new TimeFragment(new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                                        time = date + " "+hour+":"+min;
                                        try {
                                            endDateData = DateUtil.convertToDate(time,"dd-MM-yyyy hh:mm");
                                            if(startDateData.after(endDateData)){
                                                endDateData = startDateData;
                                                Toast.makeText(EditEventActivity.this,"Please select correct date",Toast.LENGTH_LONG).show();

                                            }else{
                                                String date = DateUtil.convertToString(endDateData,"dd-MM-yyyy");
                                                String time = DateUtil.convertToString(endDateData,"HH:mm");
                                                editBinding.editEndDate.setText(date);
                                                editBinding.editEndTime.setText(time);
                                            }


                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                newFragment.show(getSupportFragmentManager(),"End Time");
                            }
                        });
                        newFragment.show(getSupportFragmentManager(), "End Date");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(EditEventActivity.this,"Please select start date first",Toast.LENGTH_LONG).show();
                }
            }
        });


        editBinding.editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE2);
            }
        });



        editBinding.eventPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editBinding.editTitle.getText().toString();
                String startDate = editBinding.editStartDate.getText().toString();
                String startTime = editBinding.editStartTime.getText().toString();
                String endDate = editBinding.editEndDate.getText().toString();
                String endTime = editBinding.editEndTime.getText().toString();
                String location = editBinding.editLocation.getText().toString();
                String description = editBinding.editDescription.getText().toString();
                int numOfPeople = Integer.parseInt(editBinding.editNum.getText().toString());

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startDate)
                        || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(location) || TextUtils.isEmpty(description)
                        || numOfPeople<=0 || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endDate)
                        || TextUtils.isEmpty(endTime)) {
                    Toast.makeText(EditEventActivity.this, "Please check inputs can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, Object> event = new HashMap<>();
                event.put("location", location);
                event.put("title", title);
                event.put("startDate", new Timestamp(startDateData));
                event.put("endDate", new Timestamp(endDateData));
                event.put("description", description);
                event.put("maxParticipantsNumber", numOfPeople);
                event.put("participantsNumber", 0);
                event.put("status", "running");
                event.put("userId", userId);
                event.put("image", urlString);

                event.put("geohash",mGeoHash);
                event.put("lng",mLng);
                event.put("lat",mLat);
                event.put("userId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

                db.collection("events")
                        .document(eventId)
                        .update(event)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(EditEventActivity.this, "DocumentSnapshot success written!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditEventActivity.this, EventActivity.class);
                                intent.putExtra("eventId", eventId);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEventActivity.this, "DocumentSnapshot failed written!", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                // Set new location name
                editBinding.editLocation.setText(place.getAddress());
                mLat = place.getLatLng().latitude;
                mLng = place.getLatLng().longitude;
                mGeoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude));

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        if(requestCode == REQUEST_CODE2){
            file = data.getData();
            Bitmap selectedImage;

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), file);
                Glide.with(EditEventActivity.this)
                        .load(selectedImage)
                        .apply(RequestOptions.bitmapTransform(new CenterCrop()))
                        .into(editBinding.editImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri uploadFile = Uri.fromFile(new File(file.getPath()));
            storageReference = storageRef.child("events/" +uploadFile.getLastPathSegment());
            UploadTask uploadTask = storageReference.putFile(file);
            uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.child("events/" + file.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    urlString = uri.toString();
                }
            }).addOnFailureListener(exception -> {
                Toast.makeText(EditEventActivity.this, "Upload img err", Toast.LENGTH_SHORT).show();
            }));

        }
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

}
