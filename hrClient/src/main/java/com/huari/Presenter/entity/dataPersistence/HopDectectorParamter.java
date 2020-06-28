package com.huari.Presenter.entity.dataPersistence;

public class HopDectectorParamter {
    public short[] data;
    public double sfreq;
    public double efreq;
    public double df;
    public double th1_deltaf;
    public double the2_deltaf;

    public HopDectectorParamter(short[] data, double sfreq, double efreq, double df, double th1_deltaf, double the2_deltaf) {
        this.data = data;
        this.sfreq = sfreq;
        this.efreq = efreq;
        this.df = df;
        this.th1_deltaf = th1_deltaf;
        this.the2_deltaf = the2_deltaf;
    }
}
