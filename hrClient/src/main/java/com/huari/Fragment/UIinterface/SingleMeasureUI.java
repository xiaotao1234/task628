package com.huari.Fragment.UIinterface;

import com.huari.Presenter.entity.UI.SingleMeasureData;
import com.huari.Presenter.entity.dataPersistence.ITUResult;

public interface SingleMeasureUI extends IBaseView{
    void onData(SingleMeasureData data);

    void onCommand(boolean status);

    void onItuData(ITUResult ituResult);

    void onDemodulationData(String demod_str, float percent);

    void onSave(boolean status, float time_long);

    void requestStartCallback(String result);

    void requestEndCallback(String result);
}
