package com.huari.Presenter.UI.Impl.Net;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.PScanPrsenter;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;

public class PScanPresenterImpl implements PScanPrsenter<Request> {
    WeakReference<IBaseView> baseViewWeakReference;
    private BusinessCoreNet businessCoreNet;

    public PScanPresenterImpl(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
        businessCoreNet = BusinessCoreNet.getInstanceNet();
    }

    @Override
    public void PScanStart(Request request) {
        businessCoreNet.PSCANStart(request, baseViewWeakReference.get());
    }

    @Override
    public void PScanEnd(Request request) {
        businessCoreNet.PSCANEnd(request, baseViewWeakReference.get());
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
