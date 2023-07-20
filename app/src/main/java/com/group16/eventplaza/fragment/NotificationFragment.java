package com.group16.eventplaza.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.group16.eventplaza.R;
import com.group16.eventplaza.adapter.NotificationListAdapter;
import com.group16.eventplaza.adapter.ParticipantListAdapter;
import com.group16.eventplaza.databinding.FragmentHomeBinding;
import com.group16.eventplaza.databinding.FragmentNotificationBinding;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.NotificationInfo;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding fragmentNotificationBinding;

    private final Handler mHandler = new MHandler();

    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initRecyclerView((List<NotificationInfo>)msg.obj);
                    break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUtil.getNotificationInfos(getContext(), mHandler);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentNotificationBinding = FragmentNotificationBinding.inflate(getLayoutInflater());
        return fragmentNotificationBinding.getRoot();
    }

    private void initRecyclerView(List<NotificationInfo> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        fragmentNotificationBinding.notificationList.setLayoutManager(layoutManager);
        NotificationListAdapter adapter = new NotificationListAdapter(getActivity(), list);
        fragmentNotificationBinding.notificationList.setAdapter(adapter);
    }
}
