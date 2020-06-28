package com.huari.Presenter.entity.dataPersistence;

public class ITUResult {
    public double dataPara;  //调制深度 或者频偏指数（Hz）
    public double PosPara;   //正向调制深度 或者频偏指数（Hz）
    public double NegPara;   //负向调制深度 或者频偏指数（Hz）
    public double XdbBW;    //单位Hz
    public double BetaBW;   //单位Hz
    public double pFreq[];  //输出信号的瞬时频率参数数组
    public short realFreqLen = 0;//输出信号的瞬时频率参数数组长度
    public double pLev[];//输出信号的瞬时电平参数数组
    public short realLevLen = 0;//输出信号的瞬时电平参数数组长度

    public ITUResult(double dataPara, double posPara, double negPara, double xdbBW, double betaBW, double[] pFreq, short realFreqLen, double[] pLev, short realLevLen) {
        this.dataPara = dataPara;
        PosPara = posPara;
        NegPara = negPara;
        XdbBW = xdbBW;
        BetaBW = betaBW;
        this.pFreq = new double[pFreq.length];
        System.arraycopy(pFreq, 0, this.pFreq, 0, pFreq.length);
        this.realFreqLen = realFreqLen;
        this.pLev = new double[pLev.length];
        System.arraycopy(pLev, 0, this.pLev, 0, pLev.length);
        this.realLevLen = realLevLen;
    }
}
