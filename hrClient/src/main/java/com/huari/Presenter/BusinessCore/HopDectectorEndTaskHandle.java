package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.HopDectectorUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class HopDectectorEndTaskHandle extends TaskHandle {
    HopDectectorEndParameter hopDectectorEndParameter;

    public HopDectectorEndTaskHandle(HopDectectorEndParameter baseTaskParameter) {
        super(baseTaskParameter);
        hopDectectorEndParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        CancelTransaction(hopDectectorEndParameter.taskName);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((HopDectectorUI)hopDectectorEndParameter.baseViewWeakReference.get()).requestEndCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        hopDectectorEndParameter.taskSchedule.sendCommandNet(hopDectectorEndParameter.request);
    }

    public static class HopDectectorEndParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public HopDectectorEndParameter(Request request, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule, IBaseView baseView) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
