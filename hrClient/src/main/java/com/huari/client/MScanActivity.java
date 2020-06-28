package com.huari.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import struct.JavaStruct;
import struct.StructException;

import com.huari.Base.MscanBase;
import com.huari.NetMonitor.WindowHelper;
import com.huari.commandstruct.PPFXRequest;
import com.huari.commandstruct.PinPuParameter;
import com.huari.commandstruct.StopTaskFrame;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Parameter;
import com.huari.dataentry.Station;
import com.huari.tools.MyTools;
import com.huari.tools.Parse;
import com.huari.tools.SysApplication;

import androidx.appcompat.app.ActionBar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//离散扫描功能窗体
public class MScanActivity extends MscanBase {
    public static int PARAMETERREFRESH = 0x7;
    public static int PARSEDATA = 0x9;
    public static boolean saveFlag = false;
    com.huari.ui.MscanShowView mscanShowView;
    boolean pause;
    String logicId;
    private MyDevice device;
    private MenuItem menuItemstart = null;
    @Override
    protected void onPause() {
        super.onPause();
        SysApplication.mScanSettingSave.setPrepare(false);
        WindowHelper.instance.stopWindowService(this);
        if(menuItemstart!=null){
            menuItemstart.setTitle("开始测量");
        }
    }
    String stationname;
    String stationKey;
    String devicename;
    ActionBar actionbar;
    TextView stationtextview, devicetextview;
    ArrayList<Parameter> ap;
    Parameter centerFreqParameter;
    Parameter demodmodeParameter;
    Parameter demodBWParameter;
    String demodemod = "FM";
    float centerFreq = 0f, demodBW = 0f;
    String txname;        // 天线名字
    MenuItem startitem;    // 开始测量/停止测量
    Socket s;            // 用来接收数据
    OutputStream outs = null;
    InputStream ins = null;
    MyThread mythread;
    boolean runMythread = true;
    IniThread inithread;
    private Station stationF;
    private float[] freqlist = new float[20];

