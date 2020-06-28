package com.huari.Presenter.Transactions.CommonTransactions;

import android.os.Build;

import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Transactions.ConnectionState.ConnectionStateTransaction;
import com.huari.Presenter.Transactions.DeviceState.DeviceStateTransaction;
import com.huari.Presenter.Transactions.FreqScan.FrequencyHoppingTransaction;
import com.huari.Presenter.Transactions.FreqScan.PScanTransaction;
import com.huari.Presenter.Transactions.FreqScan.SignalSortTransaction;
import com.huari.Presenter.Transactions.HopDectector.HopDectectorTransaction;
import com.huari.Presenter.Transactions.Mscan.MscanTransaction;
import com.huari.Presenter.Transactions.MutilSignal.MultiTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.AudioDataPersistenceFileTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.AudioDataPretreatmentTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.AudioPlayerTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.IQSaveTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.ITUTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.ModulationRecognitionTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.SingleMeasureSaveTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.SingleMeasureTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;

import java.util.ArrayList;
import java.util.Arrays;

public class Factory {

    private static Factory factory;

    private Factory() {
    }

    public static Factory getInstance() {
        if (factory == null) {
            synchronized (Factory.class) {
                if (factory == null) {
                    factory = new Factory();
                }
            }
        }
        return factory;
    }

    public Transaction createTransaction(String type, TaskSchedule taskSchedule, IBaseView iBaseView) {
        Transaction transaction = null;
        switch (type) {
            case "SingleMeasureStart":
                transaction = new SingleMeasureTransaction(Constant.SingleMeasureStart, Constant.SingleMeasureGroup, null, 1, taskSchedule, taskSchedule.CommonexecutorService, iBaseView);
                break;
            case "IQSave":
                transaction = new IQSaveTransaction(Constant.IQSave, Constant.IQdataSaveGroup, null, 1, taskSchedule.IOexecutorService, taskSchedule, 0, iBaseView);
                break;
            case "Audio":
                transaction = new AudioPlayerTransaction(Constant.Audio, Constant.AudioGroup, new ArrayList<>(Arrays.asList(Constant.AudioDataPretreatmentTransaction)), 1, taskSchedule.CommonexecutorService, 0, taskSchedule);
                break;
            case "ITU":
                transaction = new ITUTransaction(Constant.ITU, Constant.ITUGroup, null, 1, taskSchedule, null, iBaseView);
                break;
            case "ModulationRecognition":
                transaction = new ModulationRecognitionTransaction(Constant.MR, Constant.ModulationRecognitionGroup, null, 1, taskSchedule, iBaseView);
                break;
            case "AudioSave":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    transaction = new AudioDataPersistenceFileTransaction(Constant.AudioSave, Constant.AudioDataSaveGroup, null, 1, taskSchedule, null);
                }
                break;
            case "SingleMeasureSave":
                transaction = new SingleMeasureSaveTransaction(Constant.SingleMeasureSave, Constant.SingleMeasureDataSaveGroup, null, 1, taskSchedule, "", iBaseView);
                break;
            case "AudioDataPretreatmentTransaction":
                transaction = new AudioDataPretreatmentTransaction(Constant.AudioDataPretreatmentTransaction, Constant.AudioGroup, null, 0, taskSchedule.CommonexecutorService, taskSchedule);
                break;
            case "PScanStart":
                transaction = new PScanTransaction(Constant.PScanStart, Constant.PScanStartGroup, Arrays.asList(Constant.SpectrumParse), 1, taskSchedule, iBaseView);
                break;
            case "FreqyencyHopping":
                transaction = new FrequencyHoppingTransaction(Constant.FreqyencyHopping, Constant.PScanArithmetic, null, 1, taskSchedule, null, iBaseView);
                break;
            case "SpectrumParse":
                transaction = new SpectrumParseTransaction(Constant.SpectrumParse, Constant.PScanStartGroup, null, 0, taskSchedule);
                break;
            case "MScanStart":
                transaction = new MscanTransaction(Constant.MScanStart, Constant.MScanStartGroup, null, 1, taskSchedule, iBaseView);
                break;
            case "DeviceInfo":
                transaction = new DeviceStateTransaction(DataTypeEnum.DeviceInfo.toString(), Constant.DeviceStateGroup, null, 1, taskSchedule, iBaseView);
                break;
            case "ConnectionState":
                transaction = new ConnectionStateTransaction(DataTypeEnum.ConnectionState.toString(), Constant.ConnectionStateGroup, null, 1, taskSchedule, iBaseView);
                break;
            case "MultiSignalStart":
                transaction = new MultiTransaction(DataTypeEnum.MultiSignal.toString(), Constant.MultiSignalGroup, null, 1, taskSchedule, iBaseView);
                break;
            case "HopDectectorStart":
                transaction = new HopDectectorTransaction(Constant.HopDectectorStart, Constant.HopDectectorGroup, Arrays.asList(Constant.SpectrumParse), 1, taskSchedule, iBaseView);
                break;
            case "SignalSort":
                transaction = new SignalSortTransaction(Constant.SignalSort, Constant.PScanArithmetic, null, 1, taskSchedule, null, iBaseView);

            default:
                break;

        }
        return transaction;
    }
}