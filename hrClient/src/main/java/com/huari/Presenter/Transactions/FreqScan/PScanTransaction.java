package com.huari.Presenter.Transactions.FreqScan;

import android.util.Log;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.PscanUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;
import com.huari.Presenter.entity.UI.PSCANData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PScanTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;
    PSCANData pscanData;
    ExecutorService executorService;
    TaskSchedule taskSchedule;
    List<short[]> bufferList;
    short[] spectrumAvg;
    short[] spectrumMax;
    short[] spectrumMin;
    boolean refresh = true;
    int frameCount = 0;
    Lock lock;

    public PScanTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.taskSchedule = taskSchedule;
        this.executorService = taskSchedule.IOexecutorService;
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public T perform(T t) {
        Log.d("datacome", "schdule Ps");
        if (((DataPackage) t).Data.containsKey(DataType.Spectrum.toString())) {
            Log.d("datacome1", "schdule Ps");
            final SpectrumData spectrumData = (SpectrumData) ((DataPackage) t).Data.get(DataType.Spectrum.toString());
            executorService.execute(() -> {
                long time = System.currentTimeMillis();
                pscanData = new PSCANData();
//                if (spectrumAvg == null || spectrumData.Spectrum.length != spectrumAvg.length) {
//                    spectrumAvg = new short[spectrumData.Spectrum.length];
//                    spectrumMax = new short[spectrumData.Spectrum.length];
//                    spectrumMin = new short[spectrumData.Spectrum.length];
//                    refresh = true;
//                } else refresh = false;
//                if (!refresh) {
//                    pscanData.Spectrum = spectrumData.Spectrum;
//                    HandleAvg(pscanData.Spectrum);
//                    HandleMax(pscanData.Spectrum);
//                    HandleMin(pscanData.Spectrum);
//                } else {
//                    HandleAvgInittal(pscanData.Spectrum);
//                    HandleMaxInittal(pscanData.Spectrum);
//                    HandleMinInittal(pscanData.Spectrum);
//                }
//                pscanData.startFreq = spectrumData.StartFreq;
//                pscanData.endFreq = spectrumData.EndFreq;
//                Log.d("datacome2", String.valueOf(System.currentTimeMillis()-time));
////                taskSchedule.UIexecutorService.execute(() ->
                pscanData.Spectrum = spectrumData.Spectrum;
                ((PscanUI) baseViewWeakReference.get()).onData(pscanData);
            });
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN;
    }

    @Override
    public void beCancel() {
        super.beCancel();
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

    public void HandleAvg(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            ++frameCount;
            spectrumAvg[i] = (short) ((spectrumAvg[i] * frameCount + Spectrum[i]) / (frameCount));
        }
        pscanData.Spectrum_avg = spectrumAvg;
    }

    public void HandleAvgInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumAvg[i] = Spectrum[i];
        }
        pscanData.Spectrum_avg = spectrumAvg;
    }

    public void HandleMax(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMax[i] = (short) Math.max(spectrumMax[i], Spectrum[i]);
        }
        pscanData.Spectrum_max = spectrumMax;
    }

    public void HandleMaxInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMax[i] = Spectrum[i];
        }
        pscanData.Spectrum_max = spectrumMax;
    }

    public void HandleMin(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMin[i] = (short) Math.min(spectrumMin[i], Spectrum[i]);
        }
        pscanData.Spectrum_min = spectrumMin;
    }

    public void HandleMinInittal(short[] Spectrum) {
        for (int i = 0; i < Spectrum.length; i++) {
            spectrumMin[i] = Spectrum[i];
        }
        pscanData.Spectrum_min = spectrumMin;
    }
}
