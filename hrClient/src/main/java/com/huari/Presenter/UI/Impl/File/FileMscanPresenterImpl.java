package com.huari.Presenter.UI.Impl.File;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.MscanPresenter;

import java.lang.ref.WeakReference;

public class FileMscanPresenterImpl implements MscanPresenter<String> {
    BusinessCoreNet businessCoreNet;
    WeakReference<IBaseView> baseViewWeakReference;

    public FileMscanPresenterImpl(WeakReference<IBaseView> baseViewWeakReference) {
        this.baseViewWeakReference = baseViewWeakReference;
        businessCoreNet = BusinessCoreNet.getInstanceNet();
    }

    @Override
    public void startMscan(String request) {
        businessCoreNet.MSCANStart(request, baseViewWeakReference.get());
    }

    @Override
    public void endMscan(String request) {
        businessCoreNet.MSCANEnd();
    }

    @Override
    public void attachView(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public void destory() {
        baseViewWeakReference.clear();
    }
}
