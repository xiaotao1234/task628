package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.PscanUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class PSCANEndTaskHandle extends TaskHandle {
    PSCANEndParameter parameter;

    public PSCANEndTaskHandle(PSCANEndParameter baseTaskParameter) {
        super(baseTaskParameter);
        this.parameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        CancelTransaction(parameter.taskName);
        CancelPsTransactions();
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((PscanUI)parameter.baseViewWeakReference.get()).requestEndCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        parameter.taskSchedule.sendCommandNet(parameter.request);
    }

    public static class PSCANEndParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public PSCANEndParameter(Request request, IBaseView baseView, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }

    /**
     * 取消频段扫描相关任务
     */
    private void CancelPsTransactions() {
        CancelTransaction(Constant.PScanStart);
        CancelTransaction(Constant.FreqyencyHopping);
        CancelTransaction(Constant.SignalSort);
    }
}
