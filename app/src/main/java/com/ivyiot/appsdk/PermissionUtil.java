package com.ivyiot.appsdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * Android M(6.0) 运行时权限检查及申请
 */
public class PermissionUtil {
    /**
     * 检查权限是否已授权，如果没有授权，需要请求权限
     *
     * @param activity      activity
     * @param permissions   权限组
     * @param permissonCode 在请求授权的回调方法中标识申请的是哪个权限
     * @return true 已授权，直接调用后续业务逻辑方法；false 未授权，需要在授权回调方法中调用业务逻辑方法
     */
    public static boolean chkPermission(Activity activity, String[] permissions, int permissonCode) {

        for (String per : permissions) {
//            if (ActivityCompat.checkSelfPermission(activity, per) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.checkSelfPermission(activity, per) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(activity, permissions, permissonCode);
                return false;
            }
        }

        return true;
    }

    /**
     * 检查权限是否已授权，如果没有授权，需要请求权限
     *
     * @param activity      activity
     * @param permissions   权限组
     * @param permissonCode 在请求授权的回调方法中标识申请的是哪个权限
     * @param isLoadPage 是否在启动页
     * @return true 已授权，直接调用后续业务逻辑方法；false 未授权，需要在授权回调方法中调用业务逻辑方法
     */
    public static boolean chkPermission(Activity activity, String[] permissions, int permissonCode, boolean isLoadPage) {

        for (String per : permissions) {
//            if (ActivityCompat.checkSelfPermission(activity, per) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.checkSelfPermission(activity, per) != PackageManager.PERMISSION_GRANTED) {
                if(isLoadPage){
                    requestPermission(activity, permissions, permissonCode, isLoadPage);
                }else {
                    requestPermission(activity, permissions, permissonCode);
                }
                return false;
            }
        }

        return true;
    }

    private static void requestPermission(final Activity activity, final String[] permissions, final int permissonCode) {
        boolean shouldShow = false;
        for (String per : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, per)) {
                shouldShow = true;
                break;
            }
        }

        if (shouldShow) {
            ActivityCompat.requestPermissions(activity, permissions, permissonCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, permissonCode);
        }
    }

    private static void requestPermission(final Activity activity, final String[] permissions, final int permissonCode, boolean isLoadPage) {
        boolean shouldShow = false;
        for (String per : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, per)) {
                shouldShow = true;
                break;
            }
        }

        if (shouldShow) {
            //ActivityCompat.requestPermissions(activity, permissions, permissonCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, permissonCode);
        }
    }

    /**
     * 检查权限是否已授权，如果没有授权，需要请求权限
     *
     * @param activity      activity
     * @param permissions   权限组
     * @return true 已授权，直接调用后续业务逻辑方法；false 未授权，需要在授权回调方法中调用业务逻辑方法
     */
    public static boolean chkPermission(Context activity, String[] permissions) {
        for (String per : permissions) {
//            if (ActivityCompat.checkSelfPermission(activity, per) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.checkSelfPermission(activity, per) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    /**
     * 检查是否已拒绝权限
     *
     * @param activity      activity
     * @param permissions   权限组
     * @return true 已授权，直接调用后续业务逻辑方法；false 未授权，需要在授权回调方法中调用业务逻辑方法
     */
    public static boolean checkPermissionDeny(Activity activity, String permissions) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions);
    }



    public static boolean chkPermissionFragemt(Activity activity, String[] permissions, int permissonCode, Fragment fragment) {

        for (String per : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, per) != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions( permissions, permissonCode);
                return false;
            }
        }

        return true;
    }


}
