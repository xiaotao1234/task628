package com.huari.Presenter.UI.Impl.Net;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.SingleMeasurePresenter;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SingleMeasurePresenterImpl implements SingleMeasurePresenter<Request> {
    private BusinessCoreNet businessCoreNet;
    WeakReference<IBaseView> iBaseViewWeakReference;

    public SingleMeasurePresenterImpl(IBaseView baseView) {
        businessCoreNet = BusinessCoreNet.getInstanceNet();
        iBaseViewWeakReference = new WeakReference<>(baseView);
    }

    /**
     * 开始单频测量
     *
     * @param request
     */
    @Override
    public void SingleMeasureStart(Request request) {
        businessCoreNet.SingleMeasureStart(request, iBaseViewWeakReference.get());
        businessCoreNet.MoudulationRecognitionStart(iBaseViewWeakReference.get());
    }

    /**
     * 结束单频测量
     *
     * @param request
     */
    @Override
    public void SingleMeasureEnd(Request request) {
        businessCoreNet.SingleMeasureEnd(request, iBaseViewWeakReference.get());
        businessCoreNet.MoudulationRecognitionEnd();
    }

    /**
     * 保存IQ数据
     *
     * @param time
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void IQDataSave(long time) {
        businessCoreNet.IQDataSave(time, iBaseViewWeakReference.get());
    }

    /**
     * 打开ITU测量
     */
    @Override
    public void ITUShow(short funcNum, double BW, double IQSampleRate, double xdbV, double betadbV) {
        businessCoreNet.ITUStart(funcNum, BW, IQSampleRate, xdbV, betadbV, iBaseViewWeakReference.get());
    }

    /**
     * 关闭ITU测量
     */
    @Override
    public void ITUEnd() {
        businessCoreNet.ITUEnd();
    }

    /**
     * 开始保存单频数据
     *
     * @param filename
     */
    @Override
    public void startRecord(String filename, Map<String, Object> parameter) {
        businessCoreNet.SaveSingleMeasureData(iBaseViewWeakReference.get(), filename, parameter);
    }

    /**
     * 停止保存单频数据
     */
    @Override
    public void endRecord() {
        businessCoreNet.SaveSingleMeasureDataEnd();
    }

    /**
     * 开始记录音频数据
     */
    @Override
    public void startRecordVoiceData() {
        businessCoreNet.AudioPlayerStart();
    }

    /**
     * 结束保存音频数据
     */
    @Override
    public void endRecordVoiceData() {
        businessCoreNet.AudioDataSaveEnd();
    }

    /**
     * 开始播放音频
     */
    @Override
    public void startAudio() {
        businessCoreNet.AudioPlayerStart();
    }

    /**
     * 停止播放音频
     */
    @Override
    public void endAudio() {
        businessCoreNet.AudioPlayerEnd();
    }

    @Override
    public void maxValueShow() {

    }

    @Override
    public void minValueShow() {

    }

    @Override
    public void arvageValueShow() {

    }

    @Override
    public void maxValueHide() {

    }

    @Override
    public void minValueHide() {

    }

    @Override
    public void arvageValueHide() {

    }

    @Override
    public void attachView(IBaseView baseView) {

    }

    @Override
    public void destory() {
        businessCoreNet = null;
        iBaseViewWeakReference = null;
    }
}
