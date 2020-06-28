package com.huari.Presenter.UI.Impl.File;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.PScanPrsenter;

import java.lang.ref.WeakReference;

public class FilePScanPresenterImpl implements PScanPrsenter<String> {
    WeakReference<IBaseView> baseViewWeakReference;
    BusinessCoreNet businessCoreNet;

    public FilePScanPresenterImpl(WeakReference<IBaseView> iBaseView) {
        baseViewWeakReference = iBaseView;
        businessCoreNet = BusinessCoreNet.getInstanceNet();
    }

    @Override
    public void PScanStart(String request) {
        businessCoreNet.PSCANStart(request, baseViewWeakReference.get());
    }

    @Override
    public void PScanEnd(String request) {

    }

    @Override
    public void FreqyencyHoppingStart() {
        businessCoreNet.FreqyencyHoppingStart(baseViewWeakReference.get());
    }

    @Override
    public void FreqyencyHoppingEnd() {
        businessCoreNet.FreqyencyHoppingEnd();
    }

    @Override
    public void SignalSortStart(float step, int smoothFrame, int dataType, boolean isLineThreshold, float lineThresholdValue) {
        businessCoreNet.SignalSortStart(step, smoothFrame, dataType, isLineThreshold, lineThresholdValue, baseViewWeakReference.get());
    }

    @Override
    public void SignalSortEnd() {
        businessCoreNet.SignalSortEnd();
    }

    @Override
    public void attachView(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public void destory() {
        baseViewWeakReference = null;
        businessCoreNet = null;
    }
}
