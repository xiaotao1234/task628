package com.huari.client;

import androidx.appcompat.app.AppCompatActivity;
import struct.JavaStruct;
import struct.StructException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.huari.commandstruct.UnManStationRequest;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.UnManStation;
import com.huari.service.MainService;
import com.huari.tools.MyTools;
import com.huari.ui.CirclePainView;
import com.huari.ui.LinView;
import com.huari.ui.SwitchButton;

public class DzActivity extends AppCompatActivity {
    CirclePainView dlCircleView;
    CirclePainView voltmeterView;
    LinView outtemperatureView;
    LinView temperatureView;
    LinView outhumidityView;
    LinView humidityView;
    SwitchButton sbCustom0;
    SwitchButton sbCustom1;
    SwitchButton sbCustom2;
    SwitchButton sbCustom3;
    View view0;
    View view1;
    View view2;
    View view3;
    TextView t0;
    TextView t1;
    TextView t2;
    TextView t3;
    ImageView back;
    boolean isRequestOFF;
    ImageView upButton;
    LinearLayout linearLayout;
    LinearLayout smallLayout;
    LinearLayout lightLayout;
    LinearLayout airLayout;

    TextView InformationText;
    boolean b0 = false;
    boolean b1 = false;
    boolean b2 = false;
    boolean b3 = false;
    String[] s = {"OFF", "ON"};
    public static int UNMANDATA = 0x9;
    boolean downOrUp;
    private MapStatusUpdate u;
    BaiduMap mBaiduMap;
    boolean first = true;
    public static int ampereValue, voltmeterValue, temperature, humidity, outtemperature, outhumidity;
    RelativeLayout load_layouty;
    public static Handler handler;
    private LinearLayout containLayout;
    private MapView mapView;
    private String key;
    private String info = "";
    private double lan;
    private double lon;
    private boolean firstin = true;
    private UnManStation unManStation = null;
    private String copyinfo;
    private float temperatureFloat;
    private float voltmeterValueFloat;
    private float ampereValueFloat;
    private float outhumidityFloat;
    private float humidityFloat;
    private float outtemperatureFloat;
    private String s1;
    private boolean s2;
    private boolean s3;
    private boolean s4;
    private boolean s5;
    private boolean s6;
    private boolean s7;
    private boolean s8;
    private boolean s9;
    private long l;
    private int last_y;
    private int off_y;
    private int viewHeight;
    private int down_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            key = bundle.getString("name");
            lon = bundle.getFloat("lon");
            lan = bundle.getFloat("lan");
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == UNMANDATA) {
                    Log.d("xiaotimei", String.valueOf(System.currentTimeMillis() - l));
                    l = System.currentTimeMillis();
                    data();
                    updataView();
                }
                if (msg.what == 12) {
                    linearLayout.setTranslationY(linearLayout.getTranslationY() + (int) msg.obj);
                }
            }
        };
        setContentView(R.layout.activity_dz);
        initView();
    }

    private void data() {
        if (key != null && key.length() > 0) {
            for (String s : GlobalData.unmanHashMap.keySet()) {
                if (GlobalData.unmanHashMap.get(s).name.equals(key)) {
                    unManStation = GlobalData.unmanHashMap.get(s);
                    break;
                }
            }
        }
        if (unManStation != null && !unManStation.info.equals("")) {
            info = unManStation.info;
            copyinfo = info;
            s1 = CharUtil("温度：（室内）", "度");
            temperatureFloat = Float.parseFloat(s1);
            temperature = Integer.parseInt(s1.substring(0, s1.indexOf(".")));

            s1 = CharUtil("（室外）", "度");
            outtemperatureFloat = Float.parseFloat(s1);
            outtemperature = Integer.parseInt(s1.substring(0, s1.indexOf(".")));

            s1 = CharUtil("湿度：（室内）", "%");
            humidityFloat = Float.parseFloat(s1);
            humidity = Integer.parseInt(s1.substring(0, s1.indexOf(".")));

            s1 = CharUtil("（室外）", "%");
            outhumidityFloat = Float.parseFloat(s1);
            outhumidity = Integer.parseInt(s1.substring(0, s1.indexOf(".")));

            s1 = CharUtil("电压：", "伏");
            ampereValueFloat = Float.parseFloat(s1);
            ampereValue = Integer.parseInt(s1.substring(0, s1.indexOf(".")));

            s1 = CharUtil("电流：", "安");
            if (s1 != null) {
                voltmeterValueFloat = Float.parseFloat(s1);
                voltmeterValue = (int) (voltmeterValueFloat * 1000);
            }
//            voltmeterValue = Integer.parseInt(s1.substring(0, s1.indexOf(".")));
            Log.d("xiaotem", temperature + " " + outtemperature + " " + humidity + " " + outhumidity + " " + ampereValue + " " + voltmeterValue);
            s6 = (CharUtil("计算机：","，").equals("打开"));
            s2 = (CharUtil("浸水：", "，").equals("正常"));
            s3 = (CharUtil("门禁：", "，").equals("正常"));
            s4 = (CharUtil("烟雾：", "，").equals("正常"));
            s5 = (CharUtil("移动：", "，").equals("正常"));

//            if(s6&&s7&&s8){
////                s9 = false;//false为关闭状态
////            }
            copyinfo = info;
        }
    }

    public String CharUtil(String before, String after) {
        int begin = copyinfo.indexOf(before) + before.length();
        if (copyinfo.length() > begin) {
            int end = copyinfo.substring(begin).indexOf(after) - after.length();
            String s = copyinfo.substring(begin, begin + end + 1);
            copyinfo = copyinfo.substring(begin + end + 1);
            return s;
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("xiaorere", "onresume");
        mapView.onResume();
        LatLng ll = new LatLng(lon, lan);
        ll = GPStoBD09LL(ll);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.station_unman_map);
        OverlayOptions option = new MarkerOptions()
                .position(ll)
                .icon(bitmap);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mBaiduMap.setMapStatus(u);
        mBaiduMap.addOverlay(option);
    }

    private void initView() {
        dlCircleView = findViewById(R.id.ampere);
        voltmeterView = findViewById(R.id.voltmeter);
        outtemperatureView = findViewById(R.id.temperature);
        temperatureView = findViewById(R.id.temperature1);
        outhumidityView = findViewById(R.id.humidity);
        humidityView = findViewById(R.id.humidity11);
        sbCustom0 = findViewById(R.id.sb_custom0);
        sbCustom1 = findViewById(R.id.sb_custom1);
        sbCustom2 = findViewById(R.id.sb_custom2);
        sbCustom3 = findViewById(R.id.sb_custom3);
        sbCustom0.setClickable(false);
        sbCustom1.setClickable(false);
        sbCustom2.setClickable(false);
        sbCustom3.setClickable(false);
        view0 = findViewById(R.id.js);
        view1 = findViewById(R.id.mj);
        view2 = findViewById(R.id.yw);
        view3 = findViewById(R.id.yd);
        t0 = findViewById(R.id.sb_text0);
        t1 = findViewById(R.id.sb_text1);
        t2 = findViewById(R.id.sb_text2);
        t3 = findViewById(R.id.sb_text3);
        back = findViewById(R.id.back);
        load_layouty = findViewById(R.id.load_layout);
        upButton = findViewById(R.id.map_data_up_down);
        upButton.setClickable(false);
        linearLayout = findViewById(R.id.big_data_map_layout);
        smallLayout = findViewById(R.id.data_map_layout);
        containLayout = findViewById(R.id.contain_layout);
        InformationText = findViewById(R.id.map_data_list);
        lightLayout = findViewById(R.id.light_layout);
        airLayout = findViewById(R.id.air_layout);
        mapView = findViewById(R.id.bmapsView);
        upButton.setOnClickListener(v -> onclick());
        dlCircleView.setSystemUiVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> finish());
        mBaiduMap = mapView.getMap();

