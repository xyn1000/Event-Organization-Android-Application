package com.group16.eventplaza.fragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.group16.eventplaza.R;
import com.group16.eventplaza.adapter.PopularEventListAdapter;
import com.group16.eventplaza.adapter.SearchEventListAdapter;
import com.group16.eventplaza.databinding.FragmentPlazaBinding;
import com.group16.eventplaza.databinding.FragmentSearchBinding;
import com.group16.eventplaza.model.EventCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<EventCard> eventCardArrayList = new ArrayList<>();
    private FirebaseFirestore db;
    private SearchEventListAdapter eventAdapter;
    private FragmentSearchBinding searchBinding;

    private String mLocation;
    private LatLng mLatLng;
    private GeoLocation center;
    private double radiusInM = 50*100; // 5km

    private String keyword;
    private String location;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        searchBinding = FragmentSearchBinding.inflate(inflater,container,false);
        View view =searchBinding.getRoot();
        registerOnClickListener();
        return view;
    }

    // filter
    private void filter () {

        // no keyword, no location
        if(keyword.equals("") && center == null){
            getEvents();
        }
        // only have keyword
        if(!keyword.equals("") && center == null){
            getEventsWithKeyword();
        }
        // only have location
        if(keyword.equals("") && center != null){
            getEventsWithLocation();
        }
        // have both keyword and location
        if(!keyword.equals("") && center !=null){
            getEventsWithLocationAndKeyword();
        }
    }

    // Get all events if keyword and location are both empty
    private void getEvents(){
        db.collection("events").whereEqualTo("status","running")
                .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    eventCardArrayList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        EventCard eventCard= new EventCard();
                        // set title
                        eventCard.setTitle(document.get("title").toString());
                        // set start date
                        Timestamp startDateSecond = (Timestamp) document.get("startDate");
                        Date getDate =startDateSecond.toDate();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd, MMM, HH:mm");
                        String startTime = simpleDateFormat.format(getDate);
                        eventCard.setDateTime(startTime);
                        // set location
                        eventCard.setLocation(document.get("location").toString());
                        // set image
                        eventCard.setImage(document.getData().getOrDefault("image","").toString());
                        // set max participants
                        eventCard.setMaxParticipant(Integer.parseInt(document.get("maxParticipantsNumber").toString()));
                        // set participants
                        participantCounter(document.getId(),eventCard, new ParticipantCounterCallBack() {
                            @Override
                            public void onCallBack(EventCard eventCard) {
                                eventCardArrayList.add(eventCard);
                                eventAdapter = new SearchEventListAdapter(eventCardArrayList);
                                LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
                                searchBinding.searchLstView.setLayoutManager(linearLayoutManager);
                                searchBinding.searchLstView.setAdapter(eventAdapter);
                            }
                        });

                    }
                }

            }
        });
    }

    // only have keyword
    private void getEventsWithKeyword () {
        db.collection("events").whereEqualTo("status","running")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            eventCardArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // match title with keyword
                                if(document.get("title").toString().contains(keyword)){
                                    EventCard eventCard= new EventCard();
                                    // set title
                                    eventCard.setTitle(document.get("title").toString());
                                    // set start date
                                    Timestamp startDateSecond = (Timestamp) document.get("startDate");
                                    Date getDate =startDateSecond.toDate();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd, MMM, HH:mm");
                                    String startTime = simpleDateFormat.format(getDate);
                                    eventCard.setDateTime(startTime);
                                    eventCard.setEventId(document.getId());
                                    // set location
                                    eventCard.setLocation(document.get("location").toString());
                                    // set max participants
                                    eventCard.setMaxParticipant(Integer.parseInt(document.get("maxParticipantsNumber").toString()));
                                    // set image
                                    eventCard.setImage(document.getData().getOrDefault("image","").toString());
                                    // set participants
                                    participantCounter(document.getId(),eventCard, new ParticipantCounterCallBack() {
                                        @Override
                                        public void onCallBack(EventCard eventCard) {
                                            eventCardArrayList.add(eventCard);
                                            eventAdapter = new SearchEventListAdapter(eventCardArrayList);
                                            LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
                                            searchBinding.searchLstView.setLayoutManager(linearLayoutManager);
                                            searchBinding.searchLstView.setAdapter(eventAdapter);
                                        }
                                    });
                                }
                            }
                        }

                    }
                });
    }



    // both keyword and location
    private void getEventsWithLocationAndKeyword () {
        getResultWithLocationAndKeyword(center);
    }


    // only have location
    private void getEventsWithLocation () {
        getResultWithLocation(center);
    }

    private void getResultWithLocationAndKeyword(GeoLocation centerLocation){
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
        eventCardArrayList.clear();
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {

                        eventCardArrayList= new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                if(doc.get("title").toString().contains(keyword)){
                                    double lat = doc.getDouble("lat");
                                    double lng = doc.getDouble("lng");

                                    // We have to filter out a few false positives due to GeoHash
                                    // accuracy, but most will match
                                    GeoLocation docLocation = new GeoLocation(lat, lng);
                                    double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                    if (distanceInM <= radiusInM ) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            EventCard eventCard= new EventCard();
                                            // TODO add more details
                                            eventCard.setEventDescription(Objects.requireNonNull(Objects.requireNonNull(
                                                    doc.getData()).getOrDefault("description", "")).toString());
                                            eventCard.setEventId(doc.getId());
                                            eventCard.setGeoLocation(docLocation);
                                            // set title
                                            eventCard.setTitle(doc.get("title").toString());
                                            // set start date
                                            Timestamp startDateSecond = (Timestamp) doc.get("startDate");
                                            Date getDate =startDateSecond.toDate();
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd, MMM, HH:mm");
                                            String startTime = simpleDateFormat.format(getDate);
                                            eventCard.setDateTime(startTime);
                                            // set location
                                            eventCard.setLocation(doc.get("location").toString());
                                            // set max participants
                                            eventCard.setMaxParticipant(Integer.parseInt(doc.get("maxParticipantsNumber").toString()));
                                            // set lat lng
                                            eventCard.setLat((Double) doc.getData().getOrDefault("lat",0));
                                            eventCard.setLng((Double) doc.getData().getOrDefault("lng",0));
                                            // set image
                                            eventCard.setImage(doc.getData().getOrDefault("image","").toString());
                                            // set participants
                                            participantCounter(doc.getId(),eventCard, new ParticipantCounterCallBack() {
                                                @Override
                                                public void onCallBack(EventCard eventCard) {
                                                    eventCardArrayList.add(eventCard);
                                                    eventAdapter = new SearchEventListAdapter(eventCardArrayList);
                                                    LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
                                                    searchBinding.searchLstView.setLayoutManager(linearLayoutManager);
                                                    searchBinding.searchLstView.setAdapter(eventAdapter);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }


                    }
                });
    }

    private void getResultWithLocation(GeoLocation centerLocation){
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


        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {

                        eventCardArrayList= new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {

                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM ) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        EventCard eventCard= new EventCard();
                                        // TODO add more details
                                        eventCard.setEventDescription(Objects.requireNonNull(Objects.requireNonNull(
                                        doc.getData()).getOrDefault("description", "")).toString());
                                        eventCard.setEventId(doc.getId());
                                        eventCard.setGeoLocation(docLocation);
                                        // set title
                                        eventCard.setTitle(doc.get("title").toString());
                                        // set start date
                                        Timestamp startDateSecond = (Timestamp) doc.get("startDate");
                                        Date getDate =startDateSecond.toDate();
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd, MMM, HH:mm");
                                        String startTime = simpleDateFormat.format(getDate);
                                        eventCard.setDateTime(startTime);
                                        eventCard.setMaxParticipant(Integer.parseInt(doc.get("maxParticipantsNumber").toString()));

                                        // set location
                                        eventCard.setLocation(doc.get("location").toString());
                                        // set lat lng
                                        eventCard.setLat((Double) doc.getData().getOrDefault("lat",0));
                                        eventCard.setLng((Double) doc.getData().getOrDefault("lng",0));
                                        eventCard.setImage(doc.getData().getOrDefault("image","").toString());
                                        // set participants
                                        participantCounter(doc.getId(),eventCard, new ParticipantCounterCallBack() {
                                            @Override
                                            public void onCallBack(EventCard eventCard) {
                                                eventCardArrayList.add(eventCard);
                                                eventAdapter = new SearchEventListAdapter(eventCardArrayList);
                                                LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
                                                searchBinding.searchLstView.setLayoutManager(linearLayoutManager);
                                                searchBinding.searchLstView.setAdapter(eventAdapter);
                                            }
                                        });
                                    }
                                }
                            }
                        }


                    }
                });
    }




    private void participantCounter(String eventId, EventCard eventCard, ParticipantCounterCallBack participantCounterCallBack) {
        db.collection("registrations")
                .whereEqualTo("eventId",eventId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                          eventCard.setCurrentParticipant(task.getResult().size());
                          participantCounterCallBack.onCallBack(eventCard);
                    }
                });
    }


    private interface  ParticipantCounterCallBack{
        void onCallBack(EventCard eventCard);
    }

    /**
     * Register all onclick listener
     *
     */
    private void registerOnClickListener(){
        searchBinding.searchEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = searchBinding.searchKeyword.getText().toString();
                location = searchBinding.searchLocation.getText().toString();
                filter();
            }
        });
        // Start search location page
        searchBinding.searchLocation.setOnClickListener(new View.OnClickListener() {
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
                searchBinding.searchLocation.setText(place.getName());
                mLatLng = place.getLatLng();


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

}