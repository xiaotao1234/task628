package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MscanUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MscanEndTaskHandle extends TaskHandle {
    MscanEndParameter mscanEndParameter;

    public MscanEndTaskHandle(MscanEndParameter baseTaskParameter) {
        super(baseTaskParameter);
        this.mscanEndParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        CancelTransaction(mscanEndParameter.taskName);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((MscanUI)mscanEndParameter.baseViewWeakReference.get()).requestEndCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        mscanEndParameter.taskSchedule.sendCommandNet(mscanEndParameter.request);
    }

    public static class MscanEndParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public MscanEndParameter(Request request, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule, IBaseView baseView) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
