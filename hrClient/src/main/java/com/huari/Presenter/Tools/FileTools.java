package com.huari.Presenter.Tools;


import android.os.Build;

import com.huari.Presenter.PresenterExection.RandomFileNullExection;
import com.huari.Presenter.entity.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileTools {

    public static RandomAccessFile getRandomAccessFile(String Type) throws FileNotFoundException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.getNumberFormat();
        String fileName = Type + df.format(new Date()).replaceAll(" ", "|");
        File file = new File(DataForApplication.forSaveFloder + File.separator + fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        RandomAccessFile randomFile = new RandomAccessFile(file.getAbsolutePath() + File.separator + fileName, "rw");
        return randomFile;
    }

    public static RandomAccessFile getRandomAccessFile() throws FileNotFoundException, FileAlreadyExistsException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.getNumberFormat();
        String fileName = "data|" + df.format(new Date()).replaceAll(" ", "|");
        File file = new File(DataForApplication.forSaveFloder + File.separator + fileName);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                throw new FileAlreadyExistsException("文件已存在");
            }
        }
        RandomAccessFile randomFile = new RandomAccessFile(file.getAbsolutePath() + File.separator + fileName, "rw");
        return randomFile;
    }

    public static boolean saveDataInRandomAccessFile(RandomAccessFile randomAccessFile, byte[] bytes) throws RandomFileNullExection, IOException {
        if (randomAccessFile != null) {
            long fileLength = randomAccessFile.length();
            //将写文件指针移到文件尾。
            randomAccessFile.seek(fileLength);
            randomAccessFile.write(bytes);
        }
        throw new RandomFileNullExection("randomAccessFile为null");
    }

    public static byte[] shortArr2byteArr(short[] shortArr, int shortArrLen) {
        byte[] byteArr = new byte[shortArrLen * 2];
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArr);
        return byteArr;
    }


    public TimeSaveTask getTimeSaveTaskInstance() {
        return new TimeSaveTask();
    }

    public class TimeSaveTask {
        private int lastLength = 0;

        public long saveWithTime(RandomAccessFile randomAccessFile, byte[] info, long time) throws IOException, RandomFileNullExection {//向文件存入一个带时间头的帧
            int delay = 0;
            if (time != 0) {
                delay = (int) (System.currentTimeMillis() - time);
            }
            time = System.currentTimeMillis();
            int available = info.length;
            byte[] headBytes = int2ByteArray(0xAAAAAAAA);//帧头
            byte[] timeBytes = int2ByteArray(delay);//当前数据帧距下一个数据帧延时的时间
            byte[] lengthBytes = int2ByteArray(available);//数据帧长度
            byte[] lastLengthBytes = int2ByteArray(lastLength);//上一个数据帧长度
            byte[] bytesForSave = new byte[available + 4 + 4 + 4 + 4];
            System.arraycopy(headBytes, 0, bytesForSave, 0, 4);
            System.arraycopy(timeBytes, 0, bytesForSave, 4, 4);
            System.arraycopy(lengthBytes, 0, bytesForSave, 8, 4);
            System.arraycopy(lastLengthBytes, 0, bytesForSave, 12, 4);
            System.arraycopy(info, 0, bytesForSave, 16, info.length);
            saveDataInRandomAccessFile(randomAccessFile, bytesForSave);
            lastLength = available;
            return time;
        }
    }

    public static byte[] int2ByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static byte[] double2ByteArray(double d) {
        byte[] output = new byte[8];
        long lng = Double.doubleToLongBits(d);
        for(int i = 0; i < 8; i++) output[i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
        return output;
    }

    public static String getFileNameAccordDate(String s, boolean addDate) {
        if (addDate) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return Constant.savePath + File.separator + s + "|" + df.format(new Date()).replaceAll(" ", "|");
        } else {
            return Constant.savePath + File.separator + s;
        }
    }
}
