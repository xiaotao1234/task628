package com.huari.algorithm;

public class ModIdentify {

    static {
        System.loadLibrary("algorithm_lib");
    }
    private native int HRCalcCharacterParam(short []dataIArray, short []dataQArray, int ns,
                                            double SampleRate, boolean _FunNum,  double [] result);


    /***************功能说明*********************
     完成调制信号方式的识别并估计符号率，Noise和模拟信号的符号率为0，数字信号输出符号率的估计值。
     能够识别的调制方式有：Noise，CW/AM/FM，2ASK/4ASK，2FSK/4FSK/MSK，
     BPSK/QPSK/OQPSK/pi/4DQPSK/8PSK/16PSK, 4QAM/16QAM/32QAM/64QAM。
     注意：针对JP项目要求，相比KF19008版本去掉了4FSK与8FSK的分类，将新增的GMSK信号判为MSK，由于时间限制，将依靠
     深度学习最终区分GMSK和MSK信号
     输入：
     dataIArray              输入基带的I路信号
     dataQArray              输入基带的Q路信号
     ns                      输入I信号样点长度,固定为16384个点
     SampleRate              输入信号的采样率，单位Hz
     _FunNum                 保留参数，任意值
     输出：
     result[0]               modnum:   输出识别结果，只有当输入fs改变或第1次时才会初始化为0
     result[1]               est_res:  输出符号率估计结果
     result[2]               freq_dev: 输出当FSK信号时，两个载波频率之间的shift
     返回值：
     异常：-1，正常完成识别：0
     *************************************/
    public int onHRCalcCharacterParam(short []dataIArray, short []dataQArray, int ns,
                                      double SampleRate, boolean _FunNum, double []result)
    {
        return HRCalcCharacterParam(dataIArray, dataQArray, ns, SampleRate, _FunNum, result);
    }
}
