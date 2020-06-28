package com.huari.Presenter.UI.Impl.Net;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.Transactions.MutilSignal.MultiTransaction;
import com.huari.Presenter.UI.Interface.MultiSignalPresenter;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.SoftReference;

public class MultiSignalPresenterImpl implements MultiSignalPresenter<Request> {
    SoftReference<IBaseView> mReferenceView;

    public MultiSignalPresenterImpl(IBaseView baseView) {
        mReferenceView = new SoftReference<>(baseView);
    }

    @Override
    public void StartMultiSignal(Request request) {
        BusinessCoreNet.getInstanceNet().MultipathDDCStart(request, mReferenceView.get());
    }

    @Override
    public void EndMultiSignal(Request request) {
        BusinessCoreNet.getInstanceNet().MultipathDDCEnd(request, mReferenceView.get());
    }

    @Override
    public void DDC1VoiceStart() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC1Voice(true);
    }

    @Override
    public void DDC2VoiceStart() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC2Voice(true);
    }

    @Override
    public void DDC1VoiceEnd() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC1Voice(false);
    }

    @Override
    public void DDC2VoiceEnd() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC2Voice(false);
    }

    @Override
    public void DDC1SpectrumStart() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC1Data(true);
    }

    @Override
    public void DDC2SpectrumStart() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC2Data(true);
    }

    @Override
    public void DDC1SpectrumEnd() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC1Data(false);
    }

    @Override
    public void DDC2SpectrumEnd() {
        ((MultiTransaction) BusinessCoreNet.getInstanceNet().getMap().get(Constant.MultiSignalStart)).setDDC2Data(false);
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
