package com.huari.Presenter.Impl;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.entity.dataPersistence.HopDectectorParamter;
import com.huari.algorithm.HopDetector;

public class HopDectectorArithmetic implements Arithmetic<HopDectectorParamter, IBaseView> {
    HopDetector hopDetector;

    @Override
    public void handle(HopDectectorParamter hopDectectorParamter,IBaseView baseView) {
        hopDetector.setOnHopDectectorListener(baseView);
        hopDetector.detect(hopDectectorParamter.data, hopDectectorParamter.sfreq, hopDectectorParamter.efreq,
                hopDectectorParamter.df, hopDectectorParamter.th1_deltaf, hopDectectorParamter.the2_deltaf);
    }

    @Override
    public void cancel() {
        hopDetector.setOnHopDectectorListener(null);
        hopDetector.release();
    }

    @Override
    public void add() {
        hopDetector = new HopDetector();
    }
}
