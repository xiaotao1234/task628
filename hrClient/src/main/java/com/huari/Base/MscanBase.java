package com.huari.Base;

import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MscanBase extends AppCompatActivity {//抽出一个基类是为了解析功能的复用
    public static String ScanType="MSCAN";
    public static Handler handler;
    public static int MSCANNINGDATA = 0x2;
    public static int MSCANNINGDATANO = 0x3;
    public static int MSCANNINGDATAFSCAN = 0x4;
    public static int MSCANNINGDATAFSCANNO = 0x5;
}
