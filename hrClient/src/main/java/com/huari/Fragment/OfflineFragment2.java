package com.huari.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huari.adapter.SimpleTestAdapter;
import com.huari.adapter.TagCloudAdapter;
import com.huari.client.DzActivity;
import com.huari.client.FileListActivity;
import com.huari.client.FindFileActivity;
import com.huari.client.HistoryListActivity;
import com.huari.client.MonthDataActivity;
import com.huari.client.OfflineActivity;
import com.huari.client.R;
import com.huari.client.SetparamActivity;
import com.huari.dataentry.recentContent;
import com.huari.tools.FileOsImpl;
import com.huari.ui.CalendarView;
import com.huari.ui.TagCloudView;
import com.huari.ui.statisticalGraph;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

//import cn.bmob.push.BmobPush;
//import cn.bmob.v3.Bmob;
//import cn.bmob.v3.BmobInstallation;
//import cn.bmob.v3.BmobInstallationManager;
//import cn.bmob.v3.BmobPushManager;
//import cn.bmob.v3.InstallationListener;
//import cn.bmob.v3.exception.BmobException;
//import cn.bmob.v3.listener.PushListener;
//import static cn.bmob.v3.Bmob.getApplicationContext;

public class OfflineFragment2 extends Fragment {
    TagCloudView tagCloudView;
    RecyclerView rv;
    SimpleTestAdapter simpleTestAdapter;
    LinearLayout pinpulayout;
    LinearLayout danpinLayout;
    LinearLayout pinduanLayout;
    LinearLayout musicLayout;
    LinearLayout fileLayout;
    LinearLayout downMapLayout;
    LinearLayout setLayout;
    LinearLayout bombLayout;
    LinearLayout dzLayout;
    LinearLayout backLayout;
    com.huari.ui.pieLineView pieLineView;
    NestedScrollView nestedScrollView;

    TextView danpinSize;
    TextView pinduanSize;
    TextView pinpuSize;
    TextView yinpinSize;
    TextView danpinMem;
    TextView pinpuMem;
    TextView pinduanMem;
    TextView yinpinMem;
    TextView danpinNew;
    TextView pinduanNew;
    TextView pinpuNew;
    TextView yinpinNew;
    statisticalGraph weekLayout;
    CalendarView monthLayout;

    List<Integer> list;
    List<String> stringList;
    List<String> danpinAll;
    List<String> pinpuAll;
    List<String> pinduanAll;
    List<String> musicAll;

    private String danpinlength;
    private String pinpulength;
    private String pinduanlength;
    private String yinpinlength;

