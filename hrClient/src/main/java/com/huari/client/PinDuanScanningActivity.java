package com.huari.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import struct.JavaStruct;
import struct.StructException;

import com.huari.Base.PinDuanBase;
import com.huari.NetMonitor.WindowController;
import com.huari.NetMonitor.WindowHelper;
import com.huari.commandstruct.PPFXRequest;
import com.huari.commandstruct.PinPuParameter;
import com.huari.commandstruct.StopTaskFrame;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Parameter;
import com.huari.dataentry.Station;
import com.huari.dataentry.Type;
import com.huari.tools.ByteFileIoUtils;
import com.huari.tools.MyTools;

import com.huari.tools.Parse;
import com.huari.tools.RealTimeSaveAndGetStore;
import com.huari.tools.SysApplication;

import androidx.appcompat.app.ActionBar;

import android.app.AlertDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

//频段扫描功能窗体
public class PinDuanScanningActivity extends PinDuanBase {

    public static int YINZIDATA;
    public static int PARAMETERREFRESH = 0x7;
    public static int PARSEDATA = 0x9;

    public static Queue<byte[]> queue;
    public static boolean saveFlag = false;
    public static int flag;
    com.huari.ui.PinDuan pinduan;
    boolean pause, trigged;
    boolean showMax, showMin, showAvg;
    boolean trig_level = true;
    int trigger_pos;


    String logicId, logicPPFXId = "", logicDDFId = "";
    private MyDevice device;
    private MenuItem pauseitem;
    boolean allowOverlyDraw = true;
    private MenuItem menuItemMax;
    private MenuItem menuItemMin;
    private MenuItem menuItemAvg;
    public static long time;

    @Override
    protected void onStart() {
        super.onStart();
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
        WindowHelper.instance.setForeground(false);
        WindowHelper.instance.stopWindowService(this);
    }

    String stationname;
    String stationKey;
    String devicename;
    ActionBar actionbar;
    TextView normaltextview, advancedtextview, titlebarname, stationtextview,
            devicetextview;
    LinearLayout l;
    ArrayList<Parameter> ap;
    Parameter startFreqParameter;
    Parameter endFreqParameter;
    Parameter stepFreqParameter;
    float startFreq = 0f, endFreq = 0, pStepFreq = 0f, centerFreq = 0f,
            daikuan = 0f;
    String txname;        // 天线名字

    MenuItem startitem;    // 开始测量/停止测量

    Socket socket;            // 用来接收数据
    OutputStream outs = null;
    InputStream ins;
    MyThread mythread;
    IniThread inithread;

    private static String fd = "fd";    // 是幅度模式还是加上因子的场强模式cq
    private String fileName;
    float lan, lon;
    private Station stationF;

