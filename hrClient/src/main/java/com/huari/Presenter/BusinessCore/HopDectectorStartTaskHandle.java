package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.HopDectectorUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Impl.HopDectectorArithmetic;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.HopDectector.HopDectectorTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class HopDectectorStartTaskHandle extends TaskHandle {
    HopDectectorStartParameter hopDectectorStartParameter;

    public HopDectectorStartTaskHandle(HopDectectorStartParameter baseTaskParameter) {
        super(baseTaskParameter);
        hopDectectorStartParameter = baseTaskParameter;
    }

    @Override
    public void requestSuccess() {
        HopDectectorTransaction hopDectectorTransaction = (HopDectectorTransaction) Factory.getInstance().createTransaction(hopDectectorStartParameter.taskName, hopDectectorStartParameter.taskSchedule, (IBaseView) hopDectectorStartParameter.baseViewWeakReference.get());
        hopDectectorTransaction.setFreqStep(hopDectectorStartParameter.freqStep);
        HopDectectorArithmetic hopDectectorArithmetic = new HopDectectorArithmetic();
        hopDectectorTransaction.setArithmetic(hopDectectorArithmetic);
        hopDectectorStartParameter.taskSchedule.addTransaction(hopDectectorTransaction);
        hopDectectorStartParameter.map.put(hopDectectorStartParameter.taskName, hopDectectorTransaction);
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((HopDectectorUI) hopDectectorStartParameter.baseViewWeakReference.get()).requestStartCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        hopDectectorStartParameter.taskSchedule.sendCommandNet(hopDectectorStartParameter.request);
    }

    public static class HopDectectorStartParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;
        double freqStep;

        public HopDectectorStartParameter(Request request, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule, double freqStep, IBaseView baseView) {
            super(taskName, map, taskSchedule);
            this.freqStep = freqStep;
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
