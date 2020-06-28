package com.huari.Presenter.UI.Interface;

import com.huari.Fragment.UIinterface.IBaseView;

public interface BasePresenter {

    public void attachView(IBaseView baseView);

    public void destory();
}
