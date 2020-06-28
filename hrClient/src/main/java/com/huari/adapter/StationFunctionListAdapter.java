package com.huari.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huari.client.MScanActivity;
import com.huari.client.R;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Parameter;
import com.huari.dataentry.Station;
import com.huari.client.DzActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StationFunctionListAdapter extends RecyclerView.Adapter<StationFunctionListAdapter.viewholder> {
    MyDevice myDevice;
    List<LogicParameter> logicParameters;
    Station station;
    Context context;
    List<String> list;
    boolean unstation;
    boolean breakDown;
    String param_names[]={"MemCenterFreq","MemDemodBW","Memdemodmode","MemATT"};

    public StationFunctionListAdapter(MyDevice myDevice, Station station, Context context, List<String> list,boolean unstation,boolean breakDown) {
        this.myDevice = myDevice;
        this.station = station;
        this.context = context;
        this.unstation = unstation;
        this.breakDown = breakDown;
        logicParameters = new ArrayList<>();
        this.list = list;
        for (String s : myDevice.getLogic().keySet()) {
            logicParameters.add(myDevice.getLogic().get(s));
        }
    }

    @NonNull

    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.station_function_item, viewGroup, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder viewholder, int i) {
        if (i < myDevice.getLogic().size()) {
            viewholder.textView.setText(logicParameters.get(i).getType().startsWith("L") ? "频谱分析" :
                    (logicParameters.get(i).getType().startsWith("S") ? "频段扫描" :
                            (logicParameters.get(i).getType().startsWith("D") ? "单频测向" :
                                    (logicParameters.get(i).getType().startsWith("M") ? "离散扫描":"其他功能"))));
        } else {
            viewholder.textView.setText(list.get(i - myDevice.getLogic().size()));
        }
        viewholder.linearLayout.setOnClickListener(v -> {
            if(!breakDown) skipActivity(i);
            else Toast.makeText(context,"设备故障，暂时无法使用此设备下的功能",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        if(unstation == true){
            return myDevice.getLogic().size() + list.size();
        }else {
            return myDevice.getLogic().size();
        }
    }

    class viewholder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        LinearLayout linearLayout;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.station_function_picture);
            textView = itemView.findViewById(R.id.station_function_text);
            linearLayout = itemView.findViewById(R.id.station_fuction_view);
        }
    }

    private void skipActivity(int i) {
        if (i < logicParameters.size()) {
            if (logicParameters.get(i).getType().startsWith("L")) {
                Intent intent = new Intent();
                intent.setAction("function0");
                Bundle bundle = new Bundle();
                bundle.putString("devicename",
                        myDevice.name);
                bundle.putString("stationname",
                        station.name);
                bundle.putString("stationKey",
                        station.id);
                bundle.putString("lid", logicParameters.get(i).id);
                intent.putExtras(bundle);
                context.startActivity(intent);
            } else if (logicParameters.get(i).getType().startsWith("DDF")) {
                Intent intent = new Intent();
                intent.setAction("function12");
                Bundle bundle = new Bundle();
                bundle.putString("devicename",
                        myDevice.name);
                bundle.putString("stationname",
                        station.name);
                bundle.putFloat("lan",
                        station.lan);
                bundle.putFloat("lon",
                        station.lon);
                bundle.putString("stationKey",
                        station.id);
                bundle.putString("lid", logicParameters.get(i).id);
                intent.putExtras(bundle);
                context.startActivity(intent);
            } else if (logicParameters.get(i).getType().startsWith("SCAN")) {
                Intent intent = new Intent();
                intent.setAction("function18");
                Bundle bundle = new Bundle();
                bundle.putString("devicename",
                        myDevice.name);
                bundle.putString("stationname",
                        station.name);
                bundle.putString("stationKey",
                        station.id);
                bundle.putString("lid", logicParameters.get(i).id);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
            else if (logicParameters.get(i).getType().startsWith("MSCAN")) {
                GlobalData.mscanset = false;
                Intent intent = new Intent();
                intent.setAction("function19");
                Bundle bundle = new Bundle();
                bundle.putString("devicename",
                        myDevice.name);
                bundle.putString("stationname",
                        station.name);
                bundle.putString("stationKey",
                        station.id);
                bundle.putString("lid", logicParameters.get(i).id);
                intent.putExtras(bundle);

                GlobalData.tmpparameterlist = new ArrayList<>();
                GlobalData.tmpparameterlist.clear();

                for (Parameter p : logicParameters.get(i).parameterlist) {
                    for (int j = 0; j<param_names.length; j++) {
                        String pname = param_names[j];

                        if (p.name.equals(pname)) {
                            boolean find = false;
                            if (GlobalData.tmpparameterlist.size() > 0) {

                                for (Parameter pt : GlobalData.tmpparameterlist) {
                                    if (pt.name.equals(pname))
                                        find = true;
                                }
                            }


                            if (!find || GlobalData.tmpparameterlist.size()==0) {
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

                                GlobalData.tmpparameterlist.add(pp);
                            }
                        }

                    }
                }

                context.startActivity(intent);
            }
        } else {
            if(i-logicParameters.size()==0){
                Intent toMap = new Intent(context,DzActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("name",myDevice.name);
                bundle1.putFloat("lon",station.lon);
                bundle1.putFloat("lan",station.lan);
                toMap.putExtra("bundle",bundle1);
                context.startActivity(toMap);
            }
//            else
//            if (i - logicParameters.size() == 0) {
//                context.startActivity(new Intent(context, IquareActivity.class));
//            } else if (i - logicParameters.size() == 1) {
//                context.startActivity(new Intent(context, ServerManagerActivity.class));
//            }
        }
    }
}
