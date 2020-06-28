package com.huari.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import struct.JavaStruct;
import struct.StructException;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
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

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MapActivity extends AppCompatActivity implements MKOfflineMapListener {

    MKOfflineMap offlinemap;
    ArrayList<MKOLUpdateElement> localMapList;
    MKOfflineMapListener mkoml = (arg0, arg1) -> {

    };
    LocationClient lcient;
    MyLocationListener listener = new MyLocationListener();
    //LocationMode lm;
    int saveCount;
    int m;        // 用以控制失败时重新发送请求任务命令的次数
    ActionBar actionbar;
    TextView head;
    TextView headTextDp;
    TextView headTextDu;
    MapView mMapView;
    BaiduMap mBaiduMap;
    //LocationMode locationMode;
    boolean isFirst = true;
    boolean follow = false;// 是否跟踪
    ArrayList<ArrayList<LatLng>> rootlist;
    ArrayList<LatLng> templist;
    ArrayList<Overlay> oldoverlayList;
    ArrayList<String[]> shixiangList;
    SharedPreferences share;
    SharedPreferences.Editor shareEdit;
    BitmapDescriptor bitMapDescriptor;
    BitmapDescriptor arrowMapDescriptor;
    HashMap<Marker, String[]> markerInfoMap;// String[]里保存的是台站的信息，用以台站被点击时展示。
    HashMap<String, String> markerIsShow;

    boolean show;

    // View view;
    // TextView name;
    // TextView jingdu;
    // TextView weidu;
    // TextView moreinfo;
    // TextView shebeiname;
    // TextView shixiangdu;
    // TextView dianping;
    // InfoWindow infowindow;

    // OverlayOptions textoptions;
    // LatLng stationLatLng;
    // Marker textmarker;

    PopupWindow pw;
    ListView maplistview;
    MapStationAdapter maplistAdatapter;
    ArrayList<String> stationlist;

    LayoutInflater inflater;
    HashMap<String, Marker> shixiangMarkerMap;

    public static Handler handler;
    public static int STATIONREFRESH = 1;
    public static int SHISHIHUAXIAN = 4;
    public static int HUAXIAN = 2;
    boolean runmyThread = true;
    String huaxianStationKey, huaxianStationName, huaxianDeviceName,
            huaxianLogicId;
    Marker shishixianMarker;
    Marker textMarker;

    MyThread my;
    MenuItem huaxianItem;

    String fromWhere;

    Socket s;
    InputStream ins = null;
    OutputStream os;
    private int cityId;
    private MapStatusUpdate u;
    private boolean flag = false;
    private float lan;
    private float lon;
    private GeoCoder geoCoder;
    private String cityNameFromLatLng;
    private LinearLayout parent;
    private int cityIdFromLatlng = 0;
    private ContentLoadingProgressBar contentLoadingProgressBar;
    private TextView progressText;
    private PopupWindow window;

    class IniThread extends Thread {
        public void run() {
            try {
                s = new Socket(GlobalData.mainIP, 5012);
                ins = s.getInputStream();
                os = s.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MapStationAdapter extends BaseAdapter {

        class Holder {
            CheckBox checkbox;
            Button bn;
        }

        @Override
        public int getCount() {
            return shixiangList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            final int index = position;
            LinearLayout linear;
            linear = (LinearLayout) inflater.inflate(R.layout.mapstationitem, null);
            CheckBox checkbox = linear
                    .findViewById(R.id.mapstationname);
            Button bn = linear.findViewById(R.id.mapcancle);
            final String[] strings = shixiangList.get(position);// 前缀（Key）、站名、纬度、经度、正北、相对、是否被选中、时间
            checkbox.setText(strings[1] + "(Lat:" + strings[2] + ",Lng:"
                    + strings[3] + ")");
            checkbox.setChecked(strings[6].equals("true"));

            checkbox.setOnCheckedChangeListener((arg0, arg113) -> {
                if (arg113) {
                    LatLng locationLatLng = new LatLng(Double
                            .parseDouble(strings[2]), Double
                            .parseDouble(strings[3]));
                    locationLatLng=GPStoBD09LL(locationLatLng);
                    OverlayOptions ooarrow = new MarkerOptions()
                            .position(locationLatLng)
                            .icon(arrowMapDescriptor).zIndex(9);
                    Marker markerarrow = (Marker) (mBaiduMap
                            .addOverlay(ooarrow));
                    markerarrow.setRotate(-Float.parseFloat(strings[4]));
                    shixiangMarkerMap.put(strings[0], markerarrow);
                } else {
                    Marker tempMarker = shixiangMarkerMap.get(strings[0]);
                    if (tempMarker != null) {
                        tempMarker.remove();
                        shixiangMarkerMap.remove(strings[0]);
                    }
                }
                strings[6] = Boolean.toString(arg113);
            });

            bn.setOnClickListener(arg0 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MapActivity.this);
                builder.setTitle("确定要删除吗？");
                builder.setPositiveButton("确定",
                        (arg012, arg112) -> {
                            shareEdit.remove(strings[0]);
                            shareEdit.commit();
                            shixiangList.remove(index);
                            MapStationAdapter.this
                                    .notifyDataSetChanged();
                            Marker tempMarker = shixiangMarkerMap
                                    .get(strings[0]);
                            if (tempMarker != null) {
                                tempMarker.remove();
                                shixiangMarkerMap.remove(strings[0]);
                            }
                        });
                builder.setNegativeButton("取消",
                        (arg01, arg11) -> {

                        });
                builder.create();
                builder.show();
            });

            return linear;
        }

    }

    public static LatLng GPStoBD09LL(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    public static LatLng GetLatLon(LatLng a, double distance, double angle) {
        double dx = distance * 10 * Math.sin(angle * Math.PI / 180);
        double dy = distance * 10 * Math.cos(angle * Math.PI / 180);

        double lon = (dx / Ed(a.latitude) + RadLon(a.longitude)) * 180 / Math.PI;
        double lat = (dy / Ec(a.latitude) + RadLat(a.latitude)) * 180 / Math.PI;

        LatLng b = new LatLng(lat, lon);
        return b;
    }

    public static double RadLon(double lon) {
        return lon * Math.PI / 180;
    }

    public static double RadLat(double lat) {
        return lat * Math.PI / 180;
    }

    /// <summary>
    /// 赤道半径 earth radius
    /// </summary>
    public static double EARTH_RADIUS = 6378137;
    /// <summary>
    /// 极半径 polar radius
    /// </summary>
    public static double POLAR_RADIUS = 6356725;

    public static double Ec(double lat) {
        return POLAR_RADIUS + (EARTH_RADIUS - POLAR_RADIUS) * (90 - lat) / 90;
    }

    boolean firstIn = true;

    public static double Ed(double lat) {
        return Ec(lat) * Math.cos(RadLat(lat));
    }

    Overlay mPolyline = null;

    //    Handler handlersx = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
////            if(mPolyline!=null){
////                mPolyline.remove();
////            }
//            LatLng ll = new LatLng(lan, lon);
//            ll = GPStoBD09LL(ll);
//            if(firstIn==true){
//                u = MapStatusUpdateFactory.newLatLng(ll);
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.zoom(18.0f);
//                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//                mBaiduMap.animateMapStatus(u);
//                firstIn = false;
//            }
////            LatLng l2 =  GetLatLon(ll,100,GlobalData.xiangdui);
////            Log.d("xiaomap", String.valueOf(GlobalData.xiangdui));
////            List<LatLng> points = new ArrayList<>();
////            points.add(ll);
////            points.add(l2);
////            OverlayOptions mOverlayOptions = new PolylineOptions()
////                    .width(10)
////                    .color(0xAAFF0000)
////                    .points(points);
//////在地图上绘制折线
//////mPloyline 折线对象
////            mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
//        }
//    };
    class MyLocationListener implements BDLocationListener {  //定位成功的数据回调

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location == null || mMapView == null)
                return;
            double lat = location.getLatitude();
            double lon = location.getLongitude();
//            MyLocationData data = new MyLocationData.Builder()
//                    .accuracy(location.getRadius()).latitude(lat)
//                    .longitude(lon).build();
//            mBaiduMap.setMyLocationData(data);
//            LatLng ll = new LatLng(30.6342387, 103.9749870);
//            ll = GPStoBD09LL(ll);
//            u = MapStatusUpdateFactory.newLatLng(ll);
//            MapStatus.Builder builder = new MapStatus.Builder();
//            builder.zoom(18.0f);
//            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//            mBaiduMap.animateMapStatus(u);
//            LatLng l2 =  GetLatLon(ll,100,GlobalData.xiangdui);
//            List<LatLng> points = new ArrayList<>();
//            points.add(ll);
//            points.add(l2);
//            OverlayOptions mOverlayOptions = new PolylineOptions()
//                    .width(10)
//                    .color(0xAAFF0000)
//                    .points(points);
////在地图上绘制折线
////mPloyline 折线对象
//            Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);

            if (follow) {
                LatLng point = new LatLng(lat, lon);
                templist.add(point);
                if (templist.size() > 9999) {
                    if (!rootlist.contains(templist)) {
                        rootlist.add(templist);
                    }
                    templist = new ArrayList<>();
                }
                for (Overlay lay : oldoverlayList) {
                    lay.remove();
                }
                for (ArrayList<LatLng> list : rootlist) {
                    if (list.size() > 1) {
                        OverlayOptions ooPolyline = new PolylineOptions()
                                .width(4).color(0xAAFF0000).points(list);
                        Overlay old = mBaiduMap.addOverlay(ooPolyline);
                        oldoverlayList.add(old);
                    }
                }
            }
        }
    }

    private void iniStationInfo()// 重新加载一遍Station，也适用于台站的增减更新
    {
        synchronized (GlobalData.stationHashMap) {
            if (markerInfoMap != null) {
                for (Marker m : markerInfoMap.keySet()) {
                    m.remove();
                }
                markerInfoMap.clear();
            } else {
                markerInfoMap = new HashMap<>();
            }
            bitMapDescriptor = BitmapDescriptorFactory
                    .fromResource(R.drawable.stationmarker);

            for (String stationKey : GlobalData.stationHashMap.keySet()) {
                Station station = GlobalData.stationHashMap.get(stationKey);
                LatLng latlng = new LatLng(station.lan, station.lon);
                latlng = GPStoBD09LL(latlng);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latlng).icon(bitMapDescriptor).zIndex(9);

                Marker marker = (Marker) (mBaiduMap.addOverlay(markerOptions));
                marker.setZIndex(2);
                String is = "false";        // 是否在执行任务.0空闲，1工作。
                String isShow = markerIsShow.get(stationKey);
                if (isShow == null) {
                    isShow = "false";
                }
                for (MyDevice de : station.showdevicelist) {
                    if (de.isOccupied == 1) {
                        is = "true";
                        break;
                    }
                }
                String[] tempstrings = new String[]{isShow, station.name,
                        station.lan + "", station.lon + "", is, station.id};
                markerInfoMap.put(marker, tempstrings);
                System.out.println("Map又填充好了");
            }
        }
    }

    private void refreshMarker() {
        try {
            for (Marker markeritem : markerInfoMap.keySet()) {
                String[] t = markerInfoMap.get(markeritem);
                {
                    if (t[0].equals("true")) {
                        mBaiduMap.hideInfoWindow();
                        View view = getLayoutInflater().inflate(
                                R.layout.detailstationinfo, null);
                        TextView name = (TextView) view
                                .findViewById(R.id.stationname);
                        TextView jingdu = (TextView) view
                                .findViewById(R.id.jingdu);
                        TextView weidu = (TextView) view
                                .findViewById(R.id.weidu);
                        TextView moreinfo = (TextView) view
                                .findViewById(R.id.moreinfo);
                        TextView shebeiname = (TextView) view
                                .findViewById(R.id.ditushebei);
                        TextView shixiangdu = (TextView) view
                                .findViewById(R.id.shebeishixiangdu);
                        TextView dianping = (TextView) view
                                .findViewById(R.id.shebeidianping);
                        try {
                            dianping.setText("电平：" + GlobalData.DDFdianping
                                    + "");
                            shixiangdu.setText("示向度：" + GlobalData.xiangdui);
                            shebeiname.setText("设备（" + GlobalData.deviceName
                                    + ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        name.setText(t[1]);
                        jingdu.setText("经度：" + t[2]);
                        weidu.setText("纬度：" + t[3]);
                        if (t[4].equals("true")) {
                            moreinfo.setText("正在使用");
                        } else {
                            moreinfo.setText("空闲");
                        }

                        Point p = mBaiduMap.getProjection().toScreenLocation(
                                markeritem.getPosition());
                        p.y -= 25;
                        LatLng llInfo = mBaiduMap.getProjection()
                                .fromScreenLocation(p);
                        InfoWindow infowindow = new InfoWindow(view, llInfo, 0);
                        mBaiduMap.showInfoWindow(infowindow);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("对不起，隐藏Marker出现了异常，办不到了");
        }

    }

    private byte[] iRequestInfo(String stationKey, String logicId,
                                String devicename) {
        Station stationF = GlobalData.stationHashMap.get(stationKey);
        ArrayList<MyDevice> am = stationF.devicelist;
        ArrayList<Parameter> ap = null;
        HashMap<String, LogicParameter> hsl = null;
        for (MyDevice md : am) {
            if (md.name.equals(devicename)) {
                hsl = md.logic;
            }
        }
        LogicParameter currentLP = hsl.get(logicId);// 获取频谱分析相关的数据，以便画出初始界面
        ap = currentLP.parameterlist;
        byte[] request = null;
        PPFXRequest pr = null;
        try {
            pr = new PPFXRequest();
            // 帧体长度暂时跳过
            pr.functionNum = 18;
            pr.stationid = MyTools.toCountString(stationKey.trim(), 76)
                    .getBytes();
            pr.logicid = MyTools.toCountString(logicId.trim(), 76).getBytes();

            pr.devicename = MyTools.toCountString(devicename.trim(), 36)
                    .getBytes();
            pr.pinduancount = 0;
            pr.logictype = MyTools.toCountString("DDF", 16).getBytes();
            PinPuParameter[] parray = new PinPuParameter[ap.size() - 1];

            int z = 0;
            for (Parameter para : ap) {
                PinPuParameter pin = new PinPuParameter();
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

            pr.parameterslength = 72 * (ap.size() - 1);
            pr.length = pr.parameterslength + 247;
            pr.p = parray;
        } catch (Exception e) {
            // System.out.println("组装开始命令的方法中出现非空指针类异常");
        }

        try {
            request = JavaStruct.pack(pr);

        } catch (StructException e) {
            e.printStackTrace();
        }
        return request;
    }

    class MyThread extends Thread {
        String stationKey, logicId, devicename;
        int i = 0;

        public MyThread(String sk, String l, String d) {
            stationKey = sk;
            logicId = l;
            devicename = d;
        }

        private void sendStartCmd() {
            try {
                m++;
                byte[] bbb = iRequestInfo(stationKey, logicId, devicename);
                os.write(bbb);
                os.flush();
            } catch (NullPointerException e) {
                if (m < 10) {
                    sendStartCmd();
                } else {
                    Toast.makeText(MapActivity.this, "发送开始命令失败，请退出该功能并重试一次吧",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                // System.out.println("sendStartCmd内部发生了异常");
            }
        }

        private void sendEndCmd() {
            m = 0;
            StopTaskFrame st = new StopTaskFrame();
            st.functionNum = 46;
            st.length = 2;
            byte[] b;
            try {
                b = JavaStruct.pack(st);
                os.write(b);
                os.flush();
                // Log.i("发送了","停止命令");
            } catch (Exception e) {
                e.printStackTrace();
                // Log.i("停止","出问题了");
            }
        }

        public void run() {
            try {
                int available = 0;
                int segment = 0;
                byte[] info = null;
                while (available == 0 && runmyThread) {
                    if(ins != null ){
                        available = ins.available();
                        if (available > 0) {
                            try {
                                info = new byte[available];
                                ins.read(info);
                                Parse.parseDDF(info);
                            } catch (Exception e) {
                                // System.out.println("开始接收DDF数据，解析发生了异常，定位到DDF的Activity的225行");
                            }
                            available = 0;
                        }
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                } else {
                    Toast.makeText(this, "未授予相应权限，不能使用地图功能", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidumap);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        lan = bundle.getFloat("lan");
        lon = bundle.getFloat("lon");
        Log.d("xiaolan", String.valueOf(lan));
        Log.d("xiaolan", String.valueOf(lon));
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            start();
        }
        start();
    }

    private void start() {
        offlinemap = new MKOfflineMap();
        offlinemap.init(mkoml);
        ArrayList<MKOLSearchRecord> records = offlinemap.searchCity("北京");
        if (records != null && records.size() == 1) {
            cityId = records.get(0).cityID;
        }
        offlinemap.start(cityId);
        offlinemap.importOfflineData();
        localMapList = offlinemap.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<>();
        }

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.ditu);
        arrowMapDescriptor = BitmapDescriptorFactory
                .fromResource(R.drawable.arrow);
        inflater = LayoutInflater.from(MapActivity.this);

        head = (TextView) findViewById(R.id.maphuaxianinfo);
        // headTextDp=(TextView) head.findViewById(R.id.mapheaddianping);
        // headTextDu=(TextView) head.findViewById(R.id.mapheadshixiangdu);

        maplistview = (ListView) getLayoutInflater().inflate(
                R.layout.mapselectlistview, null);
        parent = findViewById(R.id.parent_layout);
        shixiangList = new ArrayList<String[]>();
        shixiangMarkerMap = new HashMap<String, Marker>();
        share = getSharedPreferences("myclient", MODE_PRIVATE);
        shareEdit = share.edit();
        saveCount = share.getInt("savecount", 0);
        for (int i = 0; i <= saveCount; i++) {
            String tempStrings = share.getString("prefixion" + i, null);
            if (tempStrings != null) {
                String[] ts = tempStrings.split(",");
                shixiangList.add(ts);
            }
        }

        maplistAdatapter = new MapStationAdapter();
        maplistview.setAdapter(maplistAdatapter);
        pw = new PopupWindow(maplistview,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        pw.setOutsideTouchable(true);

        maplistview.setOnKeyListener((arg0, arg1, arg2) -> {
            if (arg1 == KeyEvent.KEYCODE_BACK) {
                pw.dismiss();
                pw = null;
                return true;
            }
            return false;
        });

        templist = new ArrayList<>();
        rootlist = new ArrayList<>();
        rootlist.add(templist);
        oldoverlayList = new ArrayList<>();

        mMapView = findViewById(R.id.bmapsView);

        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMyLocationEnabled(true);
        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setRotateGesturesEnabled(false);
        lcient = new LocationClient(MapActivity.this);   //发起定位
        lcient.registerLocationListener(listener);
        LocationClientOption op = new LocationClientOption();
        op.setOpenGps(true);        // 打开gps
        op.setCoorType("bd09ll");    // 设置坐标类型
        op.setScanSpan(1000);
        lcient.setLocOption(op);
        lcient.start();
        markerIsShow = new HashMap<>();
        iniStationInfo();

        SysApplication.getInstance().addActivity(this);
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

        mBaiduMap.setOnMarkerClickListener(marker -> {
            try {
                String[] t = markerInfoMap.get(marker);
                if (t[0].equals("false")) {
                    View view = getLayoutInflater().inflate(
                            R.layout.detailstationinfo, null);
                    TextView name = view
                            .findViewById(R.id.stationname);
                    TextView jingdu = view
                            .findViewById(R.id.jingdu);
                    TextView weidu = (TextView) view
                            .findViewById(R.id.weidu);
                    TextView moreinfo = (TextView) view
                            .findViewById(R.id.moreinfo);
                    TextView shebeiname = (TextView) view
                            .findViewById(R.id.ditushebei);
                    TextView shixiangdu = (TextView) view
                            .findViewById(R.id.shebeishixiangdu);
                    TextView dianping = (TextView) view
                            .findViewById(R.id.shebeidianping);
                    try {
                        dianping.setText("电平：" + GlobalData.DDFdianping
                                + "");
                        shixiangdu.setText("示向度：" + GlobalData.xiangdui);
                        shebeiname.setText("设备（" + GlobalData.deviceName
                                + ")");
                    } catch (Exception e) {

                    }
                    name.setText(t[1]);
                    jingdu.setText("经度：" + t[2]);
                    weidu.setText("纬度：" + t[3]);
                    if (t[4].equals("true")) {
                        moreinfo.setText("正在使用");
                    } else {
                        moreinfo.setText("空闲");
                    }
                    Point p = mBaiduMap.getProjection().toScreenLocation(
                            marker.getPosition());
                    p.y -= 47;
                    LatLng llInfo = mBaiduMap.getProjection()
                            .fromScreenLocation(p);
                    InfoWindow infowindow = new InfoWindow(view, llInfo, 0);

                    mBaiduMap.showInfoWindow(infowindow);

                    t[0] = "true";
                    markerIsShow.put(t[5], "true");
                } else {
                    t[0] = "false";
                    mBaiduMap.hideInfoWindow();
                    markerIsShow.put(t[5], "false");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });

        try {
            new IniThread().start();
        } catch (Exception e3) {
            System.out.println("地图 inithread 初始化线程异常");
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == STATIONREFRESH) {
                    System.out.println("Maphandler收到消息");
                    iniStationInfo();
                    refreshMarker();
                } else if (msg.what == HUAXIAN) {
                    huaxianStationKey = GlobalData.stationKey;
                    huaxianLogicId = GlobalData.logicId;
                    huaxianDeviceName = GlobalData.deviceName;
                    // huaxianStationName=b.getString("sname");
                } else if (msg.what == SHISHIHUAXIAN) {
                    if (shishixianMarker != null) {
                        shishixianMarker.remove();
                    }
                    LatLng xian = null;
                    xian = new LatLng(
                            GlobalData.stationHashMap
                                    .get(GlobalData.stationKey).lan,
                            GlobalData.stationHashMap
                                    .get(GlobalData.stationKey).lon);
                    xian = GPStoBD09LL(xian);
                    OverlayOptions ooarrow = new MarkerOptions().position(xian)
                            .icon(arrowMapDescriptor).zIndex(9);
                    shishixianMarker = (Marker) (mBaiduMap.addOverlay(ooarrow));
                    shishixianMarker.setZIndex(1);
                    shishixianMarker.setRotate(-GlobalData.xiangdui);
                    head.setText("电平:"
                            + GlobalData.DDFdianping
                            + "  示向度:"
                            + GlobalData.xiangdui
                            + "   台站:"
                            + GlobalData.stationHashMap
                            .get(GlobalData.stationKey).name
                            + " 设备:"
                            + GlobalData.deviceName);
                }
            }
        };
        flag = true;
        LatLng ll = new LatLng(lan, lon);
//        ll = new LatLng()
        ll = GPStoBD09LL(ll);
        geoCoder = GeoCoder.newInstance();
//        latlngToAddress(ll);
        if (firstIn == true) {
            u = MapStatusUpdateFactory.newLatLng(ll);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.zoom(18.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            mBaiduMap.animateMapStatus(u);
            firstIn = false;
        }
//        Thread thread = new Thread(() -> {
//            while (flag){
////                Message message = Message.obtain();
////                int m = (int) (Math.random()*360);
////                message.obj = m;
//                handlersx.sendEmptyMessage(1);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
    }

    private void latlngToAddress(LatLng latlng) {
        // 设置反地理经纬度坐标,请求位置时,需要一个经纬度
        //设置地址或经纬度反编译后的监听,这里有两个回调方法,
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            //经纬度转换成地址
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MapActivity.this, "找不到该地的离线地图!", Toast.LENGTH_SHORT).show();
                }
                boolean find = false;
                cityNameFromLatLng = result.getAddress();
                cityIdFromLatlng = result.getCityCode();
                if (localMapList != null && localMapList.size() > 0) {
                    for (MKOLUpdateElement mkolUpdateElement : localMapList) {
                        if (mkolUpdateElement.cityName.equals(cityNameFromLatLng)) {
                            find = true;
                            break;
                        }
                    }
                }
                if (find == false) {
                    if (cityIdFromLatlng != 0)
                        popWindowDia();
                }
            }

            //把地址转换成经纬度
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                // 详细地址转换为经纬度
                String address = result.getAddress();
            }
        });
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
    }

    private void popWindowDia() {
        View popupView = MapActivity.this.getLayoutInflater().inflate(R.layout.map_popwindow, null);
        popupView.setPadding(50, 0, 50, 0);
        contentLoadingProgressBar = popupView.findViewById(R.id.progressbar);
        progressText = popupView.findViewById(R.id.progress_number);
        contentLoadingProgressBar.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.INVISIBLE);
        TextView oktext = popupView.findViewById(R.id.ok_button);
        TextView canceltext = popupView.findViewById(R.id.cancel_button);
        TextView textviewShow = popupView.findViewById(R.id.text_show);
        popupView.setPadding(50, 0, 50, 0);
        window = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT, true);
        window.setWidth((int) getResources().getDimension(R.dimen.dp_280));
        window.showAtLocation(parent, Gravity.CLIP_VERTICAL, 0, 0);
        window.setAnimationStyle(R.style.popup_window_anim);
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        canceltext.setText("取消");
        oktext.setText("下载");
        textviewShow.setText("你还没有下载当前城市的离线地图包，当前功能将受到严重影响，是否一键下载。（下载前请断开当前局域网，并连接可正常访问的网络）");
        canceltext.setOnClickListener(v -> window.dismiss());
        oktext.setOnClickListener(v -> {
            offlinemap.start(cityIdFromLatlng);
            progressText.setVisibility(View.VISIBLE);
            contentLoadingProgressBar.setProgress(View.VISIBLE);
        });
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = offlinemap.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
//                    stateView.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                    updateView();
                }
            }
            break;

            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineDownloadActivity", String.format("add offlinemap num:%d", state));
                break;

            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                break;

            default:
                break;
        }
    }

    private void updateView() {
        if (window.isShowing()) {
            MKOLUpdateElement mkolUpdateElement = offlinemap.getUpdateInfo(cityIdFromLatlng);
            contentLoadingProgressBar.setProgress(mkolUpdateElement.ratio);
            progressText.setText(mkolUpdateElement.ratio + " " + "%");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
//        huaxianItem = menu.findItem(R.id.shishishixiang);
//        huaxianItem.setTitle(GlobalData.itemTitle);
        MenuItem local = menu.findItem(R.id.localcitylist);
        Menu localcitys = local.getSubMenu();
        for (MKOLUpdateElement mk : localMapList) {
            localcitys.add(mk.cityName);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        if (item.getItemId() == R.id.willfollow) {
//            if (item.getTitle().toString().equals("开始跟踪")) {
//                follow = true;
//                item.setTitle("停止跟踪");
//            } else if (item.getTitle().toString().equals("停止跟踪")) {
//                follow = false;
//                for (Overlay o : oldoverlayList) {
//                    o.remove();
//                }
//                item.setTitle("开始跟踪");
//            }
//        } else
        if (item.getItemId() == R.id.selectstationofwillbeshow) {
            if (show == false) {
                pw.showAtLocation(mMapView, Gravity.TOP | Gravity.RIGHT, 0,
                        getWindow().findViewById(Window.ID_ANDROID_CONTENT)
                                .getTop());
                show = true;
            } else {
                pw.dismiss();
                show = false;
            }
        }
//        } else if (item.getItemId() == R.id.shishishixiang) {
//            if (item.getTitle().equals("实时示向")) {
//
//                Intent intent = new Intent(MapActivity.this,
//                        StationListActivity.class);
//                Bundle bd = new Bundle();
//                bd.putString("from", "map");
//                intent.putExtras(bd);
//                startActivity(intent);
//            } else if (item.getTitle().equals("开始示向")) {
//                my = new MyThread(GlobalData.stationKey, GlobalData.logicId,
//                        GlobalData.deviceName);
//                my.sendStartCmd();
//                my.start();
//                item.setTitle("停止示向");
//                GlobalData.itemTitle = "停止示向";
//            } else if (item.getTitle().equals("停止示向")) {
//                if (my != null) {
//                    my.sendEndCmd();
//                }
//                if (shishixianMarker != null) {
//                    shishixianMarker.remove();
//                }
//                item.setTitle("开始示向");
//                GlobalData.itemTitle = "开始示向";
//            }
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        flag = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        my = new MyThread(GlobalData.stationKey, GlobalData.logicId,
                GlobalData.deviceName);
        my.sendStartCmd();
        my.start();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        runmyThread = false;
        if (my != null) {
            try {
                my.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lcient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    private void sendClose() {
        StopTaskFrame st = new StopTaskFrame();
        st.functionNum = 46;
        st.length = 2;
        st.tail = 22;
        byte[] b;
        try {
            b = JavaStruct.pack(st);
            os.write(b);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && pw.isShowing()) {
//            pw.dismiss();
//            show = false;
//        } else if (keyCode == KeyEvent.KEYCODE_BACK
//                && (GlobalData.itemTitle.equals("停止示向")
//                || GlobalData.itemTitle.equals("开始示向") || GlobalData.itemTitle
//                .equals("实时示向"))) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(
//                    MapActivity.this);
//            builder.setTitle("警告!");
//            builder.setMessage("确定要退出该功能吗？");
//            builder.setPositiveButton("确定",
//                    (dialog, which) -> {
//                        GlobalData.itemTitle = "实时示向";
//
//                        try {
//                            sendClose();
//                            Thread.sleep(50);
//                            s.close();
//                            if (my != null) {
//                                runmyThread = false;
//                                my.join();
//                                my = null;
//                            }
//                            System.gc();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        Intent intent = new Intent(MapActivity.this,
//                                MainActivity.class);//CircleActivity.class);
//                        startActivity(intent);
//                    });
//            builder.setNegativeButton("取消", null);
//            builder.create();
//            builder.show();
//        } else {
//            super.onKeyDown(keyCode, event);
//        }
//        return true;
//    }
}
