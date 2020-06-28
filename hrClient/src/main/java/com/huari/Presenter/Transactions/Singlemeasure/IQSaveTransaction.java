package com.huari.Presenter.Transactions.Singlemeasure;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.IQData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Tools.FileTools;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class IQSaveTransaction<T, M> extends Transaction<T, M> {//IQ数据的保存
    WeakReference<IBaseView> baseViewWeakReference;
    private RandomAccessFile randomFile = null;
    ExecutorService executorService;
    long startTime;
    long time;

    public void setTime(long time) {
        this.time = time;
    }

    public IQSaveTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, ExecutorService executorService, TaskSchedule taskSchedule, long time, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(baseView);
        this.executorService = executorService;
        this.time = time;
    }

    @Override
    public T perform(T t) {
        IQData iqData = (IQData) ((DataPackage) t).Data.get(DataType.IQ);
        saveOneFarme((iqData).IQData, iqData.CenterFreq);
        if (System.currentTimeMillis() - startTime > time) {
            ((SingleMeasureUI) baseViewWeakReference.get()).onSave(true, time);
            cancelSelf();
        }
        return t;
    }

    @Override
    public void work(T t) {
        super.work(t);
    }

    @Override
    public void beCancel() {
        super.beCancel();
        try {
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beAdd() {
        super.beAdd();
        time = 0;
        startTime = System.currentTimeMillis();
        try {
            randomFile = FileTools.getRandomAccessFile("IQ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ((SingleMeasureUI) baseViewWeakReference.get()).onSave(false, time);
        }
    }

    @Override
    public boolean handle(M m) {
        return ((DataPackage) m).DataType == DataTypeEnum.SingleMeasure;
    }

    public void saveOneFarme(short[] shorts, double centerFreq) {
        try {
            byte[] headBytes = FileTools.int2ByteArray(0xAAAAAAAA);
            byte[] bytesTmp = FileTools.shortArr2byteArr(shorts, shorts.length);
            byte[] centerFreqs = FileTools.double2ByteArray(centerFreq);
            byte[] lengthBytes = FileTools.int2ByteArray(bytesTmp.length);
            byte[] bytes = new byte[bytesTmp.length + headBytes.length + lengthBytes.length];
            System.arraycopy(headBytes, 0, bytes, 0, headBytes.length);
            System.arraycopy(centerFreqs, 0, bytes, headBytes.length, centerFreqs.length);
            System.arraycopy(lengthBytes, 0, bytes, headBytes.length + centerFreqs.length, headBytes.length * 2);
            System.arraycopy(bytesTmp, 0, bytes, headBytes.length * 2 + centerFreqs.length, bytes.length);
            FileTools.saveDataInRandomAccessFile(randomFile, bytes);
        } catch (Exception e) {
            e.printStackTrace();
            ((SingleMeasureUI) baseViewWeakReference.get()).onSave(false, time);
        }
    }

}
