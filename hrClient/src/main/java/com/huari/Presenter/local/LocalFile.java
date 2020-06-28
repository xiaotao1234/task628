package com.huari.Presenter.local;

public class LocalFile {
    DataReceiver dataReceiver;

    public LocalFile(DataReceiver dataReceiver) {
        this.dataReceiver = dataReceiver;
    }

    public void start() {

    }

    public void sendData(String data) {
        Integer.parseInt(data);
    }

    public void close() {

    }
}
