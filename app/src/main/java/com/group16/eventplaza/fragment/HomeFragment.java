package com.group16.eventplaza.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.group16.eventplaza.EditEventActivity;
import com.group16.eventplaza.PostActivity;
import com.group16.eventplaza.R;
import com.group16.eventplaza.adapter.NotificationListAdapter;
import com.group16.eventplaza.adapter.PopularEventListAdapter;
import com.group16.eventplaza.databinding.FragmentHomeBinding;
import com.group16.eventplaza.databinding.FragmentNotificationBinding;
import com.group16.eventplaza.databinding.FragmentPlazaBinding;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.EventCard;
import com.group16.eventplaza.model.NotificationInfo;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding fragmentHomeBinding;
    private final Handler mHandler = new HomeFragment.MHandler();
    private boolean isCreated=false;


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            //reload
            FirebaseUtil.getEventsByUserId(getContext(), mHandler);
            FirebaseUtil.getRegistrationEventsByUserId(getContext(), mHandler);
            FirebaseUtil.list3(getContext(), mHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            FirebaseUtil.getEventsByUserId(getContext(), mHandler);
            FirebaseUtil.getRegistrationEventsByUserId(getContext(), mHandler);
            FirebaseUtil.list3(getContext(), mHandler);
        }
    }


    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initView((ArrayList<EventCard>)msg.obj);
                    break;
                case 2:
                    initRecyclerView((ArrayList<EventCard>)msg.obj);
                    break;
                case 3:
                    initRecyclerView123((ArrayList<EventCard>)msg.obj);
                    break;
            }
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUtil.getEventsByUserId(getContext(), mHandler);
        FirebaseUtil.getRegistrationEventsByUserId(getContext(), mHandler);
        FirebaseUtil.list3(getContext(), mHandler);
        isCreated=true;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());

        fragmentHomeBinding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
                startActivity(intent);
            }
        });
        return fragmentHomeBinding.getRoot();
    }

    private void initView(ArrayList<EventCard> eventList) {
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        PopularEventListAdapter eventAdapter = new PopularEventListAdapter(eventList);

        fragmentHomeBinding.popularList.setLayoutManager(linearLayoutManager);
        fragmentHomeBinding.popularList.setAdapter(eventAdapter);

        //update list
        eventAdapter.updateList(eventList);
        if(eventAdapter.getItemCount() <= 0){
            fragmentHomeBinding.noEvents1.setVisibility(View.VISIBLE);
        }else{
            fragmentHomeBinding.noEvents1.setVisibility(View.GONE);
        }
    }

    private void initRecyclerView(ArrayList<EventCard> eventList) {
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        PopularEventListAdapter eventAdapter = new PopularEventListAdapter(eventList);

        fragmentHomeBinding.popularList1.setLayoutManager(linearLayoutManager);
        fragmentHomeBinding.popularList1.setAdapter(eventAdapter);

        //update list
        eventAdapter.updateList(eventList);
        if(eventAdapter.getItemCount() <= 0){
            fragmentHomeBinding.noEvents2.setVisibility(View.VISIBLE);
        }else{
            fragmentHomeBinding.noEvents2.setVisibility(View.GONE);
        }
    }

    private void initRecyclerView123(ArrayList<EventCard> eventList) {
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        PopularEventListAdapter eventAdapter = new PopularEventListAdapter(eventList);

        fragmentHomeBinding.popularList2.setLayoutManager(linearLayoutManager);
        fragmentHomeBinding.popularList2.setAdapter(eventAdapter);

        //update list
        eventAdapter.updateList(eventList);
        if(eventAdapter.getItemCount() <= 0){
            fragmentHomeBinding.noEvents3.setVisibility(View.VISIBLE);
        }else{
            fragmentHomeBinding.noEvents3.setVisibility(View.GONE);
        }
    }
}