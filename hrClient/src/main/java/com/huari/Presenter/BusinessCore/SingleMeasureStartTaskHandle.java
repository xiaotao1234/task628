package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.Singlemeasure.SingleMeasureTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SingleMeasureStartTaskHandle extends TaskHandle {
    SingalMeasureParameter singalMeasureParameter;

    public SingleMeasureStartTaskHandle(SingalMeasureParameter singalMeasureParameter) {
        super(singalMeasureParameter);
        this.baseTaskParameter = singalMeasureParameter;
        this.singalMeasureParameter = singalMeasureParameter;
    }

    @Override
    public void requestSuccess() {
        Factory factory = Factory.getInstance();
        SingleMeasureTransaction singleMeasureTransaction = (SingleMeasureTransaction) factory.createTransaction(singalMeasureParameter.taskName, singalMeasureParameter.taskSchedule, singalMeasureParameter.baseViewWeakReference.get());
        baseTaskParameter.taskSchedule.addTransaction(singleMeasureTransaction);
        baseTaskParameter.map.put(singalMeasureParameter.taskName, singleMeasureTransaction);
    }

    @Override
    public void requestFailed() {
    }

    @Override
    public void callbackCommon(String s) {
        ((SingleMeasureUI) singalMeasureParameter.baseViewWeakReference.get()).requestStartCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        singalMeasureParameter.taskSchedule.sendCommandNet(singalMeasureParameter.request);
    }

    public static class SingalMeasureParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public SingalMeasureParameter(Request request, IBaseView baseView, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
