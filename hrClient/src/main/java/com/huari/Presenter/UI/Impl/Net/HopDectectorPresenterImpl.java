package com.huari.Presenter.UI.Impl.Net;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.HopDectectorPresenter;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;

public class HopDectectorPresenterImpl implements HopDectectorPresenter<Request> {

    WeakReference<IBaseView> baseViewWeakReference;

    public HopDectectorPresenterImpl(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public void startHopDectector(Request request, double freqStep) {
        BusinessCoreNet.getInstanceNet().HopDectectorStart(request, (IBaseView) baseViewWeakReference.get(), freqStep);
    }

    @Override
    public void endHopDectector(Request request) {
        BusinessCoreNet.getInstanceNet().HopDectectorEnd(request, (IBaseView) baseViewWeakReference.get());
    }

    @Override
    public void attachView(IBaseView baseView) {

    }

    @Override
    public void destory() {
        baseViewWeakReference = null;
    }
}
