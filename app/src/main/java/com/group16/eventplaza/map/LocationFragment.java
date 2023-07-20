package com.group16.eventplaza.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.group16.eventplaza.R;
import com.group16.eventplaza.model.EventCard;

import java.util.ArrayList;

public class LocationFragment extends Fragment implements OnMapReadyCallback{

    private static final String LATITUDE = "LAT";
    private static final String LONGITUDE = "LNG";
    private static final int DEFAULT_ZOOM = 14;
    // Default location Sydney
    private double mLat = -34;
    private double mLng = 151;
    private GoogleMap mMaps;
    private MarkerOptions markerOptions;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mLat = getArguments().getDouble(LATITUDE);
            mLng = getArguments().getDouble(LONGITUDE);
        }
    markerOptions = new MarkerOptions();

        // Update google map location
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if(requestKey.equals("requestKey")){
                    mLat =result.getDouble(LATITUDE);
                    mLng =result.getDouble(LONGITUDE);
                    mMaps.clear();
                    LatLng location = new LatLng(mLat, mLng);
                    mMaps.moveCamera(CameraUpdateFactory.newLatLng(location));
                    mMaps.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                }


            }
        });

        getParentFragmentManager().setFragmentResultListener("events", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if(requestKey.equals("events")){
                    ArrayList<EventCard> eventCards = (ArrayList<EventCard>) result.getSerializable("eventList");

                    for (EventCard card: eventCards) {
                        LatLng location = new LatLng(card.getLat(), card.getLng());
                        mMaps.addMarker(new MarkerOptions().position(location)).setTitle(card.getTitle());
                    }

                }


            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if(getArguments() != null){
            mLat = getArguments().getDouble(LATITUDE);
            mLng = getArguments().getDouble(LONGITUDE);
        }
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMaps = googleMap;
        LatLng location = new LatLng(mLat, mLng);
        mMaps.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMaps.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        // Enable zoom controls
        mMaps.getUiSettings().setZoomControlsEnabled(true);
        // Animate the camera to zoom into the map
        mMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }

}
