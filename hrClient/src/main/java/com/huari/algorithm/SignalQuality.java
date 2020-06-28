package com.huari.algorithm;

public class SignalQuality {
    static {
        System.loadLibrary("algorithm_lib");
    }
    public float sigSNR;
    private native int SigQualityFun(float [] scandata, double centFreq, float spectrumBW, double sigCentFreq, float sigBW);

    /************************************************************************
     函数名称:信号质量（信噪比）
     创建时间:  2018/08/13
     输入：
     scandata,								单频频谱数据
     centFreq,               单频测量中心频率，单位MHz
     spectrumBW,             单频测量频谱带宽，单位KHz
     sigCentFreq,            指定信号的中心频率，单位MHz
     sigBW,                  指定信号的带宽，单位KHz
     输出（对应于成员变量）：
     sigSNR			        		指定信号的信噪比
     返回值：
     = 0:成功     -1:给定信号不在频段范围内；   -2:输入频谱数据不对
     ************************************************************************/
    public int getQuality(float [] scandata, double centFreq, float spectrumBW, double sigCentFreq, float sigBW)
    {
        return SigQualityFun(scandata, centFreq, spectrumBW, sigCentFreq, sigBW);
    }
}