//        upButton.setOnTouchListener((v, event) -> {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    down_y = (int) event.getY();
//                    last_y = (int) event.getY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    off_y = (int) (event.getY() - last_y);
//                    if (off_y > 2 || off_y < -2) {
//                        linearLayout.setTranslationY(linearLayout.getTranslationY() + off_y);
//                    }
//                    last_y = (int) event.getY();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if (event.getY() - down_y > viewHeight/2) {
//                        linearLayout.setTranslationY(viewHeight);
//                        upButton.setImageResource(R.drawable.pull_up);
//                    }
//                    if(event.getY() - down_y < -viewHeight/2){
//                        linearLayout.setTranslationY(smallLayout.getHeight() - containLayout.getHeight());
//                        upButton.setImageResource(R.drawable.pull_up);
//                    }
//                    break;
//                case MotionEvent.ACTION_CANCEL:
//                    if (event.getY() - down_y > viewHeight/2) {
//                        linearLayout.setTranslationY(viewHeight);
//                        upButton.setImageResource(R.drawable.pull_up);
//                    }
//                    if(event.getY() - down_y < -viewHeight/2){
//                        linearLayout.setTranslationY(smallLayout.getHeight() - containLayout.getHeight());
//                        upButton.setImageResource(R.drawable.pull_up);
//                    }
//                    break;
//            }
//            return true;
//        });
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewHeight = smallLayout.getHeight();
                linearLayout.setTranslationY(viewHeight);
                upButton.setImageResource(R.drawable.pull_up);
                linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        sbCustom0.setOnClickListener(v -> {
            if (unManStation != null && !unManStation.id.equals("")) {
                b0 = !b0;
                t0.setText(b0 == true ? s[1] : s[0]);
                SwitchButtonOnClickListener listener;
                if (sbCustom0.isChecked()) {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 75, (byte) 1, "NUll");
                    t0.setText("请求中");
                    isRequestOFF = true;
                } else {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 75, (byte) 0, "NULL");
                }
                listener.onClick();
            }
        });
        sbCustom1.setOnClickListener(v -> {
            if (unManStation != null && !unManStation.id.equals("")) {
                b1 = !b1;
                t1.setText(b1 == true ? s[1] : s[0]);
                SwitchButtonOnClickListener listener;
                if (sbCustom1.isChecked()) {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 76, (byte) 1, "NUll");
                } else {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 76, (byte) 0, "NULL");
                }
                listener.onClick();
            }
        });
        sbCustom2.setOnClickListener(v -> {
            b2 = !b2;
            t2.setText(b2 == true ? s[1] : s[0]);
            Log.d("xioa", String.valueOf(b0));
        });
        sbCustom3.setOnClickListener(v -> {
            b3 = !b3;
            t3.setText(b3 == true ? s[1] : s[0]);
            Log.d("xioa", String.valueOf(b0));
        });
    }

    private void onclick() {
        if (downOrUp == false) {
            linearLayout.setTranslationY(smallLayout.getHeight() - containLayout.getHeight());
            upButton.setImageResource(R.drawable.push_down);
            downOrUp = true;
        } else {
            downOrUp = false;
            int viewHeight = smallLayout.getHeight();
            linearLayout.setTranslationY(viewHeight);
            upButton.setImageResource(R.drawable.pull_up);
        }
    }

    boolean T0 = false, T1 = false, T2 = false, T3 = false;

    public void updataView() {
        if(load_layouty.getVisibility()==View.VISIBLE){
            load_layouty.setVisibility(View.GONE);
        }
        sbCustom0.setClickable(true);
        sbCustom1.setClickable(true);
        sbCustom2.setClickable(true);
        sbCustom3.setClickable(true);
        upButton.setClickable(true);
        if (!info.equals("")) {
            int dp_10 = (int) getResources().getDimension(R.dimen.dp_10);
            if (first == true) {
                first = false;
                b0 = true;
                b1 = true;
                t0.setText(s[1]);
                t1.setText(s[1]);
                sbCustom1.setChecked(true);
            }
            if(s6==false){
                sbCustom0.setChecked(false);
                t0.setText("OFF");
                isRequestOFF = false;
            }else {
                if(isRequestOFF!=true){
                    sbCustom0.setChecked(true);
                    t0.setText("ON");
                }
            }
            if (s2) {
                view0.setBackgroundResource(R.drawable.dz_top_bg_green);
            } else {
                view0.setBackgroundResource(R.drawable.dz_top_bg);
            }
            if (s3) {
                view1.setBackgroundResource(R.drawable.dz_top_bg_green);
            } else {
                view1.setBackgroundResource(R.drawable.dz_top_bg);
            }
            if (s4) {
                view2.setBackgroundResource(R.drawable.dz_top_bg_green);
            } else {
                view2.setBackgroundResource(R.drawable.dz_top_bg);
            }
            if (s5) {
                view3.setBackgroundResource(R.drawable.dz_top_bg_green);
            } else {
                view3.setBackgroundResource(R.drawable.dz_top_bg);
            }
            sbCustom0.setOnClickListener(v -> {
                b0 = !b0;
                t0.setText(b0 == true ? s[1] : s[0]);
                SwitchButtonOnClickListener listener;
                if (sbCustom0.isChecked()) {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 75, (byte) 0, "NUll");
                } else {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 75, (byte) 1, "NULL");
                    t0.setText("请求中");
                    isRequestOFF = true;
                }
                listener.onClick();
            });

            sbCustom1.setOnClickListener(v -> {
                b1 = !b1;
                t1.setText(b1 == true ? s[1] : s[0]);
                SwitchButtonOnClickListener listener;
                if (sbCustom1.isChecked()) {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 76, (byte) 0, "NUll");
                } else {
                    listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 76, (byte) 1, "NULL");
                }
                listener.onClick();
            });

            for (String s : unManStation.switcharray) {
                if (s.equals("灯")) {
                    lightLayout.setVisibility(View.VISIBLE);
                    if (T2 == false) {
                        if (CharUtil("灯：", "，").equals("关闭")) {
                            sbCustom2.setChecked(false);
                            t2.setText("OFF");
                        } else {
                            sbCustom2.setChecked(true);
                            t2.setText("ON");
                        }
                    }

                    sbCustom2.setOnClickListener(v -> {
                        t2.setText("请求中");
                        T2 = true;
                        sbCustom2.postDelayed(() -> T2 = false, 1000);
                        SwitchButtonOnClickListener listener;
                        if (sbCustom2.isChecked()) {
                            listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 78, (byte) 0, "灯");
                        } else {
                            listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 78, (byte) 1, "灯");
                        }
                        listener.onClick();
                    });
                }
                if (s.equals("空调")) {
                    airLayout.setVisibility(View.VISIBLE);
                    if (T3 == false) {
                        if (CharUtil("空调状态：", "；").equals("关闭")) {
                            sbCustom3.setChecked(false);
                            t3.setText("OFF");
                        } else {
                            sbCustom3.setChecked(true);
                            t3.setText("ON");
                        }
                    }

                    sbCustom3.setOnClickListener(v -> {
                        t3.setText("请求中");
                        T3 = true;
                        sbCustom3.postDelayed(() -> T3 = false, 1000);
                        SwitchButtonOnClickListener listener;
                        if (sbCustom3.isChecked()) {
                            listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 78, (byte) 0, "空调");
                        } else {
                            listener = new SwitchButtonOnClickListener(unManStation.id, (byte) 78, (byte) 1, "空调");
                        }
                        listener.onClick();
                    });
                }
            }
            InformationText.setText(unManStation.info);
            if (firstin == true) {
                InformationText.setText(unManStation.info);
                InformationText.setPadding(dp_10, dp_10, dp_10, dp_10);
                smallLayout.setBackgroundResource(R.drawable.lay_circle);
                firstin = false;
            }
            if (dlCircleView.getShowValue() != voltmeterValue) {
                dlCircleView.setShowValue(voltmeterValue);
            }
            if (voltmeterView.getShowValue() != ampereValue) {
                voltmeterView.setShowValue(ampereValue);
            }
            if (outtemperatureView.getValue() != outtemperature) {
                outtemperatureView.setValue(outtemperature);
            }
            if (temperatureView.getValue() != temperature) {
                temperatureView.setValue(temperature);
            }
            if (outhumidityView.getValue() != outhumidity) {
                outhumidityView.setValue(outhumidity);
            }
            if (humidityView.getValue() != humidity) {
                humidityView.setValue(humidity);
            }
        }
    }

    class SwitchButtonOnClickListener {
        byte[] request = null;
        UnManStationRequest usr;

        public SwitchButtonOnClickListener(String stationId, byte functionNum,
                                           byte onOroff, String switchName) {
            usr = new UnManStationRequest();
            usr.functionNum = functionNum;
            usr.stationid = MyTools.toCountString(stationId.trim(), 76)
                    .getBytes();
            usr.onoroff = onOroff;
            usr.switchname = switchName.getBytes();
            usr.framelength = 77 + usr.switchname.length;
            usr.length = usr.framelength + 5;
            try {
                request = JavaStruct.pack(usr);
                System.out.println(functionNum + "   唤醒、关闭或强制关闭命令组装完成");
            } catch (StructException e) {
                e.printStackTrace();
                Log.i("转换为byte[]时", "发生了异常");
            }
        }

        public void onClick() {
            MainService.send(request);
            System.out.println(usr.functionNum + "   唤醒、关闭或强制关闭命令发送成功");
        }

    }

    public LatLng GPStoBD09LL(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();
    }

}

