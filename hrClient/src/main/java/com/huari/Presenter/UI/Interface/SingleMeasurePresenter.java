package com.huari.Presenter.UI.Interface;

import java.util.Map;

public interface SingleMeasurePresenter<T> extends BasePresenter {

    public void SingleMeasureStart(T request);//开启单频测量，参数的设置更改同样在此生效

    public void SingleMeasureEnd(T request);//结束单频测量

    public void IQDataSave(long time);//定时保存IQ数据

    public void ITUShow(short funcNum, double BW, double IQSampleRate, double xdbV, double betadbV);//显示信号质量、场强等参数

    public void ITUEnd();//停止参数显示

    public void startRecord(String filename, Map<String, Object> parameter);//开始记录数据

    public void endRecord();//结束记录数据

    public void startRecordVoiceData();//开始记录音频数据

    public void endRecordVoiceData();//停止记录音频数据

    public void startAudio();//开始播放声音

    public void endAudio();//停止播放声音

    public void maxValueShow();//显示最大值

    public void minValueShow();//显示最小值

    public void arvageValueShow();//显示平均值

    public void maxValueHide();//显示最大值

    public void minValueHide();//显示最小值

    public void arvageValueHide();//显示平均值

//    public void showWaveDirectionNow(OnSingleDataListener OnSingleDataListener);//地图显示当前来波方向
//
//    public void endShowWaveDirectionNow(OnSingleDataListener OnSingleDataListener);//停止显示当前来波方向
//
//    public void showWaveDirectionMaxProbablity(OnSingleDataListener OnSingleDataListener);//地图显示最大概率来波方向
//
//    public void endShowWaveDirectionMaxProbablity(OnSingleDataListener OnSingleDataListener);//停止地图显示最大概率来波方向
//
//    public void showDeviceLocation(OnSingleDataListener OnSingleDataListener);//地图显示设备位置
//
//    public void endShowDeviceLocation(OnSingleDataListener OnSingleDataListener);//停止地图显示设备位置
//
//    public void showHistoryWaveDirection(OnSingleDataListener OnSingleDataListener);//地图显示历史来波方向
//
//    public void endShowHistoryWaveDirection(OnSingleDataListener OnSingleDataListener);//停止地图显示历史来波方向
//
//    public void showStatisticsData(OnSingleDataListener OnSingleDataListener);//显示各方向统计值
//

//    public void endShowStatisticsData(OnSingleDataListener OnSingleDataListener);//停止显示各方向统计值


//    public String[] getRendezvousList();//获取交汇数据名称列表
//
//    public T[] getrendezvousData(String s);//获取具体的交汇数据

}
