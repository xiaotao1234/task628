package com.huari.Presenter.UI.Interface;

public interface HopDectectorPresenter<T> extends BasePresenter {
    public void startHopDectector(T request, double freqStep);//开启离散数据

    public void endHopDectector(T request);//结束离散数据

}
