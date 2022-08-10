package com.ivyiot.appsdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ivyiot.ipclibrary.common.Logs;
import com.ivyiot.ipclibrary.event.EventID;
import com.ivyiot.ipclibrary.model.DiscoveryDev;
import com.ivyiot.ipclibrary.model.DiskInfo;
import com.ivyiot.ipclibrary.model.IvyNVR;
import com.ivyiot.ipclibrary.model.NVRIPCInfo;
import com.ivyiot.ipclibrary.model.NVRMotionDetect;
import com.ivyiot.ipclibrary.model.StreamMode;
import com.ivyiot.ipclibrary.model.StrogeConfigModel;
import com.ivyiot.ipclibrary.sdk.ISdkCallback;
import com.ivyiot.ipclibrary.video.INVRLiveVideoView;
import com.ivyiot.ipclibrary.video.VideoSurfaceView;
import com.ivyiot.ipclibrary.video.VideoSurfaceViewNVR;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NVRLiveVideoActivity extends AppCompatActivity implements Observer, View.OnClickListener, INVRLiveVideoView {
    private final String TAG = "MainActivity";
    private VideoSurfaceView videoview;
    private IvyNVR nvr;

    /**
     * 默认用户名密码
     */
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "foscam1";

    /**
     * 当前设置的通道集合
     */
    private List<NVRIPCInfo> mNvripcInfos;

    private VideoSurfaceViewNVR live_surface_view_nvr_1;
    private VideoSurfaceViewNVR live_surface_view_nvr_2;
    private VideoSurfaceViewNVR live_surface_view_nvr_3;
    private VideoSurfaceViewNVR live_surface_view_nvr_4;

    private int currentChannel = 0 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nvr_live_video_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        live_surface_view_nvr_1 = findViewById(R.id.live_surface_view_nvr_1);
        live_surface_view_nvr_2 = findViewById(R.id.live_surface_view_nvr_2);
        live_surface_view_nvr_3 = findViewById(R.id.live_surface_view_nvr_3);
        live_surface_view_nvr_4 = findViewById(R.id.live_surface_view_nvr_4);

        findViewById(R.id.btn_open_nvr_1).setOnClickListener(this);
        findViewById(R.id.btn_close_nvr_1).setOnClickListener(this);

        findViewById(R.id.btn_snap_nvr_1).setOnClickListener(this);
        findViewById(R.id.btn_nvr_start_record).setOnClickListener(this);
        findViewById(R.id.btn_nvr_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_get_mirror_flip).setOnClickListener(this);
        findViewById(R.id.btn_set_mirror).setOnClickListener(this);
        findViewById(R.id.btn_set_flip).setOnClickListener(this);
        findViewById(R.id.btn_get_motion_detect_config).setOnClickListener(this);
        findViewById(R.id.btn_set_motion_detect_config).setOnClickListener(this);
        findViewById(R.id.btn_reboot_device).setOnClickListener(this);
        findViewById(R.id.btn_get_disk_info).setOnClickListener(this);
        findViewById(R.id.btn_get_stroge_config).setOnClickListener(this);
        findViewById(R.id.btn_set_stroge_config).setOnClickListener(this);
        findViewById(R.id.btn_format_hard_disk).setOnClickListener(this);



        findViewById(R.id.btn_goto_playback).setOnClickListener(this);
        Intent intent = getIntent();
        DiscoveryDev dev = (DiscoveryDev) intent.getSerializableExtra("ivyDevice");
        if (null != dev) {
            nvr = new IvyNVR();
            nvr.uid = dev.uid;
            nvr.usrName = DEFAULT_USER_NAME;
            nvr.password = DEFAULT_PASSWORD;//SDKManager.getInstance().getUnfeelingPassword(camera.uid);
        }

        nvr.loginDevice(new ISdkCallback() {
            @Override
            public void onSuccess(Object result) {
                Logs.e("NVR 登陆成功");

            }

            @Override
            public void onError(int errorCode) {

            }

            @Override
            public void onLoginError(int errorCode) {

            }
        });

    }

    @Override
    protected void onResume() {
        //videoview.openVideo(camera);
        super.onResume();
        // 订阅 ipc 的事件消息
        nvr.addObserver(NVRLiveVideoActivity.this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            Message msg = (Message) arg;
            Log.e(TAG, "update: " + msg.what + ";data=" + msg.obj);
            switch (msg.what) {
                case EventID.IVY_CTRL_MSG_IPCLIST_CHG:
                    mNvripcInfos = nvr.getNvripcInfos();
                    if (null != mNvripcInfos) {
                        live_surface_view_nvr_1.openNVRVideo(nvr, mNvripcInfos.get(0).channel, StreamMode.STREAM_MAIN, NVRLiveVideoActivity.this);
                    }
                    break;
            }
        }

    }

    private NVRMotionDetect nvrMotionDetect;
    private StrogeConfigModel strogeConfigModel;
    private DiskInfo diskInfo;
    @Override
    public void onClick(View v) {
        if (null == mNvripcInfos) {
            return;
        }
        currentChannel =  mNvripcInfos.get(0).channel;
        switch (v.getId()) {
            case R.id.btn_open_nvr_1://连接
                live_surface_view_nvr_1.openNVRVideo(nvr, currentChannel, StreamMode.STREAM_MAIN, NVRLiveVideoActivity.this);
                break;
            case R.id.btn_close_nvr_1://连接
                live_surface_view_nvr_1.closeVideo(currentChannel);
                break;
            case R.id.btn_snap_nvr_1://连接
                String imgePath = "";
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    imgePath = getExternalFilesDir(null).getPath() + "/123.jpg";//沙盒路徑
                } else {
                    imgePath = Environment.getExternalStorageDirectory() + "/123.jpg";
                }
                live_surface_view_nvr_1.snap(true, imgePath, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(NVRLiveVideoActivity.this, "channel 1 snap success!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_nvr_start_record:
                String recordePath = "";
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    recordePath = getExternalFilesDir(null).getPath() + "/123.mp4";//沙盒路徑
                } else {
                    recordePath = Environment.getExternalStorageDirectory() + "/123.mp4";
                }
                nvr.startRecord(recordePath, currentChannel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(NVRLiveVideoActivity.this, "channel 1 record start success!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_nvr_stop_record:
                nvr.stopRecord(currentChannel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(NVRLiveVideoActivity.this, "channel 1 record stop success!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_mirror_flip:
                nvr.getMirrorAndFlip(currentChannel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_set_mirror:
                nvr.setMirror(1, currentChannel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_set_flip:
                nvr.setFlip(1, currentChannel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_motion_detect_config:

                nvr.getMotionDetectConfig(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        nvrMotionDetect = (NVRMotionDetect) result;
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_set_motion_detect_config:
                if(null == nvrMotionDetect){
                    return;
                }
                nvr.setMotionDetectConfig(nvrMotionDetect, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_reboot_device:
                nvr.rebootDevice(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_disk_info:
                nvr.getDiskInfo(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        diskInfo = (DiskInfo) result;
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_stroge_config:
                nvr.getStorageConfig(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        strogeConfigModel = (StrogeConfigModel) result;
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_set_stroge_config:
                if(null == strogeConfigModel){
                    return;
                }
                nvr.setStorageConfig(strogeConfigModel, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_format_hard_disk:
                if(null == diskInfo){
                    return;
                }
                nvr.formatHardDisk(diskInfo.index, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;

            case R.id.btn_goto_playback:
                Intent mIntent = new Intent();
                mIntent.setClass(this, NVRPlaybackActivity.class);
                IvyApplication.getInstance().putCache("ivyDevice", nvr);
                startActivity(mIntent);
                break;

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (null != videoview) {
            videoview.closeVideo();
        }
        if (null != nvr) {
            nvr.deleteObserver(this);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nvr.destroy();
    }


    @Override
    public void openVideoSuccess(int cameraHandlerNo) {

    }

    @Override
    public void onOpenVideoFail(int errorCode) {

    }


    @Override
    public void closeVideoSuccess() {

    }

    @Override
    public void closeVideoFail(int errorCode) {

    }

    @Override
    public void netFlowSpeedRefresh(String s) {

    }

    @Override
    public void firstFrameDone(Bitmap bitmap) {

    }

}