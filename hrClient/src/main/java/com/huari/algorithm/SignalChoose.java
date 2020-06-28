package com.huari.algorithm;

public class SignalChoose {
    static {
        System.loadLibrary("algorithm_lib");
    }
    private native void SigChooseProcDllInit();
    private native int SigChooseProcDllClear(int objId);
    private native int SigChooseProcFun(int objId, int maxSigNum, float []scandata,
                                        double StartFreq, double StopFreq, float Step, int SmoothFrame, int DataType,
                                        boolean isLineThreshold, float lineThresholdValue);

    private int maxSigNum = 2000; //最大信号个数

    /****************return data************************/
    //调用SigChooseProcFun之后，如果return >0, 这些成员变量将会被C库赋值
    public float[] AutoThreshold;
    public float[] FreqPointSN;
    public double[] CenterFreq;
    public int[] CenterFreqIndex;
    public float[] CenterFreqAmp;
    public float[] SignalBand;
    public float[] SNR;
    public String[] SignalType;
    public String[] SigModType;
    /*************************************************/
    public SignalChoose()
    {
        SigChooseProcDllInit();
    }

    //使用完之后，需要调用该函数清楚C++中的对象，其中objId是SigChooseFun使用的id
    public void SigChooseClear(int objId)
    {
        SigChooseProcDllClear(objId);
    }

    /**************功能说明******************************************************
     函数名称:信号分选主程序
     创建时间:  2020/05/25
     输入：
     int objId               对象ID，取值范围为【0-1024】，由调用者自己控制
     scandata,								一次宽带扫描的的频谱数据
     StartFreq,							起始频率KHz
     StopFreq,	            	终止频率KHz
     Step                    扫描步进KHz
     SmoothFrame             时间平滑帧数
     DataType                数据类型，0为短波[终止频率<30M]   1为超短波[起始频率>30M], 横跨了30M，选择超短波
     isLineThreshold         是否使用直线门限，true为使用直线门限进行信号提取
     lineThresholdValue      直线门限值，按照此门限值进行信号提取
     输出（对应于成员变量）：
     AutoThreshold          自动门限值 数组
     FreqPointSN            每个频点信噪比 数组
     CenterFreq             信号中心频率KHz 数组
     CenterFreqIndex        信号中心频率索引号 数组
     CenterFreqAmp          信号中心频率幅值 数组
     SignalBand             信号带宽KHz 数组
     SNR                    信号信噪比 数组
     SignalType             信号类型 数组
     SigModType             信号制式 数组
     返回值：
     -1:“平滑帧数不够”；	   > 0:“信号个数”
     ***************************************************************************/

    public int SigChooseFun(int objId, float []scandata,
                            double StartFreq, double StopFreq, float Step, int SmoothFrame, int DataType,
                            boolean isLineThreshold, float lineThresholdValue)
    {
        return SigChooseProcFun(objId, maxSigNum, scandata, StartFreq, StopFreq, Step, SmoothFrame, DataType, isLineThreshold, lineThresholdValue);
    }
}
