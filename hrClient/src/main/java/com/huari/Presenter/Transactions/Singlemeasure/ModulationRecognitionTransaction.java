package com.huari.Presenter.Transactions.Singlemeasure;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.IQData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Impl.ModulationrecognitionArithmetic;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 调制解调算法任务
 *
 * @param <T>
 * @param <M>
 */

public class ModulationRecognitionTransaction<T, M> extends Transaction<T, M> {

    WeakReference<IBaseView> baseViewWeakReference;
    Arithmetic arithmetic;

    public ModulationRecognitionTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        arithmetic = new ModulationrecognitionArithmetic();
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }

    @Override
    public T perform(T t) {
        if (arithmetic != next) {
            DataPackage dataPackage = (DataPackage) t;
            arithmetic.handle(((IQData) (dataPackage.Data.get("IQ"))).IQData, (SingleMeasureUI)baseViewWeakReference.get());
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.SingleMeasure;
    }
}
