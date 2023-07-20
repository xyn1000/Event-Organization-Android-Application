package com.group16.eventplaza.event;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.group16.eventplaza.HomeActivity;
import com.group16.eventplaza.R;
import com.group16.eventplaza.databinding.AbsLayoutBinding;

import java.util.Objects;

public abstract class AbstractEventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
    }

    public void setActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        ImageView backImg = findViewById(R.id.backIcon);
        backImg.setOnClickListener(view -> {
            finish();
        });

//        ImageView toUpdateEvent = findViewById(R.id.my_edit);
//        toUpdateEvent.setOnClickListener(view -> {
//            Intent intent = new Intent(AbstractEventActivity.this, HomeActivity.class);
//            intent.putExtra("eventId", eventId);
//            Log.d(TAG, "Jump to update event with eventId:"+eventId);
//            startActivity(intent);
//        });

        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageView imageView = findViewById(R.id.my_edit);
        if(getTitleContent().equals("Manage event")){
            imageView.setVisibility(View.VISIBLE);
            imageView.setEnabled(true);
        }else{
            imageView.setVisibility(View.INVISIBLE);
            imageView.setEnabled(false);
        }
        tvTitle.setText(getTitleContent());

    }

    public String getEventId() {
        Intent intent = getIntent();
        return intent.getStringExtra("eventId");
    }


    abstract String getTitleContent();
}