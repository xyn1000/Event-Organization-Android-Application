package com.group16.eventplaza;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.group16.eventplaza.event.EventActivity;
import com.group16.eventplaza.event.ManageEventActivity;
import com.group16.eventplaza.event.RatingEventActivity;
import com.group16.eventplaza.fragment.HomeFragment;
import com.group16.eventplaza.fragment.MineFragment;
import com.group16.eventplaza.fragment.NotificationFragment;
import com.group16.eventplaza.fragment.PlazaFragment;
import com.group16.eventplaza.fragment.SearchFragment;
import com.group16.eventplaza.fragment.SimpleFragment;
import com.group16.eventplaza.databinding.ActivityHomeBinding;
import com.group16.eventplaza.map.LocationFragment;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.utils.FirebaseUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity_log==》";

    private ActivityHomeBinding homeBinding;
    private List<Fragment> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        initFragment();

        setRd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * todo： 判断权限是否完整
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        /**
         * todo： 刷新个人中心的数据
         */
        ((MineFragment)list.get(4)).reload();

        list.set(0,new HomeFragment());

    }



    private void initFragment() {
        /**
         * todo： 添加导航栏页面fragment到list中
         */
        list = new ArrayList<>();
        list.add(new HomeFragment());
        list.add(new SearchFragment());
        list.add(new PlazaFragment());
        list.add(new NotificationFragment());
        list.add(new MineFragment());


        homeBinding.viewPager.setOffscreenPageLimit(list.size() - 1);                                                                     													//设置适配器
        homeBinding.viewPager.setUserInputEnabled(false);                                                                    													//设置适配器
        FragmentStateAdapter adapter=new FragmentStateAdapter(HomeActivity.this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return list.get(position);
            }
            @Override
            public int getItemCount() {
                return list.size();
            }
        };
        /**
         * todo： 绑定viewpager2和适配器
         */
        homeBinding.viewPager.setAdapter(adapter);
        /**
         * todo： 底部导航按钮的点切换事件
         */
        homeBinding.rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.rd1){
                    homeBinding.viewPager.setCurrentItem(0,false);
                }else if (radioGroup.getCheckedRadioButtonId() == R.id.rd2){
                    homeBinding.viewPager.setCurrentItem(1,false);
                }else if (radioGroup.getCheckedRadioButtonId() == R.id.rd3){
                    homeBinding.viewPager.setCurrentItem(2,false);
                }else if (radioGroup.getCheckedRadioButtonId() == R.id.rd4){
                    homeBinding.viewPager.setCurrentItem(3,false);
                }else if (radioGroup.getCheckedRadioButtonId() == R.id.rd5){
                    homeBinding.viewPager.setCurrentItem(4,false);
                }
            }
        });
        homeBinding.rg.check(R.id.rd3);
    }

    /**
     * todo： 设置底部按钮的资源文件位置大小
     */
    public void setRd() {
        Drawable drawable1 = getResources().getDrawable(R.drawable.radiobutton_home);
        drawable1.setBounds(0, 0, 50, 50);
        homeBinding.rd1.setCompoundDrawables(null, drawable1, null, null);

        Drawable drawable2 = getResources().getDrawable(R.drawable.radiobutton_search);
        drawable2.setBounds(0, 0, 50, 50);
        homeBinding.rd2.setCompoundDrawables(null, drawable2, null, null);

        Drawable drawable3 = getResources().getDrawable(R.drawable.radiobutton_plaza);
        drawable3.setBounds(0, 0, 50, 50);
        homeBinding.rd3.setCompoundDrawables(null, drawable3, null, null);

        Drawable drawable4 = getResources().getDrawable(R.drawable.radiobutton_notifications);
        drawable4.setBounds(0, 0, 50, 50);
        homeBinding.rd4.setCompoundDrawables(null, drawable4, null, null);

        Drawable drawable5 = getResources().getDrawable(R.drawable.radiobutton_account);
        drawable5.setBounds(0, 0, 50, 50);
        homeBinding.rd5.setCompoundDrawables(null, drawable5, null, null);
    }

    //user email:378031309@qq.com password:123456 id:vOULO5XgnsMBSJvUdCptL753dNh1
    //event title:qq 01 running id:3sAGtZFZYhOtz9vNVVxg
    //event title:qq 02 running id:yrKNAZpUq4AwRlfwg0WZ
    //event title:qq 02 running id:XZKQH6wWAlMEJSgmyHPh

    //user email:378031309@qqq.com password:123456 id:1bjWTEaMKcZyOAKngjf7DGz18XL2


    public void Event01(View view) {
        // get the userId of the event clicked
        String eventOwnerUserId = "vOULO5XgnsMBSJvUdCptL753dNh1";
        // get the event status
        String status = "ended";

        Intent intent = null;
        if (FirebaseUtil.getUserId(this).equals(eventOwnerUserId)) {
            // login user id = event owner id: owner opening event
            intent = new Intent(HomeActivity.this, ManageEventActivity.class);
        }else{
            // login user id != event owner id: visitor opening event
            if(status != "running"){
                // event ended
                intent = new Intent(HomeActivity.this, RatingEventActivity.class);
            }else{
                // event running
                intent = new Intent(HomeActivity.this, EventActivity.class);
            }

        }
        // put event id for information display
        intent.putExtra("eventId", "3sAGtZFZYhOtz9vNVVxg");
        Log.d(TAG, "login user ID:  "+FirebaseUtil.getUserId(this));
        Log.d(TAG, "event Owner User Id:  "+"vOULO5XgnsMBSJvUdCptL753dNh1");
        Log.d(TAG, "event Id: "+"3sAGtZFZYhOtz9vNVVxg");
        startActivity(intent);
    }

    public void Event02(View view) {
        // get the userId of the event clicked
        String eventOwnerUserId = "vOULO5XgnsMBSJvUdCptL753dNh1";
        Intent intent = null;
        String status = "running";
        if (FirebaseUtil.getUserId(this).equals(eventOwnerUserId)) {
            // login user id = event owner id: owner opening event
            intent = new Intent(HomeActivity.this, ManageEventActivity.class);
        }else{
            // login user id != event owner id: visitor opening event
            if(status != "running"){
                // event ended
                intent = new Intent(HomeActivity.this, RatingEventActivity.class);
            }else{
                // event running
                intent = new Intent(HomeActivity.this, EventActivity.class);
            }
        }
        // put event id for information display
        intent.putExtra("eventId", "yrKNAZpUq4AwRlfwg0WZ");
        Log.d(TAG, "login user ID:  "+FirebaseUtil.getUserId(this));
        Log.d(TAG, "event Owner User Id:  "+"vOULO5XgnsMBSJvUdCptL753dNh1");
        Log.d(TAG, "event Id: "+"yrKNAZpUq4AwRlfwg0WZ");
        startActivity(intent);
    }

    public void Event03(View view) {
            // get the userId of the event clicked
            String eventOwnerUserId = "vOULO5XgnsMBSJvUdCptL753dNh1";
            // get the event status
            String status = "running";
            Intent intent = null;
            if (FirebaseUtil.getUserId(this).equals(eventOwnerUserId)) {
                // login user id = event owner id: owner opening event
                intent = new Intent(HomeActivity.this, ManageEventActivity.class);
            }else{
                // login user id != event owner id: visitor opening event
                if(status != "running"){
                    // event ended
                    intent = new Intent(HomeActivity.this, RatingEventActivity.class);
                }else{
                    // event running
                    intent = new Intent(HomeActivity.this, EventActivity.class);
                }
            }
            // put event id for information display
            intent.putExtra("eventId", "XZKQH6wWAlMEJSgmyHPh");
            Log.d(TAG, "login user ID:  "+FirebaseUtil.getUserId(this));
            Log.d(TAG, "event Owner User Id:  "+"vOULO5XgnsMBSJvUdCptL753dNh1");
            Log.d(TAG, "event Id: "+"XZKQH6wWAlMEJSgmyHPh");
            startActivity(intent);
    }

    public void Event04Ended(View view) {
        // get the userId of the event clicked
        String eventOwnerUserId = "vOULO5XgnsMBSJvUdCptL753dNh1";
        // get the event status
        String status = "ended";
        Intent intent = null;
        if (FirebaseUtil.getUserId(this).equals(eventOwnerUserId)) {
            // login user id = event owner id: owner opening event
            intent = new Intent(HomeActivity.this, ManageEventActivity.class);
        }else{
            // login user id != event owner id: visitor opening event
            if(status != "running"){
                // event ended
                intent = new Intent(HomeActivity.this, RatingEventActivity.class);
            }else{
                // event running
                intent = new Intent(HomeActivity.this, EventActivity.class);
            }
        }
        // put event id for information display
        intent.putExtra("eventId", "AR1EMlJRIBJzmI9uHPgH");
        Log.d(TAG, "login user ID:  "+FirebaseUtil.getUserId(this));
        Log.d(TAG, "event Owner User Id:  "+"vOULO5XgnsMBSJvUdCptL753dNh1");
        Log.d(TAG, "event Id: "+"AR1EMlJRIBJzmI9uHPgH");
        startActivity(intent);
    }
}