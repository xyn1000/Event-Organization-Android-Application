package com.group16.eventplaza.event;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.group16.eventplaza.adapter.RatingListAdapter;
import com.group16.eventplaza.databinding.ActivityFinishedDetailBinding;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.DateUtil;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RatingEventActivity extends AbstractEventActivity {
    private static final String TAG = "RatingEventActivity";

    private ActivityFinishedDetailBinding activityFinishedDetailBinding;
    private String eventId;
    private final Handler mHandler = new MHandler();

    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initView((Event) msg.obj);
                    break;
                case 2:
                    initRecyclerView((List<Participant>)msg.obj);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFinishedDetailBinding = ActivityFinishedDetailBinding.inflate(getLayoutInflater());
        setContentView(activityFinishedDetailBinding.getRoot());

        eventId = getEventId();
        if (eventId != null) {
            Log.d(TAG, "eventId != null");
            FirebaseUtil.getEvent(eventId, mHandler);
            FirebaseUtil.getEventParticipants(mHandler);
        }
    }

    private void initView(Event event) {
        activityFinishedDetailBinding.eventScrollView.setVisibility(View.VISIBLE);
        activityFinishedDetailBinding.title.setText(event.getTitle());
        activityFinishedDetailBinding.description.setText(event.getDescription());
        activityFinishedDetailBinding.location.setText(event.getLocation());
        activityFinishedDetailBinding.startDate.setText(DateUtil.getDateToString(event.getStartDate().getSeconds()));

        Glide.with(RatingEventActivity.this)
                .load(Objects.requireNonNull(event.getImage()))
                .into(activityFinishedDetailBinding.eventImg);
    }

    private void initRecyclerView(List<Participant> participants) {
        List<Participant> approvedParticipants = new ArrayList<>();
        for (Participant participant : participants) {
            if (participant.getStatus().equals("approved") && participant.getEventId().equals(eventId)) {
                approvedParticipants.add(participant);
            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activityFinishedDetailBinding.requestList.setLayoutManager(layoutManager);
        RatingListAdapter adapter = new RatingListAdapter(approvedParticipants);
        activityFinishedDetailBinding.requestList.setAdapter(adapter);
    }

    @Override
    String getTitleContent() {
        return "Event";
    }

}