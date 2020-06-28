package com.huari.Presenter.UI.Interface;

public interface MultiSignalPresenter<T> extends BasePresenter{
    public void StartMultiSignal(T request);

    public void EndMultiSignal(T request);

    public void DDC1VoiceStart();

    public void DDC2VoiceStart();

    public void DDC1VoiceEnd();

    public void DDC2VoiceEnd();

    public void DDC1SpectrumStart();

    public void DDC2SpectrumStart();

    public void DDC1SpectrumEnd();

    public void DDC2SpectrumEnd();
}
