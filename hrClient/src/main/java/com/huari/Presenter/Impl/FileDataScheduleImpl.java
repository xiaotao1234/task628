package com.huari.Presenter.Impl;

import com.huari.Presenter.Interface.FileDataSchedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class FileDataScheduleImpl<T> implements FileDataSchedule<T> {
    List<Integer> list;

    @Override
    public void start(File file) {
        list = new ArrayList<>();
    }

    @Override
    public void pause() {

    }

    @Override
    public void changeProgress(int progress) {

    }

    @Override
    public void close() {

    }

    @Override
    public void preFrame() {

    }

    @Override
    public void nextFrame() {

    }

    public void parseHeader(File file) {

    }

    public T get(File file) {
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return (T) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
