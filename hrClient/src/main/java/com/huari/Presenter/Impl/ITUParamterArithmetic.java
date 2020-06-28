package com.huari.Presenter.Impl;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.IQData;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.entity.dataPersistence.ITUResult;
import com.huari.algorithm.ITUMeasure;

public class ITUParamterArithmetic<M, T> implements Arithmetic<M, T> {

    ITUMeasure ituMeasure;
    short funcNum;      //输入信号的制式类型号(1.AM;2.FM;3.PM)
    double BW;         // 输入信号的采样带宽
    double IQSampleRate; // 输入信号的采样率
    double XdbV; //输入xdb带宽计算时x的值
    double betadbV; //输入beta%带宽计算时beta的值

    public ITUParamterArithmetic(short funcNum, double BW, double IQSampleRate, double xdbV, double betadbV) {
        this.funcNum = funcNum;
        this.BW = BW;
        this.IQSampleRate = IQSampleRate;
        XdbV = xdbV;
        this.betadbV = betadbV;
    }

    @Override
    public void handle(Object m, Object o2) {
        DataPackage dataPackage = (DataPackage) m;
        short[] shorts = ((IQData) (dataPackage.Data.get("IQ"))).IQData;
        SingleMeasureUI singleMeasureUI = (SingleMeasureUI) o2;
        if(singleMeasureUI!=null){
            ituMeasure.onMeasure(shorts, shorts, (short) shorts.length, funcNum, ((IQData) dataPackage.Data.get("IQ")).CenterFreq, BW, IQSampleRate, XdbV, betadbV);
            ITUResult ituResult = new ITUResult(ituMeasure.dataPara,ituMeasure.PosPara,ituMeasure.NegPara,ituMeasure.XdbBW,ituMeasure.BetaBW,ituMeasure.pFreq,ituMeasure.realFreqLen,ituMeasure.pLev,ituMeasure.realLevLen);
            singleMeasureUI.onItuData(ituResult);
        }
    }

    @Override
    public void cancel() {
        ituMeasure = null;
    }

    @Override
    public void add() {
        ituMeasure = new ITUMeasure();
    }
}
