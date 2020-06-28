package com.huari.client;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import struct.JavaStruct;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huari.Base.AnalysisBase;
import com.huari.adapter.HistoryShowWindowAdapter;
import com.huari.adapter.ItuAdapterOfListView;
import com.huari.adapter.PagerAdapterOfSpectrum;

import com.huari.commandstruct.StopTaskFrame;
import com.huari.dataentry.ForADataInformation;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;

import com.huari.dataentry.Parameter;
import com.huari.tools.ByteFileIoUtils;
import com.huari.tools.RealTimeSaveAndGetStore;
import com.huari.tools.SysApplication;
import com.huari.tools.TimeTools;
import com.huari.ui.CustomProgress;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;


public class HistoryAnalysisActivity extends AnalysisBase {

    PowerManager pm;
    PowerManager.WakeLock wl;

    boolean cq;// 是否显示场强

    public static int IQDATA = 0x4;
    public static int AUDIODATA = 0x5;
    public static int PARAMETERREFRESH = 0x6;
    public static int FIRSTAUDIOCOME = 0x9;
    public static int tempLength = 409600;
    public static Queue<byte[]> queue;

    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";

    private static long AUDIO_SAMPLE_RATE = 44100;
    private static int AUDIO_CHANNL = 2;

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
    TextView alllength;
    TextView readnow;
    LinearLayout ituLinearLayout;
    float spwide = 0f;// 频谱带宽的一半
    ArrayList<View> viewlist;

    public static ArrayList<byte[]> audiolist1, audiolist2;
    public static boolean firstaudio = true;

    boolean partispause, fullispause = true;
    ArrayList<Parameter> ap;
    float startFreq = 0f, endFreq = 0f, pStepFreq = 0f, centerFreq = 0f,
            daikuan = 0f;
    float halfSpectrumsWide;// 频谱带宽的一半
    String logicId;
    String txname;
    MenuItem mitem;

    static byte[] info;


    View parentview;
    String[] namesofitems, advanceditems, generalparent, generaletdata;
    //private int generalindex;
    String[][] generalchild;
    TextView normaltextview, advancedtextview, titlebarname, stationtextview,
            devicetextview;

