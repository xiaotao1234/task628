package com.huari.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Parameter;
import com.huari.tools.SysApplication;
import com.huari.ui.ItemView;


import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import static com.huari.tools.SysApplication.currentLogic;
import static com.huari.tools.SysApplication.currentLogicMap;


@SuppressLint("NewApi")
public class MscanSetActivity extends AppCompatActivity implements
        OnClickListener, OnPageChangeListener {
    ViewPager vp;
    List<View> mylist;
    View ger, adv;
    LinearLayout gerLinearLayout, advLinearLayout;
    int offset, displaywidth, barwidth;
    ImageView imageview;
    int currentpage;
    TextView normaltextview, advancedtextview;

//    String[] namesofitems, advanceditems, generalparent, generaletdata,
//            advancedparent;// 每个设置选项的名字,常规、高级
//    private int generalindex;

    Dialog dialog;
    float curval;
    int curlist_no;
    boolean isempty = true;
    int choose_no = -1;
    ArrayList<String> gBigList, aBigList;
    String logicId;
    String stationName, deviceName, stationId;

    LogicParameter tmplogic;


    HashMap<String, String> oldValues;
    HashMap<String, String> newValues;
    View titlebar;
    ActionBar actionbar;
    public LogicParameter currentLogic = null;
    public LogicParameter tmpLogic = null;
    public ArrayList<Parameter> parameterlist = new ArrayList<>();
    String deviceNowName = null;
    float max, min;
    boolean checkoutNum = true;

    // boolean haveMenu;//是否有菜单设置的更改发生

    @SuppressLint("NewApi")
    class MyTextWatcher implements TextWatcher {
        String key;
        int itemno;

        public MyTextWatcher(String s, int no) {
            key = s;
            itemno = no;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals("") || (Float.parseFloat(s.toString()) < min || Float.parseFloat(s.toString()) > max)) {
                checkoutNum = false;
            } else {
                checkoutNum = true;
            }
            String m = s.toString().trim();
            if (newValues.containsKey(key)) {
                newValues.remove(key);
            }
            newValues.put(key, m);
            Parameter p = currentLogic.parameterlist.get(itemno);
            if (p != null && checkoutNum == true)
                p.defaultValue = newValues.get(p.name);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mscansetactivity);
        SysApplication.getInstance().addActivity(this);
        Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        vp = findViewById(R.id.mscangeneralandadvancedset);
        imageview = findViewById(R.id.mscanmenubar);
        ger = getLayoutInflater().inflate(R.layout.generalset, null);
        adv = getLayoutInflater().inflate(R.layout.advancedset, null);
        gerLinearLayout = ger.findViewById(R.id.myppfxgeneral);
        advLinearLayout = adv.findViewById(R.id.myppfxadvanced);
        normaltextview = findViewById(R.id.mscannormal);
        advancedtextview = findViewById(R.id.mscanadvanced);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        displaywidth = metric.widthPixels;
        barwidth = displaywidth / 6 * 2;
        offset = displaywidth / 6;
        mylist = new ArrayList<>();
        mylist.add(ger);
        mylist.add(adv);
        vp.setAdapter(new MyPagerAdapter(mylist));

        normaltextview.setOnClickListener(this);
        advancedtextview.setOnClickListener(this);

        gBigList = new ArrayList<>();
        aBigList = new ArrayList<>();

        oldValues = new HashMap<>();
        newValues = new HashMap<>();

        Intent intent = getIntent();
        Bundle mybundle = intent.getExtras();
        stationName = mybundle.getString("sname");// 该设置界面所属于的设备的名字、台站的名字、台站的id、逻辑数据的id
        deviceName = mybundle.getString("dname");
        stationId = mybundle.getString("stakey");
        logicId = mybundle.getString("lids");

        MyDevice iDevice = null;
        for (MyDevice myd : GlobalData.stationHashMap.get(stationId).showdevicelist) {
            if (myd.name.equals(deviceName)) {
                iDevice = myd;
                deviceNowName = iDevice.name;
            }
        }
        currentLogic = currentLogicMap.get(deviceNowName);
        if (currentLogic != null) {
            isempty = false;
        } else {
            currentLogic = new LogicParameter();
            currentLogicMap.put(deviceNowName, currentLogic);
        }
        tmpLogic = new LogicParameter();
        currentLogicMap.get(deviceNowName);
        tmplogic = iDevice.logic.get(logicId);
        if (!GlobalData.mscanset)
            for (Parameter p : tmplogic.parameterlist) {
                Parameter pp = new Parameter();
                pp.name = p.name;
                pp.dispname = p.dispname;
                pp.defaultValue = p.getDefaultValue();
                pp.displayType = p.displayType;
                pp.maxValue = p.maxValue;
                pp.minValue = p.minValue;
                pp.isEditable = p.isEditable;
                pp.isAdvanced = p.isAdvanced;
                pp.enumValues = p.getEnumValues();
                if (isempty) {
                    if (pp.name.contains("Mem"))
                        currentLogic.parameterlist.add(pp);
                }
                GlobalData.mscanset = true;
            }
        else {
//            for (Parameter p : GlobalData.mscan_parameterlist)
//                currentLogic.parameterlist.add(p);
        }

        loadparameters();
        dialog = new AlertDialog.Builder(MscanSetActivity.this)
                .setTitle("确定要保存参数更改吗？")
                .setNegativeButton("取消", (arg0, arg1) -> {
                    loadparameters();
                    MscanSetActivity.this.finish();
                })
                .setPositiveButton("确定", (arg0, arg1) -> {
                    boolean b = refreshParameters();
                    if (b) {
                        MScanActivity.handler.sendEmptyMessage(MScanActivity.PARAMETERREFRESH);
                        MscanSetActivity.this.finish();
                    } else {

                    }
                }).create();
        try {
            for (Parameter parameter : currentLogic.parameterlist) {
                Parameter parameter1 = new Parameter();
                parameter1 = (Parameter) parameter.clone();
                parameterlist.add(parameter1);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void loadparameters() {
        try {
            gerLinearLayout.removeAllViewsInLayout();
        } catch (Exception e) {

        }
        try {
            advLinearLayout.removeAllViewsInLayout();
        } catch (Exception e) {

        }
        for (Parameter p : currentLogic.parameterlist) {
            if (p.isAdvanced == 0) {
                if (!aBigList.contains(p.displayType)) {
                    aBigList.add(p.displayType);
                }
            } else if (p.name.contains("Mem")) {
                if (!gBigList.contains(p.displayType)) {
                    gBigList.add(p.displayType);
                }
            }
            if (p.name.contains("CenterFreq")) {
                max = p.getMaxValue();
                min = p.getMinValue();
            }
        }

        for (String s : gBigList) {
            Log.d("gBigList size", String.valueOf(gBigList.size()));
            LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.mscan_parameter, null);
            TextView tv = ll.findViewById(R.id.bigparamtertextview);
            RelativeLayout btn = ll.findViewById(R.id.button);
            btn.setOnClickListener(this);

            tv.setText("频点设置");
            gerLinearLayout.addView(ll);// 将大类参数显示在界面上，下面开始加载附挂到它下面的子选项

            int list_no = 0;

            for (Parameter p : currentLogic.parameterlist) {

                if (p.displayType.equals(s) && p.isEditable == 1
                        && p.isAdvanced == 1) {
                    ItemView ly = (ItemView) getLayoutInflater()
                            .inflate(R.layout.ppfxchildblue, null);
//                    ly.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    ly.setDeleteOwn(view -> {
                        try {
                            choose_no = (int) (view.getTag());
//                            ly.setBackgroundColor(Color.parseColor("#777777"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, list_no);
                    ly.setTag(list_no);
                    TextView textiew = ly.findViewById(R.id.exchilditemtv);
                    textiew.setTextColor(Color.parseColor("#FFFFFF"));
                    EditText ext = ly.findViewById(R.id.exchilditemet);
                    ext.setTextColor(Color.parseColor("#FFFFFF"));
                    ext.addTextChangedListener(new MyTextWatcher(p.name, list_no));
                    ext.setText(p.defaultValue);
                    textiew.setText(p.dispname);
                    if (!p.dispname.trim().equals("中心频率")) {
                        ly.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        textiew.setTextColor(Color.parseColor("#000000"));
                        ext.setTextColor(Color.parseColor("#000000"));
                    }
                    ly.setOnClickListener(v -> Log.d("xiaoindex1", String.valueOf(ly.getTag())));
                    gerLinearLayout.addView(ly, LinearLayout.LayoutParams.MATCH_PARENT, 150);
                } else if (p.displayType.equals(s) && p.isEditable == 0
                        && p.isAdvanced == 1) {
                    LinearLayout ly = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.ppfxwithspinner, null);
                    TextView tiew = ly.findViewById(R.id.exchilditemttv);
                    final Spinner sp = ly.findViewById(R.id.exchildsp);
                    ly.setTag(list_no);
                    ly.setOnClickListener(v -> {
                        Log.d("lyid", String.valueOf(ly.getTag()));
                        choose_no = (int) (ly.getTag());
                        int ms = GlobalData.tmpparameterlist.size();
                        for (int i = 0; i < gerLinearLayout.getChildCount(); i++) {
                            if (gerLinearLayout.getChildAt(i).getTag() != null && ((int) gerLinearLayout.getChildAt(i).getTag()) / ms == (int) ly.getTag() / ms) {
                                gerLinearLayout.getChildAt(i).setBackgroundColor(Color.parseColor("#2b4490"));
                            } else {
                                if (i != 0) {
                                    gerLinearLayout.getChildAt(i).setBackgroundColor(Color.parseColor("#777777"));
                                }
                            }
                        }
                    });
                    if (p.enumValues != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MscanSetActivity.this,
                                R.layout.customspinnertextview, p.enumValues);
                        final String[] temp = p.enumValues;
                        final String tempname = p.name;

                        int sp_no = list_no;

                        sp.setAdapter(adapter);
                        int temps = 0;
                        for (String st : p.enumValues) {
                            if (p.defaultValue.trim().equals(st)) {
                                break;
                            } else {
                                temps++;
                            }
                        }
                        if (temps == p.enumValues.length)
                            temps = 0;
                        sp.setSelection(temps);
                        sp.setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                String value = sp.getSelectedItem().toString();
                                if (newValues.containsKey(tempname)) {
                                    newValues.remove(tempname);
                                }
                                newValues.put(tempname, value);

                                Parameter p = currentLogic.parameterlist.get(sp_no);
                                if (p != null)
                                    p.defaultValue = newValues.get(p.name);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                    tiew.setText(p.dispname);
                    gerLinearLayout.addView(ly, LinearLayout.LayoutParams.MATCH_PARENT, 100);
                }
                list_no++;
//                if (list_no > gBigList.size() && list_no % gBigList.size() == 0) {
//                    View view = LayoutInflater.from(this).inflate(R.layout.separate, null, false);
//                    View view1 = view.findViewById(R.id.separa);
//                    gerLinearLayout.addView(view1, LinearLayout.LayoutParams.MATCH_PARENT, 40);
//                }

            }
            choose_no = -1;
        }


        for (String s : aBigList) {
            LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.big_parameter, null);
            TextView tv = ll.findViewById(R.id.bigparamtertextview);
            tv.setText("高级设置");
            advLinearLayout.addView(ll);// 将大类参数显示在界面上，下面开始加载附挂到它下面的子选项
            int list_no = 0;
            for (Parameter p : currentLogic.parameterlist) {
                if (p.displayType.equals(s) && p.isEditable == 1
                        && p.isAdvanced == 0) {
                    LinearLayout ly = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.ppfxchildblue, null);
                    TextView tiew = ly
                            .findViewById(R.id.exchilditemtv);
                    EditText ext = ly
                            .findViewById(R.id.exchilditemet);
                    ext.addTextChangedListener(new MyTextWatcher(p.name, list_no));

                    tiew.setText(p.name);
                    gerLinearLayout.addView(ly);
                } else if (p.displayType.equals(s) && p.isEditable == 0
                        && p.isAdvanced == 0) {
                    LinearLayout ly = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.ppfxwithspinner, null);
                    TextView tiew = ly
                            .findViewById(R.id.exchilditemttv);
                    final Spinner sp = ly
                            .findViewById(R.id.exchildsp);
                    Spinner tempsp = sp;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            MscanSetActivity.this,
                            R.layout.customspinnertextview, p.enumValues);
                    final String[] temp = p.enumValues;
                    final String tempname = p.name;
                    sp.setAdapter(adapter);
                    int temps = 0;
                    for (String st : p.enumValues) {
                        if (p.defaultValue.trim().equals(st.trim())) {
                            break;
                        } else {
                            temps++;
                        }
                    }
                    if (temps == p.enumValues.length)
                        temps = 0;
                    sp.setSelection(temps);
                    sp.setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {
                            String value = sp.getSelectedItem().toString();
                            if (newValues.containsKey(tempname)) {
                                newValues.remove(tempname);
                            }
                            newValues.put(tempname, value);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    tiew.setText(p.dispname);
                    advLinearLayout.addView(ly);
                }
                list_no++;
            }
        }
    }

    private void add_fun() {
        float val;

        for (Parameter p : currentLogic.parameterlist) {
            if (p.name.contains("CenterFreq")) {
                val = Float.parseFloat(p.getDefaultValue());
                if (val > curval)
                    curval = val;
            }
        }

        for (Parameter p : GlobalData.tmpparameterlist) {
            if (p.name.contains("CenterFreq")) {
                curval = curval + 1;
                p.setDefaultValue(String.valueOf(curval));
            }

            Parameter pp = new Parameter();
            pp.name = p.name;
            pp.dispname = p.dispname;
            pp.defaultValue = p.getDefaultValue();
            pp.displayType = p.displayType;
            pp.maxValue = p.maxValue;
            pp.minValue = p.minValue;
            pp.isEditable = p.isEditable;
            pp.isAdvanced = p.isAdvanced;
            pp.enumValues = p.getEnumValues();

            currentLogic.parameterlist.add(pp);
        }

        loadparameters();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            dialog.show();
        }
        return true;
    }

    private boolean refreshParameters()// 更新该设备的参数
    {
        try {
            if (!checkoutNum) {
                throw new Exception();
            }
            GlobalData.mscan_parameterlist.clear();
            for (Parameter p : currentLogic.parameterlist) {
                Parameter pp = new Parameter();
                pp.name = p.name;
                pp.dispname = p.dispname;
                pp.defaultValue = p.getDefaultValue();
                pp.displayType = p.displayType;
                pp.maxValue = p.maxValue;
                pp.minValue = p.minValue;
                pp.isEditable = p.isEditable;
                pp.isAdvanced = p.isAdvanced;
                pp.enumValues = p.getEnumValues();
                GlobalData.mscan_parameterlist.add(pp);
            }
            return true;
        } catch (Exception ex) {
            checkoutNum = true;
            currentLogic.parameterlist = parameterlist;
            Toast.makeText(MscanSetActivity.this,
                    "有参数输入不在正确范畴或格式不正确，请检查并重新输入", Toast.LENGTH_SHORT).show();
            loadparameters();
            return false;
        }
    }

    private void mscan_input() {
        CustomDialog customDialog = new CustomDialog(this);
        customDialog.setTitle("   ");

        customDialog.setCancel("取消", dialog -> {
            if (choose_no != -1) {
                loadparameters();
                Toast.makeText(MscanSetActivity.this, "取消成功！", Toast.LENGTH_SHORT).show();
            }
        });

        customDialog.setAdd("添加", dialog -> {
            add_fun();
            Toast.makeText(MscanSetActivity.this, "添加成功！", Toast.LENGTH_SHORT).show();
        });

        customDialog.setDelete("删除", dialog -> {
            int rt = delete_fun();
            switch (rt) {
                case 1:
                    Toast.makeText(MscanSetActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(MscanSetActivity.this, "请在选中频点后再进行删除操作", Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(MscanSetActivity.this, "不能删除该项，至少要保留一组数据", Toast.LENGTH_SHORT).show();
                    break;
            }
            loadparameters();
        });

        customDialog.show();
    }

    private int delete_fun() {
        if (choose_no == -1) {
            return 0;
        }
        if (currentLogic.parameterlist.size() <= GlobalData.tmpparameterlist.size())
            return -2;                 //保留一项以免全删

        int ret = 0;

        int delete_no = choose_no;

        try {
            Parameter p = currentLogic.parameterlist.get(delete_no);
            if (p.name.contains("MemCenter")) {
                for (int i = 0; i < GlobalData.tmpparameterlist.size(); i++)
                    currentLogic.parameterlist.remove(delete_no);
            } else {
                while (!(p.name.contains("MemCenter"))) {
                    delete_no -= 1;
                    p = currentLogic.parameterlist.get(delete_no);
                }
                for (int i = 0; i < GlobalData.tmpparameterlist.size(); i++)
                    currentLogic.parameterlist.remove(delete_no);
            }

//            loadparameters();
            choose_no = -1;
            return 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    class MyPagerAdapter extends PagerAdapter {
        List<View> list;

        public MyPagerAdapter(List<View> l) {
            list = l;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "我的标题";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(list.get(position), 0);
            return list.get(position);
            // return super.instantiateItem(container, position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        int single = displaywidth / 2;
        TranslateAnimation ta = new TranslateAnimation(currentpage * single,
                single * arg0, 0, 0);
        ta.setFillAfter(true);
        ta.setDuration(200);
        imageview.startAnimation(ta);
        currentpage = arg0;
    }

    @Override
    public void onClick(View v) {
        int single = displaywidth / 2;
        if (v.getId() == R.id.mscannormal) {
            vp.setCurrentItem(0);
            if (currentpage != 0) {
                TranslateAnimation ta = new TranslateAnimation(currentpage
                        * single, 0, 0, 0);
                ta.setFillAfter(true);
                ta.setDuration(200);
                imageview.startAnimation(ta);
            }
            currentpage = 0;
        }
        if (v.getId() == R.id.mscanadvanced) {
            vp.setCurrentItem(1);
            if (currentpage != 1) {
                TranslateAnimation ta = new TranslateAnimation(currentpage
                        * single, single, 0, 0);
                ta.setFillAfter(true);
                ta.setDuration(200);
                imageview.startAnimation(ta);
            }
            currentpage = 1;
        }

        if (v.getId() == R.id.button) {
            mscan_input();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog.show();
        }
        return true;
    }

}


