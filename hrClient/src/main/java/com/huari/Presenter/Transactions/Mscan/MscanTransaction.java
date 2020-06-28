package com.huari.Presenter.Transactions.Mscan;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.LevelData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MscanUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;
import com.huari.Presenter.entity.UI.MscanData;

import java.lang.ref.WeakReference;
import java.util.List;

public class MscanTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;

    public MscanTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(iBaseView);
    }

    @Override
    public T perform(T t) {
        if (((DataPackage) t).Data.containsKey(DataType.MSCAN.toString())) {
            LevelData[] levelData = (LevelData[]) ((DataPackage) t).Data.get(DataType.MSCAN.toString());
            MscanData mscanData = new MscanData(levelData.length);
            for (int i = 0; i < levelData.length; i++) {
                mscanData.CenterFreq[i] = (float) levelData[i].CenterFreq;
                mscanData.LevelFast[i] = levelData[i].LevelFast / 10.f;
                mscanData.LevelPeak[i] = levelData[i].LevelPeak / 10.f;
                mscanData.LevelAvg[i] = levelData[i].LevelAvg / 10.f;
                mscanData.LevelRMS[i] = levelData[i].LevelRMS / 10.f;
            }
            ((MscanUI) baseViewWeakReference.get()).MscanDataCallback(mscanData);
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.MSCAN;
    }
}
