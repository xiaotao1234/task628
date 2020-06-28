package com.huari.Fragment.UIinterface;

public interface HopDectectorUI extends IBaseView{

    void HopDectectorResultCallback(double[] hops);

    void SpectrumDataback(short[] Spectrum);

    void requestStartCallback(String result);

    void requestEndCallback(String result);
}
