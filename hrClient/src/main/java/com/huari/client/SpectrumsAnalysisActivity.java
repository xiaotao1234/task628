package com.huari.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.os.PowerManager;
import android.os.Process;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huari.Base.AnalysisBase;
import com.huari.NetMonitor.WindowController;
import com.huari.NetMonitor.WindowHelper;
import com.huari.adapter.ItuAdapterOfListView;
import com.huari.adapter.PagerAdapterOfSpectrum;
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
import com.huari.tools.FileOsImpl;
import com.huari.tools.MyTools;
import com.huari.tools.Parse;
import com.huari.tools.RealTimeSaveAndGetStore;
import com.huari.tools.SysApplication;

import org.greenrobot.eventbus.EventBus;

public class SpectrumsAnalysisActivity extends AnalysisBase {
    //PowerManager pm;
    //PowerManager.WakeLock wl;

    boolean cq;// 是否显示场强

    public static int AUDIODATA = 0x5;
    public static int PARAMETERREFRESH = 0x6;

    public static Queue<byte[]> queue;
    public static boolean saveFlag = false;
    float lan, lon;

    private String AudioName = "";        //原始音频数据文件 ，麦克风
    private String NewAudioName = "";     //可播放的音频文件
    private static File recordFile;

    com.huari.ui.ShowWaveView waveview;
    com.huari.ui.Waterfall waterfall;
    com.huari.ui.PartWaveShowView showwave;
    ViewPager viewpager;
    ItuAdapterOfListView listAdapter;
    PagerAdapterOfSpectrum spectrumAdapter;
    ListView itulistview;
    LinearLayout ituLinearLayout;
    ArrayList<View> viewlist;

    public static ArrayList<byte[]> audiolist1, audiolist2;
    public static boolean firstaudio = true;

    boolean partispause, fullispause = true;
    boolean triged = false;
    ArrayList<Parameter> ap;
    float startFreq = 0f, endFreq = 0f, pStepFreq = 0f, centerFreq = 0f;
    float autoFreq = -1f;

    float spwide = 0f, halfSpectrumsWide;// 频谱带宽的一半
    String logicId;
    String txname;


    View parentview;

    TextView stationtextview, devicetextview;
    LinearLayout l;

    public static String stationname = null, devicename = null, stationKey = null;

