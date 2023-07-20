package com.group16.eventplaza;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.DataUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group16.eventplaza.databinding.ActivityPostBinding;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.fragment.DateFragment;
import com.group16.eventplaza.fragment.TimeFragment;
import com.group16.eventplaza.utils.DateUtil;

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

public class PostActivity extends AppCompatActivity{
    private ActivityPostBinding postBinding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String mGeoHash;
    private double mLng;
    private double mLat;
    private static final int REQUEST_CODE2 = 2;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageReference;
    private Uri file;
    private String urlString;
    private TextInputLayout startDate;
    private TextInputLayout endDate;
    private Date startDateData;
    private Date endDateData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postBinding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(postBinding.getRoot());

        // Initialize variables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance("gs://android5216.appspot.com");
        storageRef = storage.getReference();

        // Register button onclick listener
        registerOnClickListener();


    }



    private void registerOnClickListener(){
        // Get location
        // TODO one click to open
        postBinding.postLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(PostActivity.this);
                startActivityForResult(intent,1);
            }
        });

        postBinding.postEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE2);
            }
        });

        // Select Start Date
        postBinding.postStartDate.setOnClickListener(new View.OnClickListener() {
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
                                    postBinding.postStartDate.setText(date);
                                    postBinding.postStartTime.setText(time);
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


        // Set end date
        postBinding.postEndDate.setOnClickListener(new View.OnClickListener() {
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
                                                Toast.makeText(PostActivity.this,"Please select correct date",Toast.LENGTH_LONG).show();

                                            }else{
                                                String date = DateUtil.convertToString(endDateData,"dd-MM-yyyy");
                                                String time = DateUtil.convertToString(endDateData,"HH:mm");
                                                postBinding.postEndDate.setText(date);
                                                postBinding.postEndTime.setText(time);
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
                    Toast.makeText(PostActivity.this,"Please select start date first",Toast.LENGTH_LONG).show();
                }
            }
        });

        postBinding.postBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postBinding.postEndTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });

        // post event to firestore
        postBinding.eventPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = postBinding.postTitle.getText().toString();
                String startDate = postBinding.postStartDate.getText().toString();
                String startTime = postBinding.postStartTime.getText().toString();
                String endDate = postBinding.postEndDate.getText().toString();
                String endTime = postBinding.postEndTime.getText().toString();
                String location = postBinding.postLocation.getText().toString();
                String description = postBinding.postDescription.getText().toString();
                int numOfPeople = Integer.parseInt(postBinding.postNum.getText().toString());

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startDate)
                        || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(location) || TextUtils.isEmpty(description)
                        || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endDate)
                        || TextUtils.isEmpty(endTime)) {
                    Toast.makeText(PostActivity.this, "Please check inputs can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if(numOfPeople <= 0){
                    Toast.makeText(PostActivity.this, "Maximum participant input cannot equal or less than 0", Toast.LENGTH_LONG).show();
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
                event.put("image", urlString);

                event.put("geohash",mGeoHash);
                event.put("lng",mLng);
                event.put("lat",mLat);
                event.put("userId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                System.out.println(event);
                db.collection("events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostActivity.this, "DocumentSnapshot successfully written!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PostActivity.this, EventActivity.class);
                        intent.putExtra("eventId",documentReference.getId());
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, "DocumentSnapshot failed written!", Toast.LENGTH_LONG).show();

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
                postBinding.postLocation.setText(place.getAddress());
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
                Glide.with(PostActivity.this)
                        .load(selectedImage)
                        .apply(RequestOptions.bitmapTransform(new CenterCrop()))
                        .into(postBinding.postEventImage);
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
                Toast.makeText(PostActivity.this, "Upload img err", Toast.LENGTH_SHORT).show();
            }));

        }
        super.onActivityResult(requestCode, resultCode, data);

    }



}


