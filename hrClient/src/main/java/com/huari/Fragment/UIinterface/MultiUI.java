package com.huari.Fragment.UIinterface;

public interface MultiUI extends IBaseView{
    void SpectrumDataback(short[] spectrum);


    void SpectrumDatabackDDC1(short[] spectrum);

    void VoiceDatabackDDC1(byte[] voice);


    void SpectrumDatabackDDC2(short[] spectrum);

    void VoiceDatabackDDC2(byte[] voice);


    void requestStartCallback(String result);

    void requestEndCallback(String result);
}