    private WifiManager mWifiManager;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<recentContent> list = (List<recentContent>) msg.obj;
            simpleTestAdapter.setRecentContent(list);
            if (rv != null) {
                rv.setAdapter(simpleTestAdapter);
            }
            final TagCloudAdapter adapter = new TagCloudAdapter(list, rv, context);
            tagCloudView.setAdapter(adapter);
            tagCloudView.setBackgroundColor(Color.parseColor("#00000000"));
        }
    };
    private Intent intent;
    private Context context;
    private Activity activity;
    private long danpinTime = 0;
    private String danpinnewFile = "";
    private long pinpiTime = 0;
    private String pinpunewFile = "";
    private long pinduanTime = 0;
    private String pinduannewFile = "";
    private long musicTime = 0;
    private String musicnewFile = "";

    @SuppressLint("ValidFragment")
    public OfflineFragment2(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public OfflineFragment2() {
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main31, null, false);
        nestedScrollView = view.findViewById(R.id.offline_scroll);
        pinpulayout = view.findViewById(R.id.pinpu_layout);
        danpinLayout = view.findViewById(R.id.danpin_layout);
        downMapLayout = view.findViewById(R.id.downoad_offlinemap);
        setLayout = view.findViewById(R.id.set);
        bombLayout = view.findViewById(R.id.bomb_push);
        dzLayout = view.findViewById(R.id.dz);
        pinduanLayout = view.findViewById(R.id.pinduan_layout);
        musicLayout = view.findViewById(R.id.music_layout);
        backLayout = view.findViewById(R.id.offline_page1);
        fileLayout = view.findViewById(R.id.file_layout);
        tagCloudView = view.findViewById(R.id.tag_cloud);
        pieLineView = view.findViewById(R.id.pie_show_precent);

        danpinSize = view.findViewById(R.id.danpin_size);
        pinpuSize = view.findViewById(R.id.pinpu_size);
        pinduanSize = view.findViewById(R.id.pinduan_size);
        yinpinSize = view.findViewById(R.id.yinpin_size);

        danpinMem = view.findViewById(R.id.danpin_mem);
        pinpuMem = view.findViewById(R.id.pinpu_mem);
        pinduanMem = view.findViewById(R.id.pinduan_mem);
        yinpinMem = view.findViewById(R.id.yinpin_mem);

        danpinNew = view.findViewById(R.id.danpin_new);
        pinpuNew = view.findViewById(R.id.pinpu_new);
        pinduanNew = view.findViewById(R.id.pinduan_new);
        yinpinNew = view.findViewById(R.id.yinpin_new);

        weekLayout = view.findViewById(R.id.week_view);
        weekLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        monthLayout = view.findViewById(R.id.month_view);
        monthLayout.setTouchDisallowFlag(true);
        monthLayout.setClickListener(() -> {
            if (context != null) {
                Intent intent = new Intent(context, MonthDataActivity.class);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(
                        activity, monthLayout, "shareimage").toBundle();
                startActivity(intent, options);
            }
        });

        dzLayout.setOnClickListener(v -> startActivity(new Intent(context, DzActivity.class)));
        danpinLayout.setOnClickListener(v -> click(HistoryListActivity.DF));
        pinpulayout.setOnClickListener(v -> click(HistoryListActivity.AN));
        pinduanLayout.setOnClickListener(v -> click(HistoryListActivity.PD));
        musicLayout.setOnClickListener(v -> click(HistoryListActivity.RE));
        fileLayout.setOnClickListener(v -> startActivity(new Intent(context, FileListActivity.class)));
        downMapLayout.setOnClickListener(v -> startActivity(new Intent(context, OfflineActivity.class)));
        //setLayout.setOnClickListener(v -> startActivity(new Intent(context, SetActivity.class)));
        setLayout.setOnClickListener(v -> startActivity(new Intent(context, SetparamActivity.class)));
        simpleTestAdapter = new SimpleTestAdapter();
        simpleTestAdapter.setContext(context);
        rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(context));
