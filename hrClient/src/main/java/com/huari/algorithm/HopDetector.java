package com.huari.algorithm;

import com.huari.Fragment.UIinterface.HopDectectorUI;
import com.huari.Fragment.UIinterface.IBaseView;

import java.lang.ref.WeakReference;

public class HopDetector {

    static {
        System.loadLibrary("algorithm_lib");
    }

    private int id;
    private double[] freqSet;

    /******************************Native Lib and declare*******************/
    private native int createDetector();

    private native Object[] detect(int detectorId, short[] data, double sfreq, double efreq, double df, double th1_deltaf, double the2_deltaf);

    private native void releaseDetector(int objId);

    /*************************************end********************************/

    WeakReference<IBaseView> hopDectectorUIWeakReference;

    public void setOnHopDectectorListener(IBaseView baseView) {
        hopDectectorUIWeakReference = new WeakReference<>(baseView);
    }

    public double[] getFreqSet() {
        return freqSet;
    }

    //创建跳频发现对象
    public HopDetector() {
        id = createDetector();
    }

    /***************功能说明*********************
     输入：
     data                       输入的频谱扫描数据
     sfreq                      扫描起始频率，单位Hz
     efreq                      扫描截止频率，单位Hz
     df                         扫描的频率分辨率，单位Hz
     th1_deltaf                 输入允许相邻跳信号的最大频率间隔门限1，为绝对频率值，如500e3表示500kHz，当中心频率间隔<th1_deltaf时会删除掉
     1个，算法将th1_deltaf的最大值限制为500e3
     th2_deltaf                 输入允许相邻跳信号的最大频率间隔门限2，为相对值，如50表示50倍的频率分辨率，当中心频率间隔<th2_deltaf*频率分辨率时
     会删除掉1个，算法将th2_deltaf的最大值限制为50
     输出（对应于成员变量）：
     freqSet                    频率集合
     返回值：
     异常： - 1，正常完成检测：发现的跳频个数
     *************************************/
    public int detect(short[] data, double sfreq, double efreq, double df, double th1_deltaf, double the2_deltaf) {
        Object[] resultData = detect(id, data, sfreq, efreq, df, th1_deltaf, the2_deltaf);
        //Parse object
        if (resultData == null)                // consider exception
            return -1;

        if (resultData.length <= 0)            // consider exception
            return -1;

        if (resultData[0] == null)             // no hopping found
            return -1;
        double[] hops = (double[]) resultData[0];
        if (hops.length <= 0)               // no hopping found
            return -1;

        freqSet = hops;                     //get the set of hopping frequencies
        if (hopDectectorUIWeakReference.get() != null)
            ((HopDectectorUI) hopDectectorUIWeakReference.get()).HopDectectorResultCallback(hops);
        return freqSet.length;
    }

    //释放跳频发现对象内存空间
    public void release()                  // It's very import to notify JNI releasing memory of C++ object
    {
        releaseDetector(id);
    }
}
