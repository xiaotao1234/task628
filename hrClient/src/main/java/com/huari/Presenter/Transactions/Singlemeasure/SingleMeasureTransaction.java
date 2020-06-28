package com.huari.Presenter.Transactions.Singlemeasure;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.IQData;
import com.cdhuari.entity.LevelData;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;
import com.huari.Presenter.entity.UI.SingleMeasureData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingleMeasureTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;
    SingleMeasureData singleMeasureData;
    ExecutorService executorService;
    List<short[]> bufferList;
    boolean refresh = true;
    short[] spectrumAvg;
    short[] spectrumMax;
    short[] spectrumMin;
    int frameCount = 0;
    Lock lock;

    public SingleMeasureTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, ExecutorService executorService, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.executorService = executorService;
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public T perform(final T t) {
        if (((DataPackage) t).Data.containsKey(DataType.Spectrum.toString())) {
            final SpectrumData spectrumData = (SpectrumData) ((DataPackage) t).Data.get(DataType.Spectrum.toString());
            final LevelData levelData = (LevelData) ((DataPackage) t).Data.get(DataType.Level.toString());
            final IQData iqData = (IQData) ((DataPackage) t).Data.get(DataType.IQ.toString());
            executorService.execute(() -> {
                singleMeasureData = new SingleMeasureData();
                if (spectrumAvg == null || spectrumData.Spectrum.length != spectrumAvg.length) {
                    spectrumAvg = new short[spectrumData.Spectrum.length];
                    spectrumMax = new short[spectrumData.Spectrum.length];
                    spectrumMin = new short[spectrumData.Spectrum.length];
                    refresh = true;
                } else refresh = false;
                if (!refresh) {
                    singleMeasureData.Spectrum = spectrumData.Spectrum;
                    HandleAvg(singleMeasureData.Spectrum);
                    HandleMax(singleMeasureData.Spectrum);
                    HandleMin(singleMeasureData.Spectrum);
                } else {
                    HandleAvgInittal(singleMeasureData.Spectrum);
                    HandleMaxInittal(singleMeasureData.Spectrum);
                    HandleMinInittal(singleMeasureData.Spectrum);
                }
                singleMeasureData.LevelAvg = levelData.LevelAvg;
                singleMeasureData.LevelFast = levelData.LevelFast;
                singleMeasureData.LevelPeak = levelData.LevelPeak;
                singleMeasureData.LevelRMS = levelData.LevelRMS;
                singleMeasureData.IqData = iqData.IQData;
                ((SingleMeasureUI)baseViewWeakReference.get()).onData(singleMeasureData);
            });
        }
        return t;
    }

    @Override
    public void work(T t) {
        super.work(t);
    }

    public void HandleAvg(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            ++frameCount;
            spectrumAvg[i] = (short) ((spectrumAvg[i] * frameCount + Spectrum[i]) / frameCount);
        }
        singleMeasureData.Spectrum_avg = spectrumAvg;
    }

    public void HandleAvgInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumAvg[i] = Spectrum[i];
        }
        singleMeasureData.Spectrum_avg = spectrumAvg;
    }

    public void HandleMax(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMax[i] = (short) Math.max(spectrumMax[i], Spectrum[i]);
        }
        singleMeasureData.Spectrum_max = spectrumMax;
    }

    public void HandleMaxInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMax[i] = Spectrum[i];
        }
        singleMeasureData.Spectrum_max = spectrumMax;
    }

    public void HandleMin(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMin[i] = (short) Math.min(spectrumMin[i], Spectrum[i]);
        }
        singleMeasureData.Spectrum_min = spectrumMin;
    }

    public void HandleMinInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMin[i] = Spectrum[i];
        }
        singleMeasureData.Spectrum_min = spectrumMin;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        bufferList.clear();
        spectrumMax = null;
        spectrumAvg = null;
        spectrumMin = null;
    }

    @Override
    public void beAdd() {
        super.beAdd();
        lock = new ReentrantLock();
        bufferList = new ArrayList<>();
    }


    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.SingleMeasure;
    }
}
