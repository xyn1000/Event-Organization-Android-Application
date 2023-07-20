package com.group16.eventplaza.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group16.eventplaza.R;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter <RequestListAdapter.ViewHolder>{
    private List<Participant> participantList;

    public RequestListAdapter(List<Participant> participants){
        participantList =  participants;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Participant participant = participantList.get(position);
        viewHolder.requestParticipantName.setText("@Participant " + FirebaseUtil.getUserNameById(null, participant.getUserId()));

        if (participant.getStatus().equals("approved")) {
            viewHolder.agree.setVisibility(View.INVISIBLE);
            viewHolder.reject.setVisibility(View.INVISIBLE);
        }

        viewHolder.agree.setOnClickListener(view -> {
            FirebaseUtil.deleteNotification(participant.getEventId(), participant.getUserId());
            FirebaseUtil.modifyRegistration(participant.getEventId(), participant.getUserId());
            Toast.makeText(view.getContext(), "Request agreed",Toast.LENGTH_SHORT).show();
            // to do refresh
            viewHolder.agree.setVisibility(View.INVISIBLE);
            viewHolder.reject.setVisibility(View.INVISIBLE);
        });
        viewHolder.reject.setOnClickListener(view -> {
            FirebaseUtil.deleteNotification(participant.getEventId(), participant.getUserId());
            FirebaseUtil.deleteRegistration(participant.getEventId(), participant.getUserId(), false);
            Toast.makeText(view.getContext(), "Request rejected",Toast.LENGTH_SHORT).show();
            viewHolder.agree.setVisibility(View.INVISIBLE);
            viewHolder.reject.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestParticipantName;
        Button agree;
        Button reject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestParticipantName = itemView.findViewById(R.id.requestParticipantName);
            agree = itemView.findViewById(R.id.agree);
            reject = itemView.findViewById(R.id.reject);
        }
    }
}