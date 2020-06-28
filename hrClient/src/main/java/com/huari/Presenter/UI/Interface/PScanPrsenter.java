package com.huari.Presenter.UI.Interface;

public interface PScanPrsenter<T> extends BasePresenter {
    public void PScanStart(T request);//开启单频测量，参数的设置更改同样在此生效

    public void PScanEnd(T request);//结束单频测量

    public void FreqyencyHoppingStart();

    public void FreqyencyHoppingEnd();

    public void SignalSortStart(float step, int smoothFrame, int dataType, boolean isLineThreshold, float lineThresholdValue);

    public void SignalSortEnd();

}