    ActionBar actionbar;

    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 2);

    Socket socket;// 用来接收数据
    OutputStream outs;
    InputStream ins;
    MyThread mythread;
    IniThread inithread;

    static Parameter centerParameter;
    static Parameter filterSpanParameter;
    static Parameter spectrumParameter;

    boolean showMax, showMin, showAvg, water;

    // 解析声音相关的东西
    private String fileName;
    private static String fileBasePath;
    private Station stationF;
    private MyDevice device;
    public static int flag;
    private MenuItem miVoice;
    private MenuItem mitem;
    private Menu menu1;
    private MyDevice iDevice;
    private boolean haveVoice = false;
    int count = 0;
    public static long time;

    @SuppressWarnings("deprecation")
    class IniThread extends Thread {
        public void run() {
            try {
                socket = new Socket(GlobalData.mainIP, GlobalData.port2);
                socket.setSoTimeout(5000);
                ins = socket.getInputStream();
                outs = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendClose() {
        Thread thread = new Thread(() -> {
            StopTaskFrame st = new StopTaskFrame();
            st.length = 2;
            st.functionNum = 16;
            st.tail = 22;
            byte[] b;
            try {
                b = JavaStruct.pack(st);
                outs.write(b);
                outs.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    class MyThread extends Thread {

        public boolean isEnd() {
            return stop;
        }

        boolean stop = false;

        private void setEnd(boolean b) {
            stop = b;
        }

        private void sendStartCmd() {
            stop = false;
            Thread thread = new Thread(() -> {
                try {
                    byte[] bbb = iRequestInfo();

                    GlobalData.clearSpectrums();
                    if (filterSpanParameter != null) {
                        halfSpectrumsWide = Float
                                .parseFloat(filterSpanParameter.defaultValue) / 2000f;
                    }
                    if (spectrumParameter != null) {
                        halfSpectrumsWide = Float
                                .parseFloat(spectrumParameter.defaultValue) / 2000f;
                    }
                    startFreq = (float) (Math.floor(Float
                            .parseFloat(centerParameter.defaultValue)
                            * 1000f
                            - halfSpectrumsWide * 1000)) / 1000;
                    endFreq = (Float.parseFloat(centerParameter.defaultValue) * 1000f + halfSpectrumsWide * 1000) / 1000;
                    waveview.setF(startFreq, endFreq, pStepFreq);

                    outs.write(bbb,0,bbb.length);
                    outs.flush();

//                } catch (NullPointerException e) {
//                    System.out.println("异常");
//                    sendStartCmd();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("84854959异常");
                }
            });
            thread.start();
        }


        private void sendEndCmd() {
		    stop = true;
            ByteFileIoUtils.runFlag = false;
            Thread thread = new Thread(() -> {
                StopTaskFrame st = new StopTaskFrame();
                st.functionNum = 16;
                st.length = 2;
                byte[] b;
                try {
                    b = JavaStruct.pack(st);
                    outs.write(b);
                    outs.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        public void run() {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                int available = 0;
                time = 0;
                byte[] info;
                flag = 0;

                while (available == 0 && !stop) {
                    //Log.d("xiaoxiaoll", "循环");
                    if (ins != null) {
                        available = ins.available();
                        if (available > 0) {
                            //Log.d("avaliable", String.valueOf(available));
                            info = new byte[available];
                            int read = 0;
                            try {
                                read = ins.read(info);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (read > 0)
                                try {
                                    Parse.newParseSpectrumsAnalysis(info);
                                    if (saveFlag == true) {
                                        if (flag == 0) {
                                            Log.d("xiaoxiaoll1", "存储准备");
                                            time = 0;
                                            savePrepare();
                                            flag++;
                                        }
                                        time = RealTimeSaveAndGetStore.SaveAtTime(available, info, time, 2);//给数据加一个时间的包头后递交到缓存队列中
                                    }
                                    available = 0;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void savePrepare() {
        ByteFileIoUtils.runFlag = true;
        queue = new LinkedBlockingDeque<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fileName = "AN|" + df.format(new Date()).replaceAll(" ", "|");
//                    + "||" + stationname + "|" + devicename + "|" + stationKey + "|" + lan + "|" + lon;
//                    +"|"+logicId;    //会导致名字长度超出限制
//		SharedPreferences sharedPreferences = getSharedPreferences("myclient", MODE_PRIVATE);
//		SharedPreferences.Editor shareEditor = sharedPreferences.edit();
//		shareEditor.putString(fileName, stationname + "|" + devicename + "||" + stationKey + "|||" + lan + "||||" + lon + "|||||" + logicId);
//		shareEditor.commit();  //以文件名作为key来将台站信息存入shareReferences
//		Log.d("xiaoxiao", String.valueOf(fileName.length()));
        SysApplication.byteFileIoUtils.writeBytesToFile(fileName, 2,System.currentTimeMillis()); //开始保存数据前的初始化
        RealTimeSaveAndGetStore.serializeFlyPig(stationF, devicename, device, logicId);//在消费者线程开启后，开始Statio的序列化并放入队列缓冲区中等待消费者线程遍历之
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectrums_analysis);
        inithread = new IniThread();
        inithread.start();

        SysApplication.getInstance().addActivity(this);

        GlobalData.willplay = false;

        Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);

//		pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);
//		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyTag");

        ituLinearLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.listviewwithitu, null);
        itulistview = ituLinearLayout.findViewById(R.id.itulistview);
        viewlist = new ArrayList<>();

        spectrumAdapter = new PagerAdapterOfSpectrum(viewlist);
        if (GlobalData.ituHashMap == null) {
            GlobalData.ituHashMap = new HashMap<>();
        }
        listAdapter = new ItuAdapterOfListView(SpectrumsAnalysisActivity.this,
                GlobalData.ituHashMap);
        viewpager = findViewById(R.id.firstviewpager);
        itulistview.setAdapter(listAdapter);

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

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        stationname = bundle.getString("stationname");
        devicename = bundle.getString("devicename");
        stationKey = bundle.getString("stationKey");
        logicId = bundle.getString("lid");
        iDevice = null;
        for (MyDevice myd : GlobalData.stationHashMap.get(stationKey).showdevicelist) {
            if (myd.name.equals(devicename)) {
                iDevice = myd;
            }
        }

        try {
            autoFreq = bundle.getFloat("MESSAGE");

        } catch (Exception e) {

        }

        for (String s : GlobalData.stationHashMap.keySet()) {
            Log.d("stationHashMaplan", String.valueOf(GlobalData.stationHashMap.get(s).lan));
            Log.d("stationHashMaplon", String.valueOf(GlobalData.stationHashMap.get(s).lon));
        }
        LinearLayout titlebar = (LinearLayout) getLayoutInflater().inflate(
                R.layout.actionbarview, null);
        stationtextview = (TextView) titlebar.findViewById(R.id.name1);
        devicetextview = (TextView) titlebar.findViewById(R.id.name2);

        stationtextview.setText(stationname);
        devicetextview.setText(devicename);

        showwave = (com.huari.ui.PartWaveShowView) getLayoutInflater().inflate(
                R.layout.a, null);

        viewlist.add(showwave);
        viewlist.add(ituLinearLayout);
        viewpager.setAdapter(spectrumAdapter);
        parentview = getLayoutInflater().inflate(
                R.layout.activity_spectrums_analysis, null);

        waterfall = findViewById(R.id.waterfall);

        GlobalData.create_colortbl();
        waterfall.set_ColorTbl(GlobalData.ColorTbl);

        // 开始设置waveview的相关参数。参数从GlobalData中读取。
        waveview = findViewById(R.id.buildshowwaveview);
        stationF = GlobalData.stationHashMap.get(stationKey);
        ArrayList<MyDevice> am = stationF.showdevicelist;
        HashMap<String, LogicParameter> hsl = null;
        for (MyDevice md : am) {
            if (md.name.equals(devicename)) {
                hsl = md.logic;
                device = md;
            }
        }
        LogicParameter currentLP = hsl.get(logicId);// 获取频谱分析相关的数据，以便画出初始界面
        ap = currentLP.parameterlist;

        for (Parameter p : ap) {
            if (p.name.equals("ifbw")) {
                spwide = Float.parseFloat(p.defaultValue);
                halfSpectrumsWide = spwide / 2000;
            } else if (p.name.equals("rbw") || p.name.equals("step")) {
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
        startFreq = (float) (Math.floor(centerFreq * 1000f - halfSpectrumsWide
                * 1000)) / 1000;
        endFreq = (centerFreq * 1000f + halfSpectrumsWide * 1000) / 1000;
        waveview.setF(startFreq, endFreq, pStepFreq);
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

        GlobalData.isFirstAudio = true;

        handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d("Anala", String.valueOf(count++));
                fullispause = false;

                if (msg.what == DIANPINGDATA && fullispause == false
                        && partispause == false) {
                    if (cq == false) {
                        showwave.refresh(GlobalData.dianping);
                    } else {
                        try {
                            showwave.refresh(GlobalData.dianping
                                    + GlobalData.yinzi[0]);
                        } catch (NullPointerException e) {
                            showwave.refresh(GlobalData.dianping);
                        }
                    }
                } else if (msg.what == 0x6)// 参数更新了，可能涉及到坐标的变化，所以需要刷新界面
                {
                    if (filterSpanParameter != null) {
                        halfSpectrumsWide = Float
                                .parseFloat(filterSpanParameter.defaultValue) / 2000f;
                    }
                    if (spectrumParameter != null) {
                        halfSpectrumsWide = Float
                                .parseFloat(spectrumParameter.defaultValue) / 2000f;
                    }
                    startFreq = (float) (Math.floor(Float
                            .parseFloat(centerParameter.defaultValue)
                            * 1000f - halfSpectrumsWide * 1000)) / 1000;
                    endFreq = (Float
                            .parseFloat(centerParameter.defaultValue) * 1000f + halfSpectrumsWide * 1000) / 1000;
                    waveview.setM(null);
                    waveview.setMax(null);
                    waveview.setMin(null);
                    waveview.setAvg(null);
                    waveview.setF(startFreq, endFreq, pStepFreq);
                    GlobalData.ituHashMap.clear();
                    listAdapter.notifyDataSetChanged();
                } else if (msg.what == 0x10) {
                    if (water && GlobalData.Spectrumpinpu != null) {
                        waterfall.set_newdata(GlobalData.Spectrumpinpu, GlobalData.Spectrumpinpu.length);
                        waterfall.postInvalidate();
                    }
                    waveview.setHave(true);
                    waveview.setM(GlobalData.Spectrumpinpu);
                    if (showMax) {
                        waveview.setMax(GlobalData.Spectrummax);
                    } else {
                        waveview.setMax(null);
                    }
                    if (showMin) {
                        waveview.setMin(GlobalData.Spectrummin);
                    } else {
                        waveview.setMin(null);
                    }
                    if (showAvg) {
                        waveview.setAvg(GlobalData.Spectrumavg);
                    } else {
                        waveview.setAvg(null);
                    }
                    if (GlobalData.Spectrumpinpu != null) {
                        waveview.setFandC(startFreq, endFreq,
                                GlobalData.Spectrumpinpu.length);
                        waveview.postInvalidate();
                    }
                } else if (msg.what == AUDIODATA)  //=0x5   声音
                {
                    synchronized (synObject) {

                        at.write(audioBuffer, 0, audioBuffer.length);
                        at.flush();
                        at.play();
                        at.stop();
                        while (at.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        audioindex = 0;
                    }
                } else if (msg.what == ITUDATA) {
                    listAdapter.notifyDataSetChanged();
                } else if (msg.what == Trigger) {
                    MenuItem mi = menu1.findItem(R.id.zanting1);
                    mi.setTitle("停止测量");
                } else if (msg.what == FINISH) {
                    //GlobalData.isFirstAudio = true;
                }
            }
        };

        if (autoFreq > 0) {
            new Thread(new jumper()).start();
            triged = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        miVoice = menu.findItem(R.id.recorder);
        if (!haveVoice) {
            miVoice.setEnabled(false);
        } else {
            miVoice.setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("onCreateOptionsMenu", String.valueOf(haveVoice));
        getMenuInflater().inflate(R.menu.pinpufenxiitem, menu);
        menu1 = menu;
        mitem = menu.findItem(R.id.zanting1);
        miVoice = menu.findItem(R.id.recorder);
        if (!haveVoice) {
            miVoice.setEnabled(false);
        } else {
            miVoice.setEnabled(true);
        }
        if (mitem.getTitle().equals("开始测量")) {
            fullispause = true;
            partispause = true;
        } else {
            fullispause = false;
            partispause = false;
        }
        if (menu.findItem(R.id.ppfxshowmin).getTitle().equals("不显示最小值")) {
            showMin = true;
        } else {
            showMin = false;
        }
        if (menu.findItem(R.id.ppfxshowmax).getTitle().equals("不显示最大值")) {
            showMax = true;
        } else {
            showMax = false;
        }
        if (menu.findItem(R.id.ppfxshowavg).getTitle().equals("不显示平均值")) {
            showAvg = true;
        } else {
            showAvg = false;
        }
        if (menu.findItem(R.id.capture).getTitle().equals("关闭瀑布图")) {
            water = true;
        } else {
            water = false;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public static boolean isRunning = false;

    class jumper implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                SpectrumsAnalysisActivity.handle.sendEmptyMessage(SpectrumsAnalysisActivity.Trigger);
                if (autoFreq > 0)
                    startDo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.zanting1) {
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                mitem = item;
                if (item.getTitle().equals("停止测量")) {
                    isRunning = false;
                    WindowController.getInstance(this).pauseRecord();
                    item.setTitle("开始测量");
                    endDo();
                    autoFreq = -1f;
                } else {
                    item.setTitle("停止测量");
                    startDo();
                }
                lastClickTime = curClickTime;
            }
        } else if (item.getItemId() == R.id.custompinpuset) {
            if (!isRunning) {
                Intent intent = new Intent(SpectrumsAnalysisActivity.this,
                        PPFXsetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sname", stationname);
                bundle.putString("dname", devicename);
                bundle.putString("stakey", stationKey);
                bundle.putString("lids", logicId);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            } else {
                AlertDialog.Builder ab = new AlertDialog.Builder(
                        SpectrumsAnalysisActivity.this);
                ab.setTitle("警告！");
                ab.setMessage("功能运行期间不可更改设置，确定要停止功能进行设置吗？");
                ab.setPositiveButton("确定",
                        (dialog, which) -> {
                            //GlobalData.isFirstAudio = true;
                            partispause = true;
                            fullispause = true;
                            mitem.setTitle("开始测量");
                            if (mythread != null) {
                                try {
                                    mythread.sendEndCmd();
                                    isRunning = false;
                                    mythread.setEnd(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            mythread = null;

                            GlobalData.Spectrumpinpu = null;
                            GlobalData.oldcount = 0;
                            GlobalData.haveCount = 0;
                            System.gc();

                            Intent intent = new Intent(
                                    SpectrumsAnalysisActivity.this,
                                    PPFXsetActivity.class);
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
        } else if (item.getItemId() == R.id.changqiang) {
            if (item.getTitle().equals("场强")) {
                if (partispause) {
                    cq = true;
                    item.setTitle("电平");
                    showwave.setDanwei("场强dBuV/m");
                } else {
                    Toast.makeText(SpectrumsAnalysisActivity.this,
                            "任务运行期间，不可切换", Toast.LENGTH_SHORT).show();
                }
            } else if (item.getTitle().equals("电平")) {
                if (partispause) {
                    cq = false;
                    item.setTitle("场强");
                    showwave.setDanwei("电平dBuV");
                } else {
                    Toast.makeText(SpectrumsAnalysisActivity.this,
                            "任务运行期间，不可切换", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (item.getItemId() == R.id.ppfxshowmax) {
            if (item.getTitle().equals("不显示最大值")) {
                item.setTitle("显示最大值");
                showMax = false;
            } else if (item.getTitle().equals("显示最大值")) {
                item.setTitle("不显示最大值");
                showMax = true;
            }
        } else if (item.getItemId() == R.id.ppfxshowmin) {
            if (item.getTitle().equals("不显示最小值")) {
                item.setTitle("显示最小值");
                showMin = false;
            } else if (item.getTitle().equals("显示最小值")) {
                item.setTitle("不显示最小值");
                showMin = true;
            }
        } else if (item.getItemId() == R.id.ppfxshowavg) {
            if (item.getTitle().equals("不显示平均值")) {
                item.setTitle("显示平均值");
                showAvg = false;
            } else if (item.getTitle().equals("显示平均值")) {
                item.setTitle("不显示平均值");
                showAvg = true;
            }
        } else if (item.getItemId() == R.id.capture) {
            if (item.getTitle().equals("显示瀑布图")) {
                item.setTitle("关闭瀑布图");
                water = true;
                waterfall.setVisibility(View.VISIBLE);
            } else {
                item.setTitle("显示瀑布图");
                water = false;
                waterfall.setVisibility(View.GONE);
            }
        } else if (item.getItemId() == R.id.recorder) {
            if (item.getTitle().equals("录音")) {
                if (mythread != null && isRunning) {
                    try {
                        AudioName = FileOsImpl.forSaveFloder + File.separator + "voice" + File.separator + "temp";
                        NewAudioName = getWavFilePath();

                        recordFile = new File(AudioName);
                        recordFile.delete();
                        if (!recordFile.getParentFile().exists()) {
                            recordFile.getParentFile().mkdirs();
                        }

                        fos = new FileOutputStream(recordFile);// 建立一个可存取字节的文件

                        isRecording = true;
                        item.setTitle("停止录音");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "请在测量开始后再记录声音", Toast.LENGTH_SHORT).show();
                }
            } else {
                item.setTitle("录音");
                if (isRecording) {
                    try {
                        if (fos != null)
                            fos.close();   // 关闭写入流
                        FileOsImpl.copyWaveFile(AudioName, NewAudioName);   //给裸数据加上头文件
                        isRecording = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    private void startDo() {
        isRunning = true;
        partispause = false;
        fullispause = false;

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
            //System.out.println(mythread.getState());
            if (mythread.getState() != Thread.State.RUNNABLE)
                mythread.start();
        } catch (Exception e) {
            // System.out.println("单频测量中mythread.start（ ）时发生错误了");
            Log.d("xiao", "线程未开启");
        }
    }

    private void endDo() {
        partispause = true;
        fullispause = true;
        //mitem.setTitle("开始测量");
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


        GlobalData.Spectrumpinpu = null;
        GlobalData.oldcount = 0;
        GlobalData.haveCount = 0;

        //System.gc();

        if (isRecording) {
            MenuItem itm;
            itm = menu1.findItem(R.id.recorder);
            itm.setTitle("录音");
            isRecording = false;
            try {
                if (fos != null)
                    fos.close();// 关闭写入流
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOsImpl.copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 1) {
            Bundle bundle = data.getExtras();
            stationtextview.setText(bundle.getString("stname"));
            devicetextview.setText(bundle.getString("dename"));
        }
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
            if (para.name.equals("AudioType"))
            {
                if (para.defaultValue.toUpperCase().equals("PCM"))
                    GlobalData.audio_type = 35;
                if (para.defaultValue.toUpperCase().equals("GSM610"))
                    GlobalData.audio_type = 39;
            }
        }

        // 帧体长度暂时跳过
        pr.functionNum = 16;
        pr.stationid = MyTools.toCountString(stationKey, 76).getBytes();
        pr.logicid = MyTools.toCountString(logicId, 76).getBytes();
        pr.devicename = MyTools.toCountString(devicename, 36).getBytes();
        pr.pinduancount = 0;

        pr.logictype = MyTools.toCountString("level", 16).getBytes();
        PinPuParameter[] parray = null;
        if (havetianxian) {
            parray = new PinPuParameter[ap.size() - 1];
        } else {
            parray = new PinPuParameter[ap.size()];
        }

        int z = 0;
        for (Parameter para : ap) {
            PinPuParameter pin = new PinPuParameter();
            pr.tianxianname = MyTools.toCountString("NULL", 36).getBytes();
            if (!(para.name.contains("Anten"))) {
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

    private int[] stringtoarray(String k, String b) {
        String[] v = k.split(b);
        int[] x = new int[v.length];
        for (int z = 0; z < v.length; z++) {
            x[z] = Integer.parseInt(v[z]);
        }
        return x;
    }

    private void willExit() {
        if (isRunning)
            endDo();

        try {
            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mythread = null;

        try {
            GlobalData.Spectrumpinpu = null;
            GlobalData.Avg = null;
            GlobalData.oldcount = 0;
            GlobalData.haveCount = 0;
            releaseAudioResource();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        willExit();
        super.onDestroy();
    }

    private void releaseAudioResource() {
        at.stop();
        at.release();

        GlobalData.isFirstAudio = true;
    }

    @Override
    protected void onResume() {
        //wl.acquire();
        fullispause = false;
        partispause = false;
        isRunning = false;
        WindowHelper.instance.setForeground(true);
        WindowHelper.instance.startWindowService(getApplicationContext());
        startWindow();
//		RealTimeSaveAndGetStore.ParseLocalDdfData("nba",2,30);
        LogicParameter currentLogic = iDevice.logic.get(logicId);
        for (Parameter parameter : (currentLogic.parameterlist)) {
            String p = parameter.dispname.trim();
            if (p.equals("声音开关")) {
                haveVoice = !parameter.defaultValue.trim().equals("OFF");
                Log.d("声音开关", String.valueOf(haveVoice));
                break;
            }
        }
        super.onResume();
        parentview.postDelayed(() -> {
            if (!haveVoice) {
                miVoice.setEnabled(false);
            } else {
                miVoice.setEnabled(true);
            }
        }, 500);
    }

    private void startWindow() {
        Type type = new Type(WindowController.FLAG_ANALYSIS);
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
    protected void onPause() {
        //wl.release();
        super.onPause();

        isRunning = false;
        WindowController.getInstance(this).pauseRecord();
        mitem.setTitle("开始测量");
        endDo();
        Log.d("analis", "onpause");
        ByteFileIoUtils.runFlag = false;
        WindowHelper.instance.setForeground(false);
        WindowHelper.instance.stopWindowService(this);
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    public static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * 获取麦克风输入的原始音频流文件路径
     *
     * @return
     */
    public static String getRawFilePath() {
        String mAudioRawPath = "";
        if (isSdcardExit()) {
            fileBasePath = FileOsImpl.forSaveFloder;
            mAudioRawPath = fileBasePath + File.separator + "Voice" + File.separator + "transfer";
        }
        if (!(new File(fileBasePath).exists())) {
            new File(fileBasePath).mkdirs();
        }
        return mAudioRawPath;
    }

    public static String getWavFilePath() {
        String mAudioWavPath = "";
        if (isSdcardExit()) {
            String fileBasePath = FileOsImpl.forSaveFloder;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fileName = "RE|" + df.format(new Date()) + ".wav";
            fileName = fileName.replaceAll(":", "_");
            mAudioWavPath = fileBasePath + File.separator + "data" + File.separator + fileName;
            if (!((new File(mAudioWavPath)).getParentFile().exists())) {
                new File(mAudioWavPath).mkdirs();
            }
        }
        return mAudioWavPath;
    }
}
