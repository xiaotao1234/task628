package com.huari.Presenter.Interface;

import java.io.File;

public interface FileDataSchedule<T> {

    public void start(File file);

    public void pause();

    public void changeProgress(int progress);

    public void close();

    public void preFrame();

    public void nextFrame();
}
