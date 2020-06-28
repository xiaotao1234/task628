package com.huari.Presenter.entity.dataPersistence;

public class SignalSortResult {
    public float[] AutoThreshold;//自动门限值 数组
    public float[] FreqPointSN;//每个频点信噪比 数组
    public double[] CenterFreq;//信号中心频率KHz 数组
    public int[] CenterFreqIndex;//信号中心频率索引号 数组
    public float[] CenterFreqAmp;//信号中心频率幅值 数组
    public float[] SignalBand;//信号带宽KHz 数组
    public float[] SNR;//信号信噪比 数组
    public String[] SignalType;//信号类型 数组
    public String[] SigModType;    //信号制式 数组

    public SignalSortResult(int autoThreshold, int freqPointSN, int centerFreq, int centerFreqIndex, int centerFreqAmp, int signalBand, int SNR, int signalType, int sigModType) {
        AutoThreshold = new float[autoThreshold];
        FreqPointSN = new float[freqPointSN];
        CenterFreq = new double[centerFreq];
        CenterFreqIndex = new int[centerFreqIndex];
        CenterFreqAmp = new float[centerFreqAmp];
        SignalBand = new float[signalBand];
        this.SNR = new float[SNR];
        SignalType = new String[signalType];
        SigModType = new String[sigModType];
    }
}
