package com.huari.Presenter.Interface;


import com.huari.Presenter.entity.CallbackData;

public interface DataSchdule<T> {

    public boolean weatherThisType(CallbackData callbackData);

    public T dataCopy(T t);

    public boolean weatherSchdule(T t);

    public T getData(CallbackData callbackData);

}