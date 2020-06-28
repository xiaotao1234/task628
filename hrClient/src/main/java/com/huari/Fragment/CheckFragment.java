package com.huari.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.cdhuari.entity.DeviceInfo;
import com.huari.Fragment.UIinterface.DeviceStateUI;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.client.R;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CheckFragment extends BaseFragment implements DeviceStateUI {
    ListView dev_list;
    private Context context;
    MainFragment parent;
    ArrayList <Dev_info> device_status;
    DevAdapter adapter;

    Handler handler;

    Lock lock1 = new ReentrantLock();

    public CheckFragment(Context context, MainFragment parent) {
        this.context = context;
        this.parent = parent;
        visible = false;
        is_played = false;
        is_recorded = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (device_status == null){
            device_status = new ArrayList<Dev_info>();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_check, container, false);

        dev_list = view.findViewById(R.id.dev_list1);

        try {
            adapter = new DevAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dev_list.setAdapter(adapter);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0x1)
                    adapter.notifyDataSetChanged();
            }
        };

        BusinessCoreNet.getInstanceNet().setOnDeviceStateListener(this);

        return  view;
    }

    public void refreshTable() {
        if (device_status != null) {
            Message msg = new Message();
            msg.what = 0x1;
            handler.sendMessage(msg);
        }
    }

    @Override
    public void DeviceStateCallback(DeviceInfo deviceInfo) {
        lock1.lock();
        device_status.clear();
        assert deviceInfo != null;
        device_status.add(new Dev_info("接收机电量", deviceInfo.RemPower + ""));

        if (deviceInfo.Compass != -9999) {
            device_status.add(new Dev_info("电子罗盘", deviceInfo.Compass + ""));
        } else
            device_status.add(new Dev_info("电子罗盘", "罗盘数据出错"));
        lock1.unlock();

        refreshTable();
    }

    public static class Dev_info {
        String dev_name;
        String dev_info;

        Dev_info(String name, String info){
            dev_name = name;
            dev_info = info;
        }
    }

    public class DevAdapter extends BaseAdapter
    {
        private int mCurrentItem = 0;
        private boolean isClick = false;

        class ViewHolder {
            TextView tv1, tv2, tv3;
        }

        @Override
        public int getCount() {
            if ( device_status != null ) {
                return device_status.size();
            }
            else
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

        @SuppressLint({"SetTextI18n", "InflateParams"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DevAdapter.ViewHolder viewholder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.device_data, null);
                viewholder = new DevAdapter.ViewHolder();
                viewholder.tv1 = convertView
                        .findViewById(R.id.dev_info_no);
                viewholder.tv2 = convertView
                        .findViewById(R.id.dev_info_name);
                viewholder.tv3 = convertView
                        .findViewById(R.id.dev_info_msg);
                convertView.setTag(viewholder);
            } else {
                viewholder = (DevAdapter.ViewHolder) convertView.getTag();
            }

            lock1.lock();

            if ( device_status != null &&  device_status.size() > 0 ) {
                viewholder.tv1.setText(position + 1 + "");
                viewholder.tv2.setText(device_status.get(position).dev_name);
                viewholder.tv3.setText(device_status.get(position).dev_info);
            } else {
                viewholder.tv1.setText("");
                viewholder.tv2.setText("");
                viewholder.tv3.setText("");
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
            lock1.unlock();

            return convertView;
        }

        //获取行号
        public void setCurrentItem(int currentItem){
            this.mCurrentItem = currentItem;
        }

        //是否点击
        public void setClick(boolean click){
            this.isClick = click;
        }
    }
}
