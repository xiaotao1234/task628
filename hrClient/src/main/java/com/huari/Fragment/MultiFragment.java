package com.huari.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.MultiUI;
import com.huari.Presenter.UI.Impl.Net.MultiSignalPresenterImpl;
import com.huari.Presenter.UI.Interface.MultiSignalPresenter;
import com.huari.Presenter.entity.Request;
import com.huari.client.R;

import java.util.ArrayList;
import java.util.HashMap;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MultiFragment extends BaseFragment implements MultiUI {
    com.huari.ui.ShowWaveView waveview;
    com.huari.ui.ShowWaveView waveview1;
    com.huari.ui.ShowWaveView waveview2;
    MultiSignalPresenter multiSignalPresenter;
    private Context context;
    MainFragment parent;
    SharedPreferences sharedPreferences;
    Request request;
    HashMap<String, Object> map;
    float startFreq = 0f, endFreq = 0f, pStepFreq = 0.0125f, centerFreq = 0f, spwide = 0f;// 频谱带宽;
    float startFreq1 = 0f, endFreq1 = 0f, pStepFreq1 = 0.0125f, ddcFreq1 = 0f, demodwd1 = 0f;// DDC1频谱带宽;
    float startFreq2 = 0f, endFreq2 = 0f, pStepFreq2 = 0.0125f, ddcFreq2 = 0f, demodwd2 = 0f;// DDC2频谱带宽;

    public MultiFragment(Context context, MainFragment parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        multiSignalPresenter = new MultiSignalPresenterImpl(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi, container, false);
        try {
            waveview = view.findViewById(R.id.waveview);
            waveview1 = view.findViewById(R.id.waveview1);
            waveview2 = view.findViewById(R.id.waveview2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    void init_view() {
        sharedPreferences = getDefaultSharedPreferences(context);
        try {
            centerFreq = Float.parseFloat(sharedPreferences.getString("ddc_CenterFreq", "101.7"));
            spwide = Float.parseFloat(sharedPreferences.getString("ddc_IFBandWidth", "200"));
            startFreq = centerFreq - spwide / 2000;
            endFreq = centerFreq + spwide / 2000;

            ddcFreq1 = Float.parseFloat(sharedPreferences.getString("ddc1_CenterFreq", "101.7"));
            demodwd1 = Float.parseFloat(sharedPreferences.getString("ddc1_DemoBandWidth", "200"));
            startFreq1 = ddcFreq1 - demodwd1 / 2000;
            endFreq1 = ddcFreq1 + demodwd1 / 2000;

            ddcFreq2 = Float.parseFloat(sharedPreferences.getString("ddc2_CenterFreq", "101.7"));
            demodwd2 = Float.parseFloat(sharedPreferences.getString("ddc2_DemoBandWidth", "200"));
            startFreq2 = ddcFreq2 - demodwd2 / 2000;
            endFreq2 = ddcFreq2 + demodwd2 / 2000;
        } catch (Exception e) {
            e.printStackTrace();
        }

        waveview.setM(null);
        waveview.setF(startFreq, endFreq, pStepFreq);

        waveview1.setM(null);
        waveview1.setF(startFreq1, endFreq1, pStepFreq1);

        waveview2.setM(null);
        waveview2.setF(startFreq2, endFreq2, pStepFreq2);
    }

    void config() {
        mDialogFactory.showCustomDialog("多信道分析参数设置及选项", true, 6);
    }

    void play() {
        init_view();
        if (request == null) {
            request = new Request();
            request.type = String.valueOf(DataTypeEnum.SingleMeasure);
            request.list = new ArrayList<>();
            map = new HashMap<>();
        } else {
            map.clear();
        }
        if (!is_played) {
            request.type = String.valueOf(DataTypeEnum.MultiSignal);
            request.list.clear();
            map.put("CenterFreq_Double", centerFreq);
            map.put("IFBandwidth_Double", spwide);
            map.put("Attenuation_Str", Integer.parseInt(sharedPreferences.getString("Attenuation", "10")));
            request.list.add(map);

            HashMap map1 = new HashMap<String, Object>();
            map1.put("DDC1Freq_Double", ddcFreq1);
            map1.put("DemoBandWidth_Double", demodwd1);
            map1.put("DemodulationType_Str", sharedPreferences.getString("ddc1_DemodulationType", "FM"));
            request.list.add(map1);

            HashMap map2 = new HashMap<String, Object>();
            map2.put("DDC2Freq_Double", ddcFreq2);
            map2.put("DemoBandWidth_Double", demodwd2);
            map2.put("DemodulationType_Str", sharedPreferences.getString("ddc2_DemodulationType", "FM"));
            request.list.add(map2);
            //map.put("AudioSwitch_Str", sharedPreferences.getBoolean("AudioSwitch", true)?"ON":"OFF");
            multiSignalPresenter.StartMultiSignal(request);
        } else {
            request.type = "StopTask";
            request.list.clear();
            multiSignalPresenter.EndMultiSignal(request);
        }
    }

    void save() {
        is_recorded = !is_recorded;
    }

    @Override
    public void SpectrumDataback(short[] spectrum) {
        if (spectrum != null) {
            waveview.setHave(true);
            waveview.setFandC(startFreq, endFreq, spectrum.length);
            waveview.setM(spectrum);
        }
        waveview.postInvalidate();
    }

    @Override
    public void SpectrumDatabackDDC1(short[] spectrum) {
        if (spectrum != null) {
            waveview1.setHave(true);
            waveview1.setFandC(startFreq1, endFreq1, spectrum.length);
            waveview1.setM(spectrum);
        }
        waveview1.postInvalidate();
    }

    @Override
    public void VoiceDatabackDDC1(byte[] voice) {

    }

    @Override
    public void SpectrumDatabackDDC2(short[] spectrum) {
        if (spectrum != null) {
            waveview2.setHave(true);
            waveview2.setFandC(startFreq2, endFreq2, spectrum.length);
            waveview2.setM(spectrum);
        }
        waveview2.postInvalidate();
    }

    @Override
    public void VoiceDatabackDDC2(byte[] voice) {

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

}
