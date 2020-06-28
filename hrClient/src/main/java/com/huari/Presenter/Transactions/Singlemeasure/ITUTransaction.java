package com.huari.Presenter.Transactions.Singlemeasure;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.IQData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

public class ITUTransaction<T, M> extends Transaction<T, M> {

    Arithmetic arithmetic;
    WeakReference<IBaseView> baseViewWeakReference;

    public ITUTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, Arithmetic arithmetic, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(baseView);
        this.arithmetic = arithmetic;
    }

    @Override
    public T perform(T t) {
        if (arithmetic != null) {
            DataPackage dataPackage = (DataPackage) t;
            arithmetic.handle(((IQData) (dataPackage.Data.get("IQ"))).IQData, (IBaseView)baseViewWeakReference.get());
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.SingleMeasure;
    }

    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }
}
