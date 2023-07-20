package com.group16.eventplaza.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group16.eventplaza.HomeActivity;
import com.group16.eventplaza.R;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.event.ManageEventActivity;
import com.group16.eventplaza.event.RatingEventActivity;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.NotificationInfo;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.DateUtil;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter <NotificationListAdapter.ViewHolder>{
    private List<NotificationInfo> notificationInfos;
    private Context context;

    public NotificationListAdapter(Context context, List<NotificationInfo> participants){
        notificationInfos =  participants;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        NotificationInfo notificationInfo = notificationInfos.get(position);
        //viewHolder.requestParticipantName.setText(FirebaseUtil.getUserNameById(null, notificationInfo.getRequestUserId()));
        viewHolder.requestParticipantName.setText(notificationInfo.getMessage());
        viewHolder.requestTime.setText(DateUtil.getDateToString(notificationInfo.getRequestTime().getSeconds()));
        viewHolder.eventId = notificationInfo.getEventId();
        viewHolder.itemView.setOnClickListener(view -> {
            Intent intent;
            System.out.println("Login user id: "+FirebaseUtil.getUserId(context));
            System.out.println("Login user id - event owner Id: "+notificationInfo.getEventUid());
            if (FirebaseUtil.getUserId(context).equals(notificationInfo.getEventUid())) {
                // login user id = event owner id: owner opening event
                System.out.println("got to management");
                intent = new Intent(context, ManageEventActivity.class);
            }else{
                // login user id != event owner id: visitor opening event
                // get status by event id
                FirebaseUtil.getEventStatusByEventId(context,notificationInfo.getEventId());
                SharedPreferences sp = context.getSharedPreferences("status", Context.MODE_PRIVATE);
                String status =sp.getString("status",null);
                if(status != null && !status.equals("running")){
                    // event ended
                    intent = new Intent(context, RatingEventActivity.class);
                }else{
                    // event running
                    intent = new Intent(context, EventActivity.class);
                }

            }
            intent.putExtra("eventId", viewHolder.eventId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationInfos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestParticipantName;
        TextView requestTime;
        String eventId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestParticipantName = itemView.findViewById(R.id.requestParticipantName);
            requestTime = itemView.findViewById(R.id.requestTime);
        }
    }
}