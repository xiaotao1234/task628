package com.huari.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.provider.Settings;

import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MScanSettingSave;
import com.huari.dataentry.PinDuanSettingSave;
import com.squareup.leakcanary.LeakCanary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import cn.bmob.push.BmobPush;
//import cn.bmob.v3.Bmob;
//import cn.bmob.v3.BmobInstallation;
//import cn.bmob.v3.BmobInstallationManager;
//import cn.bmob.v3.InstallationListener;
//import cn.bmob.v3.exception.BmobException;

public class SysApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static SysApplication instance;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private List<Activity> mList = new LinkedList<>();
    public static FileOsImpl fileOs;
    public static TimeTools timeTools;
    public static PermissionManager permissionManager;
    public static ByteFileIoUtils byteFileIoUtils;
    public static PinDuanSettingSave settingSave;
    public static MScanSettingSave mScanSettingSave;
    public static Map<String,LogicParameter> currentLogicMap;
    public static LogicParameter  currentLogic;
    public static boolean SocketFlag = false;
    public static String id;
    public static boolean isFirst;

    @Override
    public void onCreate() {
        super.onCreate();
//        context = getApplicationContext();
//        TinyDancer.create()
//                .show(context);
//        //alternatively
//        TinyDancer.create()
//                .redFlagPercentage(.1f) // set red indicator for 10%....different from default
//                .startingXPosition(200)
//                .startingYPosition(600)
//                .show(context);
//        //you can add a callback to get frame times and the calculated
//        //number of dropped frames within that window
//        TinyDancer.create()
//                .addFrameDataCallback((previousFrameNS, currentFrameNS, droppedFrames) -> { }).show(context);
        LeakCanary.install(this);
        fileOs = FileOsImpl.getInstance();
        fileOs.getOsDicteoryPath(getApplicationContext());
        currentLogicMap = new HashMap<>();
        currentLogic = new LogicParameter();
        timeTools = TimeTools.getInstance();
        permissionManager = PermissionManager.getInastance();
        byteFileIoUtils = ByteFileIoUtils.getInstance();
        settingSave = new PinDuanSettingSave();
        mScanSettingSave = new MScanSettingSave();
        id = Settings.System.getString(getApplicationContext().getContentResolver(), Settings.System.ANDROID_ID);
    }

    public synchronized static SysApplication getInstance() {
        if (null == instance) {
            instance = new SysApplication();
        }
        return instance;
    }

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
            mList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
    public static Context getContext(){
        return context;
    }
}