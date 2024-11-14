package com.ivyiot.appsdk;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ivyiot.ipclibrary.common.Global;
import com.ivyiot.ipclibrary.model.DevAbility;
import com.ivyiot.ipclibrary.model.DevAudioDetect;
import com.ivyiot.ipclibrary.model.DevInfo;
import com.ivyiot.ipclibrary.model.DevSystemTime;
import com.ivyiot.ipclibrary.model.EDefinitionItem;
import com.ivyiot.ipclibrary.model.IvyCamera;
import com.ivyiot.ipclibrary.model.StreamMode;
import com.ivyiot.ipclibrary.sdk.CmdHelper;
import com.ivyiot.ipclibrary.sdk.ISdkCallback;
import com.ivyiot.ipclibrary.sdk.SDKResponse;
import com.ivyiot.ipclibrary.video.AudioThread;
import com.ivyiot.ipclibrary.video.IVideoListener;
import com.ivyiot.ipclibrary.video.TalkThread;
import com.ivyiot.ipclibrary.video.VideoSurfaceView;

import java.util.Observable;
import java.util.Observer;

public class ResumeLiveVideoActivity extends AppCompatActivity implements Observer, IVideoListener {
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
        setContentView(R.layout.resume_live_video_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        camera = (IvyCamera) IvyApplication.getInstance().getCache("ivyDevice");
        videoview = findViewById(R.id.videoview);
        videoview.openVideo(camera);

    }

    @Override
    protected void onResume() {

        super.onResume();
        // 订阅 ipc 的事件消息
        camera.addObserver(ResumeLiveVideoActivity.this);

        // 向 ipc 发送命令，获取或者设置ipc信息。
        camera.getDevInfo(new ISdkCallback<DevInfo>() {
            @Override
            public void onSuccess(DevInfo result) {
                Log.e(TAG, "getDevInfo onSuccess. uid= " + result.uid + " ,mac= " + result.mac);
                devInfo = result;
                Toast.makeText(ResumeLiveVideoActivity.this, "getDevInfo success. ipc name=" + result.devName, Toast.LENGTH_SHORT).show();
                videoview.setVisibility(View.VISIBLE);
                //打开视频，命令结果通过回调方法接收，见 IVideoListener
                videoview.openVideo(camera, ResumeLiveVideoActivity.this);
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
        }
    }

    /**
     * 声音侦测配置
     */
    private DevAudioDetect devAudioDetect;




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
        Log.e(TAG, "netFlowSpeedRefresh: "+ speedValue);
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
        if(null != camera){
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