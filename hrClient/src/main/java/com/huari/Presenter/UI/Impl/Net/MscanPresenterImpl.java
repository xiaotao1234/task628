package com.huari.Presenter.UI.Impl.Net;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.UI.Interface.MscanPresenter;
import com.huari.Presenter.entity.Request;

import java.lang.ref.SoftReference;

public class MscanPresenterImpl implements MscanPresenter<Request> {

    SoftReference<IBaseView> mReferenceView;

    public MscanPresenterImpl(IBaseView baseView) {
        mReferenceView = new SoftReference<>(baseView);
    }

    @Override
    public void startMscan(Request request) {
        BusinessCoreNet.getInstanceNet().MSCANStart(request, mReferenceView.get());
    }

    @Override
    public void endMscan(Request request) {
        BusinessCoreNet.getInstanceNet().MSCANEnd(request, mReferenceView.get());
    }

    @Override
    public void attachView(IBaseView baseView) {
        mReferenceView = new SoftReference<>(baseView);
    }

    @Override
    public void destory() {
        mReferenceView = null;
    }
}
