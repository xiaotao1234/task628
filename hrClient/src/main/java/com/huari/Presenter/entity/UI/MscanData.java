package com.huari.Presenter.entity.UI;

public class MscanData {
    public float[] CenterFreq;
    public float[] LevelFast;
    public float[] LevelPeak;
    public float[] LevelAvg;
    public float[] LevelRMS;

    public MscanData(int length) {
        CenterFreq = new float[length];
        LevelFast = new float[length];
        LevelPeak = new float[length];
        LevelAvg = new float[length];
        LevelRMS = new float[length];
    }
}
