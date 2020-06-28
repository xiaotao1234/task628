package com.huari.Presenter.Transactions.CommonTransactions;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.ReqResult;
import com.huari.Presenter.Interface.Requestcallback;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.util.List;

public class CallbackTransaction<T, M> extends Transaction<T, M> {
    Requestcallback requestcallback;


    public CallbackTransaction(String eventType, int taskSetNumber, List preEventType, int referenceCount, Requestcallback requestcallback, TaskSchedule taskSchedule) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.requestcallback = requestcallback;
    }

    @Deprecated
    public void setCallback(Requestcallback callback) {
        this.requestcallback = callback;
    }

    @Override
    public void work(T t) {
        ReqResult reqResult = ((ReqResult) ((DataPackage) t).Data.get("ReqResult"));
//        if (((DataPackage) t).Data.containsKey("ReqResult") && reqResult.ReqType.equals(eventType)) {
            requestcallback.requestBack(reqResult.Result);
//        } else {
//            super.work(t);
//        }
    }

    @Override
    public T perform(T t) {
        return t;
    }

    @Override
    public void beCancel() {
        super.beCancel();
    }

    @Override
    public void beAdd() {
        super.beAdd();
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.ReqResult;
    }
}
