package com.huari.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingDeque;

import struct.JavaStruct;
import struct.StructException;

import com.huari.Base.AnalysisBase;
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
import com.huari.ui.DataSave;
import com.huari.ui.Disk;
import com.huari.ui.HColumns;
import com.huari.ui.MyData;
import com.huari.ui.VColumns;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import androidx.appcompat.app.ActionBar;

import android.os.Process;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

//单频测向功能窗体
public class SinglefrequencyDFActivity extends AnalysisBase {

    public static final int VCFRESH = 1;
    public static final int DISKREFRESH = 2;
    public static final int NETREFRESH = 3;
    public static final int FOURREFRESH = 4;
    public static final int PARTFRESH = 8;
    public static final int PARAMETERSREFRESH = 10;// 有参数更新了
    public static final int Trigger = 0x911;
    public static final int FINISH = 0x774;
    public static int flag;

    private boolean tomap = false;
    public static long time;

    @Override
    protected void onStart() {
        super.onStart();
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
    }

    SharedPreferences sharepre;
    SharedPreferences.Editor shareEditor;
    String prefixion = "prefixion";
    int i = 0;

    float lan, lon;

    HColumns hcs;
    VColumns vcs;
    com.huari.ui.FourModeView fmv; //表格View
    com.huari.ui.ShowWaveViewOfDDF showwaveview;
    Disk disks;
    View p;
    ActionBar actionbar;
    LinearLayout l;
    boolean pause = true;
    int e, q, dianping;
    float absolutedegree, relativedegree;
    String showmode, tongjimode, jiaodumode;
    float startFreq = 0f, endFreq = 0, pStepFreq = 0f, centerFreq = 0f,
            daikuan = 0f;
    float halfSpectrumsWide = 0.075f;// 频谱带宽的一半
    float autoFreq = -1f;

    String stationname = null, devicename = null, stationKey = null;
    String logicId;
    String txname;
    String logicindex;// 理论上逻辑参数的类型应该是“DDF”，但是由于需要填满后面的空余字节，所以实际上得到的类型是“DDF$%&#*&#”。
    // 此变量即用来表示这个带有乱码的字符串。
    ArrayList<Parameter> ap;

    Socket socket;// 用来接收数据
    OutputStream outs;
    InputStream ins;
    MyThread mythread;
    IniThread inithread;
    boolean runmyThread = true;

    private Menu menu1;
    MenuItem mitem;
    TextView stationtextview;
    TextView devicetextview;

    Parameter centerParameter;
    Parameter filterSpanParameter;// 次选带宽
    Parameter spectrumParameter;// 首选带宽

    public static boolean saveFlag = false;
    public static Queue<byte[]> queue;
    private String fileName;
    private Station stationF;
    private MyDevice device;

