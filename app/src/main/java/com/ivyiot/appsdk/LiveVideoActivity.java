package com.ivyiot.appsdk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.ivyio.sdk.PictureFile;
import com.ivyio.sdk.PictureList;
import com.ivyiot.ipclibrary.audio.AudioThread;
import com.ivyiot.ipclibrary.audio.TalkThread;
import com.ivyiot.ipclibrary.common.Global;
import com.ivyiot.ipclibrary.event.EventID;
import com.ivyiot.ipclibrary.model.DevAbility;
import com.ivyiot.ipclibrary.model.DevAudioDetect;
import com.ivyiot.ipclibrary.model.DevInfo;
import com.ivyiot.ipclibrary.model.DevMotionDetect;
import com.ivyiot.ipclibrary.model.DevSDInfo;
import com.ivyiot.ipclibrary.model.DevSystemTime;
import com.ivyiot.ipclibrary.model.DiscoveryDev;
import com.ivyiot.ipclibrary.model.ECameraPlatform;
import com.ivyiot.ipclibrary.model.EDefinitionItem;
import com.ivyiot.ipclibrary.model.IvyCamera;
import com.ivyiot.ipclibrary.model.PTZCmd;
import com.ivyiot.ipclibrary.model.PictureDetail;
import com.ivyiot.ipclibrary.model.ResetPointList;
import com.ivyiot.ipclibrary.model.StreamMode;
import com.ivyiot.ipclibrary.sdk.CmdHelper;
import com.ivyiot.ipclibrary.sdk.ISdkCallback;
import com.ivyiot.ipclibrary.sdk.SDKResponse;
import com.ivyiot.ipclibrary.util.CommonUtil;
import com.ivyiot.ipclibrary.util.PermissionUtil;
import com.ivyiot.ipclibrary.video.IVideoListener;
import com.ivyiot.ipclibrary.video.VideoSurfaceView;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

public class LiveVideoActivity extends AppCompatActivity implements Observer, View.OnClickListener, IVideoListener {
    private final String TAG = "MainActivity";
    private VideoSurfaceView videoview;
    private IvyCamera camera;

    /**
     * 默认用户名密码
     */
    private static final String DEFAULT_USER_NAME = "cloud888";
    private static final String DEFAULT_PASSWORD = "wang123";
    /**
     * 音频播放类
     */
    private AudioThread audioThread;
    /**
     * 对讲语音发送类
     */
    private TalkThread talkThread;
    /**
     * 录音权限标识
     */
    private static final int PERMISSIONS_CODE_RECORD_AUDIO = 1;

    private enum EInfraLedMode {
        /**
         * 自动
         */
        AUTO,
        /**
         * 手动
         */
        MANUEL,
        /**
         * 定时
         */
        SCHEDULE
    }

    /**
     * 当前设备能力集
     */
    private DevAbility devAbility;
    /**
     * 当前设备信息
     */
    private DevInfo devInfo;
    /**
     * 当前清晰度选项
     */
    private EDefinitionItem.EResolutionMode[] definitionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_video_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        DiscoveryDev dev = (DiscoveryDev) intent.getSerializableExtra("ivyDevice");
        if (null != dev) {
            camera = new IvyCamera();
            camera.uid = "75E74XTE45RJ8537AI6E5YBM";//dev.uid;
            camera.usrName = DEFAULT_USER_NAME;
            camera.password = DEFAULT_PASSWORD;//SDKManager.getInstance().getUnfeelingPassword(camera.uid);
            videoview = findViewById(R.id.videoView);
        }
        findViewById(R.id.btn_conn).setOnClickListener(this);
        findViewById(R.id.btn_live).setOnClickListener(this);
        findViewById(R.id.btn_snap).setOnClickListener(this);
        //监听
        findViewById(R.id.btn_open_audio).setOnClickListener(this);
        findViewById(R.id.btn_close_audio).setOnClickListener(this);
        //对讲
        findViewById(R.id.btn_open_talk).setOnClickListener(this);
        findViewById(R.id.btn_close_talk).setOnClickListener(this);

