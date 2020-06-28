package com.huari.Fragment.UIinterface;

import com.huari.Presenter.entity.UI.PSCANData;
import com.huari.Presenter.entity.dataPersistence.SignalSortResult;

import java.util.List;

public interface PscanUI extends IBaseView{
    void onData(PSCANData pscanData);

    void onCommand(boolean status);

    void onFreqyencyHoppingData(List<Double> freqyencyHoppingData);

    void onSignalSortData(SignalSortResult signalSortResult);

    void requestStartCallback(String result);

    void requestEndCallback(String result);
}
