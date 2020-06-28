package com.huari.Presenter.local;

public interface Transmission {
    void start();

    void sendData(String data);

    void close();
}
