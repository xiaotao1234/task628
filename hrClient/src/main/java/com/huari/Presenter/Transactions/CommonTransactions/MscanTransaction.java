package com.huari.Presenter.Transactions.CommonTransactions;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.LevelData;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.UI.MscanData;
import com.huari.Presenter.entity.DataType;

import java.util.List;

public class MscanTransaction<T, M> extends Transaction<T, M> {
    MscanData mscanData;

    public MscanTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
    }

    @Override
    public T perform(T t) {
        LevelData[] levelData = (LevelData[]) ((DataPackage) t).Data.get(DataType.MSCAN);
        int frameLength = levelData.length;
        mscanData = new MscanData(frameLength);
        mscanData.CenterFreq = new float[frameLength];
        mscanData.LevelFast = new float[frameLength];
        mscanData.LevelAvg = new float[frameLength];
        mscanData.LevelPeak = new float[frameLength];
        mscanData.LevelRMS = new float[frameLength];

        for (int i = 0; i < levelData.length; i++) {
            mscanData.CenterFreq[i] = (float) levelData[i].CenterFreq;
            mscanData.LevelRMS[i] = (float) levelData[i].LevelRMS / 10f;
            mscanData.LevelPeak[i] = (float) levelData[i].LevelPeak / 10f;
            mscanData.LevelAvg[i] = (float) levelData[i].LevelAvg / 10f;
            mscanData.LevelFast[i] = (float) levelData[i].LevelFast / 10f;
        }
        return null;
    }

    @Override
    public boolean handle(M m) {
        return false;
    }

    @Override
    public void beCancel() {
        super.beCancel();
    }

    @Override
    public void beAdd() {
        super.beAdd();
//        mscanData = new MscanData();
    }
}

