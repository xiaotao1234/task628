package com.huari.Presenter.Impl;

import com.alibaba.fastjson.JSON;
import com.cdhuari.Transmission;
import com.cdhuari.TransmissionFactory;
import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Interface.DataWorkCallback;
import com.huari.Presenter.entity.CallbackData;
import com.huari.Presenter.entity.Constant;

public class NetWorkImpl<T> implements DataWork<T> {
    Transmission transmission;

    @Override
    public void initialize(final DataWorkCallback dataWorkCallback) {
        transmission = TransmissionFactory.getClientInstance(Constant.IP, Constant.PORT, data -> {
            CallbackData callbackData = new CallbackData(CallbackData.CallbackType.Net, data);
            dataWorkCallback.Databack(callbackData);
        });
        try {
            transmission.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        if (transmission != null)
            transmission.sendData("pause");
    }

    @Override
    public void close() {
        if (transmission != null)
            transmission.close();
    }

    @Override
    public void sendMessage(T t) {
        if (transmission != null) {
            String s = JSON.toJSONString(t);
            transmission.sendData(s);
        }
    }

}