    class IniThread extends Thread {
        public void run() {
            try {
                socket = new Socket(GlobalData.mainIP, GlobalData.port2);
                ins = socket.getInputStream();
                outs = socket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class jumper implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                SinglefrequencyDFActivity.handle.sendEmptyMessage(SinglefrequencyDFActivity.Trigger);
                if (autoFreq > 0)
                    startDo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyThread extends Thread {
        boolean stop = false;

        private void setEnd(boolean b) {
            stop = b;
        }

        private void sendStartCmd() {
            Thread thread = new Thread(() -> {
                try {
                    byte[] bbb = iRequestInfo();
                    outs.write(bbb, 0, bbb.length);
                    outs.flush();
                } catch (NullPointerException e) {
                    System.out.println("sendStartCmd内部的NullPointException");
                    ByteFileIoUtils.runFlag = false;
                } catch (Exception e) {
                    System.out.println("sendStartCmd内部发生了异常");
                    ByteFileIoUtils.runFlag = false;
                }
            });
            thread.start();
        }

        public void savePrepare(long time) {
            ByteFileIoUtils.runFlag = true;
            queue = new LinkedBlockingDeque<>();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.getNumberFormat();
            fileName = "DF|" + df.format(new Date()).replaceAll(" ", "|");
//                    + "||" + stationname + "|" + devicename + "|" + stationKey + "|" + lan + "|" + lon;
//                    +"|"+logicId;    //会导致文件名长度超出限制
//            SharedPreferences sharedPreferences = getSharedPreferences("myclient", MODE_PRIVATE);
//            SharedPreferences.Editor shareEditor = sharedPreferences.edit();
//            shareEditor.putString(fileName, stationname + "|" + devicename + "||" + stationKey + "|||" + lan + "||||" + lon + "|||||" + logicId);
//            shareEditor.commit();  //以文件名作为key来将台站信息存入shareReferences
//            Log.d("xiaoxiao", String.valueOf(fileName.length()));
            SysApplication.byteFileIoUtils.writeBytesToFile(fileName, 1, time); //开始保存数据前的初始化，开启消费者所在线程
            RealTimeSaveAndGetStore.serializeFlyPig(stationF, devicename, device, logicId);//在消费者线程开启后，开始Statio的序列化并放入队列缓冲区中等待消费者线程遍历之
        }

        private void sendEndCmd() {
            Thread thread = new Thread(() -> {
                ByteFileIoUtils.runFlag = false;
                saveFlag = false;
                StopTaskFrame st = new StopTaskFrame();
                st.functionNum = 46;
                st.length = 2;
                byte[] b;
                try {
                    b = JavaStruct.pack(st);
                    outs.write(b, 0, b.length);
                    outs.flush();
                    // Log.i("发送了","停止命令");
                } catch (Exception e) {
                    e.printStackTrace();
                    // Log.i("停止","出问题了");
                }
            });
            thread.start();
        }

        public void run() {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                int available = 0;

                byte[] info = null;
                byte[] tmpdata = null;
                time = 0;
                flag = 0;
                int read = 0;
                int last = 0;

                while (!stop) {
                    if (ins != null) {
                        available = ins.available();
                        info = new byte[available];
                        last = 0;
                        while (available > 0) {
                            try {
                                tmpdata = new byte[available];
                                read = ins.read(tmpdata);
                                if (read > 0)
                                {
                                    System.arraycopy(tmpdata,0,info,last,read);
                                    available -= read;
                                    last += read;
                                }
                                else
                                {
                                    available = 0;
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // System.out.println("开始接收DDF数据，解析发生了异常，定位到DDF的Activity的225行");
                            }
                            if (saveFlag == true) {
                                if (flag == 0) {
                                    time = 0;
                                    savePrepare(System.currentTimeMillis());
                                    flag++;
                                }
                                time = RealTimeSaveAndGetStore.SaveAtTime(available, info, time, 1);//给数据加一个时间的包头后递交到缓存队列中
                            }
                        }

                        Parse.parseDDF(info);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public void run() {
//            try {
//                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
//                int available = 0;
//                time = 0;
//                byte[] info;
//                flag = 0;
//
//                while (available == 0 && !stop) {
//                    //Log.d("xiaoxiaoll", "循环");
//                    if (ins != null) {
//                        available = ins.available();
//                        if (available > 0) {
//                            //Log.d("avaliable", String.valueOf(available));
//                            info = new byte[available];
//                            int read = 0;
//                            try {
//                                read = ins.read(info);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            if (read > 0)
//                                try {
//                                    Parse.newParseSpectrumsAnalysis(info);
//                                    if (saveFlag == true) {
//                                        if (flag == 0) {
//                                            Log.d("xiaoxiaoll1", "存储准备");
//                                            time = 0;
//                                            savePrepare(System.currentTimeMillis());
//                                            flag++;
//                                        }
//                                        time = RealTimeSaveAndGetStore.SaveAtTime(available, info, time, 1);//给数据加一个时间的包头后递交到缓存队列中
//                                    }
//                                    available = 0;
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!tomap) {
            mitem.setTitle("开始测量");
            endDo();

            WindowHelper.instance.setForeground(false);
            WindowHelper.instance.stopWindowService(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tomap = false;
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
        startWindow();
    }

    private void startWindow() {
        Type type = new Type(WindowController.FLAG_DF);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_frequencyscanning);
            sharepre = getSharedPreferences("myclient", MODE_PRIVATE);
            shareEditor = sharepre.edit();
            inithread = new IniThread();
            inithread.start();
            SysApplication.getInstance().addActivity(this);
            Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);// 捕获UncaughtException异常！！！！！！！！！！！
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            int twidth = metric.widthPixels;
            int theight = metric.heightPixels;
            float density = metric.density;
            int densityDpi = metric.densityDpi;
            double dui = Math.sqrt(twidth * twidth + theight * theight);
            double x = dui / densityDpi;

            if (x >= 6.5) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            }

            l = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.actionbarview, null);

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            System.out.println("OnCreate中获取Intent传递的值之前");
            stationname = bundle.getString("stationname");
            devicename = bundle.getString("devicename");
            stationKey = bundle.getString("stationKey");
            lan = bundle.getFloat("lan");
            lon = bundle.getFloat("lon");
            logicId = bundle.getString("lid");
            Log.d("xiaoxiaox", String.valueOf(logicId.length()));
            stationtextview = l.findViewById(R.id.name1);
            devicetextview = l.findViewById(R.id.name2);
            stationtextview.setText(stationname);
            devicetextview.setText(devicename);

            actionbar = getSupportActionBar();
            actionbar.setDisplayShowHomeEnabled(false);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setCustomView(l);

            fmv = findViewById(R.id.buildcusli);
            showwaveview = findViewById(R.id.ddfshowwaveview);
            vcs = findViewById(R.id.buildvcs);
            disks = findViewById(R.id.builddisk);
            p = LayoutInflater.from(this).inflate(
                    R.layout.activity_frequencyscanning, null);
            vcs.setOnex0(75);
            tongjimode = getResources().getString(R.string.tongjimode);
            showmode = getResources().getString(R.string.showmode);
            jiaodumode = getResources().getString(R.string.jiaodumode);
            if (tongjimode.equals("幅度")) {
                fmv.tongjimodeswitch("fudu");
            } else if (tongjimode.equals("质量")) {
                fmv.tongjimodeswitch("zhiliang");
            } else if (tongjimode.equals("概率")) {
                fmv.tongjimodeswitch("gailv");
            }

            if (showmode.equals("图形")) {
                fmv.setGraphModeShow(true);
            } else if (showmode.equals("表格")) {
                fmv.setGraphModeShow(false);
            }

            if (jiaodumode.equals("正北")) {
                disks.setRischecked(false);
            } else if (jiaodumode.equals("相对")) {
                disks.setRischecked(true);
            }

            stationF = GlobalData.stationHashMap.get(stationKey);
            Iterator<String> it = GlobalData.stationHashMap.keySet().iterator();

            if (stationF == null) {
                return;
            }

            ArrayList<MyDevice> am = stationF.showdevicelist;
            HashMap<String, LogicParameter> hsl = null;
            for (MyDevice md : am) {
                if (md.name.equals(devicename)) {
                    device = md;
                    hsl = md.logic;
                }
            }

            try {
                autoFreq = bundle.getFloat("MESSAGE");

            } catch (Exception e) {
                e.printStackTrace();
            }

            GlobalData.isFirstAudio = true;

            LogicParameter currentLP = hsl.get(logicId);// 获取频谱分析相关的数据，以便画出初始界面
            ap = currentLP.parameterlist;

            for (Parameter p : ap) {
                if (p.name.equals("DemodulationSpan")) {
                    daikuan = Float.parseFloat(p.defaultValue);
                } else if (p.name.equals("StepFreq")) {
                    pStepFreq = Float.parseFloat(p.defaultValue);
                } else if (p.name.equals("CenterFreq")) {
                    centerFreq = Float.parseFloat(p.defaultValue);
                    if (autoFreq > 0) {
                        centerFreq = autoFreq;
                        p.defaultValue = Float.toString(autoFreq);
                    }
                    centerParameter = p;
                } else if (p.name.equals("AntennaSelect")) {
                    txname = p.defaultValue;
                }
                if (p.name.equals("FilterSpan")) {
                    halfSpectrumsWide = Float.parseFloat(p.defaultValue) / 2000f;
                    filterSpanParameter = p;
                }
                if (p.name.equals("SpectrumSpan")) {
                    halfSpectrumsWide = Float.parseFloat(p.defaultValue) / 2000f;
                    spectrumParameter = p;
                }
            }

            startFreq = (float) (Math.floor(Float
                    .parseFloat(centerParameter.defaultValue) * 1000f
                    - halfSpectrumsWide * 1000)) / 1000;
            endFreq = (Float.parseFloat(centerParameter.defaultValue) * 1000f + halfSpectrumsWide * 1000) / 1000;
            showwaveview.setF(startFreq, endFreq, pStepFreq);

            handle = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case NETREFRESH:// 来了频谱数据/
                            showwaveview.setHave(true);// 以下三行被屏蔽掉的地方3
                            showwaveview.setM(GlobalData.pinpu);
                            // showwaveview.setFandC((float)GlobalData.startfreq,(float)GlobalData.endfreq,GlobalData.count);
                            showwaveview.setFandC((float) GlobalData.startfreq,
                                    (float) GlobalData.endfreq, GlobalData.count);
                            break;
                        case PARTFRESH:
                            param_update();
                            break;
                        case PARAMETERSREFRESH:
                            showwaveview.setHave(false);
                            startFreq = (float) (Math.floor(Float
                                    .parseFloat(centerParameter.defaultValue)
                                    * 1000f - halfSpectrumsWide * 1000)) / 1000;
                            endFreq = (Float
                                    .parseFloat(centerParameter.defaultValue) * 1000f + halfSpectrumsWide * 1000) / 1000;
                            showwaveview.setF(startFreq, endFreq, pStepFreq);
                            break;
                        case Trigger:
                            MenuItem mi = menu1.findItem(R.id.singlepause);
                            mi.setTitle("停止测量");
                            break;
                        case FINISH:
                            //GlobalData.isFirstAudio = true;
                            break;
                    }

                }
            };
            GlobalData.stationKey = stationKey;
            GlobalData.deviceName = devicename;
            GlobalData.logicId = logicId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (autoFreq > 0) {
            new Thread(new jumper()).start();
        }
    }

    private void releaseAudioResource() {
        at.stop();
        at.release();

        GlobalData.isFirstAudio = true;
    }

    private void willExit() {

        DataSave.datamap.clear();
        fmv.clear();
        try {
            if (isRunning)
                endDo();

            if (mythread != null) {
                runmyThread = false;
                Thread.sleep(10);
                mythread.join();
                mythread = null;
            }
            releaseAudioResource();

            if (disks != null) {
                disks.gc();
            }
            //System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (socket != null) {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        willExit();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.singlefreactionbar, menu);
        menu1 = menu;
        MenuItem i11 = menu.findItem(R.id.single11);// 相对
        MenuItem i12 = menu.findItem(R.id.single12);// 正北
        MenuItem i21 = menu.findItem(R.id.single21);// 幅度
        MenuItem i22 = menu.findItem(R.id.single22);// 概率
        MenuItem i23 = menu.findItem(R.id.single23);// 质量
        MenuItem i24 = menu.findItem(R.id.single24);// 图像
        MenuItem i25 = menu.findItem(R.id.single25);// 表格

        mitem = menu.findItem(R.id.singlepause);

        if (tongjimode.equals("幅度")) {
            i21.setChecked(true);
        } else if (tongjimode.equals("质量")) {
            i23.setChecked(true);
        } else if (tongjimode.equals("概率")) {
            i22.setChecked(true);
        }

        if (showmode.equals("图像")) {
            i24.setChecked(true);
        } else if (showmode.equals("表格")) {
            i25.setChecked(true);
        }

        if (jiaodumode.equals("正北")) {
            i12.setChecked(true);
        } else if (jiaodumode.equals("相对")) {
            i11.setChecked(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public static boolean isRunning = false;

    private void endDo() {
        isRunning = false;
        WindowController.getInstance(this).pauseRecord();
        pause = true;

        if (mythread != null) {
            try {
                mythread.sendEndCmd();
                isRunning = false;
                Thread.sleep(20);
                mythread.setEnd(true);
                if (mythread != null && mythread.isAlive()) {
                    mythread.sendEndCmd();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mythread = null;
            }
        }
    }

    private void startDo() {
        if (mythread == null) {
            mythread = new MyThread();
        }

        try {
            Thread.sleep(5);
            mythread.sendStartCmd();
            mythread.setEnd(false);
            mythread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println(mythread.getState());
            if (mythread.getState() != Thread.State.RUNNABLE)
                mythread.start();
        } catch (Exception e) {
            // System.out.println("单频测向中mythread.start（ ）时发生错误了");
            Log.d("xiao", "线程未开启");
        }

        pause = false;
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.single11:
                disks.setRischecked(true);
                disks.postInvalidate();
                fmv.setNorthBoolean(false);
                jiaodumode = "相对";
                item.setChecked(true);
                break;
            case R.id.single12:
                disks.setRischecked(false);
                disks.postInvalidate();
                fmv.setNorthBoolean(true);
                jiaodumode = "正北";
                item.setChecked(true);
                break;
            case R.id.single21:
                fmv.tongjimodeswitch("fudu");
                item.setChecked(true);
                break;
            case R.id.single23:
                fmv.tongjimodeswitch("zhiliang");
                item.setChecked(true);
                break;
            case R.id.single22:
                fmv.tongjimodeswitch("gailv");
                item.setChecked(true);
                break;
            case R.id.single24:
                fmv.setGraphModeShow(true);
                item.setChecked(true);
                break;
            case R.id.single25:
                fmv.setGraphModeShow(false);
                item.setChecked(true);
                break;
            case R.id.singleitem3:
                disks.diskclear();
                fmv.clear();
                GlobalData.clearDDF();
                showwaveview.clear();
                if (hcs != null) {
                    hcs.setE(-20);
                    hcs.setQ(0);
                    hcs.postInvalidate();
                } else {
                    vcs.setE(-20);
                    vcs.setQ(0);
                    vcs.postInvalidate();
                }
                break;
            case R.id.singlepause:
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                    if (item.getTitle().equals("开始测量")) {
                        try {
                            isRunning = true;
                            item.setTitle("停止测量");
                            pause = false;
                            disks.diskclear();// 以下多行被屏蔽掉的地方5
                            fmv.clear();
                            showwaveview.clear();
                            if (hcs != null) {
                                hcs.setE(-20);
                                hcs.setQ(0);
                                hcs.postInvalidate();
                            } else {
                                vcs.setE(-20);
                                vcs.setQ(0);
                                vcs.postInvalidate();
                            }
                        } catch (Exception e) {
                            // System.out.println("点击“开始任务”后,发送开始命令前，发生了异常");
                            Log.d("xiao", "error");
                        }

                        startDo();

                    } else if (item.getTitle().equals("停止测量")) {
                        item.setTitle("开始测量");
                        endDo();
                        autoFreq = -1f;
                    }
                    lastClickTime = curClickTime;
                }
                break;
            case R.id.forwardtomap:// 交汇
                Intent toMap = new Intent(SinglefrequencyDFActivity.this,
                        MapActivity.class);
                // Bundle b=new Bundle();
                // b.putString("from", "Single");
                // toMap.putExtras(b);
                Bundle bundle1 = new Bundle();
                bundle1.putFloat("lon", stationF.lon);
                bundle1.putFloat("lan", stationF.lan);
                Log.d("xiaolan", String.valueOf(stationF.lon));
                Log.d("xiaolan", String.valueOf(stationF.lan));
                toMap.putExtra("bundle", bundle1);
                tomap = true;
                startActivity(toMap);
                break;
            case R.id.menusavedata:
                if (pause == false) {
                    shareEditor
                            .putString(prefixion + i, prefixion + i + ","
                                    + stationname + i + "," + lan + "," + lon + ","
                                    + absolutedegree + "," + relativedegree + ","
                                    + "false");
                    shareEditor.commit();
                    i++;
                    shareEditor.putInt("savecount", i);
                    if (shareEditor.commit()) {
                        Toast.makeText(SinglefrequencyDFActivity.this, "保存成功！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.singleset:

                if (pause == true) {
                    Intent intent = new Intent(SinglefrequencyDFActivity.this,
                            SingleSetActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("sname", stationname);
                    bundle.putString("dname", devicename);
                    bundle.putString("stakey", stationKey);
                    bundle.putString("lids", logicId);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 0);
                } else {
                    AlertDialog.Builder ab = new AlertDialog.Builder(
                            SinglefrequencyDFActivity.this);
                    ab.setTitle("警告！");
                    ab.setMessage("功能运行期间不可更改设置，确定要停止功能进行设置吗？");
                    ab.setPositiveButton("确定",
                            (dialog, which) -> {
                                pause = true;
                                findViewById(R.id.zanting1);
                                mitem.setTitle("开始测量");

                                mythread.sendEndCmd();

                                if (mythread != null) {
                                    try {
                                        mythread.destroy();
                                    } catch (Exception e) {

                                    }
                                }
                                mythread = null;

                                GlobalData.Spectrumpinpu = null;
                                GlobalData.oldcount = 0;
                                GlobalData.haveCount = 0;
                                System.gc();

                                Intent intent = new Intent(
                                        SinglefrequencyDFActivity.this,
                                        SingleSetActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("sname", stationname);
                                bundle.putString("dname", devicename);
                                bundle.putString("stakey", stationKey);
                                bundle.putString("lids", logicId);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, 0);
                            });
                    ab.setNegativeButton("取消", null);
                    ab.create();
                    ab.show();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    private byte[] iRequestInfo() {
        byte[] request = null;
        boolean havetianxian = false;

        for (Parameter para : ap) {

            if ((para.name.contains("Anten"))) {
                havetianxian = true;
                break;
            }
        }

        PPFXRequest pr = null;
        try {
            pr = new PPFXRequest();
            // 帧体长度暂时跳过
            pr.functionNum = 18;
            if (stationKey == null) {
                // System.out.println("组装开始命令的方法中stationKey是空的，所以发生了异常");
            }
            pr.stationid = MyTools.toCountString(stationKey.trim(), 76)
                    .getBytes();
            pr.logicid = MyTools.toCountString(logicId.trim(), 76).getBytes();

            pr.devicename = MyTools.toCountString(devicename.trim(), 36)
                    .getBytes();
            pr.pinduancount = 0;
            pr.logictype = MyTools.toCountString("DDF", 16).getBytes();

            PinPuParameter[] parray = null;
            if (havetianxian) {
                parray = new PinPuParameter[ap.size() - 1];
            } else {
                parray = new PinPuParameter[ap.size()];
            }

            int z = 0;
            for (Parameter para : ap) {
                PinPuParameter pin = new PinPuParameter();
                if (para.name.equals("AudioType")){
                    if (para.defaultValue.toUpperCase().contains("PCM"))
                        GlobalData.audio_type = 35;
                    if (para.defaultValue.toUpperCase().contains("GSM"))
                        GlobalData.audio_type = 39;
                }
                if (!para.name.contains("AntennaSele")) {
                    pin.name = MyTools.toCountString(para.name.trim(), 36)
                            .getBytes();
                    pin.value = MyTools.toCountString(para.defaultValue.trim(),
                            36).getBytes();
                    parray[z] = pin;
                    z++;
                } else {
                    pr.tianxianname = MyTools.toCountString(
                            para.defaultValue.trim(), 36).getBytes();
                }

            }
            if (havetianxian) {
                pr.parameterslength = 72 * (ap.size() - 1);
            } else {
                pr.parameterslength = 72 * ap.size();
            }

            pr.length = pr.parameterslength + 247;
            pr.p = parray;
        } catch (NullPointerException e) {
             System.out.println("组装开始命令的方法中出现空指针异常");
        } catch (Exception e) {
             System.out.println("组装开始命令的方法中出现非空指针类异常");
        }

        try {
            request = JavaStruct.pack(pr);

        } catch (StructException e) {
            e.printStackTrace();
        }
        return request;
    }

    public void param_update() {
        MyData mydata;
        try {
            if (pause == false) {
                dianping = GlobalData.DDFdianping;// new
                // Random().nextInt(121)-20;
                e = dianping;
                q = GlobalData.qua;
                absolutedegree = GlobalData.xiangdui + GlobalData.north;
                relativedegree = GlobalData.xiangdui;
                vcs.refresh(e, q);// 被屏蔽掉的地方2
                if (dianping >= vcs.getEthreshold() && q >= vcs.getQthreshold()) {
                    disks.refresh(relativedegree, absolutedegree);// 被屏蔽掉的地方4
                    DataSave.sum = DataSave.sum + 1;
                    if (DataSave.datamap.containsKey(absolutedegree)) {
                        mydata = DataSave.datamap.get(absolutedegree);
                        mydata.count = mydata.count + 1;
                        mydata.maxplitude = Math.max(mydata.maxplitude, e);
                        mydata.maxquality = Math.max(mydata.maxquality, q);
                    } else {
                        mydata = new MyData();
                        mydata.count = mydata.count + 1;
                        mydata.maxplitude = e;
                        mydata.maxquality = q;
                        mydata.reldegree = relativedegree;
                        DataSave.datamap.put(absolutedegree, mydata);
                    }
                    if (mydata.count >= DataSave.maxcount) {
                        DataSave.maxcount = mydata.count;
                        DataSave.MaxProdegree = absolutedegree;
                    }
                    if (mydata.maxplitude >= DataSave.maxpli) {
                        DataSave.maxpli = mydata.maxplitude;
                        DataSave.MaxPlidegree = absolutedegree;
                    }
                    if (mydata.maxquality >= DataSave.maxqua) {
                        DataSave.maxqua = mydata.maxquality;
                        DataSave.MaxQuadegree = absolutedegree;
                    }
                    fmv.refresh();// 被屏蔽掉的地方1
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg0 == 0 && arg1 == 1) {
            Bundle bundle = arg2.getExtras();
            stationtextview.setText(bundle.getString("stname"));
            devicetextview.setText(bundle.getString("dename"));
        }
    }
}
