package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MultiUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MultipathDDCEndTaskHandle extends TaskHandle {
    MultipathDDCEndParameter multipathDDCEndParameter;

    public MultipathDDCEndTaskHandle(MultipathDDCEndParameter baseTaskParameter) {
        super(baseTaskParameter);
        multipathDDCEndParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        CancelTransaction(multipathDDCEndParameter.taskName);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((MultiUI) multipathDDCEndParameter.mReferenceView.get()).requestEndCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        multipathDDCEndParameter.taskSchedule.sendCommandNet(multipathDDCEndParameter.request);
    }

    public static class MultipathDDCEndParameter extends BaseTaskParameter {
        WeakReference<IBaseView> mReferenceView;
        Request request;

        public MultipathDDCEndParameter(Request request, IBaseView baseView, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
            super(taskName, map, taskSchedule);
            this.request = request;
            this.mReferenceView = new WeakReference<>(baseView);
        }
    }
}
