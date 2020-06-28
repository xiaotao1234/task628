package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MultiUI;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.MutilSignal.MultiTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MultipathDDCStartTaskHandle extends TaskHandle {
    MultipathDDCStartParameter multipathDDCStartParameter;

    public MultipathDDCStartTaskHandle(MultipathDDCStartParameter baseTaskParameter) {
        super(baseTaskParameter);
        multipathDDCStartParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        MultiTransaction multiTransaction = (MultiTransaction) Factory.getInstance().createTransaction(Constant.MultiSignalStart, multipathDDCStartParameter.taskSchedule, (IBaseView) multipathDDCStartParameter.mReferenceView.get());
        multipathDDCStartParameter.taskSchedule.addTransaction(multiTransaction);
        baseTaskParameter.map.put(multipathDDCStartParameter.taskName, multiTransaction);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        if (multipathDDCStartParameter.mReferenceView != null)
            ((MultiUI) multipathDDCStartParameter.mReferenceView.get()).requestStartCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        multipathDDCStartParameter.taskSchedule.sendCommandNet(multipathDDCStartParameter.request);
    }

    public static class MultipathDDCStartParameter extends BaseTaskParameter {
        WeakReference<IBaseView> mReferenceView;
        Request request;

        public MultipathDDCStartParameter(Request request, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule, IBaseView iBaseView) {
            super(taskName, map, taskSchedule);
            this.request = request;
            mReferenceView = new WeakReference<>(iBaseView);
        }
    }
}
