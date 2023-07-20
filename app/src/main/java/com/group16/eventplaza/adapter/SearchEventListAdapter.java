package com.group16.eventplaza.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.group16.eventplaza.R;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.event.ManageEventActivity;
import com.group16.eventplaza.model.EventCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class SearchEventListAdapter extends RecyclerView.Adapter<SearchEventListAdapter.SearchEventListViewHolder> {
    private ArrayList<EventCard> eventCardArrayList;


    public SearchEventListAdapter(ArrayList<EventCard> eventCardArrayList) {
        this.eventCardArrayList = eventCardArrayList;
    }
    @NonNull
    @Override
    public SearchEventListAdapter.SearchEventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_single_event, parent, false);
        return new SearchEventListAdapter.SearchEventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchEventListViewHolder holder, int position) {
        holder.getTitle().setText(eventCardArrayList.get(position).getTitle());
        holder.getmDateTime().setText(eventCardArrayList.get(position).getDateTime());
        holder.getLocation().setText(eventCardArrayList.get(position).getLocation());
        holder.getCurrentParticipant().setText(String.valueOf(eventCardArrayList.get(position).getParticipant()));
        holder.getMaxParticipant().setText(String.valueOf(eventCardArrayList.get(position).getMaxParticipant()));
        Glide.with(holder.itemView.getContext())
                .load(Objects.requireNonNull(eventCardArrayList.get(position).getImage()))
                .into(holder.mImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), EventActivity.class);
                intent.putExtra("eventId",eventCardArrayList.get(position).getEventId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return eventCardArrayList.size();
    }

    public void updateList(ArrayList<EventCard> eventCardArrayList) {
        this.eventCardArrayList = eventCardArrayList;
        this.notifyDataSetChanged();
    }

    public class SearchEventListViewHolder extends RecyclerView.ViewHolder {

//        private TextView mDate, mDescription, mOwner;
        private ImageView mImage;
        private TextView title, mDateTime, location, currentParticipant, maxParticipant;

        public SearchEventListViewHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.searchEventImage);
            title = itemView.findViewById(R.id.searchEventTitle);
            mDateTime = itemView.findViewById(R.id.searchEventDateTime);
            location = itemView.findViewById(R.id.searchEventLocation);
            currentParticipant = itemView.findViewById(R.id.participant);
            maxParticipant  = itemView.findViewById(R.id.max_participant);
        }

        public ImageView getmImage() {
            return mImage;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getmDateTime() {
            return mDateTime;
        }

        public TextView getLocation() {
            return location;
        }

        public TextView getCurrentParticipant() {
            return currentParticipant;
        }

        public TextView getMaxParticipant() {
            return maxParticipant;
        }

        public void setTitle(TextView title) {
            this.title = title;
        }



    }
}