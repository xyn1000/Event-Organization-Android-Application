package com.group16.eventplaza.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group16.eventplaza.R;
import com.group16.eventplaza.model.Participant;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.util.List;

public class RatingListAdapter extends RecyclerView.Adapter <RatingListAdapter.ViewHolder>{
    private List<Participant> participantList;

    public RatingListAdapter(List<Participant> participants){
        participantList =  participants;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Participant participant = participantList.get(position);
        viewHolder.requestParticipantName.setText("@Participant " + FirebaseUtil.getUserNameById(null, participant.getUserId()));
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestParticipantName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestParticipantName = itemView.findViewById(R.id.requestParticipantName);
        }
    }
}