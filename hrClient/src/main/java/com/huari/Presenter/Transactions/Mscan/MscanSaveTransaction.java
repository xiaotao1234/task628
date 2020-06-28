package com.huari.Presenter.Transactions.Mscan;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Transactions.CommonTransactions.SerializableDataSaveTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;

import java.util.List;

public class MscanSaveTransaction<T, M> extends SerializableDataSaveTransaction<T, M> {
    public MscanSaveTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, String name, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule, name, iBaseView);
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.MSCAN;
    }
}
