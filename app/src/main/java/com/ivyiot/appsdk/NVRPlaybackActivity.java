package com.ivyiot.appsdk;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ivyiot.appsdk.adapter.PlaybackListAdapter;
import com.ivyiot.ipclibrary.model.IvyNVR;
import com.ivyiot.ipclibrary.model.PlaybackRecordInfo;
import com.ivyiot.ipclibrary.sdk.ISdkCallback;
import com.ivyiot.ipclibrary.video.INVRPBVideoListener;
import com.ivyiot.ipclibrary.video.PBVideoSurfaceViewNVR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NVRPlaybackActivity extends AppCompatActivity implements Observer,View.OnClickListener, INVRPBVideoListener {
    private final String TAG = "PlaybackActivity";
    private PBVideoSurfaceViewNVR pbvideoview;
    private IvyNVR nvr;

    /** 回放列表 */
    private ArrayList<String> recordArr;
    /** 回放列表 */
    private List<PlaybackRecordInfo> recordIvyArr;

    private TextView tv_playback_buffer;

    private ListView lv_sd_playback_list;
    private int currentChannel = 3 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nvr_play_back_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        nvr = (IvyNVR) IvyApplication.getInstance().getCache("ivyDevice");
        pbvideoview = findViewById(R.id.pbvideoview);

        findViewById(R.id.btn_pblist).setOnClickListener(this);
        findViewById(R.id.btn_pbvideo).setOnClickListener(this);
        findViewById(R.id.btn_pb_pause).setOnClickListener(this);
        findViewById(R.id.btn_pb_resume).setOnClickListener(this);
        findViewById(R.id.btn_pb_seek).setOnClickListener(this);
        findViewById(R.id.btn_pb_audio_open).setOnClickListener(this);
        findViewById(R.id.btn_pb_audio_close).setOnClickListener(this);
        findViewById(R.id.btn_pb_video_download).setOnClickListener(this);
        findViewById(R.id.btn_pb_video_download_cancel).setOnClickListener(this);
        lv_sd_playback_list = findViewById(R.id.lv_sd_playback_list);

        tv_playback_buffer = findViewById(R.id.tv_playback_buffer);
        //注意：Calendar类的月份从0开始。
        Calendar cal = Calendar.getInstance();
        //2019.9.23 00:00:00
        cal.set(2022, 3, 16, 0, 0, 0);
        int todayStart = (int) (cal.getTimeInMillis() / 1000);
        //2019.9.23 23:59:59
        cal.set(2022, 3, 16, 23, 59, 59);
        int todayEnd = (int) (cal.getTimeInMillis() / 1000);
        nvr.getPBList(511, todayStart, todayEnd, currentChannel, new ISdkCallback<ArrayList<PlaybackRecordInfo>>() {

            @Override
            public void onSuccess(ArrayList<PlaybackRecordInfo> result) {
                recordIvyArr = result;
                Log.e(TAG, "onSuccess: " + recordIvyArr.size());
                Toast.makeText(NVRPlaybackActivity.this, "get play back list success. size=" + recordIvyArr.size(), Toast.LENGTH_SHORT).show();
                //pbvideoview.openPBVideo(camera, recordIvyArr.get(playLocation), PlaybackActivity.this);
//                playLocation += 1;
                if(null != lv_sd_playback_list){
                    lv_sd_playback_list.setAdapter(new PlaybackListAdapter(NVRPlaybackActivity.this, recordIvyArr));
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "onError: " + errorCode);
            }

            @Override
            public void onLoginError(int errorCode) {
            }
        });
        lv_sd_playback_list.setOnItemClickListener((parent, view, position, id) -> {
            pbvideoview.closeDiskPBVideo(currentChannel);
            pbvideoview.openDiskPBVideo(nvr, currentChannel, recordIvyArr.get(position), NVRPlaybackActivity.this);

        });

    }


    int seekPosition = 60;
    int playLocation = 1;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_live:
                pbvideoview.closeDiskPBVideo(currentChannel);
                pbvideoview.setVisibility(View.GONE);
                break;
            case R.id.btn_pblist:
                //注意：Calendar类的月份从0开始。
                Calendar cal = Calendar.getInstance();
                //2019.9.23 00:00:00
                cal.set(2021, 5, 5, 0, 0, 0);
                int todayStart = (int) (cal.getTimeInMillis() / 1000);
                //2019.9.23 23:59:59
                cal.set(2021, 5, 5, 23, 59, 59);
                int todayEnd = (int) (cal.getTimeInMillis() / 1000);
