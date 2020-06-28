package com.huari.Presenter.BusinessCore;

import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SingleMeasureEndTaskHandle extends TaskHandle {
    SingalMeasureEndParameter singalMeasureEndParameter;

    public SingleMeasureEndTaskHandle(SingalMeasureEndParameter singalMeasureEndParameter) {
        super(singalMeasureEndParameter);
        this.singalMeasureEndParameter = singalMeasureEndParameter;
    }

    @Override
    public void requestSuccess() {
        CancelSingleMeasureTransactions();
    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void callbackCommon(String s) {
        ((SingleMeasureUI)singalMeasureEndParameter.baseViewWeakReference.get()).requestEndCallback(s);
    }

    @Override
    public void afterTaskAdd() {
        singalMeasureEndParameter.taskSchedule.sendCommandNet(singalMeasureEndParameter.request);
    }

    /**
     * 取消单频相关任务
     */
    private void CancelSingleMeasureTransactions() {
        CancelTransaction(Constant.SingleMeasureStart);
        CancelTransaction(Constant.IQSave);
        CancelTransaction(Constant.MR);
        CancelTransaction(Constant.Audio);
        CancelTransaction(Constant.AudioSave);
        CancelTransaction(Constant.ITU);
        CancelTransaction(Constant.SingleMeasureSave);
    }

    public static class SingalMeasureEndParameter extends BaseTaskParameter {
        WeakReference<IBaseView> baseViewWeakReference;
        Request request;

        public SingalMeasureEndParameter(Request request, IBaseView baseView, String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
            super(taskName, map, taskSchedule);
            this.request = request;
            baseViewWeakReference = new WeakReference<>(baseView);
        }
    }
}
