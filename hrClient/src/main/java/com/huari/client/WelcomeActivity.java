package com.huari.client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.entity.Constant;
import com.huari.tools.SysApplication;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class WelcomeActivity extends Activity {
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                } else {
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        SysApplication.getInstance().addActivity(this);
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        Constant.IP = sharedPreferences.getString("ServerIP", "192.168.1.109");

        if (ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            start();
        }
    }

    private void start() {
        Handler handler = new Handler();
        handler.postDelayed(new Loading(), 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("welcome", Context.MODE_PRIVATE);
        if (preferences.getBoolean("islock", true)) {
            SysApplication.isFirst = true;
        }
    }

    class Loading extends Thread {
        public void run() {
            BusinessCoreNet.getInstanceNet();
            if (SysApplication.isFirst == true) {
                startActivity(new Intent(WelcomeActivity.this, LockActivity.class));
                finish();
            } else {
                startActivity(new Intent(WelcomeActivity.this, MajorActivity.class));
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            SysApplication.getInstance().exit();
        }
        return super.onKeyDown(keyCode, event);
    }

}
