package com.huari.Presenter.Transactions.HopDectector;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.HopDectectorUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Interface.Arithmetic;
import com.huari.Presenter.Tools.BufferForFixCapacity;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.DataType;
import com.huari.Presenter.entity.dataPersistence.HopDectectorParamter;
import com.huari.algorithm.HopDetector;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HopDectectorTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;
    BufferForFixCapacity bufferForFixCapacity;
    ExecutorService executorService;
    boolean runFlag = false;
    HopDetector hopDetector;
    Arithmetic arithmetic;
    double the2_deltaf;
    double th1_deltaf;
    double freqStep;

    public HopDectectorTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public T perform(T t) {
        if (((DataPackage) t).Data.containsKey(DataType.Spectrum.toString())) {
            DataPackage dataPackage = (DataPackage) t;
            short[] shorts = ((SpectrumData) dataPackage.Data.get(Constant.Spectrum)).Spectrum;
            short[] Spectrum = new short[shorts.length];
            System.arraycopy(shorts, 0, Spectrum, 0, shorts.length);
            taskSchedule.UIexecutorService.execute(() -> ((HopDectectorUI)baseViewWeakReference.get()).SpectrumDataback(((SpectrumData) dataPackage.Data.get(Constant.Spectrum)).Spectrum));
            bufferForFixCapacity.add((DataPackage) t);
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.PSCAN;
    }

    @Override
    public void beAdd() {
        super.beAdd();
        runFlag = true;
        bufferForFixCapacity = new BufferForFixCapacity(100);
        arithmetic.add();
        hopDetector = new HopDetector();
        executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());//设置为单一线程，且不设等待队列（根据业务需要如此设计）
        Executors.newSingleThreadExecutor();
        startArithmetic();
    }

    @Override
    public void beCancel() {
        super.beCancel();
        runFlag = false;
        bufferForFixCapacity = null;
        arithmetic = null;
        executorService.shutdownNow();
    }

    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }

    public void setFreqStep(double freqStep) {
        this.freqStep = freqStep;
    }

    public void startArithmetic() {
        executorService.execute(() -> {
            for (; ; ) {
                if (!runFlag) break;
                DataPackage dataPackage = bufferForFixCapacity.get();
                SpectrumData spectrumData = (SpectrumData) dataPackage.Data.get(Constant.Spectrum);
                short[] data = new short[spectrumData.Spectrum.length];
                System.arraycopy(spectrumData.Spectrum, 0, data, 0, spectrumData.Spectrum.length);
                HopDectectorParamter hopDectectorParamter = new HopDectectorParamter(data, spectrumData.StartFreq,
                        spectrumData.EndFreq, (spectrumData.EndFreq - spectrumData.StartFreq) / freqStep, th1_deltaf, the2_deltaf);
                arithmetic.handle(hopDectectorParamter, baseViewWeakReference.get());
            }
        });
    }
}

