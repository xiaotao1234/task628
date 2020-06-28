package com.huari.algorithm;



public class ITUMeasure {

    //测量参数
    public double dataPara;  //调制深度 或者频偏指数（Hz）
    public double PosPara;   //正向调制深度 或者频偏指数（Hz）
    public double NegPara;   //负向调制深度 或者频偏指数（Hz）
    public double XdbBW;    //单位Hz
    public double BetaBW;   //单位Hz

    public double pFreq[];  //
    public short  realFreqLen=0;
    public double pLev[];
    public short  realLevLen=0;

    static {
        System.loadLibrary("algorithm_lib");
    }
    private native int measure(short []dataIArray, short []dataQArray, short num,
                               short funcNum,  double CenterFreq,  double BW,  double IQSampleRate,  double XdbV, double betadbV);
    /***********************ITU测量主函数**********************************************
     *1.功能描述：对输入信号实现信号特征量的测量，包括瞬时频率、频偏、调制度、占用带宽*
     *2.接口参数：                                                                    *
     *      输入：                                                                    *
     *          dataIArray   ：输入I数据数组                                          *
     *          dataQArray   ：输入Q数据数组                                          *
     *          Num          ：输入I/Q数据数据个数                                    *
     *          funcNum      ：输入信号的制式类型号(1.AM;2.FM;3.PM)                   *
     *          CenterFreq   : 输入信号的中心频率                                     *
     *          BW           : 输入信号的采样带宽                                     *
     *          IQSampleRate : 输入信号的采样率                                       *
     *          XdbV         : 输入xdb带宽计算时x的值                                 *
     *          betadbV      : 输入beta%带宽计算时beta的值                            *
     *      输出（对成员变量赋值）：                                                   *
     *          dataPara;   //调制深度 或者频偏指数（Hz）                              *
     *          PosPara;    //正向调制深度 或者频偏指数（Hz）                          *
     *          NegPara;    //负向调制深度 或者频偏指数（Hz）                          *
     *          XdbBW;      //单位Hz                                                 *
     *          BetaBW;     //单位Hz                                                 *
     *          pFreq        : 输出信号的瞬时频率参数数组                              *
     *          FreqLen      : 输出信号的瞬时频率参数数组长度                          *
     *          pLev         : 输出信号的瞬时电平参数数组                              *
     *          LevLen       : 输出信号的瞬时电平参数数组长度                          *
     **********************************************************************************/
    public int onMeasure(short []dataIArray, short []dataQArray, short num,
                         short funcNum,  double CenterFreq,  double BW,  double IQSampleRate,  double XdbV, double betadbV)
    {
        if(num<=0)
            return -1;
        pFreq = new double[num];
        pLev = new  double[num];
        //如果计算成功，ITUMeasure的成员变量将填充测量结果
        int res = measure(dataIArray, dataQArray, num, funcNum,  CenterFreq,  BW,  IQSampleRate,  XdbV, betadbV);
        return res;
    }
}
