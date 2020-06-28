package com.huari.Presenter.Transactions.FreqScan;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

public class SignalSortTransaction<T, M> extends Transaction<T, M> {
    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }

    Arithmetic arithmetic;
    WeakReference<IBaseView> baseViewWeakReference;

    public SignalSortTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, Arithmetic arithmetic, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.arithmetic = arithmetic;
        baseViewWeakReference = new WeakReference<>(iBaseView);
    }

    @Override
    public T perform(T t) {
        if (arithmetic != null && baseViewWeakReference.get() != null) {
            taskSchedule.IOexecutorService.execute(() -> {
                DataPackage dataPackage = (DataPackage) t;
                DataPackage dataPackageCopy = new DataPackage();
                dataPackageCopy.DataType = dataPackage.DataType;
                dataPackageCopy.Data.putAll(dataPackage.Data);
                arithmetic.handle(((SpectrumData) (dataPackage.Data.get("Spectrum"))).Spectrum, baseViewWeakReference.get());
            });
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        if (arithmetic != null)
            arithmetic.cancel();
        baseViewWeakReference = null;
    }

    @Override
    public void beAdd() {
        super.beAdd();
        if (arithmetic != null)
            arithmetic.add();
    }
}
