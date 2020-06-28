package com.huari.Presenter.UI.Impl.File;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;
import com.huari.Presenter.Transactions.MutilSignal.MultiTransaction;
import com.huari.Presenter.UI.Interface.MultiSignalPresenter;
import com.huari.Presenter.entity.Constant;

import java.lang.ref.WeakReference;

public class FileMultiSignalPresenterImpl implements MultiSignalPresenter<String> {
    WeakReference<IBaseView> baseViewWeakReference;
    BusinessCoreNet businessCoreNet;

    public FileMultiSignalPresenterImpl(WeakReference<IBaseView> baseViewWeakReference) {
        this.baseViewWeakReference = baseViewWeakReference;
        this.businessCoreNet = BusinessCoreNet.getInstanceNet();
    }

    @Override
    public void StartMultiSignal(String request) {
        businessCoreNet.MultipathDDCStart(request,baseViewWeakReference.get());
    }

    @Override
    public void EndMultiSignal(String request) {
        businessCoreNet.MultipathDDCEnd();
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
        baseViewWeakReference =new WeakReference<>(baseView);
    }

    @Override
    public void destory() {
        baseViewWeakReference = null;
    }
}