        //设备信息
        findViewById(R.id.btn_get_device_info).setOnClickListener(this);
        //能力集
        findViewById(R.id.btn_get_device_ability).setOnClickListener(this);
        //清晰度
        findViewById(R.id.btn_get_definition).setOnClickListener(this);
        findViewById(R.id.btn_set_definition).setOnClickListener(this);
        //云台相关
        findViewById(R.id.btn_ptz_up).setOnClickListener(this);
        findViewById(R.id.btn_ptz_down).setOnClickListener(this);
        findViewById(R.id.btn_ptz_left).setOnClickListener(this);
        findViewById(R.id.btn_ptz_right).setOnClickListener(this);
        //预置位
        findViewById(R.id.btn_preset_get).setOnClickListener(this);
        findViewById(R.id.btn_preset_execute).setOnClickListener(this);
        findViewById(R.id.btn_preset_add).setOnClickListener(this);
        findViewById(R.id.btn_preset_delete).setOnClickListener(this);
        //巡航
        findViewById(R.id.btn_cruise_horizontal).setOnClickListener(this);
        findViewById(R.id.btn_cruise_vertical).setOnClickListener(this);
        findViewById(R.id.btn_cruise_stop).setOnClickListener(this);
        //红外
        findViewById(R.id.btn_night_vision_auto).setOnClickListener(this);
        findViewById(R.id.btn_night_vision_open).setOnClickListener(this);
        findViewById(R.id.btn_night_vision_close).setOnClickListener(this);
        findViewById(R.id.btn_night_vision_schedule).setOnClickListener(this);
        //设备音量
        findViewById(R.id.btn_get_ipc_volume).setOnClickListener(this);
        findViewById(R.id.btn_set_ipc_volume).setOnClickListener(this);
        //声音侦测
        findViewById(R.id.btn_get_sound_detect).setOnClickListener(this);
        findViewById(R.id.btn_set_sound_detect).setOnClickListener(this);


        findViewById(R.id.btn_get_wifi_detail).setOnClickListener(this);
        findViewById(R.id.btn_goto_playback).setOnClickListener(this);
        findViewById(R.id.btn_get_device_time).setOnClickListener(this);
        findViewById(R.id.btn_zoom_in).setOnClickListener(this);
        findViewById(R.id.btn_sdcard_format).setOnClickListener(this);
        findViewById(R.id.btn_get_sdcard_info).setOnClickListener(this);


        findViewById(R.id.btn_sleep).setOnClickListener(this);
        findViewById(R.id.btn_wakeup).setOnClickListener(this);
        findViewById(R.id.btn_reboot).setOnClickListener(this);
        findViewById(R.id.btn_wdr).setOnClickListener(this);

        findViewById(R.id.btn_get_picture_list).setOnClickListener(this);
        findViewById(R.id.btn_picture_download).setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        //videoview.openVideo(camera);
        super.onResume();
        // 订阅 ipc 的事件消息
        camera.addObserver(LiveVideoActivity.this);

        // 向 ipc 发送命令，获取或者设置ipc信息。
        camera.getDevInfo(new ISdkCallback<DevInfo>() {
            @Override
            public void onSuccess(DevInfo result) {
                Log.e(TAG, "getDevInfo onSuccess. uid= " + result.uid + " ,mac= " + result.mac);
                devInfo = result;
                Toast.makeText(LiveVideoActivity.this, "getDevInfo success. ipc name=" + result.devName, Toast.LENGTH_SHORT).show();
                videoview.setVisibility(View.VISIBLE);
                //打开视频，命令结果通过回调方法接收，见 IVideoListener
                //videoview.openVideo(camera, LiveVideoActivity.this);
            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "getDevInfo onError:" + errorCode);
            }

            @Override
            public void onLoginError(int errorCode) {
                Log.e(TAG, "getDevInfo onLoginError:" + errorCode);
//                        if(15 == errorCode){
//                            camera.modifyUsrNameAndPwd("fos", "abc123", new ISdkCallback() {
//                                @Override
//                                public void onSuccess(Object result) {
//
//                                }
//
//                                @Override
//                                public void onError(int errorCode) {
//
//                                }
//
//                                @Override
//                                public void onLoginError(int errorCode) {
//
//                                }
//                            });
//                        }
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            Message msg = (Message) arg;
            Log.e(TAG, "update: " + msg.what + ";data=" + msg.obj);
            switch (msg.what) {
                case EventID.IVY_CTRL_MSG_VIDEO_STREAM_MODE:
                    int hdsdValue = (int) msg.obj;
                    break;
            }
        }
    }

