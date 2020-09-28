package com.ivyiot.appsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ivyiot.appsdk.adapter.DeviceSearchAdapter;
import com.ivyiot.ipclibrary.LibraryInfo;
import com.ivyiot.ipclibrary.model.DiscoveryDev;
import com.ivyiot.ipclibrary.sdk.SDKManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final String TAG = "MainActivity";

    private ListView lv_device_search;
    private List<DiscoveryDev> devArr = new ArrayList<>();
    private DeviceSearchAdapter devAda;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_device_search = findViewById(R.id.lv_device_search);
        findViewById(R.id.btn_refresh).setOnClickListener(this);
        Toast.makeText(this, "The version code is " + LibraryInfo.VersionCode, Toast.LENGTH_LONG).show();
        searchIvyCameraInWLAN();
        lv_device_search.setOnItemClickListener(this);
        findViewById(R.id.btn_audio_wave_add).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_refresh:
                searchIvyCameraInWLAN();
                break;
            case R.id.btn_audio_wave_add:
                Intent mIntent = new Intent();
                mIntent.setClass(this, SoundWaveAddActivity.class);
                //mIntent.putExtra("uid", "TMR3OULDQCTFQJMDZZZZ9Y5I");
                mIntent.putExtra("wifi_ssid", "TP-LINK_MZP");
                mIntent.putExtra("wifi_password", "123456app");
                startActivity(mIntent);
                break;
        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DiscoveryDev dev = devArr.get(position);
        if(null != dev){
            Intent mIntent = new Intent();
            mIntent.setClass(this, LiveVideoActivity.class);
            mIntent.putExtra("ivyDevice", dev);
            startActivity(mIntent);
        }
    }

}
