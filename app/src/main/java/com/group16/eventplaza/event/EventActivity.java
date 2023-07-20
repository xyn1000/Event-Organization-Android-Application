package com.group16.eventplaza.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import com.group16.eventplaza.R;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.group16.eventplaza.EditProfileActivity;

import com.group16.eventplaza.adapter.ParticipantListAdapter;
import com.group16.eventplaza.databinding.ActivityActiveDetailBinding;
import com.group16.eventplaza.map.LocationFragment;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.DateUtil;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;

import java.util.Objects;


public class EventActivity extends AbstractEventActivity {
    private static final String TAG = "EventActivity";
    private ActivityActiveDetailBinding activityActiveDetailBinding;
    private String eventId;
    private Event event;
    private Geocoder mGeocoder;

    private final Handler mHandler = new MHandler();

    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initView((Event) msg.obj);
                    event = (Event)msg.obj;
                    break;
                case 2:
                    initRecyclerView((List<Participant>)msg.obj);
                    break;
                case 3:
                    initRequestBtn(String.valueOf(msg.obj));
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityActiveDetailBinding = ActivityActiveDetailBinding.inflate(getLayoutInflater());
        setContentView(activityActiveDetailBinding.getRoot());
        mGeocoder = new Geocoder(this, Locale.getDefault());

        eventId = getEventId();
        if (eventId != null) {
            Log.d(TAG, "eventId != null");
            FirebaseUtil.getEvent(eventId, mHandler);
            FirebaseUtil.getEventParticipants(mHandler);
            FirebaseUtil.getCurrentUidRequestStatus(this, mHandler, eventId);
        }
    }


    private void initView(Event event) {
        activityActiveDetailBinding.eventScrollView.setVisibility(View.VISIBLE);
        activityActiveDetailBinding.title.setText(event.getTitle());
        activityActiveDetailBinding.description.setText(event.getDescription());
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = mGeocoder.getFromLocation(event.getLat(),event.getLng(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        activityActiveDetailBinding.location.setText(addressList.get(0).getLocality());
        activityActiveDetailBinding.location1.setText(addressList.get(0).getLocality());
        activityActiveDetailBinding.startDate.setText(DateUtil.getDateToString(event.getStartDate().getSeconds()));
        activityActiveDetailBinding.eventPositionDetail.setText(event.getLocation());
        activityActiveDetailBinding.eventPositionDetail1.setText(event.getLocation());
        activityActiveDetailBinding.participantsInfo
                .setText("Participants " + event.getParticipantsNumber()
                        + "/" + event.getMaxParticipantsNumber());
        int left = (int) (event.getMaxParticipantsNumber() - event.getParticipantsNumber());
        activityActiveDetailBinding.leftParticipantsInfo.setText(left + " spot left");


        // set google map location
        Bundle bundle = new Bundle();
        bundle.putDouble("LAT",event.getLat());
        bundle.putDouble("LNG",event.getLng());
        this.getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.event_google_map, LocationFragment.class,bundle)
                .commit();

        if (FirebaseUtil.getUserId(this).equals(event.getUserId())) {
            activityActiveDetailBinding.requestBtn.setVisibility(View.INVISIBLE);
        }

        // if 0 spot left requestBtn invisible
        Long max = event.getMaxParticipantsNumber();
        Long current = event.getParticipantsNumber();
        Log.d(TAG, "max------:"+max);
        Log.d(TAG, "current------:"+current);
        if (max <= current) {
            activityActiveDetailBinding.requestBtn.setVisibility(View.INVISIBLE);
            activityActiveDetailBinding.requestBtn.setEnabled(false);
        }
        Glide.with(EventActivity.this)
                .load(Objects.requireNonNull(event.getImage()))
                .into(activityActiveDetailBinding.eventImg);
    }

    private void initRecyclerView(List<Participant> participants) {
        List<Participant> approvedParticipants = new ArrayList<>();
        for (Participant participant : participants) {
//            if (participant.getUserId().equals(FirebaseUtil.getUserId(this))) {
//                activityActiveDetailBinding.requestBtn.setText("Cancel request");
//            }
            if (participant.getStatus().equals("approved") && participant.getEventId().equals(eventId)) {
                approvedParticipants.add(participant);
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activityActiveDetailBinding.participantList.setLayoutManager(layoutManager);
        ParticipantListAdapter adapter = new ParticipantListAdapter(approvedParticipants);
        activityActiveDetailBinding.participantList.setAdapter(adapter);
    }

    private void initRequestBtn(String status) {
        if (status == null) {
            return;
        }
        Log.d(TAG, "status------:"+status);
        if (status.equals("pending")) {
            Log.d(TAG, "requestBtn status pending");
            activityActiveDetailBinding.requestBtn.setText("Cancel request");
            activityActiveDetailBinding.requestBtn.setOnClickListener(view -> {
                FirebaseUtil.deleteNotification(eventId, FirebaseUtil.getUserId(this));
                FirebaseUtil.deleteRegistration(eventId, FirebaseUtil.getUserId(this), false);
                Toast.makeText(getApplicationContext(),"Request canceled",Toast.LENGTH_SHORT).show();
                activityActiveDetailBinding.requestBtn.setText("Request to join");

            });
        } else if (status.equals("approved")) {
            Log.d(TAG, "requestBtn status approved");
            activityActiveDetailBinding.requestBtn.setText("Exit Event");
            activityActiveDetailBinding.requestBtn.setOnClickListener(view -> {
                FirebaseUtil.deleteNotification(eventId, FirebaseUtil.getUserId(this));
                FirebaseUtil.deleteRegistration(eventId, FirebaseUtil.getUserId(this), true);
                Toast.makeText(getApplicationContext(),"Exited",Toast.LENGTH_SHORT).show();
                //update participants number
                FirebaseUtil.getEventParticipants(mHandler);
                activityActiveDetailBinding.requestBtn.setText("Request to join");
            });
        } else {
            Log.d(TAG, "requestBtn status else");
            activityActiveDetailBinding.requestBtn.setText("Request to join");
            activityActiveDetailBinding.requestBtn.setOnClickListener(view -> {
                Log.d(TAG, "requestBtn onclick begin");
                FirebaseUtil.getUserIdByEventId(this, eventId);
                Log.d(TAG, "requestBtn eventUserIdddd: "+event.getUserId());
                String eventUserId = event.getUserId();
                String requestUserName = FirebaseUtil.getUserNameById(null, eventUserId);
                FirebaseUtil.insertNotification(eventId, eventUserId, String.valueOf(System.currentTimeMillis() / 1000),
                        FirebaseUtil.getUserId(this),requestUserName+" request to join your event "+event.getTitle());
                FirebaseUtil.insertRegistration(eventId, "pending", FirebaseUtil.getUserId(this));
                Toast.makeText(getApplicationContext(),"Request sent",Toast.LENGTH_SHORT).show();
                activityActiveDetailBinding.requestBtn.setText("Cancel request");
            });
        }
    }

    @Override
    String getTitleContent() {
        return "Event";
    }

}