    /**
     * 声音侦测配置
     */
    private DevAudioDetect devAudioDetect;

    private DevMotionDetect devMotionDetect;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_conn://连接
                // 连接ipc
//                Global.es.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        int result = camera.login();
//                        Log.e(TAG, "login: " + result);
//                    }
//                });

                // 订阅 ipc 的事件消息
                camera.addObserver(LiveVideoActivity.this);

                // 向 ipc 发送命令，获取或者设置ipc信息。
                camera.getDevInfo(new ISdkCallback<DevInfo>() {
                    @Override
                    public void onSuccess(DevInfo result) {
                        Log.e(TAG, "getDevInfo onSuccess. uid= " + result.uid + " ,mac= " + result.mac);
                        devInfo = result;
                        Toast.makeText(LiveVideoActivity.this, "getDevInfo success. ipc name=" + result.devName, Toast.LENGTH_SHORT).show();
                        getDoorSensorInfo();
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e(TAG, "getDevInfo onError:" + errorCode);
                    }

                    @Override
                    public void onLoginError(int errorCode) {
                        Log.e(TAG, "getDevInfo onLoginError:" + errorCode);
//                        if(15 == errorCode){
//                            camera.modifyUsrNameAndPwd("fos", "abc123", new ISdkCallback() {
//                                @Override
//                                public void onSuccess(Object result) {
//
//                                }
//
//                                @Override
//                                public void onError(int errorCode) {
//
//                                }
//
//                                @Override
//                                public void onLoginError(int errorCode) {
//
//                                }
//                            });
//                        }
                    }
                });
                break;
            case R.id.btn_live://直播
                videoview.setVisibility(View.VISIBLE);
                camera.loginDevice(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        //打开视频，命令结果通过回调方法接收，见 IVideoListener
                        videoview.openVideo(camera, LiveVideoActivity.this);
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });

                break;
            case R.id.btn_snap:
                String imgePath = "";
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    imgePath = getExternalFilesDir(null).getPath() + "123.jpg";//沙盒路徑
                } else {
                    imgePath = Environment.getExternalStorageDirectory() + "123.jpg";
                }
                //抓拍
                videoview.snap(true, imgePath, new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(LiveVideoActivity.this, "snap success!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_open_audio://打开监听
                camera.openAudio(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (null == audioThread) {
                            audioThread = new AudioThread(camera, false);
                            audioThread.startAudio();
                            audioThread.start();
                        }
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_close_audio://关闭监听
                camera.closeAudio(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (null != audioThread) {
                            audioThread.stopAudio();
                            audioThread = null;
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                    }

                    @Override
                    public void onLoginError(int errorCode) {
                    }
                });
                break;
            case R.id.btn_open_talk://打开对讲
                if (PermissionUtil.chkPermission(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_CODE_RECORD_AUDIO)) {
                    camera.openTalk(new ISdkCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            if (null == talkThread) {
                                talkThread = new TalkThread(camera, false);
                                talkThread.startTalk();
                                talkThread.start();
                            }
                        }

                        @Override
                        public void onError(int errorCode) {

                        }

                        @Override
                        public void onLoginError(int errorCode) {

                        }
                    });
                }
                break;
            case R.id.btn_close_talk://关闭对讲
                camera.closeTalk(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (null != talkThread) {
                            talkThread.stopTalk();
                            talkThread = null;
                        }
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_device_info://获取设备信息
                // 向 ipc 发送命令，获取或者设置ipc信息。
                camera.getDevInfo(new ISdkCallback<DevInfo>() {
                    @Override
                    public void onSuccess(DevInfo result) {
                        Log.e(TAG, "getDevInfo onSuccess. uid= " + result.uid + " ,mac= " + result.mac);
                        devInfo = result;
                        Toast.makeText(LiveVideoActivity.this, "getDevInfo success. ipc name=" + result.devName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e(TAG, "getDevInfo onError:" + errorCode);
                    }

                    @Override
                    public void onLoginError(int errorCode) {
                        Log.e(TAG, "getDevInfo onLoginError:" + errorCode);
                    }
                });
                break;
            case R.id.btn_get_device_ability://获取能力集
                // 向 ipc 发送命令，获取或者设置ipc信息。
                camera.getDevAbility(new ISdkCallback<DevAbility>() {
                    @Override
                    public void onSuccess(DevAbility result) {
                        devAbility = result;
                        Log.e(TAG, "getDevAbility onSuccess.  " + devAbility.toString());
                        Toast.makeText(LiveVideoActivity.this, "getDevAbility success. ipc name=" + devAbility.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e(TAG, "getDevAbility onError:" + errorCode);
                    }

                    @Override
                    public void onLoginError(int errorCode) {
                        Log.e(TAG, "getDevAbility onLoginError:" + errorCode);
                    }
                });
                break;
            case R.id.btn_get_definition://获取清晰度-- 需要设备信息和能力集两个前置条件
                if (null == devInfo) {
                    Toast.makeText(LiveVideoActivity.this, "先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (null == devAbility) {
                    Toast.makeText(LiveVideoActivity.this, "先获取设备能力集", Toast.LENGTH_SHORT).show();
                    return;
                }
                definitionItem = camera.getDefinitionItems(devAbility, devInfo);
                break;
            case R.id.btn_set_definition://设置清晰度
                if (null == devInfo) {
                    Toast.makeText(LiveVideoActivity.this, "先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (null == devAbility) {
                    Toast.makeText(LiveVideoActivity.this, "先获取设备能力集", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (null == definitionItem) {
                    Toast.makeText(LiveVideoActivity.this, "先获取清晰度集合", Toast.LENGTH_SHORT).show();
                    return;
                }
                final boolean streamMode = definitionItem.length == 2 && !CommonUtil.is313EPlatform(devAbility);
                if (streamMode) {//主/子码流切换
                    changeStream(1);
                } else {//三档切换
                    //camera.changeDefinition(definitionItem.getResolution(definitionItem.getCurrRules()[0]), null);
                    camera.changeDefinition(0, null);
                }
                break;
            case R.id.btn_ptz_up:
                //camera.ptzControl(PTZCmd.PTZ_MOVE_UP, null);
                camera.setFlip(1, null);
                break;
            case R.id.btn_ptz_down:
                camera.setFlip(0, null);
                //camera.ptzControl(PTZCmd.PTZ_MOVE_DOWN, null);
                break;
            case R.id.btn_ptz_left:
                //camera.ptzControl(PTZCmd.PTZ_STOP, null);
                camera.setMirror(1, null);
                break;
            case R.id.btn_ptz_right:
                //camera.ptzControl(PTZCmd.PTZ_MOVE_RIGHT, null);
                camera.setMirror(0, null);
                break;
            case R.id.btn_preset_get:
                camera.getPTZPresetList(new ISdkCallback<ResetPointList>() {
                    @Override
                    public void onSuccess(ResetPointList result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_preset_execute:
                camera.goToPTZPresetPoint("TopMost", new ISdkCallback() {
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
            case R.id.btn_preset_add:
                camera.addPTZPreset("test1", new ISdkCallback() {
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
            case R.id.btn_preset_delete:
                camera.deletePTZPreset("test1", new ISdkCallback<ResetPointList>() {
                    @Override
                    public void onSuccess(ResetPointList result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_cruise_horizontal:
                camera.startPTZCruise("Horizontal", null);
                break;
            case R.id.btn_cruise_vertical:
                camera.startPTZCruise("Vertical", null);
                break;
            case R.id.btn_cruise_stop:
                camera.stopPTZCruise(null);
                break;
            case R.id.btn_night_vision_auto:
                camera.setInfraredLed(EInfraLedMode.AUTO.ordinal(), 0, null);
                break;
            case R.id.btn_night_vision_open:
                camera.setInfraredLed(EInfraLedMode.MANUEL.ordinal(), 1, null);
                break;
            case R.id.btn_night_vision_close:
                camera.setInfraredLed(EInfraLedMode.MANUEL.ordinal(), 0, null);
                break;
            case R.id.btn_night_vision_schedule:
                camera.setInfraredLed(EInfraLedMode.SCHEDULE.ordinal(), 1, null);
                break;
            case R.id.btn_get_ipc_volume:
                camera.getSpeakVolume(null);
                break;
            case R.id.btn_set_ipc_volume:
                camera.setSpeakVolume(50, null);
                break;
            case R.id.btn_get_sound_detect:
                camera.getAudioDetectConfig(new ISdkCallback<DevAudioDetect>() {
                    @Override
                    public void onSuccess(DevAudioDetect result) {
                        devAudioDetect = result;
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_set_sound_detect:
                if (null != devAudioDetect) {
                    camera.setAudioDetectConfig(devAudioDetect, new ISdkCallback<DevAudioDetect>() {
                        @Override
                        public void onSuccess(DevAudioDetect result) {

                        }

                        @Override
                        public void onError(int errorCode) {

                        }

                        @Override
                        public void onLoginError(int errorCode) {

                        }
                    });
                }

                break;

            case R.id.btn_goto_playback:
                Intent mIntent = new Intent();
                mIntent.setClass(this, PlaybackActivity.class);
                IvyApplication.getInstance().putCache("ivyDevice", camera);
                startActivity(mIntent);
                break;
            case R.id.btn_get_device_time:
                camera.getSystemTime(new ISdkCallback<DevSystemTime>() {
                    @Override
                    public void onSuccess(DevSystemTime result) {
                        Toast.makeText(LiveVideoActivity.this, "get device time success. systemTime=" + result.year + "_" + result.mon + "_" + result.day + "_" + result.hour + "_" + result.minute + "_" + result.sec, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_zoom_in:
                camera.ptzControl(PTZCmd.PTZ_ZOOM_IN, null);
                break;
            case R.id.btn_get_wifi_detail:
                /*camera.getWiFiSetting(new ISdkCallback<DevWiFiDetail>() {
                    @Override
                    public void onSuccess(DevWiFiDetail result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });*/
                camera.getMotionDetectConfig(new ISdkCallback<DevMotionDetect>() {
                    @Override
                    public void onSuccess(DevMotionDetect result) {
                        devMotionDetect = result;
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_get_sdcard_info:
                if (null != devMotionDetect) {
                    camera.setMotionDetectConfig(devMotionDetect, null);
                }
                camera.getSDInfo(new ISdkCallback<DevSDInfo>() {
                    @Override
                    public void onSuccess(DevSDInfo result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });

                break;
            case R.id.btn_sdcard_format:
                if (null == devAbility) {
                    Toast.makeText(LiveVideoActivity.this, "先获取设备能1111力集!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ECameraPlatform.Amba != CommonUtil.getCameraPlatform(devAbility)) {//安霸平台
                    Toast.makeText(LiveVideoActivity.this, "设备不支持", Toast.LENGTH_SHORT).show();
                    return;
                }
                camera.formatSD(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(LiveVideoActivity.this, "格式化成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_sleep:
                camera.deviceSleep(new ISdkCallback<DevSDInfo>() {
                    @Override
                    public void onSuccess(DevSDInfo result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });

                break;
            case R.id.btn_wakeup:
                camera.deviceWakeUp(new ISdkCallback() {
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
            case R.id.btn_reboot:
                camera.rebootDevice(new ISdkCallback() {
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
            case R.id.btn_wdr:
//                camera.setWDRMode(true, new ISdkCallback() {
//                    @Override
//                    public void onSuccess(Object result) {
//
//                    }
//
//                    @Override
//                    public void onError(int errorCode) {
//
//                    }
//
//                    @Override
//                    public void onLoginError(int errorCode) {
//
//                    }
//                });

                camera.updateDeviceName("yhj", new ISdkCallback() {
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
            case R.id.btn_get_picture_list:
                //注意：Calendar类的月份从0开始。
                Calendar cal = Calendar.getInstance();
                //2019.9.23 00:00:00
                cal.set(2021, 10, 14, 0, 0, 0);
                int todayStart = (int) (cal.getTimeInMillis() / 1000);
                //2019.9.23 23:59:59
                cal.set(2021, 10, 14, 23, 59, 59);
                int todayEnd = (int) (cal.getTimeInMillis() / 1000);
                camera.getPictureList(todayStart, todayEnd, 511, 0, new ISdkCallback<PictureList>() {
                    @Override
                    public void onSuccess(PictureList result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
                break;
            case R.id.btn_picture_download:
                PictureDetail pictureInfo = new PictureDetail();
                pictureInfo.format = 200;
                pictureInfo.time = 1630308602;
                pictureInfo.type = 1;
                pictureInfo.direction = 1;
                pictureInfo.weight = 1028;
                camera.downloadPictureFile(pictureInfo, new ISdkCallback<PictureFile>() {
                    @Override
                    public void onSuccess(PictureFile result) {

                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
//                camera.setCloudRecord("682cf1b1f1a24f68b52f5f21f2f01824", "8047dd522762fa846c91bc908e10c85923447700626EEDFEC4",
//                        "https://test-api.myfoscam.cn", 443, "00626EEDFEC4", new ISdkCallback() {
//                    @Override
//                    public void onSuccess(Object result) {
//
//                    }
//
//                    @Override
//                    public void onError(int errorCode) {
//
//                    }
//
//                    @Override
//                    public void onLoginError(int errorCode) {
//
//                    }
//                });
//                syncTime();
                camera.startRecord("", new ISdkCallback() {
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
                camera.stopRecord(new ISdkCallback() {
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

        }
    }


    //将手机时间同步到IPC
    private void syncTime() {
        camera.syncSystemTime(new ISdkCallback<DevSystemTime>() {
            @Override
            public void onSuccess(DevSystemTime result) {
                Log.e(TAG, "syncSystemTime onSuccess");
            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "syncSystemTime onError:" + errorCode);
            }

            @Override
            public void onLoginError(int errorCode) {
                Log.e(TAG, "syncSystemTime onLoginError:" + errorCode);
            }
        });
    }

    private void getDoorSensorInfo() {
        Global.es.execute(new Runnable() {
            @Override
            public void run() {
                SDKResponse response = CmdHelper.sendCGICommand(camera.getHandle(), "cmd=getDevInfo");
                Log.e(TAG, response.result + ", " + response.json);
            }
        });
    }

    @Override
    public void snapFinished(byte[] rgb) {

    }

    @Override
    public void firstFrameDone(Bitmap rgb) {

        Log.e(TAG, "firstFrameDone: " + (rgb == null ? "null" : rgb.toString()));
    }

    @Override
    public void openVideoSucc() {
        Log.e(TAG, "openVideoSucc: ");
    }

    @Override
    public void openVideoFail(int errorCode) {
        Log.e(TAG, "openVideoFail: " + errorCode);
    }

    @Override
    public void closeVideoSucc() {

    }

    @Override
    public void closeVideoFail(int errorCode) {

    }

    @Override
    public void netFlowSpeedRefresh(String speedValue) {
        Log.e(TAG, "netFlowSpeedRefresh: " + speedValue);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_CODE_RECORD_AUDIO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera.openTalk(new ISdkCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (null == talkThread) {
                            talkThread = new TalkThread(camera, true);
                            talkThread.startTalk();
                            talkThread.start();

                        }
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onLoginError(int errorCode) {

                    }
                });
            } else {
                Toast.makeText(this, "Permissions were not granted.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        if (null != camera) {
            camera.deleteObserver(this);
        }
    }

    /**
     * 主/子码流切换
     *
     * @param index
     */
    private void changeStream(final int index) {
        if (index == 0) {
            camera.setStreamType(StreamMode.STREAM_MAIN);
        } else {
            camera.setStreamType(StreamMode.STREAM_SUB);
        }
        videoview.closeVideo();
        videoview.openVideo(camera, this);
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
        //camera.destroy();
    }

}