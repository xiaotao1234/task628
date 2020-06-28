package com.huari.Presenter.Impl;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.PscanUI;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.dataPersistence.SignalSortResult;
import com.huari.algorithm.SignalChoose;

public class SignalSortArithmetic<T> implements Arithmetic<T, IBaseView> {
    SignalChoose signalChoose;
    float Step;//                    扫描步进KHz
    int SmoothFrame;//             时间平滑帧数
    int DataType;//          数据类型，0为短波[终止频率<30M]   1为超短波[起始频率>30M], 横跨了30M，选择超短波
    boolean isLineThreshold;//是否使用直线门限，true为使用直线门限进行信号提取
    float lineThresholdValue;// 直线门限值，按照此门限值进行信号提取

    public SignalSortArithmetic(float step, int smoothFrame, int dataType, boolean isLineThreshold, float lineThresholdValue) {
        Step = step;
        SmoothFrame = smoothFrame;
        DataType = dataType;
        this.isLineThreshold = isLineThreshold;
        this.lineThresholdValue = lineThresholdValue;
    }

    @Override
    public void handle(T t, IBaseView iBaseView) {
        if(iBaseView!=null){
            SpectrumData spectrumData = ((SpectrumData) ((DataPackage) t).Data.get(Constant.Spectrum));
            float[] spe = new float[spectrumData.Spectrum.length];
            for (int i = 0; i < spe.length; i++) spe[i] = spectrumData.Spectrum[i];
            signalChoose.SigChooseFun(1, spe, spectrumData.StartFreq, spectrumData.EndFreq, Step, SmoothFrame, DataType, isLineThreshold, lineThresholdValue);
            SignalSortResult signalSortResult = new SignalSortResult(signalChoose.AutoThreshold.length, signalChoose.FreqPointSN.length, signalChoose.CenterFreq.length,
                    signalChoose.CenterFreqIndex.length, signalChoose.CenterFreqAmp.length, signalChoose.SignalBand.length, signalChoose.SNR.length, signalChoose.SignalType.length, signalChoose.SigModType.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.AutoThreshold, 0, signalChoose.AutoThreshold.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.FreqPointSN, 0, signalChoose.FreqPointSN.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.CenterFreq, 0, signalChoose.CenterFreq.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.CenterFreqIndex, 0, signalChoose.CenterFreqIndex.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.CenterFreqAmp, 0, signalChoose.CenterFreqAmp.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.SignalBand, 0, signalChoose.SignalBand.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.SNR, 0, signalChoose.SNR.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.SignalType, 0, signalChoose.SignalType.length);
            System.arraycopy(signalChoose.AutoThreshold, 0, signalSortResult.SigModType, 0, signalChoose.SigModType.length);
            ((PscanUI) iBaseView).onSignalSortData(signalSortResult);
        }
    }

    @Override
    public void cancel() {
        signalChoose = null;
    }

    @Override
    public void add() {
        signalChoose = new SignalChoose();
    }
}
