package com.ivyiot.appsdk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.ivyiot.appsdk.adapter.DeviceSearchAdapter;
import com.ivyiot.ipclibrary.model.DiscoveryDev;
import com.ivyiot.ipclibrary.sdk.SDKManager;

import java.util.ArrayList;
import java.util.List;


/**
 * 声波添加
 */
public class SoundWaveAddActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "SoundWaveAddActivity";

    private List<DiscoveryDev> devArr = new ArrayList<>();
    private DeviceSearchAdapter devAda;
    private ListView lv_device_search;

    private String wifi_ssid = "";
    private String uid = "";
    private String wifi_password = "";
    private Handler mainThread = new Handler();



    private PowerManager.WakeLock wakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound_wave_add_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //保持屏幕常亮
        PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myApp:myLock");
        }
        lv_device_search = findViewById(R.id.lv_device_search);
        findViewById(R.id.btn_play_sound_wave).setOnClickListener(this);
        Intent mIntent = getIntent();
        wifi_ssid = mIntent.getStringExtra("wifi_ssid");
        uid = mIntent.getStringExtra("uid");
        wifi_password = mIntent.getStringExtra("wifi_password");
    }

    @Override
    protected void onResume() {
        super.onResume();
        wakeLock.acquire(10 * 10 * 60 * 1000L /*10 minutes*/);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play_sound_wave:
                if(!TextUtils.isEmpty(wifi_ssid)){
                    SDKManager.getInstance().startSoundWaveAdd(uid, wifi_ssid, wifi_password, 1);
                    mainThread.post(searchWlanDeviceRunable);
                }
                mainThread.post(searchWlanDeviceRunable);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainThread.removeCallbacks(searchWlanDeviceRunable);

    }


    /**
     * 局域网中搜索IvyCamera
     */
    private void searchIvyCameraInWLAN() {
        // 局域网内扫描
        DiscoveryDev[] nodes = SDKManager.getInstance().discoveryDeviceInWLAN();
        if (nodes != null) {
            devArr.clear();
            for (DiscoveryDev dev : nodes ) {
                devArr.add(dev);
            }
            if (devAda == null) {
                devAda = new DeviceSearchAdapter(this,  devArr);
                lv_device_search.setAdapter(devAda);
            } else {
                devAda.notifyDataSetChanged();
            }
        }
    }


    Runnable searchWlanDeviceRunable = new Runnable() {
        @Override
        public void run() {
            searchIvyCameraInWLAN();
            mainThread.postDelayed(this, 2 * 1000);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
