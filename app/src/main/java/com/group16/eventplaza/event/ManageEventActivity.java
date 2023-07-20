package com.group16.eventplaza.event;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.group16.eventplaza.EditEventActivity;
import com.group16.eventplaza.EditProfileActivity;
import com.group16.eventplaza.HomeActivity;
import com.group16.eventplaza.PostActivity;
import com.group16.eventplaza.R;
import com.group16.eventplaza.adapter.RequestListAdapter;
import com.group16.eventplaza.databinding.ActivityManageEventBinding;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.DateUtil;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageEventActivity extends AbstractEventActivity {
    private static final String TAG = "ManageEventActivity";

    private ActivityManageEventBinding activityManageEventBinding;
    private final Handler mHandler = new MHandler();
    private String eventId;
    private Event event;
    private List<Participant> participants;
    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initView((Event) msg.obj);
                    event=(Event) msg.obj;
                    break;
                case 2:
                    initRecyclerView((List<Participant> )msg.obj);
                    participants =(List<Participant> )msg.obj;
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManageEventBinding = ActivityManageEventBinding.inflate(getLayoutInflater());
        setContentView(activityManageEventBinding.getRoot());

        eventId = getEventId();
        if (eventId != null) {
            Log.d(TAG, "eventId != null : "+eventId);
            FirebaseUtil.getEvent(eventId, mHandler);
            FirebaseUtil.getEventParticipants(mHandler);
        }

        ImageView toUpdateEvent = findViewById(R.id.my_edit);
        toUpdateEvent.setOnClickListener(view -> {
            Intent intent = new Intent(ManageEventActivity.this, EditEventActivity.class);
            intent.putExtra("eventId", eventId);
            Log.d(TAG, "Jump to update event with eventId:"+eventId);
            startActivity(intent);
        });
    }

    private void initView(Event event) {
        activityManageEventBinding.eventScrollView.setVisibility(View.VISIBLE);
        activityManageEventBinding.title.setText(event.getTitle());
        activityManageEventBinding.description.setText(event.getDescription());
        activityManageEventBinding.location.setText(event.getLocation());
        activityManageEventBinding.startDate.setText(DateUtil.getDateToString(event.getStartDate().getSeconds()));
        activityManageEventBinding.participantsInfo
                .setText("Participants " + event.getParticipantsNumber()
                        + "/" + event.getMaxParticipantsNumber());
        int left = (int) (event.getMaxParticipantsNumber() - event.getParticipantsNumber());
        activityManageEventBinding.leftParticipantsInfo.setText(left + " spot left");

        Glide.with(ManageEventActivity.this)
                .load(Objects.requireNonNull(event.getImage()))
                .into(activityManageEventBinding.eventImg);

        initListener(event.getStatus(),event.getTitle());
    }



    private void initRecyclerView(List<Participant> participants) {
        List<Participant> approvedParticipants = new ArrayList<>();
        List<Participant> pendingParticipants = new ArrayList<>();
        for (Participant participant : participants) {
            if (participant.getStatus().equals("pending") && participant.getEventId().equals(eventId)) {
                pendingParticipants.add(participant);
            }
            if (participant.getStatus().equals("approved") && participant.getEventId().equals(eventId)) {
                approvedParticipants.add(participant);
            }
        }
        List<Participant> sortedParticipants = new ArrayList<>();
        sortedParticipants.addAll(pendingParticipants);
        sortedParticipants.addAll(approvedParticipants);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activityManageEventBinding.requestList.setLayoutManager(layoutManager);
        RequestListAdapter adapter = new RequestListAdapter(sortedParticipants);
        activityManageEventBinding.requestList.setAdapter(adapter);
    }

    private void initListener(String status, String title) {
        //FirebaseUtil.cancelEvent(getEventId());
        if (status == null) {
            return;
        }
        if (status.equals("running")) {
            Log.d(TAG, "cancelBtn status if running");
            activityManageEventBinding.cancelBtn.setText("Cancel Event");
            activityManageEventBinding.cancelBtn.setOnClickListener(view -> {
                Log.d(TAG, "cancelBtn onclick begin");
                //String eventUserId = FirebaseUtil.getUserIdByEventId(this, eventId);
                String eventUserId = event.getUserId();
                // cancel event
                FirebaseUtil.cancelEvent(eventId);
                // create notification for participants
                Toast.makeText(getApplicationContext(),"Event canceled",Toast.LENGTH_SHORT).show();
                for(Participant participant : participants){
                    if (participant.getEventId().equals(eventId)) {
                        FirebaseUtil.insertNotification(eventId, eventUserId, String.valueOf(System.currentTimeMillis() / 1000),
                                participant.getUserId(), "event "+title+" has been canceled");
                    }
                }

                Log.d(TAG, "cancel notification sent");
                // to do refresh
                activityManageEventBinding.cancelBtn.setText("Event canceled");
                activityManageEventBinding.cancelBtn.setEnabled(false);
                activityManageEventBinding.cancelBtn.setBackgroundColor(Color.GRAY);
            });
        } else {
            Log.d(TAG, "cancelBtn status if ended or canceled");
            if(status.equals("ended")){
                activityManageEventBinding.cancelBtn.setText("Event ended");
            }else{
                activityManageEventBinding.cancelBtn.setText("Event canceled");
            }
            activityManageEventBinding.cancelBtn.setEnabled(false);
            activityManageEventBinding.cancelBtn.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    String getTitleContent() {
        return "Manage event";
    }

}