package com.huari.Presenter.UI.Listener;

import com.huari.Presenter.entity.UI.PSCANData;

import java.util.List;

public interface OnPscanDataListener {
    void onData(PSCANData pscanData);

    void onCommand(boolean status);

    void onFreqyencyHoppingData(List<Double> freqyencyHoppingData);

    void onSignalSortData(List<Double> SignalSortData);
}