//        mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        mMainHandler = new Handler();
        return view;
    }


    private void click(String s) {
        if (context != null) {
            intent = new Intent(context, FindFileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("type", s);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private Runnable mMainRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWifiManager.isWifiEnabled()) {
                mMainHandler.postDelayed(mMainRunnable, 1000);
            } else {
                //dzLayout.postDelayed(() -> push(),5000);
            }
        }
    };
    private Handler mMainHandler;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        FileOsImpl.getRecentList(handler); //  请求刷新历史数据
    }

    @Override
    public void onResume() {
        Log.d("fragmentLive", "onresume");
        super.onResume();
        FileOsImpl.getRecentList(handler);
        List<recentContent> listFile = new ArrayList<>();
        File file1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        if (file1.listFiles() != null) {
            for (File file : file1.listFiles()) {
                if (file.getName().contains("DF")) {
                    listFile.add(new recentContent(file.getAbsolutePath(), file.getName(), 1));
                } else if (file.getName().contains("AN")) {
                    listFile.add(new recentContent(file.getAbsolutePath(), file.getName(), 2));
                } else if (file.getName().contains("PD")) {
                    listFile.add(new recentContent(file.getAbsolutePath(), file.getName(), 3));
                } else if (file.getName().contains("RE")) {
                    listFile.add(new recentContent(file.getAbsolutePath(), file.getName(), 4));
                }
            }
            initData(listFile);
        }
    }

    private void popWindowDeleteFile(View view, File file) {
        View popupView = activity.getLayoutInflater().inflate(R.layout.push_edit_view, null);
        popupView.setPadding(50, 0, 50, 0);
        PopupWindow window = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.showAtLocation(backLayout, Gravity.CLIP_VERTICAL, 0, 0);
        window.setAnimationStyle(R.style.popup_window_anim);
        window.setWidth((int) getResources().getDimension(R.dimen.dp_200));
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        window.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        TextView cancel = popupView.findViewById(R.id.cancel);
        TextView ensure = popupView.findViewById(R.id.ensure);
        ensure.setOnClickListener(v -> {
            if (file.exists()) {
                file.delete();
            }
            window.dismiss();
        });
        cancel.setOnClickListener(v -> window.dismiss());
    }

    private void initData(List<recentContent> list) {
        danpinAll = new ArrayList<>();
        pinpuAll = new ArrayList<>();
        pinduanAll = new ArrayList<>();
        musicAll = new ArrayList<>();
        for (recentContent recentContent : list) {
            switch (recentContent.getType()) {
                case 1:
                    danpinAll.add(recentContent.getFile());
                    break;
                case 2:
                    pinpuAll.add(recentContent.getFile());
                    break;
                case 3:
                    pinduanAll.add(recentContent.getFile());
                    break;
                case 4:
                    musicAll.add(recentContent.getFile());
                    break;
                default:
                    break;
            }
        }
        int danpin = 0;
        for (String filename : danpinAll) {
            if (danpinTime < new File(filename).lastModified()) {
                danpinTime = new File(filename).lastModified();
                danpinnewFile = filename;
            }
            danpin = danpin + (int) (new File(filename).length());
        }
        danpinlength = getSize(danpin);

        int pinpu = 0;
        for (String filename : pinpuAll) {
            if (pinpiTime < new File(filename).lastModified()) {
                pinpiTime = new File(filename).lastModified();
                pinpunewFile = filename;
            }
            pinpu = pinpu + (int) (new File(filename).length());
        }
        pinpulength = getSize(pinpu);

        int pinduan = 0;
        for (String filename : pinduanAll) {
            if (pinduanTime < new File(filename).lastModified()) {
                pinduanTime = new File(filename).lastModified();
                pinduannewFile = filename;
            }
            pinduan = pinduan + (int) (new File(filename).length());
        }
        pinduanlength = getSize(pinduan);

        int yinpin = 0;
        for (String filename : musicAll) {
            if (musicTime < new File(filename).lastModified()) {
                musicTime = new File(filename).lastModified();
                musicnewFile = filename;
            }
            yinpin = yinpin + (int) (new File(filename).length());
        }
        yinpinlength = getSize(yinpin);
        weekLayout.startWeek(FileOsImpl.forSaveFloder);
        refreshView();
    }

    private void refreshView() {
        danpinSize.setText("共" + danpinAll.size() + "条数据");
        pinpuSize.setText("共" + pinpuAll.size() + "条数据");
        pinduanSize.setText("共" + pinduanAll.size() + "条数据");
        yinpinSize.setText("共" + musicAll.size() + "条数据");

        danpinMem.setText("共占用" + danpinlength + "空间");
        pinpuMem.setText("共占用" + pinpulength + "空间");
        pinduanMem.setText("共占用" + pinduanlength + "空间");
        yinpinMem.setText("共占用" + yinpinlength + "空间");

        danpinNew.setText("最新：" + new File(danpinnewFile).getName());
        pinpuNew.setText("最新：" + new File(pinpunewFile).getName());
        pinduanNew.setText("最新：" + new File(pinduannewFile).getName());
        yinpinNew.setText("最新：" + new File(musicnewFile).getName());

        list = new ArrayList<>();
        stringList = new ArrayList<>();
        if (danpinAll.size() != 0) {
            stringList.add("单频测向");
            list.add(danpinAll.size());
        }
        if (pinpuAll.size() != 0) {
            stringList.add("频谱分析");
            list.add(pinpuAll.size());
        }
        if (pinduanAll.size() != 0) {
            stringList.add("频段扫描");
            list.add(pinduanAll.size());
        }
        if (musicAll.size() != 0) {
            stringList.add("音频回放");
            list.add(musicAll.size());
        }
        pieLineView.setList(list, stringList);
    }

    public String getSize(long size) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        long m = size;
        while (size / 1024 > 0) {
            i++;
            m = size % 1024;
            size = size / 1024;
        }
        switch (i) {
            case 0:
                stringBuilder.append(size);
                stringBuilder.append(" ");
                stringBuilder.append("B");
                break;
            case 1:
                stringBuilder.append(size);
                stringBuilder.append(".");
                Format f1 = new DecimalFormat("000");
                String ss = f1.format(m);
                stringBuilder.append(ss);
                stringBuilder.append(" ");
                stringBuilder.append("KB");
                break;
            case 2:
                stringBuilder.append(size);
                stringBuilder.append(".");
                Format f2 = new DecimalFormat("000");
                String s2 = f2.format(m);
                stringBuilder.append(s2);
                stringBuilder.append(" ");
                stringBuilder.append("MB");
                break;
            case 3:
                stringBuilder.append(size);
                stringBuilder.append(".");
                Format f3 = new DecimalFormat("000");
                String s3 = f3.format(m);
                stringBuilder.append(s3);
                stringBuilder.append(" ");
                stringBuilder.append("GB");
                break;
        }
        return String.valueOf(stringBuilder);
    }

}
