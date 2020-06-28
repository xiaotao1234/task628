package com.huari.Presenter.UI.Interface;

public interface MscanPresenter<T> extends BasePresenter {

    public void startMscan(T request);//开启离散数据

    public void endMscan(T request);//结束离散

}
