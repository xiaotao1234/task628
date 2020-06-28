package com.huari.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.HopDectectorUI;
import com.huari.Presenter.UI.Impl.Net.HopDectectorPresenterImpl;
import com.huari.Presenter.UI.Interface.HopDectectorPresenter;
import com.huari.Presenter.entity.Request;
import com.huari.client.R;

import java.util.ArrayList;
import java.util.HashMap;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class Freq_hoppingFragment extends BaseFragment implements HopDectectorUI {
    com.huari.ui.PinDuan pinduan;
    Context context;
    MainFragment parent;
    SharedPreferences sharedPreferences;
    boolean showMax, showMin, showAvg, waterfall;
    float startFreq = 0f, endFreq = 0f, stepFreq = 0f;
    Request request;
    HashMap<String, Object> map;
    HopDectectorPresenter hopePresenter;

    public Freq_hoppingFragment(Context context, MainFragment parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hopePresenter = new HopDectectorPresenterImpl(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freqhopping, container, false);
        try {
            pinduan = view.findViewById(R.id.mypin);

            init_view();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    void init_view() {
        sharedPreferences = getDefaultSharedPreferences(context);

        try {
            startFreq = Float.parseFloat(sharedPreferences.getString("StartFreq", "88"));
            endFreq = Float.parseFloat(sharedPreferences.getString("EndFreq", "108"));
            stepFreq = Float.parseFloat(sharedPreferences.getString("FreqStep", "25"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        pinduan.setParameters(startFreq, endFreq, stepFreq);

        waterfall = sharedPreferences.getBoolean("hope_falls_switch", true);
        showMax = sharedPreferences.getBoolean("hope_max_switch", false);
        showAvg = sharedPreferences.getBoolean("hope_avg_switch", false);
        showMin = sharedPreferences.getBoolean("hope_min_switch", false);
    }

    void play() {
        init_view();

        if (request == null) {
            request = new Request();
            request.type = String.valueOf(DataTypeEnum.PSCAN);
            request.list = new ArrayList<>();
            map = new HashMap<>();
        } else {
            map.clear();
        }
        if (!is_played) {
            request.type = String.valueOf(DataTypeEnum.PSCAN);
            request.list.clear();
            map.put("Attenuation_Str", Integer.parseInt(sharedPreferences.getString("Attenuation", "10")));
            map.put("RFMode_Str", sharedPreferences.getString("RFMode", "标准"));
            request.list.add(map);

            HashMap map2 = new HashMap<String, Object>();
            map2.put("StartFreq_Double", startFreq);
            map2.put("EndFreq_Double", endFreq);
            map2.put("FreqStep_Double", stepFreq);
            request.list.add(map2);

            hopePresenter.startHopDectector(request,stepFreq);

        } else {
            request.type = "StopTask";
            request.list.clear();
            hopePresenter.endHopDectector(request);
        }
    }

    void save() {
        is_recorded = !is_recorded;
    }

    void config() {
        mDialogFactory.showCustomDialog("跳频检测参数设置及选项", true, 4);
        init_view();
    }


    public void HopDectectorResultCallback(double[] hops) {
        Log.d("datacome", String.valueOf(hops.length));

    }

    @Override
    public void SpectrumDataback(short[] Spectrum) {
//        synchronized (Spectrum) {
        if (Spectrum != null) {
            pinduan.setM(Spectrum);
            if (waterfall) {
                pinduan.waterfall.update_data(Spectrum, Spectrum.length);
                pinduan.waterfall.postInvalidate();
            } else
                pinduan.hideWaterfall(true);

//            if (showMax)
//                pinduan.setMax(data.Spectrum_max);
//            if (showMin)
//                pinduan.setMin(data.Spectrum_min);
//            if (showAvg)
//                pinduan.setAvg(data.Spectrum_avg);

            pinduan.pss.invalidate();
        }
//        }
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

    public void onSave(boolean status, float time_long) {

    }
}
