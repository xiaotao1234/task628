package com.huari.Fragment.UIinterface;

import com.huari.Presenter.entity.UI.MscanData;

public interface MscanUI extends IBaseView{
    void MscanDataCallback(MscanData mscanData);

    void requestStartCallback(String result);

    void requestEndCallback(String result);
}
