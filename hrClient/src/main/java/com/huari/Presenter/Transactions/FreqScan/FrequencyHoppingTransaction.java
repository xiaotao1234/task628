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

public class FrequencyHoppingTransaction<T, M> extends Transaction<T, M> {
    Arithmetic arithmetic;
    WeakReference<IBaseView> baseViewWeakReference;

    public FrequencyHoppingTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, Arithmetic arithmetic, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(baseView);
        this.arithmetic = arithmetic;
    }

    @Override
    public T perform(T t) {
        if (arithmetic != null && baseViewWeakReference != null) {
            taskSchedule.IOexecutorService.execute(() -> {
                DataPackage dataPackage = (DataPackage) t;
                DataPackage dataPackageCopy = new DataPackage();
                dataPackageCopy.DataType = dataPackage.DataType;
                dataPackageCopy.Data.putAll(dataPackage.Data);
                arithmetic.handle(((SpectrumData) (dataPackage.Data.get("Spectrum"))).Spectrum, (IBaseView)baseViewWeakReference.get());
            });
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN;
    }

    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
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
