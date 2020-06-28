package com.huari.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.MscanUI;
import com.huari.Presenter.UI.Impl.Net.MscanPresenterImpl;
import com.huari.Presenter.entity.Request;
import com.huari.Presenter.entity.UI.MscanData;
import com.huari.client.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MscanFragment extends BaseFragment implements MscanUI {
    Context context;
    MainFragment parent;
    SharedPreferences sharedPreferences;
    com.huari.ui.MscanShowView mscanShowView;
    View view;
    ListView listview;
    Request request;
    Map map;
    Handler handler;

    ArrayList<Float> freqlist, datalist;
    public MyAdapter adapter;

    MscanPresenterImpl mscanPresenter;

    public MscanFragment(Context context, MainFragment parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mscanPresenter = new MscanPresenterImpl(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mscan, container, false);
        mscanShowView = view.findViewById(R.id.mymscan);
        listview = view.findViewById(R.id.listview1);

        try {
            adapter = new MyAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        listview.setAdapter(adapter);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0x1) {
                    mscanShowView.invalidate();
                    adapter.notifyDataSetChanged();
                }
            }
        };

        return view;
    }

    void config() {
        mDialogFactory.showCustomDialog("离散扫描参数设置及选项", true, 2);
    }

    void play() {
        sharedPreferences = getDefaultSharedPreferences(context);

        if (request == null) {
            request = new Request();
            request.type = String.valueOf(DataTypeEnum.MSCAN);
            request.list = new ArrayList<>();
            map = new HashMap<String, Object>();
        } else {
            map.clear();
        }

        if (!is_played) {
            request.type = String.valueOf(DataTypeEnum.MSCAN);
            request.list.clear();
            map.put("Attenuation_Str", Integer.parseInt(sharedPreferences.getString("Attenuation", "10")));
            map.put("RFMode_Str", sharedPreferences.getString("RFMode", "标准"));
            map.put("DetectionType_Str", sharedPreferences.getString("Detection", "PEAK"));
            request.list.add(map);

            HashMap map1 = new HashMap<String, Object>();
            map1.put("CenterFreq_Double", Double.parseDouble(sharedPreferences.getString("CenterFreq1", "88.1")));
            map1.put("IFBandwidth_Double", Double.parseDouble(sharedPreferences.getString("Ifbw1", "100")));
            request.list.add(map1);

            HashMap map2 = new HashMap<String, Object>();
            map2.put("CenterFreq_Double", Double.parseDouble(sharedPreferences.getString("CenterFreq2", "88.1")));
            map2.put("IFBandwidth_Double", Double.parseDouble(sharedPreferences.getString("Ifbw2", "100")));
            request.list.add(map2);

            HashMap map3 = new HashMap<String, Object>();
            map3.put("CenterFreq_Double", Double.parseDouble(sharedPreferences.getString("CenterFreq3", "88.1")));
            map3.put("IFBandwidth_Double", Double.parseDouble(sharedPreferences.getString("Ifbw3", "100")));
            request.list.add(map3);

            HashMap map4 = new HashMap<String, Object>();
            map4.put("CenterFreq_Double", Double.parseDouble(sharedPreferences.getString("CenterFreq4", "88.1")));
            map4.put("IFBandwidth_Double", Double.parseDouble(sharedPreferences.getString("Ifbw4", "100")));
            request.list.add(map4);
            mscanPresenter.startMscan(request);
        } else {
            request.type = "StopTask";
            request.list.clear();
            mscanPresenter.endMscan(request);
        }
    }

    void save() {
        is_recorded = !is_recorded;
    }

    @Override
    public void MscanDataCallback(MscanData mscanData) {
        if (mscanData.CenterFreq != null) {
            if (freqlist == null)
                freqlist = new ArrayList<>();
            else
                freqlist.clear();

            if (datalist == null)
                datalist = new ArrayList<>();
            else
                datalist.clear();

            for (int i = 0; i < mscanData.CenterFreq.length; i++) {
                freqlist.add(mscanData.CenterFreq[i]);
                datalist.add(mscanData.LevelFast[i]);
            }

            mscanShowView.setM(mscanData.LevelFast, (float[]) mscanData.CenterFreq);

            refresh_data();
        }
    }

    @Override
    public void requestStartCallback(String result) {
        if (result.equals("success")) {
            is_played = !is_played;
            parent.update_titile_icon();
        }
    }

    @Override
    public void requestEndCallback(String result) {
        if (result.equals("success")) {
            is_played = !is_played;
            parent.update_titile_icon();
        }
    }

    public void refresh_data() {
        if (datalist.size() > 0) {
            Message msg = new Message();
            msg.what = 0x1;
            handler.sendMessage(msg);
        }
    }

    public class MyAdapter extends BaseAdapter {
        private int mCurrentItem = 0;
        private boolean isClick = false;

        class ViewHolder {
            TextView tv1, tv2, tv3;
        }

        @Override
        public int getCount() {
            if (datalist != null && freqlist != null) {
                return freqlist.size();
            } else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MyAdapter.ViewHolder viewholder;

            if (convertView == null) {
                convertView = View.inflate(context, R.layout.mscanlistviewdata, null);
                viewholder = new MyAdapter.ViewHolder();
                viewholder.tv1 = convertView
                        .findViewById(com.huari.diskactivity.R.id.listxuhao);
                viewholder.tv2 = convertView
                        .findViewById(com.huari.diskactivity.R.id.listpinlv);
                viewholder.tv3 = convertView
                        .findViewById(com.huari.diskactivity.R.id.listfudu);
                convertView.setTag(viewholder);
            } else {
                viewholder = (MyAdapter.ViewHolder) convertView.getTag();
            }


            if (datalist.size() > 0 && freqlist.size() > 0) {
                if (position < datalist.size()) {
                    viewholder.tv1.setText(position + 1 + "");
                    viewholder.tv2.setText(freqlist.get(position) + "");
                    viewholder.tv3.setText(datalist.get(position) + "");
                } else {
                    viewholder.tv1.setText("");
                    viewholder.tv2.setText("");
                    viewholder.tv3.setText("");
                }
            }

            if (mCurrentItem == position && isClick) {
                convertView.setBackgroundColor(Color.parseColor("#00CEFF"));
                viewholder.tv1.setTextColor(Color.parseColor("#9933cc"));
                viewholder.tv2.setTextColor(Color.parseColor("#9933cc"));
                viewholder.tv3.setTextColor(Color.parseColor("#9933cc"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#000000"));
                viewholder.tv1.setTextColor(Color.parseColor("#ffffff"));
                viewholder.tv2.setTextColor(Color.parseColor("#ffffff"));
                viewholder.tv3.setTextColor(Color.parseColor("#ffffff"));
            }

            return convertView;
        }

        //获取行号
        public void setCurrentItem(int currentItem) {
            this.mCurrentItem = currentItem;
        }

        //是否点击
        public void setClick(boolean click) {
            this.isClick = click;
        }
    }
}