    int offset, displaywidth, barwidth;
    String stationname = null, devicename = null, stationKey = null;
    //static String mydevicename;// 仅在播放声音创建声音播放器时使用。
    ActionBar actionbar;

    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 2);
    String tempstationname, tempdevicename;

    Socket socket;// 用来接收数据
    OutputStream outs;
    InputStream ins;
    HistoryAnalysisActivity.IniThread inithread;

    static Parameter centerParameter;
    static Parameter filterSpanParameter;
    static Parameter spectrumParameter;

    boolean showMax, showMin, showAvg;

    // 解析声音相关的东西
    private String filename;
    private SharedPreferences sharepre;
    private SharedPreferences.Editor shareEditor;
    private CustomProgress customProgress;
    private ImageView contorl;
    ImageView previousButton;
    ImageView nextButton;
    private List<Parameter> parameterList;
    private ImageView showStation;
    private List<String> names;
    private List<String> defaultValues;
    private ImageView back;


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


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_analysis);
        customProgress = findViewById(R.id.video_progress);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        sharepre = getSharedPreferences("myclient", MODE_PRIVATE);
        shareEditor = sharepre.edit();
        inithread = new HistoryAnalysisActivity.IniThread();
        inithread.start();
        SysApplication.getInstance().addActivity(this);
        GlobalData.willplay = false;
        Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);
        pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyTag");
        ituLinearLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.listviewwithitu, null);
        itulistview = ituLinearLayout.findViewById(R.id.itulistview);
        viewlist = new ArrayList<>();
        spectrumAdapter = new PagerAdapterOfSpectrum(viewlist);
        if (GlobalData.ituHashMap == null) {
            GlobalData.ituHashMap = new HashMap<>();
        }
        listAdapter = new ItuAdapterOfListView(HistoryAnalysisActivity.this,
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
        filename = intent.getStringExtra("filename");
        LinearLayout titlebar = (LinearLayout) getLayoutInflater().inflate(
                R.layout.actionbarview, null);
        stationtextview = titlebar.findViewById(R.id.name1);
        devicetextview = titlebar.findViewById(R.id.name2);
        contorl = findViewById(R.id.play_control);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        alllength = findViewById(R.id.music_length);
        readnow = findViewById(R.id.play_plan);
        showStation = findViewById(R.id.station_button);
        showStation = findViewById(R.id.station_button);
        showStation.setOnClickListener(v -> popWindow(showStation));
        previousButton.setOnClickListener(v -> RealTimeSaveAndGetStore.previousFrame(HistoryAnalysisActivity.this));
        nextButton.setOnClickListener(v -> RealTimeSaveAndGetStore.nextFrame(HistoryAnalysisActivity.this));
        contorl.setOnClickListener(v -> RealTimeSaveAndGetStore.pauseOrResume(contorl));
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

        waveview = findViewById(R.id.buildshowwaveview);
        handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                fullispause = false;
                partispause = false;
                try {
                    if (msg.what == DIANPINGDATA && fullispause == false
                            && partispause == false) {
                        Log.d("historyana","come");
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
                        Log.d("historyana","comeview");
                        if (GlobalData.Spectrumpinpu != null) {
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
                        waveview.postInvalidate();
                        waveview.setFandC(startFreq, endFreq,
                                GlobalData.Spectrumpinpu.length);
                    } else if (msg.what == AUDIODATA)  //=0x5   声音
                    {
                        synchronized (synObject) {
                            at.write(audioBuffer, 0, audioBuffer.length);
                            at.flush();
                            at.play();
                            at.stop();
                            while (at.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                                Thread.sleep(1);
                            }
                            audioindex = 0;
                        }
                    } else if (msg.what == ITUDATA) {
                        listAdapter.notifyDataSetChanged();
                    } else if (msg.what == 121) {
                        if (first == true) {
                            first = false;
                            alllength.setText(TimeTools.getInstance().transform(RealTimeSaveAndGetStore.time));
                        }
                        readnow.setText(TimeTools.getInstance().transform(RealTimeSaveAndGetStore.time / 100 * (Integer) msg.obj));
                        customProgress.setProgress((Integer) msg.obj);
                        if ((Integer) msg.obj == 100) {
                            contorl.setBackgroundResource(R.drawable.stop_icon);
                            RealTimeSaveAndGetStore.StopFlag = true;
                        }
                    } else if (msg.what == 34) {
                        AfterGetStation((ForADataInformation) msg.obj);
                    }
                } catch (Exception e) {

                }
            }
        };
    }

    boolean first = true;

    private void popWindow(View view) {
        // TODO: 2016/5/17 构建一个popupwindow的布局
        View popupView = HistoryAnalysisActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null);
        // TODO: 2016/5/17 为了演示效果，简单的设置了一些数据，实际中大家自己设置数据即可，相信大家都会。
        RecyclerView lsvMore = popupView.findViewById(R.id.lsvMore);
        List<String> list = new ArrayList<>();
        list.add("ddada");
        list.add("ddada");
        list.add("ddada");
        list.add("ddada");
        list.add("ddada");
        HistoryShowWindowAdapter historyShowWindowAdapter = new HistoryShowWindowAdapter(names, defaultValues);
        lsvMore.setLayoutManager(new LinearLayoutManager(this));
        lsvMore.setAdapter(historyShowWindowAdapter);
        // TODO: 2016/5/17 创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(popupView, 500, 600);
        // TODO: 2016/5/17 设置动画
        window.setAnimationStyle(R.style.popup_window_anim);
        // TODO: 2016/5/17 设置背景颜色
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        // TODO: 2016/5/17 设置可以获取焦点
        window.setFocusable(true);
        // TODO: 2016/5/17 设置可以触摸弹出框以外的区域
        window.setOutsideTouchable(true);
        // TODO：更新popupwindow的状态
        window.update();
        // TODO: 2016/5/17 以下拉的方式显示，并且可以设置显示的位置
        window.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
    }

    private void AfterGetStation(ForADataInformation stationF) {
        if (stationF == null) {
            return;
        }

        names = new ArrayList<>();
        defaultValues = new ArrayList<>();
        HashMap<String, LogicParameter> list = stationF.getDevice().getLogic();
        parameterList = list.get(stationF.getLogicId()).getParameterlist();
        if (parameterList != null) {
            for (Parameter parameter : parameterList) {
                names.add(parameter.dispname);
                defaultValues.add(parameter.defaultValue);
            }
        }

        logicId = stationF.getLogicId();
        LogicParameter currentLP = stationF.getDevice().logic.get(logicId);// 获取频谱分析相关的数据，以便画出初始界面
        ap = currentLP.parameterlist;
        stationtextview.setText(stationF.getStationName());
        devicetextview.setText(stationF.getDeviceName());
        for (Parameter p : ap) {
            if (p.name.equals("ifbw")) {
                spwide = Float.parseFloat(p.defaultValue);
                halfSpectrumsWide = spwide / 2000;
            } else if (p.name.equals("rbw") || p.name.equals("step")) {
                pStepFreq = Float.parseFloat(p.defaultValue);
            } else if (p.name.equals("CenterFreq")) {
                centerFreq = Float.parseFloat(p.defaultValue);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wl.acquire();
        fullispause = false;
        partispause = false;
        GlobalData.clearSpectrums();
        RealTimeSaveAndGetStore.ParseLocalWrap(filename, 2, handle);
        customProgress.setSystemUiVisibility(View.INVISIBLE);
        customProgress.setProgress(0);
        RealTimeSaveAndGetStore.progress  = 0;
        customProgress.setProgressListener(progress -> {
            if (RealTimeSaveAndGetStore.thread.isAlive()) {
                RealTimeSaveAndGetStore.progressFlg = true;
                RealTimeSaveAndGetStore.progress = (int) progress;
                if (RealTimeSaveAndGetStore.thread.getState() == Thread.State.WAITING) {
                    synchronized (RealTimeSaveAndGetStore.person) {
                        RealTimeSaveAndGetStore.person.notify();
                    }
                }
            } else {
                RealTimeSaveAndGetStore.ParseLocalWrap(filename, 2, handle);
                RealTimeSaveAndGetStore.progressFlg = true;
                RealTimeSaveAndGetStore.progress = (int) progress;
            }
        });
        RealTimeSaveAndGetStore.deserializeFlyPig(filename, handle);
    }

    @Override
    protected void onPause() {
        wl.release();
        super.onPause();
        RealTimeSaveAndGetStore.StopFlag = false;
        ByteFileIoUtils.runFlag = false;
        RealTimeSaveAndGetStore.ParseFlg = false;
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
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioRawPath = fileBasePath + "/" + AUDIO_RAW_FILENAME;
        }

        return mAudioRawPath;
    }

    public static String getWavFilePath() {
        String mAudioWavPath = "";
        if (isSdcardExit()) {
            String fileBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fileName = "rec|" + df.format(new Date()).replaceAll(" ", "|") + ".wav";
            mAudioWavPath = fileBasePath + File.separator + "Audio" + File.separator + fileName;
            if (!(new File(mAudioWavPath).exists())) {
                new File(mAudioWavPath).mkdirs();
            }
        }
        return mAudioWavPath;
    }
}
