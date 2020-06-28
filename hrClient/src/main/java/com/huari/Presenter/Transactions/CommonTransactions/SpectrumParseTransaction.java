package com.huari.Presenter.Transactions.CommonTransactions;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.SpectrumData;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpectrumParseTransaction<T, M> extends Transaction<T, M> {
    Map<String, List<short[]>> map;
    Lock lock;

    public SpectrumParseTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
    }


    @Override
    public void work(T t) {
        if (((DataPackage) t).Data.containsKey(DataType.Spectrum.toString())) {
            lock.lock();
            try {
                SpectrumData spectrumData = (SpectrumData) ((DataPackage) t).Data.get(DataType.Spectrum.toString());
                String key = spectrumData.StartFreq + spectrumData.EndFreq + "";
                List<short[]> bufferList = map.get(key);
                if (map.containsKey(key)) {
                    bufferList = map.get(key);
                } else {
                    bufferList = new ArrayList<>();
                    map.put(key, bufferList);
                }
                if (spectrumData.Index == 1) {
                    bufferList.add(spectrumData.Spectrum);
                } else {
                    bufferList.add(spectrumData.Spectrum);
                    int length = 0;
                    for (short[] buffer : bufferList) {
                        length = length + buffer.length;
                    }
                    short[] shorts = new short[length];
                    int now = 0;
                    for (short[] buffer : bufferList) {
                        System.arraycopy(buffer, 0, shorts, now, buffer.length);
                        now = now + buffer.length;
                    }
                    bufferList.clear();
                    map.remove(key);
                    spectrumData.Spectrum = shorts;
                    super.work(t);
                }
            } finally {
                lock.unlock();
            }
        }
    }


    @Override
    public T perform(T t) {
        return t;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        //bufferList.clear();
    }

    @Override
    public void beAdd() {
        //bufferList = new ArrayList<>();
        lock = new ReentrantLock();
        super.beAdd();
        map = new HashMap<>();
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN || m == DataTypeEnum.FSCAN;
    }
}
