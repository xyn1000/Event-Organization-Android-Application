package com.group16.eventplaza.fragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.group16.eventplaza.BuildConfig;
import com.group16.eventplaza.R;
import com.group16.eventplaza.adapter.PopularEventListAdapter;
import com.group16.eventplaza.databinding.FragmentPlazaBinding;
import com.group16.eventplaza.map.LocationFragment;
import com.group16.eventplaza.model.EventCard;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlazaFragment extends Fragment{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LOCATION = "location";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private FragmentPlazaBinding plazaBinding;
    private String mLocation;
    private PopularEventListAdapter eventAdapter;
    private LatLng mLatLng;
    private static final int DEFAULT_ZOOM = 11;
    // Default location Sydney
    private double mLat = -34;
    private double mLng = 151;
    private GoogleMap mMaps;
    private Geocoder mGeocoder;

    // Location
    private PlacesClient placesClient;
    private Location mLastKnownLocation = new Location("");
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Data source
    private ArrayList<EventCard> eventCardArrayList ;
    private FirebaseFirestore db;
    private GeoLocation center;
    private double radiusInM = 50*100; // 5km


    // Required empty public constructor
    public PlazaFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocation = getArguments().getString(LOCATION);
        }
        // Construct a PlaceClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(getActivity().getApplicationContext());
        mGeocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

        // Get device location
        getLocationPermission();

        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // bind view
        plazaBinding = FragmentPlazaBinding.inflate(inflater,container,false);
        View view =plazaBinding.getRoot();
        plazaBinding.currentLocation.setText(mLocation);



        registerOnClickListener(); // register onclick listener
        getCurrentLocation();     // get current location and display in map

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void registerOnClickListener(){

        // Start search location page
        plazaBinding.changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(getActivity().getApplicationContext());
                startActivityForResult(intent,1);
            }
        });
    }

    /**
     * Get auto-complete location result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                // Set new location name
                mLocation = place.getName();
                plazaBinding.currentLocation.setText(place.getName());
                mLatLng = place.getLatLng();

                Bundle newLocation = new Bundle();
                newLocation.putDouble("LAT",mLatLng.latitude);
                newLocation.putDouble("LNG",mLatLng.longitude);
                getActivity().getSupportFragmentManager().setFragmentResult("requestKey",newLocation);
                center = new GeoLocation(place.getLatLng().latitude,place.getLatLng().longitude);


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(center != null){
            eventCardArrayList = new ArrayList<>();
            getEventDocument(center);

        }
    }

    private void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Get device current location
     */

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        if(locationPermissionGranted){
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        mLastKnownLocation = task.getResult();
                        if(mLastKnownLocation != null){

                            Bundle bundle = new Bundle();
                            bundle.putDouble("LAT",mLastKnownLocation.getLatitude());
                            bundle.putDouble("LNG",mLastKnownLocation.getLongitude());
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .add(R.id.fragment_container_view, LocationFragment.class,bundle)
                                    .commit();

                            center = new GeoLocation(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                            getEventDocument(center);

                            try {
                                List<Address> address = mGeocoder.getFromLocation(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),1);
                                mLocation = address.get(0).getLocality();
                                plazaBinding.currentLocation.setText(mLocation);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Get events near by center location within 5km range
     * @param centerLocation
     */


    private void getEventDocument(GeoLocation centerLocation){

        eventAdapter = new PopularEventListAdapter();
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        plazaBinding.popularList.setLayoutManager(linearLayoutManager);
        plazaBinding.popularList.setAdapter(eventAdapter);
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(centerLocation,radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("events")
                    .whereEqualTo("status","running")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }

        eventCardArrayList= new ArrayList<>();


        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {

                        ArrayList<EventCard> locations = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    EventCard eventCard= new EventCard();
                                    // TODO add more details
                                    eventCard.setEventDescription(Objects.requireNonNull(Objects.requireNonNull(
                                    doc.getData()).getOrDefault("description", "")).toString());
                                    eventCard.setEventId(doc.getId());
                                    eventCard.setGeoLocation(docLocation);
                                    eventCard.setTitle(doc.getData().get("title").toString());
                                    eventCard.setLat((Double) doc.getData().getOrDefault("lat",0));
                                    eventCard.setLng((Double) doc.getData().getOrDefault("lng",0));
                                    eventCard.setImage(doc.getData().getOrDefault("image","").toString());

                                    // Set Time format
                                   Timestamp timestamp = (Timestamp) doc.getData().getOrDefault("startDate","");
                                   Date date = timestamp.toDate();

                                    SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm");
                                    eventCard.setDateTime(simpleDateFormat.format(date));
                                    locations.add(eventCard);
                                    getUserData(Objects.requireNonNull(doc.get("userId")).toString(), eventCard, new UserDataCallBack() {
                                        // get user data
                                        @Override
                                        public void onCallBack(EventCard eventCard) {
                                            eventCardArrayList.add(eventCard);

                                            // display event card
                                            eventAdapter.updateList(eventCardArrayList);

                                            if (eventCardArrayList.size() <= 0){
                                                plazaBinding.noEvents.setVisibility(View.VISIBLE);
                                            }else {
                                                plazaBinding.noEvents.setVisibility(View.GONE);
                                            }
                                        }
                                    });

                                    }
                                }
                            }
                            if(snap.getDocuments().size() <= 0){
                                eventAdapter = new PopularEventListAdapter(eventCardArrayList);
                                LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
                                plazaBinding.popularList.setLayoutManager(linearLayoutManager);
                                plazaBinding.popularList.setAdapter(eventAdapter);
                                eventAdapter.updateList(eventCardArrayList);
                                if (eventCardArrayList.size() <= 0){
                                    plazaBinding.noEvents.setVisibility(View.VISIBLE);
                                }else {
                                    plazaBinding.noEvents.setVisibility(View.GONE);
                                }
                            }

                        }



                            // Set map marker
                        Bundle eventList = new Bundle();
                        eventList.putSerializable("eventList", locations);
                        getActivity().getSupportFragmentManager().setFragmentResult("events",eventList);
                    }
                });
    }


    /**
     * Get Owner display name and avatar
     * @param id
     * @param eventCard
     * @param userDataCallBack
     */
    private void getUserData(String id,EventCard eventCard,UserDataCallBack userDataCallBack){
        db.collection("users").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                assert data != null;
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("displayName", "")).toString())) {
                                    eventCard.setOwner(Objects.requireNonNull(data.get("displayName")).toString());
                                }
                                if (!TextUtils.isEmpty(Objects.requireNonNull(data.getOrDefault("urlString", "")).toString())) {
                                    eventCard.setOwnerAvatar(Objects.requireNonNull(data.get("urlString")).toString());
                                }
                            }
                            assert data != null;
//                            eventCard.setOwnerAvatar(data.get("urlString").toString());
                            userDataCallBack.onCallBack(eventCard);
                        }
                    }
                });
    }

    private interface UserDataCallBack{
        void onCallBack(EventCard eventCard);
    }






}