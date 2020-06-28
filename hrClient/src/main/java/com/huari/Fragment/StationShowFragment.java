package com.huari.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huari.adapter.RecordAllAdapter;
import com.huari.adapter.RecordAllBaseAdapter;
import com.huari.adapter.StationFunctionListAdapter;
import com.huari.client.R;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Station;
import com.huari.dataentry.UnManStation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StationShowFragment extends Fragment {

    public static boolean firstIn = true;
    public static int stationnum = 0;
    public static RecyclerView rvShow;
    public static RecyclerView stationFunction;
    public static List<Station> stations;
    public static Context context;
    public static List<String> list = new ArrayList<>();
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.d("stationcome", "第一段来了");
                    if (GlobalData.stationHashMap != null && GlobalData.stationHashMap.size() != 0) {
                        stationLoad();
                        leftListRecycle();
                    }
                    break;
                case 0x8:
                    Log.d("stationcome", "第二段来了");
                    if (GlobalData.unmanHashMap != null && GlobalData.unmanHashMap.size() != 0) {
                        unManStationLoad();
                        leftListRecycle();
                    }
                    break;
            }
        }
    };

    private static void unManStationLoad() {
        if (stations == null) {
            stations = new ArrayList<>();
        }
//        stations.clear();
        Station station = new Station();
        for (String key : GlobalData.unmanHashMap.keySet()) {
            station.setName(GlobalData.unmanHashMap.get(key).server);
            if (!stations.contains(station)) {
                MyDevice myDevice = new MyDevice();
                myDevice.setName(GlobalData.unmanHashMap.get(key).name);
                myDevice.setLogic(new HashMap<>());
                if (station.getShowdevicelist() == null) {
                    station.setShowdevicelist(new ArrayList<>());
                }
                station.getShowdevicelist().add(myDevice);
                station.lon = (float) GlobalData.unmanHashMap.get(key).lon;
                station.lan = (float) GlobalData.unmanHashMap.get(key).lan;
                stations.add(station);
            } else {
                MyDevice myDevice = new MyDevice();
                myDevice.setName(GlobalData.unmanHashMap.get(key).name);
                myDevice.setLogic(new HashMap<>());
                Station station1 = stations.get(stations.indexOf(station));
                station1.lon = (float) GlobalData.unmanHashMap.get(key).lon;
                station1.lan = (float) GlobalData.unmanHashMap.get(key).lan;
                if (station1.getShowdevicelist().contains(myDevice)) {
                    station1.getShowdevicelist().remove(station1.getShowdevicelist().indexOf(myDevice));
                }
                station1.getShowdevicelist().add(myDevice);
            }
        }
    }

    private static void stationLoad() {
        if (stations == null) {
            stations = new ArrayList<>();
        }
//                    stations.clear();
        for (String key : GlobalData.stationHashMap.keySet()) {
            if (!stations.contains(GlobalData.stationHashMap.get(key))) {
                stations.add(GlobalData.stationHashMap.get(key));
            } else {
                stations.set(stations.indexOf(GlobalData.stationHashMap.get(key)), GlobalData.stationHashMap.get(key));
            }
        }
        if (firstIn == true) {
            stationnum = stations.size();
        }
    }

    private static RecordAllAdapter mWrapper;

    public StationShowFragment() {
    }

    @SuppressLint("ValidFragment")
    public StationShowFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_all_record_query, container, false);
        this.rvShow = view.findViewById(R.id.rvShow);
        stationFunction = view.findViewById(R.id.station_function_list);
//        list.add("地图");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (list.size() == 0) {
            list.add("遥控遥测设备");
            Log.d("unmasize", String.valueOf(list.size()));
        }
        if (GlobalData.stationHashMap != null && GlobalData.stationHashMap.size() != 0) {
            stationLoad();
            leftListRecycle();
        }
        if (GlobalData.unmanHashMap != null && GlobalData.unmanHashMap.size() != 0) {
            unManStationLoad();
            leftListRecycle();
        }
    }

    public static void leftListRecycle() {
        if (mWrapper != null) {
            mWrapper.setmContent(stations);
            mWrapper.setDataChange(true);
            mWrapper.notifyDataSetChanged();
            mWrapper.setDataChange(false);
        } else {
            rvShow.setLayoutManager(new LinearLayoutManager(context));
            mWrapper = new RecordAllAdapter(context, stations, (device, station, unstation, breakDown) -> reightListRecycle(device, station, unstation, breakDown));
            rvShow.setAdapter(mWrapper);
        }
    }

    public static void reightListRecycle(MyDevice myDevice, Station station, boolean unStation, boolean breakDown) {
        stationFunction.setLayoutManager(new LinearLayoutManager(context));
        Log.d("unmasize", String.valueOf(list.size()));
        StationFunctionListAdapter stationFunctionListAdapter = new StationFunctionListAdapter(myDevice, station, context, list, unStation, breakDown);
        stationFunction.setAdapter(stationFunctionListAdapter);
        stationFunctionListAdapter.notifyDataSetChanged();
    }
}
