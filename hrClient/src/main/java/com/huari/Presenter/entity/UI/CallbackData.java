package com.huari.Presenter.entity;

import java.io.Serializable;

public class CallbackData<T> implements Serializable {//返回数据的包装类

    public CallbackData(CallbackType type, T t) {
        this.type = type;
        this.t = t;
    }

    public CallbackType type;
    public T t;

    public enum CallbackType{
        Net,
        File,
    }
}