    class IniThread extends Thread {
        public void run() {
            try {
                Log.d("avaliable", "尝试开始连接");
                socket = new Socket(GlobalData.mainIP, GlobalData.port2);
                socket.setKeepAlive(true);
                socket.setSoTimeout(100);

                ins = socket.getInputStream();
                outs = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MyThread extends Thread {

        boolean end = false;

        private void setEnd(boolean b) {
            end = b;
        }

        private void sendStartCmd() {
//            try {
//                int length = ins.available();
//                byte[] bytes = new byte[length];
//                ins.read(bytes);
//                Log.d("inssize", String.valueOf(ins.available()));
//            } catch (IOException e) {
//                Log.d("inssize", "error");
//                e.printStackTrace();
//            }
            Thread thread = new Thread(() -> {
                end = false;
                GlobalData.clearPinDuan();
                pinduan.clear();
                pinduan.setParameters(
                        Float.parseFloat(startFreqParameter.defaultValue.trim()),
                        Float.parseFloat(endFreqParameter.defaultValue.trim()),
                        Float.parseFloat(stepFreqParameter.defaultValue));
                try {
                    byte[] bbb = iRequestInfo();
                    System.out.println("客户端发送的数据长度是" + bbb.length);
                    if (outs != null) {
                        outs.write(bbb);
                        outs.flush();
                    }
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
                stf.functionNum = 17;
                try {
                    byte[] stop = JavaStruct.pack(stf);
                    if (outs != null) {
                        outs.write(stop);
                        outs.flush();
                        Log.i("发送", "停止命令");
                    }
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
                time = 0;
                flag = 0;//为0则标志第一次进入
                while (available == 0 && !end) {
                    if (ins != null) {
                        available = ins.available();
                        if (available > 100000) {
                            int length = ins.available();
                            byte[] bytes = new byte[length];
                            ins.read(bytes);
                        }
                        available = ins.available();
                        if (available > 0) {
                            Log.d("avaliablepd", String.valueOf(available));
                            info = new byte[available];
                            ins.read(info);
                            try {
                                Parse.newParsePDScan(info);
                                if (saveFlag == true) {
                                    if (flag == 0) {
                                        time = 0;
                                        savePrepare(System.currentTimeMillis());
                                        flag++;
                                    }
                                    time = RealTimeSaveAndGetStore.SaveAtTime(available, info, time, 3);//给数据加一个时间的包头后递交到缓存队列中
                                }
                            } catch (Exception e) {
                                Log.d("xiao", "解析频段扫描数据发生异常");
                            }
                            available = 0;
                            try {
                                sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePrepare(long starttime) {
        ByteFileIoUtils.runFlag = true;
        queue = new LinkedBlockingDeque<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fileName = "PD|" + df.format(new Date()).replaceAll(" ", "|");
//                    + "||" + stationname + "|" + devicename + "|" + stationKey + "|" + lan + "|" + lon;
//                    +"|"+logicId;    //会导致名字长度超出限制
        SharedPreferences sharedPreferences = getSharedPreferences("myclient", MODE_PRIVATE);
        SharedPreferences.Editor shareEditor = sharedPreferences.edit();
        shareEditor.putString(fileName, stationname + "|" + devicename + "||" + stationKey + "|||" + lan + "||||" + lon + "|||||" + logicId);
        shareEditor.commit();  //以文件名作为key来将台站信息存入shareReferences
        Log.d("xiaoxiao", String.valueOf(fileName.length()));
        SysApplication.byteFileIoUtils.writeBytesToFile(fileName, 3, starttime); //开始保存数据前的初始化
        RealTimeSaveAndGetStore.serializeFlyPig(stationF, devicename, device, logicId);//在消费者线程开启后，开始Statio的序列化并放入队列缓冲区中等待消费者线程遍历之
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinduansanmiao);
        actionbar = getSupportActionBar();

        inithread = new IniThread();
        inithread.start();

        SysApplication.getInstance().addActivity(this);
        Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);
        pause = true;
        pinduan = findViewById(R.id.mypin);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        if (GlobalData.show_horiz) {
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
        stationtextview = (TextView) titlebar.findViewById(R.id.name1);
        devicetextview = (TextView) titlebar.findViewById(R.id.name2);

        stationtextview.setText(stationname);
        devicetextview.setText(devicename);
        ArrayList<MyDevice> am = null;
        try {
            stationF = GlobalData.stationHashMap.get(stationKey);
            am = stationF.showdevicelist;
        } catch (Exception e) {
            Toast.makeText(PinDuanScanningActivity.this, "空的",
                    Toast.LENGTH_SHORT).show();
        }

        HashMap<String, LogicParameter> hsl = null;
        for (MyDevice md : am) {
            if (md.name.equals(devicename)) {
                hsl = md.logic;
                device = md;
            }
        }

        for (String tmp : hsl.keySet()) {
            LogicParameter llp = hsl.get(tmp);

            if (llp.type.startsWith("L")) {
                logicPPFXId = llp.id;       //频谱分析
            }

            if (llp.type.startsWith("D")) {
                logicDDFId = llp.id;       //测向
            }
        }

        ScanType = "PSCAN";

        LogicParameter currentLP = hsl.get(logicId);// 获取频段扫描参数相关的数据，以便画出初始界面
        ap = currentLP.parameterlist;
        for (Parameter p : ap) {
            if (p.name.trim().equals("StartFreq")) {
                startFreqParameter = p;
                startFreq = Float.parseFloat(p.defaultValue.trim());
            } else if (p.name.trim().equals("StopFreq")) {
                endFreqParameter = p;
                endFreq = Float.parseFloat(p.defaultValue.trim());
            } else if (p.name.trim().equals("StepFreq")) {
                stepFreqParameter = p;
                pStepFreq = Float.parseFloat(p.defaultValue.trim()) / 1000f;
            } else if (p.name.trim().equals("ScanType")) {
                ScanType = p.defaultValue.trim();
            }
        }

        pinduan.setParameters(startFreq, endFreq, pStepFreq * 1000);

        l = (LinearLayout) getLayoutInflater().inflate(
                R.layout.actionbarview, null);
        stationtextview = l.findViewById(R.id.name1);
        devicetextview = l.findViewById(R.id.name2);
        stationtextview.setText(stationname);
        devicetextview.setText(devicename);

        actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(false);
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setDisplayShowTitleEnabled(true);
        actionbar.setCustomView(l);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                Log.d("pinduan", String.valueOf(count++));
                if (msg.what == PARAMETERREFRESH) {
                    for (Parameter p : ap) {
                        if (p.name.equals("DemodulationSpan")) {
                            daikuan = Float.parseFloat(p.defaultValue);
                        } else if (p.name.equals("StepFreq")) {
                            pStepFreq = Float.parseFloat(p.defaultValue);
                        } else if (p.name.equals("CenterFreq")) {
                            centerFreq = Float.parseFloat(p.defaultValue);
                        } else if (p.name.equals("AntennaSelect")) {
                            txname = p.defaultValue;
                        }
                    }
                    pinduan.clear();
                    pinduan.setParameters(
                            Float.parseFloat(startFreqParameter.defaultValue
                                    .trim()), Float
                                    .parseFloat(endFreqParameter.defaultValue
                                            .trim()), Float
                                    .parseFloat(stepFreqParameter.defaultValue));

                    // pinduan.setParameters(startFreq, endFreq,pStepFreq);
                } else if (msg.what == SCANNINGDATA)// PSCAN模式，且点数未超过上限（即画出Max、Min、Avg）
                {
                    if (pause == false) {
                        try {
                            int a = GlobalData.pinduanQueue.size();
                            Log.d("PinduanUI", String.valueOf(a));
                            if (a > 5) {
                                GlobalData.pinduanQueue.poll();
                                GlobalData.pinduanQueue.poll();
                            }
                            short[] t = GlobalData.pinduanQueue.poll();
                            //
                            // for(int i=0;i<t.length;i++)
                            // {
                            // t[i]=(short) (t[i]+GlobalData.PDyinzi[i]);
                            // }

                            if (t == null) {
                                System.out.println("获取到的short【】是空的");
                            } else {
                                pinduan.setM(t);
                                if (showMax) {
                                    pinduan.setMax(GlobalData.pinduanMax);
                                } else {
                                    pinduan.setMax(null);
                                }
                                if (showMin) {
                                    pinduan.setMin(GlobalData.pinduanMin);
                                } else {
                                    pinduan.setMin(null);
                                }
                                if (showAvg) {
                                    pinduan.setAvg(GlobalData.pinduanAvg);
                                } else {
                                    pinduan.setAvg(null);
                                }
                                // pinduan.setHave(true);
                                pinduan.refreshWave();
                                pinduan.refreshTable();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (msg.what == SCANNINGDATANO)// PSCAN模式，且点数超过上限（即不画出Max、Min、Avg）。
                {
                    if (pause == false) {
                        try {
                            short[] t = null;
                            synchronized (GlobalData.pinduanQueue) {
                                int a = GlobalData.pinduanQueue.size();
                                if (a > 5) {
                                    GlobalData.pinduanQueue.poll();
                                    GlobalData.pinduanQueue.poll();
                                    Log.d("polldata", String.valueOf(a));
                                }
                                t = GlobalData.pinduanQueue.poll();
                            }

                            // for(int i=0;i<t.length;i++)
                            // {
                            // t[i]=(short) (t[i]+GlobalData.PDyinzi[i]);
                            // }
                            pinduan.setM(t);
                            if (t == null) {
                                Log.d("polldata", "null");
                            }
                            pinduan.setMax(null);
                            pinduan.setMin(null);
                            pinduan.setAvg(null);
                            // pinduan.setHave(true);
                            pinduan.refreshWave();
                            pinduan.refreshTable();
                        } catch (Exception e) {

                        }

                    }
                } else if (msg.what == SCANNINGDATAFSCAN)// 非PSCAN模式，且点数未超上限（即可以画出Max、Min、Avg)
                {
                    if (pause == false) {
                        // for(int i=0;i<GlobalData.pinduanScan.length;i++)
                        // {
                        // GlobalData.pinduanScan[i]=(short)
                        // (GlobalData.pinduanScan[i]+GlobalData.PDyinzi[i]);
                        // }

                        pinduan.setM(GlobalData.pinduanScan);

                        if (showMax) {
                            pinduan.setMax(GlobalData.pinduanMax);
                        } else {
                            pinduan.setMax(null);
                        }
                        if (showMin) {
                            pinduan.setMin(GlobalData.pinduanMin);
                        } else {
                            pinduan.setMin(null);
                        }
                        if (showAvg) {
                            pinduan.setAvg(GlobalData.pinduanAvg);
                        } else {
                            pinduan.setAvg(null);
                        }
                        // pinduan.setHave(true);
                        pinduan.refreshWave();
                        pinduan.refreshTable();
                    }
                } else if (msg.what == SCANNINGDATAFSCANNO)// 非PSCAN模式，且点数超上限（即不可以画出Max、Min、Avg)
                {
                    if (pause == false) {
                        synchronized (GlobalData.a) {
                            // for(int i=0;i<GlobalData.pinduanScan.length;i++)
                            // {
                            // GlobalData.pinduanScan[i]=(short)
                            // (GlobalData.pinduanScan[i]+GlobalData.PDyinzi[i]);
                            // }

                            pinduan.setM(GlobalData.pinduanScan);
                            pinduan.setMax(null);
                            pinduan.setMin(null);
                            pinduan.setAvg(null);
                            // pinduan.setHave(true);
                            pinduan.refreshWave();
                            pinduan.refreshTable();
                        }

                    }
                } else if (msg.what == Trigger) {
                    trigger();
                }

            }
        };

        pinduan.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            int p1 = 500;  //max
            int p2 = 0;
            double t1 = -1000000;
            double t2 = 1;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pinduan.adapter.setCurrentItem(position);
                pinduan.adapter.setClick(true);
                pinduan.adapter.notifyDataSetChanged();

                t2 = System.currentTimeMillis();
                p2 = position;
                if (p2 == p1 && t2 - t1 < 1000) {
                    trigger_pos = p2;
                    handler.sendEmptyMessage(PinDuanScanningActivity.Trigger);

                    //Log.d("TAG", "onItemClick: the position is = " + String.valueOf(position) + "\n and the id of it is = " + String.valueOf(id));
                } else {
                    p1 = p2;
                    t1 = t2;
                }

            }
        });

        pinduan.triggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trigged = true;
                handler.sendEmptyMessage(PinDuanScanningActivity.Trigger);
            }
        });
    }

    private void trigger() {
        if (trigger_pos > 0 || trigged) {

            try {
                Thread.sleep(100);

                if (trig_level) {

                    Intent intent = new Intent(PinDuanScanningActivity.this, SpectrumsAnalysisActivity.class);
                    //2通过Iintent携带额外数据
                    intent.setAction("function0");
                    Bundle bundle = new Bundle();
                    bundle.putString("devicename", devicename);
                    bundle.putString("stationname", stationname);
                    bundle.putString("stationKey", stationKey);
                    bundle.putString("lid", logicPPFXId);

                    Thread thread2 = new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            inithread = new IniThread();

                            inithread.start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    if (!trigged) {
                        long tmp = (long) (startFreq * 1000000) + (long) (pinduan.pss.datalist.get(trigger_pos) * pinduan.pss.bujin * 1000);

                        willExit();

                        intent.putExtra("MESSAGE", tmp / 1000000f);
                        intent.putExtras(bundle);
                        startActivity(intent);

                        thread2.start();

                    } else if (pinduan.pss.mk) {
                        willExit();
                        intent.putExtra("MESSAGE", pinduan.pss.mkHz);
                        trigged = false;
                        intent.putExtras(bundle);
                        startActivity(intent);

                        thread2.start();
                    }
                } else {
                    Intent intent = new Intent(PinDuanScanningActivity.this, SinglefrequencyDFActivity.class);
                    //2通过intent携带额外数据
                    intent.setAction("function12");
                    Bundle bundle = new Bundle();
                    bundle.putString("devicename", devicename);
                    bundle.putString("stationname", stationname);
                    bundle.putString("stationKey", stationKey);
                    bundle.putFloat("lan", stationF.lan);
                    bundle.putFloat("lon", stationF.lon);
                    bundle.putString("lid", logicDDFId);

                    if (!trigged) {
                        long tmp = (long) (startFreq * 1000000) + (long) (pinduan.pss.datalist.get(trigger_pos) * pinduan.pss.bujin * 1000);
                        intent.putExtra("MESSAGE", tmp / 1000000f);
                        willExit();
                        intent.putExtras(bundle);
                        startActivity(intent);

                    } else if (pinduan.pss.mk) {
                        intent.putExtra("MESSAGE", pinduan.pss.mkHz);
                        trigged = false;
                        willExit();
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        Log.d("xiaolaile", "menu");
        getMenuInflater().inflate(R.menu.pin_duan_scanning, menu);
        pauseitem = menu.findItem(R.id.pinduanstart);
        menuItemMin = menu.findItem(R.id.minshow);
        menuItemMax = menu.findItem(R.id.maxshow);
        menuItemAvg = menu.findItem(R.id.avgshow);
        if (SysApplication.settingSave.isShowBig()) {
            showMax = true;
            menuItemMax = menu.findItem(R.id.maxshow).setTitle("不显示最大值");
        } else {
            showMax = false;
        }
        if (SysApplication.settingSave.isShowaSmall()) {
            showMin = true;
            menuItemMin = menu.findItem(R.id.minshow).setTitle("不显示最小值");
        } else {
            showMin = false;
        }
        if (SysApplication.settingSave.isShowAverage()) {
            showAvg = true;
            menuItemAvg = menu.findItem(R.id.avgshow).setTitle("不显示平均值");
        } else {
            showAvg = false;
        }
        if (!SysApplication.settingSave.isOrientation()) {
            menu.findItem(R.id.caputure).setTitle("手机纵向");
            pinduan.setTopViewLayoutParamsH();
        }
        adjustMenu();
        return true;
    }

    private void adjustMenu() {
        if (menuItemAvg != null && menuItemMax != null && menuItemMin != null) {
            if (!allowOverlyDraw) {
                showMin = false;
                showMax = false;
                showAvg = false;
                SysApplication.settingSave.setShowaSmall(false);
                SysApplication.settingSave.setShowAverage(false);
                SysApplication.settingSave.setShowBig(false);
                menuItemMin.setEnabled(false);
                menuItemMax.setEnabled(false);
                menuItemAvg.setEnabled(false);
                menuItemMin.setTitle("显示最小值");
                menuItemMax.setTitle("显示最大值");
                menuItemAvg.setTitle("显示平均值");
            } else {
                if (SysApplication.settingSave.isShowBig()) {
                    showMax = true;
                    SysApplication.settingSave.setShowBig(true);
                    menuItemMax.setTitle("不显示最大值");
                }
                if (SysApplication.settingSave.isShowaSmall()) {
                    showMin = true;
                    SysApplication.settingSave.setShowaSmall(true);
                    menuItemMin.setTitle("不显示最小值");
                }
                if (SysApplication.settingSave.isShowAverage()) {
                    showAvg = true;
                    SysApplication.settingSave.setShowAverage(true);
                    menuItemAvg.setTitle("不显示平均值");
                }

                menuItemMin.setEnabled(true);
                menuItemMax.setEnabled(true);
                menuItemAvg.setEnabled(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
        startWindow();
        LogicParameter currentLogic = device.logic.get(logicId);
        float startvalue = 0;
        float endvalue = 0;
        float stepinto = 0;
        for (Parameter parameter : (currentLogic.parameterlist)) {
            String p = parameter.dispname.trim();
            if (p.equals("开始频率")) {
                startvalue = Float.parseFloat(parameter.defaultValue.trim());
            } else if (p.equals("结束频率")) {
                endvalue = Float.parseFloat(parameter.defaultValue.trim());
            } else if (p.equals("频率步进")) {
                stepinto = Float.parseFloat(parameter.defaultValue.trim());
            }
        }
        if ((endvalue - startvalue) / stepinto * 1000 > 20000) {
            allowOverlyDraw = false;
        } else {
            allowOverlyDraw = true;
        }
        stationtextview.postDelayed(() -> adjustMenu(), 100);
    }

    private void startWindow() {
        Type type = new Type(WindowController.FLAG_PINDUAN);
        EventBus.getDefault().postSticky(type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                WindowHelper.instance.setHasPermission(true);
                WindowHelper.instance.startWindowService(getApplicationContext());
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("提示：")
                        .setMessage("需要悬浮窗权限")
                        .setCancelable(true)
                        .setPositiveButton("设置", (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
            }
        } else {
            WindowHelper.instance.setHasPermission(true);
            WindowHelper.instance.startWindowService(getApplicationContext());
        }
    }

    public static boolean isRunning = false;
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //final MenuItem i = item;
        if (id == R.id.pinduanstart) {
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                startitem = item;
                //Log.d("inssize", "error4");
                if (item.getTitle().equals("开始测量")) {
                    isRunning = true;

                    //Log.d("inssize", "error1");
                    pause = false;

                    if (mythread == null) {
                        mythread = new MyThread();
                        //Log.d("inssize", "error2");
                    }
                    try {
                        // Log.d("inssize", "error3");
                        mythread.sendStartCmd();
                    } catch (Exception e) {
                        Log.d("mythreaderror", "start");
                    }
                    try {
                        mythread.start();
                    } catch (Exception e) {

                    }
                    item.setTitle("停止测量");
                } else if (item.getTitle().equals("停止测量")) {
                    pause();
                }
                lastClickTime = curClickTime;
            }
        } else if (id == R.id.tiaozheng) {
            if (item.getTitle().equals("调频调幅")) {
                item.setTitle("调整限值");
                pinduan.setTiaoZhi(false);
            } else if (item.getTitle().equals("调整限值")) {
                item.setTitle("调频调幅");
                pinduan.setTiaoZhi(true);
            }
        } else if (id == R.id.showtable) {

            if (item.getTitle().equals("隐藏表格")) {
                pinduan.hideTable(true);
                item.setTitle("展示表格");
                SysApplication.settingSave.setFormVisible(false);
            } else if (item.getTitle().equals("展示表格")) {
                pinduan.hideTable(false);
                item.setTitle("隐藏表格");
                SysApplication.settingSave.setFormVisible(true);
            }
        } else if (id == R.id.pinduanset) {
            if (pause) {
                Intent intent = new Intent(PinDuanScanningActivity.this,
                        PinDuanSetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sname", stationname);
                bundle.putString("dname", devicename);
                bundle.putString("stakey", stationKey);
                bundle.putString("lids", logicId);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                AlertDialog.Builder ab = new AlertDialog.Builder(
                        PinDuanScanningActivity.this);
                ab.setTitle("警告！");
                ab.setMessage("功能运行期间不可更改设置，确定要停止功能进行设置吗？");
                ab.setPositiveButton("确定",
                        (dialog, which) -> {
                            pause = true;
                            GlobalData.clearPinDuan();
                            startitem.setTitle("开始测量");
                            Intent intent = new Intent(
                                    PinDuanScanningActivity.this,
                                    PinDuanSetActivity.class);
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
        } else if (id == R.id.maxshow) {
            if (item.getTitle().equals("不显示最大值")) {
                item.setTitle("显示最大值");
                SysApplication.settingSave.setShowBig(false);
                showMax = false;
            } else if (item.getTitle().equals("显示最大值")) {
                item.setTitle("不显示最大值");
                SysApplication.settingSave.setShowBig(true);
                showMax = true;
            }
        } else if (id == R.id.minshow) {
            if (item.getTitle().equals("不显示最小值")) {
                SysApplication.settingSave.setShowaSmall(false);
                item.setTitle("显示最小值");
                showMin = false;
            } else if (item.getTitle().equals("显示最小值")) {
                SysApplication.settingSave.setShowaSmall(true);
                item.setTitle("不显示最小值");
                showMin = true;
            }
        } else if (id == R.id.avgshow) {
            if (item.getTitle().equals("不显示平均值")) {
                SysApplication.settingSave.setShowAverage(false);
                item.setTitle("显示平均值");
                showAvg = false;
            } else if (item.getTitle().equals("显示平均值")) {
                item.setTitle("不显示平均值");
                SysApplication.settingSave.setShowAverage(true);
                showAvg = true;
            }
        } else if (id == R.id.caputure) {
            if (item.getTitle().equals("手机横向")) {
                SysApplication.settingSave.setOrientation(false);
                GlobalData.show_horiz = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                item.setTitle("手机纵向");
            } else if (item.getTitle().equals("手机纵向")) {
                SysApplication.settingSave.setOrientation(true);
                item.setTitle("手机横向");
                GlobalData.show_horiz = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                pinduan.setTopViewLayoutParamsV();
            }
        } else if (id == R.id.trigger) {
            if (item.getTitle().equals("触发测量") && logicDDFId.length() > 5) {
                item.setTitle("触发测向");
                trig_level = false;
            } else if (item.getTitle().equals("触发测向") && logicPPFXId.length() > 5) {
                item.setTitle("触发测量");
                trig_level = true;
            }
        }
        return true;
    }

    private void pause() {
        isRunning = false;
        if (pauseitem != null) {
            pauseitem.setTitle("开始测量");
        }
        if (mythread != null && mythread.isAlive()) {
            mythread.sendEndCmd();
            mythread.setEnd(true);
            mythread = null;
        }
        WindowController.getInstance(this).pauseRecord();
        pause = true;
        GlobalData.clearPinDuan();
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
        pr.functionNum = 17;
        pr.stationid = MyTools.toCountString(stationKey, 76).getBytes();
        pr.logicid = MyTools.toCountString(logicId, 76).getBytes();
        pr.devicename = MyTools.toCountString(devicename, 36).getBytes();
        pr.pinduancount = 0;

        pr.logictype = MyTools.toCountString("SCAN", 16).getBytes();
        PinPuParameter[] parray = null;
        if (havetianxian) {
            parray = new PinPuParameter[ap.size() - 1];
        } else {
            parray = new PinPuParameter[ap.size()];
        }
        int z = 0;

        pr.tianxianname = MyTools.toCountString("NULL", 36).getBytes();
        for (Parameter para : ap) {
            PinPuParameter pin = new PinPuParameter();
            if (!(para.name.contains("AntennaSelect"))) {
                pin.name = MyTools.toCountString(para.name, 36).getBytes();
                pin.value = MyTools.toCountString(para.defaultValue, 36)
                        .getBytes();
                parray[z] = pin;
                z++;
            } else {
                pr.tianxianname = MyTools.toCountString(para.defaultValue, 36)
                        .getBytes();
            }

        }
        if (havetianxian) {
            pr.parameterslength = 72 * (ap.size() - 1);
        } else {
            pr.parameterslength = 72 * ap.size();
        }

        pr.length = pr.parameterslength + 247;
        pr.p = parray;
        try {
            request = JavaStruct.pack(pr);
        } catch (StructException e) {
            e.printStackTrace();
        }
        return request;
    }

    private void willExit() {
        try {
            isRunning = false;
            mythread.sendEndCmd();
            Thread.sleep(50);

            if (mythread != null) {
                mythread.setEnd(true);
                //mythread.join();
                mythread = null;
            }

            GlobalData.clearPinDuan();

        } catch (Exception e) {
            System.out.println("异常");
        } finally {
            try {
                if (socket != null) {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        willExit();
        super.onDestroy();
    }

}
