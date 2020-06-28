package com.huari.Presenter.Transactions.Singlemeasure;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.huari.Presenter.Interface.DataParseForSave;
import com.huari.Presenter.PresenterExection.RandomFileNullExection;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.Tools.FileTools;
import com.huari.Presenter.abstruct.Transaction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.O)
public class AudioDataPersistenceFileTransaction<T, M> extends Transaction<T, M> {//存储一帧音频数据
    private RandomAccessFile randomFile = null;
    FileTools.TimeSaveTask timeSaveTask;

    public void setDataParseForSave(DataParseForSave dataParseForSave) {
        this.dataParseForSave = dataParseForSave;
    }

    DataParseForSave dataParseForSave;

    public AudioDataPersistenceFileTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, DataParseForSave dataParseForSave) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.dataParseForSave = dataParseForSave;
    }

    @Override
    public T perform(T t) {
        if(dataParseForSave!=null)
        saveOneFarme((byte[]) dataParseForSave.parse(t));
        return t;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        dataParseForSave = null;
        try {
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beAdd() {
        super.beAdd();
        try {
            FileTools fileTools = new FileTools();
            timeSaveTask = fileTools.getTimeSaveTaskInstance();
            randomFile = FileTools.getRandomAccessFile("audio");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveOneFarme(byte[] bytes) {
        try {
            FileTools.saveDataInRandomAccessFile(randomFile, bytes);
        } catch (RandomFileNullExection randomFileNullExection) {
            randomFileNullExection.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handle(M m) {
        return false;
    }

}
