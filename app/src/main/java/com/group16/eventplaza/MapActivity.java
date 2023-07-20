
package com.group16.eventplaza;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.group16.eventplaza.databinding.ActivityMapBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMapClickListener {
    ActivityMapBinding mapBinding;
    private GoogleMap map;
    private int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location myLocation;
    private LatLng latLng1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapBinding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(mapBinding.getRoot());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mapBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /**
         * todo： 确认选择当前位置  返回上一界面
         */
        mapBinding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty( mapBinding.location.getText()))
                    return;
                Intent intent = new Intent();
                intent.putExtra("location", mapBinding.location.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * todo： 谷歌地图初始化完成
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        /**
         * todo： 绑定我的位置按钮点击事件
         */
        map.setOnMyLocationButtonClickListener(this);
        /**
         * todo： 绑定当前位置点击事件
         */
        map.setOnMyLocationClickListener(this);
        /**
         * todo： 绑定地图点击事件
         */
        map.setOnMapClickListener(this);
        enableMyLocation();

    }
    /**
     * todo： 位置按钮点击事件
     */
    @Override
    public boolean onMyLocationButtonClick() {

        myLocation = map.getMyLocation();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Address> addresses;
                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1);
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String zipCode = addresses.get(0).getPostalCode();
                    String country = addresses.get(0).getCountryCode();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapBinding.location.setText(city);
//                            mapBinding.city.setText(city);

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }).start();


        return false;
    }

    /**
     * todo： 我的位置点击事件
     */

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    /**
     * todo： 判断权限 开启定位当前位置功能
     */

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            return;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }

    /**
     * todo： 当前位置变化监听
     */

    @Override
    public void onMyLocationChange(@NonNull Location location) {

    }


    /**
     * todo： 地图点击事件
     */
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        latLng1 = latLng;
        /**
         * todo： 清除当前地图上的标志物
         */
        map.clear();
        /**
         * todo： 添加标记点
         */
        map.addMarker(new MarkerOptions()
                .position(latLng1)
                .title("Choose this location"));
        /**
         * todo： 移动视图到当前位置  并且以此位置为中心
         */
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    /**
                     * todo： 获取到位置信息  并把位置信息显示到底部文字
                     */
                    List<Address> addresses;
                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(latLng1.latitude,latLng1.longitude, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String zipCode = addresses.get(0).getPostalCode();
                    String country = addresses.get(0).getCountryCode();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapBinding.location.setText(city);
//                            mapBinding.city.setText(city);

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }).start();

    }
}