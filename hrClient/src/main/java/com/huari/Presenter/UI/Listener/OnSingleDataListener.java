package com.huari.Presenter.UI.Listener;

import com.huari.Presenter.entity.UI.SingleMeasureData;

public interface OnSingleDataListener {

    void onData(SingleMeasureData data);

    void onCommand(boolean status);

    void onItuData(float freq, float strength, float bandwidth, float freq_deviat);

    void onDemodulationData(String demod_str, float percent);

    void onSave(boolean status, float time_long);
}