//                camera.getPBList(todayStart, todayEnd, 2,  new ISdkCallback<ArrayList<PlaybackRecordInfo>>() {
//                    @Override
//                    public void onSuccess(ArrayList<PlaybackRecordInfo> result) {
//                        recordIvyArr = result;
//                        Log.e(TAG, "onSuccess: " + recordIvyArr.size());
//                        Toast.makeText(NVRPlaybackActivity.this, "get play back list success. size=" + recordIvyArr.size(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(int errorCode) {
//                        Log.e(TAG, "onError: " + errorCode);
//                    }
//
//                    @Override
//                    public void onLoginError(int errorCode) {
//                    }
//                });
                break;
            case R.id.btn_pbvideo:
                pbvideoview.setVisibility(View.VISIBLE);
                if (recordIvyArr != null && recordIvyArr.size() > 0) {
                    seekPosition = 2;
                    pbvideoview.closeDiskPBVideo(currentChannel);
                    pbvideoview.openDiskPBVideo(nvr, currentChannel, recordIvyArr.get(playLocation), NVRPlaybackActivity.this);
                    playLocation += 1;
                }
                break;
            case R.id.btn_pb_pause:
                pbvideoview.pausePBVideo();
                break;
            case R.id.btn_pb_resume:
                pbvideoview.resumePBVideo();
                break;
            case R.id.btn_pb_seek:
                seekPosition += 20;
                pbvideoview.seekPBVideo(seekPosition, currentChannel);
                break;
            case R.id.btn_pb_audio_open:
                pbvideoview.openPBAudio();
                break;
            case R.id.btn_pb_audio_close:
                pbvideoview.closePBAudio();
                break;
            case R.id.btn_pb_video_download:
                pbvideoview.closeDiskPBVideo(currentChannel);
                String path = Environment.getExternalStorageDirectory() + "/123456.mp4";
                File imgFile = new File(path);
                if(!imgFile.exists()){
                    try {
                        imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_pb_video_download_cancel:
                break;
        }
    }


    @Override
    public void openPBVideoSucc() {
        Log.e(TAG, "openPBVideoSucc: ");
    }

    @Override
    public void openPBVideoFail(int errorCode) {
        Log.e(TAG, "openPBVideoFail: " + errorCode);
    }

    @Override
    public void closePBVideoSucc() {

    }

    @Override
    public void closePBVideoFail(int errorCode) {

    }

    @Override
    public void pausePBVideoSucc() {

    }

    @Override
    public void pausePBVideoFail(int errorCode) {

    }

    @Override
    public void resumePBVideoSucc() {

    }

    @Override
    public void resumePBVideoFail(int errorCode) {

    }

    @Override
    public void seekPBVideoSucc() {

    }

    @Override
    public void seekPBVideoFail(int errorCode) {

    }

    @Override
    public void onPlayLoadProgress(int progress) {
        Log.e(TAG, "onPlayLoadProgress: " + progress);
        tv_playback_buffer.setText("当前缓冲进度："+progress +" %");

    }

    @Override
    public void onPlayStart() {
        Log.e(TAG, "onPlayStart: ");
    }


    @Override
    public void onPlaying(int progress) {//正在播放
        Log.d(TAG, "onPlaying:  " + progress );
        tv_playback_buffer.setText("当前播放进度："+ progress);
    }

    @Override
    public void onPlayComplete() {
        Log.d(TAG, " seek--:  onPlayComplete"  );

    }

    @Override
    public void onPlayFail() {
        Log.e(TAG, "onPlayFail: ");
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
    protected void onStop() {
        pbvideoview.closeDiskPBVideo(currentChannel);
        super.onStop();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            Message msg = (Message) arg;
            Log.e(TAG, "update: " + msg.what + ";data=" + msg.obj);
        }
    }
}
