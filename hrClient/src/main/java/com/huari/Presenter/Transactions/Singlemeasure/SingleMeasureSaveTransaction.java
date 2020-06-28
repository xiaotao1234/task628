package com.huari.Presenter.Transactions.Singlemeasure;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Transactions.CommonTransactions.SerializableDataSaveTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;

import java.util.List;

public class SingleMeasureSaveTransaction extends SerializableDataSaveTransaction {
    public SingleMeasureSaveTransaction(String eventType, int taskSetNumber, List preEventType, int referenceCount, TaskSchedule taskSchedule, String name, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule, name, baseView);
    }

    @Override
    public boolean handle(Object o) {
        return o == DataTypeEnum.SingleMeasure;
    }
}
