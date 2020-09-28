package com.ivyiot.appsdk;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ivyiot.ipclibrary.model.IvyCamera;
import com.ivyiot.ipclibrary.model.PlaybackRecordInfo;
import com.ivyiot.ipclibrary.sdk.ISdkCallback;
import com.ivyiot.ipclibrary.video.IPBVideoListener;
import com.ivyiot.ipclibrary.video.PBVideoSurfaceView;

import java.util.ArrayList;
import java.util.Calendar;

public class PlaybackActivity extends AppCompatActivity implements View.OnClickListener, IPBVideoListener {
    private final String TAG = "MainActivity";
    private PBVideoSurfaceView pbvideoview;
    private IvyCamera camera;

    /** 回放列表 */
    private ArrayList<String> recordArr;
    /** 回放列表 */
    private ArrayList<PlaybackRecordInfo> recordIvyArr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_back_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        camera = (IvyCamera) IvyApplication.getInstance().getCache("ivyDevice");
        pbvideoview = findViewById(R.id.pbvideoview);

        findViewById(R.id.btn_pblist).setOnClickListener(this);
        findViewById(R.id.btn_pbvideo).setOnClickListener(this);
        findViewById(R.id.btn_pb_pause).setOnClickListener(this);
        findViewById(R.id.btn_pb_resume).setOnClickListener(this);
        findViewById(R.id.btn_pb_seek).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_live:
                pbvideoview.closePBVideo();
                pbvideoview.setVisibility(View.GONE);
                break;
            case R.id.btn_pblist:
                //注意：Calendar类的月份从0开始。
                Calendar cal = Calendar.getInstance();
                //2019.9.23 00:00:00
                cal.set(2020, 8, 25, 0, 0, 0);
                int todayStart = (int) (cal.getTimeInMillis() / 1000);
                //2019.9.23 23:59:59
                cal.set(2020, 8, 25, 23, 59, 59);
                int todayEnd = (int) (cal.getTimeInMillis() / 1000);
                camera.getPBList(todayStart, todayEnd, 2, new ISdkCallback<ArrayList<PlaybackRecordInfo>>() {
                    @Override
                    public void onSuccess(ArrayList<PlaybackRecordInfo> result) {
                        recordIvyArr = result;
                        Log.e(TAG, "onSuccess: " + result.size());
                        Toast.makeText(PlaybackActivity.this, "get play back list success. size=" + result.size(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e(TAG, "onError: " + errorCode);
                    }

                    @Override
                    public void onLoginError(int errorCode) {
                    }
                });
                break;
            case R.id.btn_pbvideo:
                pbvideoview.setVisibility(View.VISIBLE);
                if (recordIvyArr != null && recordIvyArr.size() > 0) {
                    pbvideoview.openPBVideo(camera, recordIvyArr.get(0), PlaybackActivity.this);
                }
                break;
            case R.id.btn_pb_pause:
                pbvideoview.pausePBVideo();
                break;
            case R.id.btn_pb_resume:
                pbvideoview.resumePBVideo();
                break;
            case R.id.btn_pb_seek:
                pbvideoview.seekPBVideo(0);
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
    public void onPlayStart() {
        Log.e(TAG, "onPlayStart: ");
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

}
