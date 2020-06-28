package com.huari.Presenter.Impl;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.algorithm.ModIdentify;

public class ModulationrecognitionArithmetic<T> implements Arithmetic<short[], IBaseView> {

    ModIdentify modIdentify;
    double sampleRate;  //输入信号的采样率，单位Hz

    @Override

    public void handle(short[] shorts, IBaseView baseView) {
        if (baseView != null) {
            double[] result = new double[3];
            modIdentify.onHRCalcCharacterParam(shorts, shorts, shorts.length, sampleRate, false, result);
            ((SingleMeasureUI) baseView).onDemodulationData(String.valueOf(result[0]), (float) result[1]);
        }
    }

    @Override
    public void cancel() {
        modIdentify = null;
    }

    @Override
    public void add() {
        modIdentify = new ModIdentify();
    }
}