    class IniThread extends Thread {
        public void run() {
            try {
                s = new Socket(GlobalData.mainIP, GlobalData.port2);
                ins = s.getInputStream();
                outs = s.getOutputStream();
                handler.sendEmptyMessageDelayed(1,100);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendClose() {
        Thread thread = new Thread(() -> {
            StopTaskFrame stf = new StopTaskFrame();
            stf.length = 2;
            stf.functionNum = 23;  //请求离散扫描
            stf.tail = 22;

            try {
                byte[] stop = JavaStruct.pack(stf);
                outs.write(stop);
                outs.flush();
                Log.i("发送", "停止命令");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    class MyThread extends Thread {
        private void sendStartCmd() {

            Thread thread = new Thread(() -> {
                try {
                    byte[] bbb = iRequestInfo();
                    //System.out.println("客户端发送的数据长度是" + bbb.length);
                    outs.write(bbb);
                    outs.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        private void sendEndCmd() {
            Thread thread = new Thread(() -> {
                StopTaskFrame stf = new StopTaskFrame();
                stf.length = 2;
                stf.functionNum = 23;
                try {
                    byte[] stop = JavaStruct.pack(stf);
                    outs.write(stop);
                    outs.flush();
                    Log.i("发送", "停止命令");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        public void run() {
            try {
                int available = 0;
                byte[] info = null;
                long time = 0;
                int flag = 0;//为0则标志第一次进入
                while (available == 0 && runMythread) {
                    available = ins.available();
                    if (available > 0) {
                        info = new byte[available];
                        ins.read(info);
                        Log.d("xiaoxiao", String.valueOf(info.length));
                        try {
                            Parse.newParseMScan(info);

                        } catch (Exception e) {
                            Log.d("xiao", "解析离散扫描数据发生异常");
                        }
                        available = 0;
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mscan);
        if (outs != null && ins != null) {
            SysApplication.mScanSettingSave.setPrepare(true);
        }
        actionbar = getSupportActionBar();
        SysApplication.getInstance().addActivity(this);
        Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);
        pause = true;
        mscanShowView = findViewById(R.id.mymscan);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        if (GlobalData.show_horiz2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        stationname = bundle.getString("stationname");
        devicename = bundle.getString("devicename");
        stationKey = bundle.getString("stationKey");
        logicId = bundle.getString("lid");

        LinearLayout titlebar = (LinearLayout) getLayoutInflater().inflate(
                R.layout.actionbarview, null);

        ArrayList<MyDevice> am = null;
        try {
            stationF = GlobalData.stationHashMap.get(stationKey);
            am = stationF.showdevicelist;
        } catch (Exception e) {
            Toast.makeText(MScanActivity.this, "空的",
                    Toast.LENGTH_SHORT).show();
        }

        HashMap<String, LogicParameter> hsl = null;
        for (MyDevice md : am) {
            if (md.name.equals(devicename)) {
                hsl = md.logic;
                device = md;
            }
        }
        LogicParameter currentLP = hsl.get(logicId);// 获取离散扫描参数相关的数据，以便画出初始界面
        ap = currentLP.parameterlist;

        for (Parameter p : ap) {
            if (p.name.trim().equals("MemCenterFreq")) {
                centerFreqParameter = p;
                centerFreq = Float.parseFloat(p.defaultValue.trim());
            } else if (p.name.trim().equals("Memdemodmode")) {
                demodmodeParameter = p;
                demodemod = p.defaultValue.trim();
            } else if (p.name.trim().equals("MemDemodBW")) {
                demodBWParameter = p;
                demodBW = Float.parseFloat(p.defaultValue.trim()) / 1000f;
            }
        }


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == PARAMETERREFRESH) {
                    for (Parameter p : ap) {
                        if (p.name.equals("MemDemodBW")) {
                            demodBW = Float.parseFloat(p.defaultValue);
                        } else if (p.name.equals("Memdemodmode")) {
                            demodemod = p.defaultValue.trim();
                        } else if (p.name.equals("MemCenterFreq")) {
                            centerFreq = Float.parseFloat(p.defaultValue);
                        } else if (p.name.equals("AntennaSelect")) {
                            txname = p.defaultValue;
                        }
                    }

                } else if (msg.what == MSCANNINGDATA)// MSCAN模式，
                {
                    if (pause == false) {
                        try {
                            int a = GlobalData.pinduanQueue.size();
                            float[] t = GlobalData.mscandata;
                            mscanShowView.setM(t, freqlist);
                            mscanShowView.postInvalidate();
                            if (t == null) {
                                System.out.println("获取到的short[]是空的");
                            }

                        } catch (Exception e) {

                        }

                    }
                } else if (msg.what == 1) {
                    if(menuItemstart!=null){
                        menuItemstart.setEnabled(true);
                    }
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg0 == 0 && arg1 == 2) {
            Bundle bundle = arg2.getExtras();
            stationtextview.setText(bundle.getString("stname"));
            devicetextview.setText(bundle.getString("dename"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mscanning, menu);
        MenuItem menuItem = menu.findItem(R.id.caputure);
        menuItemstart = menu.findItem(R.id.pinduanstart);
        if (SysApplication.mScanSettingSave.isH() == true) {
            menuItem.setTitle("手机纵向");
        }
        if (SysApplication.mScanSettingSave.isPrepare() == false) {
            menuItemstart.setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        inithread = new IniThread();
        inithread.start();
        if (outs != null && ins != null) {
            SysApplication.mScanSettingSave.setPrepare(true);
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final MenuItem i = item;
        if (id == R.id.pinduanstart) {
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                startitem = item;
                if (item.getTitle().equals("开始测量")) {
                    int num = 0;
                    for (Parameter p : GlobalData.mscan_parameterlist) {
                        if (p.dispname.equals("中心频率")) {
                            num++;
                        }
                    }
                    if (num == 0 || num == 1) {
                        Toast.makeText(this, "只有一个频点，不能开始扫描，请添加频点", Toast.LENGTH_SHORT).show();
                    } else {
                        item.setTitle("停止测量");

                        pause = false;
                        if (mythread == null) {
                            mythread = new MyThread();
                        }
                        try {
                            mythread.sendStartCmd();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            mythread.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (item.getTitle().equals("停止测量")) {
                    item.setTitle("开始测量");
                    pause = true;
                    mythread.sendEndCmd();
                    GlobalData.clearPinDuan();
                }
                lastClickTime = curClickTime;
            }
        } else if (id == R.id.mscanset) {//设置
            if (pause) {
                Intent intent = new Intent(MScanActivity.this,
                        MscanSetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sname", stationname);
                bundle.putString("dname", devicename);
                bundle.putString("stakey", stationKey);
                bundle.putString("lids", logicId);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                AlertDialog.Builder ab = new AlertDialog.Builder(
                        MScanActivity.this);
                ab.setTitle("警告！");
                ab.setMessage("功能运行期间不可更改设置，确定要停止功能进行设置吗？");
                ab.setPositiveButton("确定",
                        (dialog, which) -> {
                            pause = true;
                            mythread.sendEndCmd();
                            GlobalData.clearPinDuan();
                            startitem.setTitle("开始测量");
                            Intent intent = new Intent(
                                    MScanActivity.this,
                                    MscanSetActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("sname", stationname);
                            bundle.putString("dname", devicename);
                            bundle.putString("stakey", stationKey);
                            bundle.putString("lids", logicId);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        });
                ab.setNegativeButton("取消", null);
                ab.create();
                ab.show();
            }
        } else if (id == R.id.caputure) {
            if (item.getTitle().equals("手机横向")) {
                SysApplication.mScanSettingSave.setH(true);
                item.setTitle("手机纵向");
                GlobalData.show_horiz2 = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                //pinduan.refreshWave();
            } else if (item.getTitle().equals("手机纵向")) {
                item.setTitle("手机横向");
                SysApplication.mScanSettingSave.setH(false);
                GlobalData.show_horiz2 = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                //pinduan.refreshWave();
            }
        }
        return true;
    }

    private byte[] iRequestInfo() {
        byte[] request = null;
        boolean havetianxian = false;
        PPFXRequest pr = new PPFXRequest();
        for (Parameter para : ap) {
            if ((para.name.contains("Anten"))) {
                havetianxian = true;
                break;
            }
        }

        // 帧体长度暂时跳过
        pr.functionNum = 23;
        pr.stationid = MyTools.toCountString(stationKey, 76).getBytes();
        pr.logicid = MyTools.toCountString(logicId, 76).getBytes();
        pr.devicename = MyTools.toCountString(devicename, 36).getBytes();
        pr.pinduancount = 0;

        pr.logictype = MyTools.toCountString("MSCAN", 16).getBytes();

        ArrayList<PinPuParameter> parray = new ArrayList<>();

        int z = 0;
        int fn = 0;

        pr.tianxianname = MyTools.toCountString("NULL", 36).getBytes();
        if (GlobalData.mscan_parameterlist.size() == 0)
            for (Parameter para : ap) {
                PinPuParameter pin = new PinPuParameter();
                pin.name = MyTools.toCountString(para.name, 36).getBytes();
                pin.value = MyTools.toCountString(para.defaultValue, 36).getBytes();
                if (!(para.name.contains("AntennaSelect"))) {
                    if (para.name.contains("MemCenter")) {
                        freqlist[fn] = Float.parseFloat(para.defaultValue);
                        fn++;
                    }
                } else {
                    pr.tianxianname = MyTools.toCountString(para.defaultValue, 36)
                            .getBytes();
                }
                if (para.name.contains("Mem")) {
                    parray.add(pin);
                    z++;
                }
            }
        else {
            for (Parameter para : GlobalData.mscan_parameterlist) {
                PinPuParameter pin = new PinPuParameter();
                pin.name = MyTools.toCountString(para.name, 36).getBytes();
                pin.value = MyTools.toCountString(para.defaultValue, 36).getBytes();
                if (!(para.name.contains("AntennaSelect"))) {
                    if (para.name.contains("MemCenter")) {
                        freqlist[fn] = Float.parseFloat(para.defaultValue);
                        fn++;
                    }
                } else {
                    pr.tianxianname = MyTools.toCountString(para.defaultValue, 36)
                            .getBytes();
                }
                if (para.name.contains("Mem")) {
                    parray.add(pin);
                    z++;
                }
            }
        }

        PinPuParameter[] pinarry = null;

        pinarry = new PinPuParameter[z];

        for (int m = 0; m < z; m++) {
            pinarry[m] = parray.get(m);
        }
//        if (havetianxian) {
//            pr.parameterslength = 72 * (z - 1);
//        } else {
//            pr.parameterslength = 72 * z;
//        }
        pr.parameterslength = 72 * z;
        pr.length = pr.parameterslength + 247;

        pr.p = pinarry;

        try {
            request = JavaStruct.pack(pr);
        } catch (StructException e) {
            e.printStackTrace();
        }
        return request;
    }

    private void willExit() {
        try {
            sendClose();
            Thread.sleep(50);
            s.close();
            if (mythread != null) {
                runMythread = false;
                mythread.join();
                mythread = null;
            }
            if (inithread != null) {
                inithread.join();
                inithread = null;
            }
            GlobalData.clearPinDuan();

        } catch (Exception e) {
            System.out.println("异常");
        }
    }

    @Override
    protected void onDestroy() {
        willExit();
        super.onDestroy();
    }
}
