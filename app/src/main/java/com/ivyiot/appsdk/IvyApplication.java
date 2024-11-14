package com.ivyiot.appsdk;

import android.Manifest;
import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.ivyio.sdk.LogLevel;
import com.ivyiot.ipclibrary.sdk.SDKManager;

import java.util.HashMap;
import java.util.Map;

public class IvyApplication extends Application {
    private String TAG = "IvyApplication";

    /**
     * 全局暂存
     */
    private Map<String, Object> cache;
    /**
     * application 实例
     */
    private static IvyApplication instance;

    public static IvyApplication getInstance() {
        if (null == instance) {
            instance = new IvyApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        cache = new HashMap<>();
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
        SDKManager.getInstance().init(this);

        String sdkVersion = SDKManager.getInstance().getSdkVersion();
        Log.e(TAG, "sdk version:" + sdkVersion);
    }

    /**
     * 全局变量存储
     *
     * @param key   key
     * @param value value
     */
    public void putCache(String key, Object value) {
        cache.put(key, value);
    }

    /**
     * 取出对象，并从cache中remove
     *
     * @param key 对象对应的key
     * @return Object
     */
    public Object getCache(String key) {
        return cache.get(key);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        for(String key : cache.keySet()){
            cache.remove(key);
        }
    }
}
