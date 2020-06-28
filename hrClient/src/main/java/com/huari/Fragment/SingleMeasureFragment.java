package com.huari.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.UI.Impl.Net.SingleMeasurePresenterImpl;
import com.huari.Presenter.UI.Interface.SingleMeasurePresenter;
import com.huari.Presenter.entity.Request;
import com.huari.Presenter.entity.UI.SingleMeasureData;
import com.huari.Presenter.entity.dataPersistence.ITUResult;
import com.huari.adapter.ItuAdapterOfListView;
import com.huari.adapter.PagerAdapterOfSpectrum;
import com.huari.client.R;
import com.huari.dataentry.GlobalData;

import java.util.ArrayList;
import java.util.HashMap;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.huari.dataentry.GlobalData.ituHashMap;

public class SingleMeasureFragment extends BaseFragment implements SingleMeasureUI {
    boolean showMax, showMin, showAvg, water;

    com.huari.ui.ShowWaveView waveview;
    com.huari.ui.Waterfall waterfall;
    com.huari.ui.PartWaveShowView showwave;
    ViewPager viewpager;
    ItuAdapterOfListView listAdapter;
    PagerAdapterOfSpectrum spectrumAdapter;
    ListView itu_listview;
    LinearLayout ituLinearLayout;
    ArrayList<View> viewlist;
    Request request;
    HashMap<String, Object> map;
    MainFragment parent;

    SharedPreferences sharedPreferences;
    SingleMeasurePresenter singleMeasurePresenter;

    float startFreq = 0f, endFreq = 0f, pStepFreq = 0.0125f, centerFreq = 0f;
    float autoFreq = -1f;

    float spwide = 0f;// 频谱带宽

    private Context context;

    public SingleMeasureFragment(Context context, MainFragment parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleMeasurePresenter = new SingleMeasurePresenterImpl(this);
        setHasOptionsMenu(true);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_singlemeasure, container, false);
        try {
            ituLinearLayout = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.listviewwithitu, null);
            itu_listview = ituLinearLayout.findViewById(R.id.itulistview);
            viewlist = new ArrayList<>();

            spectrumAdapter = new PagerAdapterOfSpectrum(viewlist);

            if (GlobalData.ituHashMap == null) {
                GlobalData.ituHashMap = new HashMap<>();
            }
            listAdapter = new ItuAdapterOfListView(context, GlobalData.ituHashMap);

            viewpager = view.findViewById(R.id.firstviewpager);
            itu_listview.setAdapter(listAdapter);

            showwave = (com.huari.ui.PartWaveShowView) getLayoutInflater().inflate(
                    R.layout.a, null);
            viewlist.add(showwave);
            viewlist.add(ituLinearLayout);
            viewpager.setAdapter(spectrumAdapter);

            waterfall = view.findViewById(R.id.waterfall);
            waveview = view.findViewById(R.id.buildshowwaveview);
            init_view();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    void init_view() {
        sharedPreferences = getDefaultSharedPreferences(context);
        try {
            centerFreq = Float.parseFloat(sharedPreferences.getString("CenterFreq", "101.7"));
            spwide = Float.parseFloat(sharedPreferences.getString("IFBandWidth", "200"));
            startFreq = centerFreq - spwide / 2000;
            endFreq = centerFreq + spwide / 2000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        waveview.setM(null);
        waveview.setMax(null);
        waveview.setMin(null);
        waveview.setAvg(null);
        waveview.setF(startFreq, endFreq, pStepFreq);
        GlobalData.ituHashMap.clear();
        listAdapter.notifyDataSetChanged();

        water = sharedPreferences.getBoolean("falls_switch", true);
        showMax = sharedPreferences.getBoolean("max_switch", false);
        showAvg = sharedPreferences.getBoolean("avg_switch", false);
        showMin = sharedPreferences.getBoolean("min_switch", false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.spectrums_analysis, menu);
    }

    void config() {
        mDialogFactory.showCustomDialog("单频测量参数设置及选项", true, 1);
        init_view();
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
            request.type = String.valueOf(DataTypeEnum.SingleMeasure);
            request.list.clear();
            map.put("CenterFreq_Double", centerFreq);
            map.put("IFBandwidth_Double", spwide);
            map.put("DemoBandWidth_Double", Float.parseFloat(sharedPreferences.getString("DemoBandWidth", "150")));
            map.put("DemodulationType_Str", sharedPreferences.getString("DemodulationType", "FM"));
            map.put("RFMode_Str", sharedPreferences.getString("RFMode", "标准"));
            map.put("Attenuation_Str", Integer.parseInt(sharedPreferences.getString("Attenuation", "10")));
            map.put("AudioSwitch_Str", sharedPreferences.getBoolean("AudioSwitch", true) ? "ON" : "OFF");
            map.put("SpectrumSwitch_Str", sharedPreferences.getBoolean("SpectrumSwitch", true) ? "ON" : "OFF");
            map.put("IQSwitch_Str", sharedPreferences.getBoolean("IQSwitch", true) ? "ON" : "OFF");
            request.list.add(map);
            singleMeasurePresenter.SingleMeasureStart(request);
//            singleMeasurePresenter.ITUShow();
        } else {
            request.type = "StopTask";
            request.list.clear();
            singleMeasurePresenter.SingleMeasureEnd(request);
            singleMeasurePresenter.ITUEnd();
            singleMeasurePresenter.endRecord();
        }
    }

    void save() {
        is_recorded = !is_recorded;
        if (is_recorded) {
            singleMeasurePresenter.startRecord(null, null);//开始记录数据
        } else {
            singleMeasurePresenter.endRecord();
        }
    }

    public void onData(SingleMeasureData data) {
        if (data.Spectrum != null) {
            if (water) {
                waterfall.set_newdata(data.Spectrum, data.Spectrum.length);
                waterfall.postInvalidate();
            }

            waveview.setHave(true);
            waveview.setFandC(startFreq, endFreq, data.Spectrum.length);

            waveview.setM(data.Spectrum);
        }

        if (showMax && data.Spectrum_max != null) {
            waveview.setMax(data.Spectrum_max);
        } else {
            waveview.setMax(null);
        }
        if (showMin && data.Spectrum_min != null) {
            waveview.setMin(data.Spectrum_min);
        } else {
            waveview.setMin(null);
        }
        if (showAvg && data.Spectrum_avg != null) {
            waveview.setAvg(data.Spectrum_min);
        } else {
            waveview.setAvg(null);
        }
        waveview.postInvalidate();

        if (data.LevelFast != -999) {
            showwave.refresh(data.LevelFast / 10);
            showwave.postInvalidate();
        }
    }

    public void onCommand(boolean status) {


    }

    @Override
    public void onItuData(ITUResult ituResult) {

    }

    public void onItuData(float freq, float strength, float bandwidth, float freq_deviat) {
        ituHashMap.put("场强", strength + "");
        ituHashMap.put("带宽", bandwidth + "");
        ituHashMap.put("频偏", freq_deviat + "");
        listAdapter.notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    public void onDemodulationData(String demod_str, float percent) {
        ituHashMap.put("调制识别", demod_str + String.format("%.2f", percent));
        listAdapter.notifyDataSetChanged();
    }

    public void onSave(boolean status, float time_long) {

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
