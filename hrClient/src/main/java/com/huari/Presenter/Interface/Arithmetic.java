package com.huari.Presenter.Interface;

public interface Arithmetic<M, T> {
    public void handle(M m, T t);

    public void cancel();

    public void add();
}

