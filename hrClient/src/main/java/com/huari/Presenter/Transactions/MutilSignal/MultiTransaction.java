package com.huari.Presenter.Transactions.MutilSignal;

import com.cdhuari.entity.AudioData;
import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.SpectrumData;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.MultiUI;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.DataType;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class MultiTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> iBaseViewWeakReference;
    boolean ddc1Data = true;
    boolean ddc2Data = true;
    boolean ddc1Voice = true;
    boolean ddc2Voice = true;

    public MultiTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.iBaseViewWeakReference = new WeakReference<>(iBaseView);
    }

    @Override
    public T perform(T t) {
        Map map = ((DataPackage) t).Data;
        if (map.containsKey(DataType.Spectrum.toString())) {
            SpectrumData spectrumData = (SpectrumData) map.get(DataType.Spectrum.toString());
            switch (spectrumData.DDCChannel) {
                case 0:
                    if (iBaseViewWeakReference != null)
                        ((MultiUI) iBaseViewWeakReference.get()).SpectrumDataback(spectrumData.Spectrum);
                    break;
                case 1:
                    if (ddc1Data)
                        ((MultiUI) iBaseViewWeakReference.get()).SpectrumDatabackDDC1(spectrumData.Spectrum);
                    break;
                case 2:
                    if (ddc2Data)
                        ((MultiUI) iBaseViewWeakReference.get()).SpectrumDatabackDDC2(spectrumData.Spectrum);
                    break;
            }
        }
        if (map.containsKey(DataType.Audio.toString())) {
            AudioData audioData = (AudioData) map.get(DataType.Audio.toString());
            switch (audioData.DDCChannel) {
                case 0:
                    break;
                case 1:
                    if (ddc1Voice)
                        ((MultiUI) iBaseViewWeakReference.get()).VoiceDatabackDDC1(audioData.Audio);
                    break;
                case 2:
                    if (ddc2Voice)
                        ((MultiUI) iBaseViewWeakReference.get()).VoiceDatabackDDC2(audioData.Audio);
                    break;
            }
        }
        return t;
    }

    public void setDDC1Data(boolean ddc1Data) {
        this.ddc1Data = ddc1Data;
    }

    public void setDDC2Data(boolean ddc2Data) {
        this.ddc2Data = ddc2Data;
    }

    public void setDDC1Voice(boolean ddc1Voice) {
        this.ddc1Voice = ddc1Voice;
    }

    public void setDDC2Voice(boolean ddc2Voice) {
        this.ddc2Voice = ddc2Voice;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.MultiSignal;
    }
}
