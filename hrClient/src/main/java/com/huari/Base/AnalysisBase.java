package com.huari.Base;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

import java.io.FileOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.appcompat.app.AppCompatActivity;

public abstract class AnalysisBase extends AppCompatActivity {//抽出一个基类是为了解析功能的复用
    public static Handler handle;
    public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    public static int DIANPINGDATA = 0x987;
    public static int PINPUDATA = 0x10;
    public static int ITUDATA = 0x3;
    public static int Trigger = 0x911;
    public static int FINISH = 0x774;
    public static int tempbufferindex = 0;
    public static byte[] tempAudioBuffer;
    public static byte[] audioBuffer;
    public static int audioindex = 0;
    public static int audioBuffersize;
    public static int tempLength = 409600;
    public static AudioTrack at;
    public static boolean isRecording = false;
    public static FileOutputStream fos = null;
    public static Object synObject = new Object();

    public static long AUDIO_SAMPLE_RATE = 44100;
    public static int  AUDIO_CHANNL = 2;

    public static void createAudioPlay(int frequency, byte bitcounts, short channelcount) {
        AUDIO_SAMPLE_RATE = frequency;
        AUDIO_CHANNL = channelcount;

        if (bitcounts == 0 && channelcount == 1) {
            audioBuffersize = AudioTrack
                    .getMinBufferSize(frequency, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_8BIT);
            //audioBuffersize = Math.max(audioBuffersize, framelength);

//            //根据采样率，采样精度，单双声道来得到frame的大小。
//            //计算最小缓冲区
//            //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
//            //创建AudioTrack

            at = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT, audioBuffersize*4,
                    AudioTrack.MODE_STREAM);

        } else if (bitcounts == 1 && channelcount == 1) {
            audioBuffersize = AudioTrack.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            //audioBuffersize = Math.max(audioBuffersize, framelength);

            at = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, audioBuffersize,
                    AudioTrack.MODE_STREAM);
        } else if (bitcounts == 0 && channelcount == 2) {
            audioBuffersize = AudioTrack.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_8BIT);
            //audioBuffersize = Math.max(audioBuffersize, framelength);

            at = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_8BIT, audioBuffersize,
                    AudioTrack.MODE_STREAM);

        } else if (bitcounts == 1 && channelcount == 2) {
            audioBuffersize = AudioTrack.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
            //audioBuffersize = Math.max(audioBuffersize, framelength);

            at = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, audioBuffersize,
                    AudioTrack.MODE_STREAM);

        }
        audioBuffer = new byte[audioBuffersize];
        tempAudioBuffer = new byte[tempLength];
        tempbufferindex = 0;
        at.play();
    }
}
