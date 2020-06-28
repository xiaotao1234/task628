package com.huari.Presenter.UI.Impl.File;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.SingleMeasurePresenter;

import java.lang.ref.WeakReference;
import java.util.Map;

public class FileSingleMeasurePresenterImpl implements SingleMeasurePresenter<String> {
    WeakReference<IBaseView> baseViewWeakReference;

    public FileSingleMeasurePresenterImpl(WeakReference<IBaseView> baseViewWeakReference) {
        this.baseViewWeakReference = baseViewWeakReference;
    }

    @Override
    public void SingleMeasureStart(String request) {
        BusinessCoreNet.getInstanceNet().SingleMeasureStart(request, baseViewWeakReference.get());
    }

    @Override
    public void SingleMeasureEnd(String request) {
        BusinessCoreNet.getInstanceNet().SingleMeasureEnd();
    }

    @Override
    public void IQDataSave(long time) {

    }

    @Override
    public void ITUShow(short funcNum, double BW, double IQSampleRate, double xdbV, double betadbV) {
        BusinessCoreNet.getInstanceNet().ITUStart(funcNum, BW, IQSampleRate, xdbV, betadbV, baseViewWeakReference.get());
    }

    @Override
    public void ITUEnd() {
        BusinessCoreNet.getInstanceNet().ITUEnd();
    }

    @Override
    public void startRecord(String filename, Map<String, Object> parameter) {

    }

    @Override
    public void endRecord() {

    }

    @Override
    public void startRecordVoiceData() {
        BusinessCoreNet.getInstanceNet().AudioPlayerStart();
    }

    @Override
    public void endRecordVoiceData() {
        BusinessCoreNet.getInstanceNet().AudioDataSaveEnd();
    }

    @Override
    public void startAudio() {
        BusinessCoreNet.getInstanceNet().AudioPlayerStart();
    }

    @Override
    public void endAudio() {
        BusinessCoreNet.getInstanceNet().AudioPlayerEnd();
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
        baseViewWeakReference = null;
    }
}
