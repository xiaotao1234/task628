package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MscanUI;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.Mscan.MscanTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MscanStartTaskHandle extends TaskHandle {
    MscanStartParameter mscanStartParameter;

    public MscanStartTaskHandle(MscanStartParameter baseTaskParameter) {
        super(baseTaskParameter);
        this.mscanStartParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        MscanTransaction mscanTransaction = (MscanTransaction) Factory.getInstance().createTransaction(Constant.MScanStart, mscanStartParameter.taskSchedule, (IBaseView) mscanStartParameter.baseView.get());
        mscanTransaction.taskSchedule.addTransaction(mscanTransaction);
        baseTaskParameter.map.put(mscanStartParameter.taskName, mscanTransaction);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((MscanUI)mscanStartParameter.baseView.get()).requestStartCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        mscanStartParameter.taskSchedule.sendCommandNet(mscanStartParameter.request);
    }

    public static class MscanStartParameter extends BaseTaskParameter {
        Request request;
        WeakReference<IBaseView> baseView;

        public MscanStartParameter(Request request, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule, IBaseView baseView) {
            super(taskName, map, taskSchedule);
            this.request = request;
            this.baseView = new WeakReference<>(baseView);
        }
    }
}
