package com.ivyiot.appsdk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ivyio.sdk.DevType;
import com.ivyio.sdk.LogLevel;
import com.ivyiot.appsdk.adapter.DeviceSearchAdapter;
import com.ivyiot.ipclibrary.LibraryInfo;
import com.ivyiot.ipclibrary.model.DiscoveryDev;
import com.ivyiot.ipclibrary.sdk.SDKManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";

    private ListView lv_device_search_ipc;
    private ListView lv_device_search_nvr;
    private List<DiscoveryDev> devIPCArr = new ArrayList<>();
    private List<DiscoveryDev> devNVRArr = new ArrayList<>();
    private DeviceSearchAdapter devIPCAda;
    private DeviceSearchAdapter devNVRAda;
    /**
     * storage permission
     */
    private static final int PERMISSIONS_STORAGE_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_device_search_ipc = findViewById(R.id.lv_device_search_ipc);
        lv_device_search_nvr = findViewById(R.id.lv_device_search_nvr);
        findViewById(R.id.btn_refresh).setOnClickListener(this);
        //Toast.makeText(this, "The version code is " + LibraryInfo.VersionCode, Toast.LENGTH_LONG).show();
        searchIvyCameraInWLAN();
        findViewById(R.id.btn_audio_wave_add).setOnClickListener(this);
        lv_device_search_ipc.setOnItemClickListener((parent, view, position, id) -> {
            DiscoveryDev dev = devIPCArr.get(position);
            if (null != dev) {
                Intent mIntent = new Intent();
                mIntent.setClass(MainActivity.this, LiveVideoActivity.class);
                mIntent.putExtra("ivyDevice", dev);
                startActivity(mIntent);
            }
        });
        lv_device_search_nvr.setOnItemClickListener((parent, view, position, id) -> {
            DiscoveryDev dev = devNVRArr.get(position);
            if (null != dev) {
                Intent mIntent = new Intent();
                mIntent.setClass(MainActivity.this, NVRLiveVideoActivity.class);
                mIntent.putExtra("ivyDevice", dev);
                startActivity(mIntent);
            }
        });
        String imagePath = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            imagePath = getExternalFilesDir(null).getPath() + "/123.log";//沙盒路徑
        } else {
            imagePath = Environment.getExternalStorageDirectory() + "/123.log";
        }
        if(PermissionUtil.chkPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})){
            SDKManager.getInstance().initIvySDKLog("", LogLevel.ALL);//在init之前调用
            SDKManager.getInstance().initIvyAppLog(imagePath,  true);//在init之前调用
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_refresh:
                searchIvyCameraInWLAN();
                break;
            case R.id.btn_audio_wave_add:
//                Intent mIntent = new Intent();
//                mIntent.setClass(this, SoundWaveAddActivity.class);
//                mIntent.putExtra("uid", "2UM3CT9DSJQ46UQ5ZZZZ9Y5I");
//                mIntent.putExtra("wifi_ssid", "TP-LINK_MZP");
//                mIntent.putExtra("wifi_password", "123456app");
//                startActivity(mIntent);

//                Intent mIntent = new Intent();
//                mIntent.setClass(this, CloudPlaybackActivity.class);
//                startActivity(mIntent);
                Intent mIntent = new Intent();
                mIntent.setClass(this, LiveVideoActivity.class);
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
            devIPCArr.clear();
            devNVRArr.clear();
            for (DiscoveryDev dev : nodes) {
                if (dev.type == DevType.IVY_NVR || dev.type == DevType.FOS_NVR) {
                    devNVRArr.add(dev);
                } else {
                    devIPCArr.add(dev);
                }
            }
            devIPCAda = new DeviceSearchAdapter(this, devIPCArr);
            lv_device_search_ipc.setAdapter(devIPCAda);
            devNVRAda = new DeviceSearchAdapter(this, devNVRArr);
            lv_device_search_nvr.setAdapter(devNVRAda);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String imagePath = "";
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    imagePath = getExternalFilesDir(null).getPath() + "/123.log";//沙盒路徑
                } else {
                    imagePath = Environment.getExternalStorageDirectory() + "/123.log";
                }
                if(PermissionUtil.chkPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_STORAGE_CODE)){
                    SDKManager.getInstance().initIvySDKLog("", LogLevel.ALL);//在init之前调用
                    SDKManager.getInstance().initIvyAppLog(imagePath,  true);//在init之前调用
                }
            } else {
                //  Tip.show("Permissions were not granted.");

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
