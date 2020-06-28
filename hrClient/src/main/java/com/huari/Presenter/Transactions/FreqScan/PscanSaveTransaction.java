package com.huari.Presenter.Transactions.FreqScan;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Transactions.CommonTransactions.SerializableDataSaveTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;

import java.util.List;

public class PscanSaveTransaction<T, M> extends SerializableDataSaveTransaction<T, M> {
    public PscanSaveTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, String name, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule, name, iBaseView);
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN;
    }
}
