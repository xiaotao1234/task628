package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.PscanUI;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.FreqScan.PScanTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class PSCANStartTaskHandle extends TaskHandle {
    PSCANStartParameter pscanStartParameter;

    public PSCANStartTaskHandle(PSCANStartParameter pscanStartParameter) {
        super(pscanStartParameter);
        this.pscanStartParameter = pscanStartParameter;
    }

    @Override
    public void requestSuccess() {
        PScanTransaction pScanTransaction = (PScanTransaction) Factory.getInstance().createTransaction(Constant.PScanStart, pscanStartParameter.taskSchedule, pscanStartParameter.baseViewWeakReference.get());
        pscanStartParameter.taskSchedule.addTransaction(pScanTransaction);
        baseTaskParameter.map.put(pscanStartParameter.taskName, pScanTransaction);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((PscanUI)pscanStartParameter.baseViewWeakReference.get()).requestStartCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        pscanStartParameter.taskSchedule.sendCommandNet(pscanStartParameter.request);
    }

    public static class PSCANStartParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public PSCANStartParameter(Request request, IBaseView baseView, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
