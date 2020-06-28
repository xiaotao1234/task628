package com.huari.Presenter.Transactions.Singlemeasure;

import android.media.AudioFormat;
import android.media.AudioTrack;

import com.cdhuari.entity.AudioData;
import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AudioDataPretreatmentTransaction<T, M> extends Transaction<T, M> {//处理等待直到达到最小播放流长度的音频
    ExecutorService executorService;
    public static byte[] audioBuffer;
    public static int audioBuffersize = 0;
    public int boundary = 40000;

    public AudioDataPretreatmentTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, ExecutorService executorService, TaskSchedule taskSchedule) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.executorService = executorService;
    }

    @Override
    public void work(T t) {//对声音帧进行数据等待，满一个声音帧时传达给声音播放帧进行播放
        byte[] bytes = ((AudioData)((DataPackage) t).Data.get("Audio")).Audio;
        audioBuffersize = audioBuffersize + bytes.length;
        System.arraycopy(bytes, 0, audioBuffer, audioBuffersize, bytes.length);
        if (audioBuffersize >= boundary) {
            ((DataPackage) t).Data.put("Audio", audioBuffer);
            audioBuffer = null;
            audioBuffersize = 0;
            super.work(t);
        }
    }

    @Override
    public T perform(T t) {
        return t;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        audioBuffer = null;
        executorService = null;
    }

    @Override
    public void beAdd() {
        super.beAdd();
        parpera();
    }

    @Override
    public boolean handle(M m) {
        return DataTypeEnum.SingleMeasure == m;
    }

    private void parpera(){
        boundary = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

}
