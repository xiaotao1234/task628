package com.huari.Presenter.Transactions.Singlemeasure;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.cdhuari.entity.AudioData;
import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AudioPlayerTransaction<T, M> extends Transaction<T, M> {//音频播放，流形式
    ExecutorService executorService;
    public static AudioTrack at;
    int bufferSize;

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setDDCChannel(int DDCChannel) {
        this.DDCChannel = DDCChannel;
    }

    int DDCChannel;

    public AudioPlayerTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, ExecutorService executorService, int DDCChannel, TaskSchedule taskSchedule) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.executorService = executorService;
        this.DDCChannel = DDCChannel;
    }

    @Override
    public T perform(T t) {
        if (((AudioData) ((DataPackage) t).Data.get("Audio")).DDCChannel == DDCChannel) {
            final byte[] bytes = ((AudioData) ((DataPackage) t).Data.get("Audio")).Audio;
            executorService.execute(() -> at.write(bytes, 0, bytes.length));
        }
        return t;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        at.flush();
        at.stop();
    }

    @Override
    public void beAdd() {
        super.beAdd();
        bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        createAudioPlay();
    }

    @Override
    public boolean handle(M m) {
        return ((DataPackage) m).DataType == DataTypeEnum.SingleMeasure;
    }


    public void createAudioPlay() {
        bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        at.play();
    }
}
