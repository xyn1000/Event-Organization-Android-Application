package com.group16.eventplaza.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.group16.eventplaza.R;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.event.ManageEventActivity;
import com.group16.eventplaza.event.RatingEventActivity;
import com.group16.eventplaza.model.EventCard;
import com.group16.eventplaza.utils.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class PopularEventListAdapter extends RecyclerView.Adapter<PopularEventListAdapter.PopularEventListViewHolder>{

    private ArrayList<EventCard> eventCardArrayList;
    private FirebaseAuth mAuth;


    public PopularEventListAdapter(ArrayList<EventCard> eventCardArrayList) {
        this.eventCardArrayList = eventCardArrayList;
    }

    public PopularEventListAdapter() {
    }

    @NonNull
    @Override
    public PopularEventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_event_card,parent,false);
        return new PopularEventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularEventListViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.getDescription().setText(eventCardArrayList.get(position).getTitle());
            holder.mOwner.setText(eventCardArrayList.get(position).getOwner());
        Glide.with(holder.itemView.getContext())
                .load(Objects.requireNonNull(eventCardArrayList.get(position).getOwnerAvatar()))
                .into(holder.getAvatar());
        Glide.with(holder.itemView.getContext())
                .load(Objects.requireNonNull(eventCardArrayList.get(position).getImage()))
                .into(holder.getImage());
            holder.getDate().setText(eventCardArrayList.get(position).getDateTime());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                Intent intent;
                @Override
                public void onClick(View view) {
                    mAuth = FirebaseAuth.getInstance();
                    Log.i("UID",mAuth.getCurrentUser().getUid());

                    if(FirebaseUtil.getUserId(holder.itemView.getContext()).equals(eventCardArrayList.get(position).getOwnerId())){
                        intent = new Intent(holder.itemView.getContext(), ManageEventActivity.class);
                    }else{
                        intent = new Intent(holder.itemView.getContext(), EventActivity.class);
                    }
                    Log.i("EVENTUID",    eventCardArrayList.get(position).getOwner());

                    intent.putExtra("eventId",eventCardArrayList.get(position).getEventId());
                    holder.itemView.getContext().startActivity(intent);
                }
            });
    }



    @Override
    public int getItemCount() {
        if(eventCardArrayList == null){
            return 0;
        }
        return eventCardArrayList.size();
    }

    public void updateList(ArrayList<EventCard> eventCardArrayList){
        this.eventCardArrayList = eventCardArrayList;
        this.notifyDataSetChanged();
    }


    public class PopularEventListViewHolder extends RecyclerView.ViewHolder{

        private TextView mDate,mDescription,mOwner;
        private ImageView mImage,mAvatar;

        public PopularEventListViewHolder(@NonNull View itemView) {
            super(itemView);
            mDate = itemView.findViewById(R.id.event_time);
            mDescription = itemView.findViewById(R.id.event_description);
            mOwner = itemView.findViewById(R.id.event_owner_name);
            mImage = itemView.findViewById(R.id.event_image);
            mAvatar = itemView.findViewById(R.id.event_owner_avatar);
        }

        public TextView getDescription() {
            return mDescription;
        }

        public ImageView getImage() {
            return mImage;
        }

        public TextView getDate() {
            return mDate;
        }

        public TextView getOwner() {
            return mOwner;
        }

        public ImageView getAvatar() {
            return mAvatar;
        }
    }

}
