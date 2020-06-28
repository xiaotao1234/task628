package com.huari.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huari.client.SpectrumsAnalysisActivity;
import com.huari.dataentry.FileSearchData;
import com.huari.dataentry.FileSearchMassage;
import com.huari.dataentry.SimpleStation;
import com.huari.dataentry.recentContent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import static android.content.Context.MODE_PRIVATE;
import static com.huari.Base.AnalysisBase.AUDIO_CHANNL;
import static com.huari.Base.AnalysisBase.AUDIO_SAMPLE_RATE;
import static com.huari.Base.AnalysisBase.audioBuffersize;

/**
 *
 */
/*
 *create by xiao tao
 * 2019/7/19/13:37
 */
public class FileOsImpl {
    List<File> files = new ArrayList<>();//列表内文件
    List<String> filesName = new ArrayList<>();//列表内文件名
    public File OsDicteoryPath;//文件的根目录
    public Stack<File> fileStack = new Stack<>();//文件的遍历栈
    List<File> willDeleteFiles = new ArrayList<>();//将要删除的文件列表
    List<File> selectedFiles = new ArrayList<>();//被勾选的文件列表
    public static List<recentContent> recentUseFiles = new ArrayList<>();//最近有对其进行操作的文件列表
    public static List<SimpleStation> simpleStations = new ArrayList<>();
    File currentFloder;//当前所在目录
    File currentFile;//当前正在使用的文件
    private static FileOsImpl fileOsImpl;
    public static String forSaveFloder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    private Thread thread;
    public int MUSIC = 0, WORD = 1, EXCEL = 2;

    private static List<recentContent> list;

    public FileOsImpl() {
    }

    public static List<recentContent> setRecentUseFiles(List<recentContent> recentUseFiles) {
        if (recentUseFiles == null) {
            File file1 = new File(forSaveFloder + File.separator + "data" + File.separator + "forsave");
            try {
                InputStream inputStream = new FileInputStream(file1);
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                list = (List<recentContent>) ois.readObject();
                FileOsImpl.recentUseFiles = list;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("xiaoxiaolai", "1");
            }
            Message message = Message.obtain();
            message.obj = list;
//            Main3Activity.handler.sendMessage(message);
            return list;
        } else {
            FileOsImpl.recentUseFiles = recentUseFiles;
            return recentUseFiles;
        }
    }

    public static void SaveRecentUseFiles() {
        File filesave = new File(forSaveFloder + File.separator + "data" + File.separator + "forsave");
        if (!filesave.getParentFile().exists()) {
            filesave.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filesave);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(recentUseFiles);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = AUDIO_SAMPLE_RATE;
        int channels = AUDIO_CHANNL;
        long byteRate = 16 * AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[audioBuffersize];
        byte[] endStation = new byte[76];
        byte[] endDevice = new byte[36];
        Arrays.fill(endStation, (byte) 0);
        Arrays.fill(endDevice, (byte) 0);
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 44;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (data.length != 0 && in.read(data) != -1) {
                out.write(data);
            }
            byte[] endStation1 = SpectrumsAnalysisActivity.stationname.getBytes("UTF8");
            byte[] endDevice1 = SpectrumsAnalysisActivity.devicename.getBytes("UTF8");
            for (int i = 0; i < endStation1.length; i++) {
                endStation[i] = endStation1[i];
            }
            for (int i = 0; i < endDevice1.length; i++) {
                endDevice[i] = endDevice1[i];
            }
            out.write(endStation);
            out.write(endDevice);
            in.close();
            out.close();
            new File(inFilename).delete();
            File file1 = new File(outFilename);
            File file2 = new File(FileOsImpl.forSaveFloder + File.separator + "data" + File.separator
                    + file1.getName().substring(0, 3)
                    + new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date(file1.lastModified())));
            file1.renameTo(file2);
            SysApplication.fileOs.save(file2.getAbsolutePath(), file2.getName(), 4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                           long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public void save(String filename, String fileOnlyName, int type) {
        if (type == 5) {
            return;
        }
        GetTheRecentFirst();
        recentContent recentContent = new recentContent(filename, fileOnlyName, type);
        Log.d("xiaofile", filename);
        if (!recentUseFiles.contains(recentContent)) {
            recentUseFiles.add(0, new recentContent(filename, fileOnlyName, type));
            if (recentUseFiles.size() > 50) {
                recentUseFiles.remove(50);
            }
        } else {
            int i = recentUseFiles.indexOf(new recentContent(filename, fileOnlyName, type));
            recentUseFiles.add(0, recentUseFiles.remove(i));
        }
        File file1 = new File(forSaveFloder + File.separator + "data" + File.separator + "ForSaveHistory");
        file1.delete();
        if (!file1.getParentFile().exists()) {
            file1.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file1);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(recentUseFiles);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRecentFilesFormMem() {
        File file1 = new File(forSaveFloder + File.separator + "data" + File.separator + "ForSaveHistory");
        file1.delete();
        if (!file1.getParentFile().exists()) {
            file1.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file1);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(recentUseFiles);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GetTheRecentFirst() { //从文件中反序列化出最近数据
        if (recentUseFiles.size() == 0) {
            try {
                File file1 = new File(forSaveFloder + File.separator + "data" + File.separator + "ForSaveHistory");
                FileInputStream fileInputStream = new FileInputStream(file1);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                recentUseFiles.clear();
                recentUseFiles = (List<recentContent>) objectInputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void getRecentList(Handler handler) {
        if (recentUseFiles.size() != 0) {
            Message message = Message.obtain();
            message.obj = recentUseFiles;
            handler.sendMessage(message);
        }
        getRecentFromFile(handler);
    }

    public static void getRecentFromFile(Handler handler) {
        try {
            File file1 = new File(forSaveFloder + File.separator + "data" + File.separator + "ForSaveHistory");
            if (file1.length() == 0) {
                Message message = Message.obtain();
                message.obj = recentUseFiles;
                handler.sendMessage(message);
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(file1);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            recentUseFiles.clear();
            recentUseFiles = (List<recentContent>) objectInputStream.readObject();
            Message message = Message.obtain();
            message.obj = recentUseFiles;
            handler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileOsImpl getInstance() { //单例获取实例
        if (fileOsImpl == null) {
            fileOsImpl = new FileOsImpl();
        }
        return fileOsImpl;
    }

    public Stack<File> getFileStack() {  //获得路径记录栈
        return fileStack;
    }

    public void pushStack(File file) {  //路径记录栈的入栈
        fileStack.push(file);
        Log.d("stack_os_in", fileStack.toString());
    }


    public File popStack() {  //路径记录栈的出栈
        File file = fileStack.pop();
        Log.d("stack_os_out", fileStack.toString());
        return file;
    }


    public File getOsDicteoryPath(Context context) {  //获得初始根目录地址
        SharedPreferences sharedPreferences = context.getSharedPreferences("User", MODE_PRIVATE);
        String filepatc = sharedPreferences.getString("RootDirectory", Environment.getExternalStorageDirectory().getAbsolutePath());
        OsDicteoryPath = new File(filepatc);
        if (!OsDicteoryPath.mkdirs()) {
            Log.d("xiao", "file has exist");
        } else {
            Log.d("xiao", "file has make");
        }
        return OsDicteoryPath;
    }


    public File getCurrentFloder() {  //获得当前目录的目录名
        return currentFloder;
    }

    public void setCurrentFloder(File floder) {  //设置当前文件目录，设置后会自动设置当前目录文件列表和文件名列表
        if (floder != null) {
            currentFloder = floder;
            if (files != null) {
                files.clear();
            }
            if (filesName != null) {
                filesName.clear();
            }
            for (File file : currentFloder.listFiles()) {
                filesName.add(file.getName());
                files.add(file);
            }
        }
    }

    public List<File> getFiles() {  //获得当前目录下的所有文件
        return files;
    }

    public List<File> getWillDeleteFiles() {
        return willDeleteFiles;
    }

    public void setWillDeleteFiles(File file) {
        if (file != null) {
            willDeleteFiles.add(file);
        }
    }

    public void setOsDicteoryPath(File file) {  //设置根目录地址
        if (file != null && file.exists()) {
            OsDicteoryPath = file;
        }
    }

    public void setSelectedFiles(File file) {
        if (file != null) {
            selectedFiles.add(file);
        }
    }

    public void setCurrentFile(File currentFile) {  //获得当前被选择的文件
        this.currentFile = currentFile;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public List<File> getSelectedFiles() {  //获得被选中的文件目录
        return selectedFiles;
    }

//    public void createFloder(String name) {
//        File file = new File(currentFloder.getAbsolutePath() + File.separator + name);
//        if (file.exists() == false) {
//            if (file.mkdirs()) ;
//        }
//    }

    public void addFloder(String floderName) {          //添加文件夹
        File addFile = new File(currentFloder.getAbsolutePath() + File.separator + floderName);
        if (!addFile.exists()) {
            addFile.mkdirs();
        }
    }

    public List<String> getFilesName() {
        return filesName;
    }

    public void searh(File file, String key) {    //查找相关文件
        Log.d("xiao", key);
        Runnable runnable = () -> {
            long time = System.currentTimeMillis();
            FileSearchMassage fileSearchResult = new FileSearchMassage(searchInThisFloder(file, key));
            long timeafter = System.currentTimeMillis() - time;
            Log.d("xiaotaoni", String.valueOf(timeafter));
            EventBus.getDefault().post(fileSearchResult);
            EventBus.getDefault().postSticky(fileSearchResult);
        };
        thread = new Thread(runnable);
        thread.start();
    }

    public List<FileSearchData> searchInThisFloder(File file, String key) {             //在文件夹内递归搜索相应文件
        List<FileSearchData> thisPartResult = new ArrayList<>();
        if (file.isDirectory()) {
            for (File file1 : file.listFiles()) {
                if (file1.getName().toLowerCase().contains(key) && file1.isDirectory()) {
                    thisPartResult.add(new FileSearchData(file1, file1.getName().toLowerCase().indexOf(key)));
                }
                thisPartResult.addAll(searchInThisFloder(file1, key));
            }
        } else {
            if (file.getName().toLowerCase().contains(key)) {
                thisPartResult.add(new FileSearchData(file, file.getName().toLowerCase().indexOf(key)));
            }
        }
        return thisPartResult;
    }

    public void backups(Handler handler) {//文件备份,备份所有文件
        File file1 = new File(forSaveFloder + File.separator + "backupstem");
        File filesave = new File(forSaveFloder + File.separator + "data");
        if (!file1.exists()) {
            file1.mkdirs();
        }
        int tim = 0;
        for (File file : filesave.listFiles()) {
            Message message = Message.obtain();
            message.what = 1;
            tim = tim + 1;
            message.obj = tim + "/" + filesave.listFiles().length;
            handler.sendMessage(message);
            if (file.getName().equals("ForSaveHistory") || file.getName().equals("ForSaveStation")) {
                try {
                    copyFileUsingFileChannels(file, new File(file1.getAbsolutePath() + File.separator + file.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessageTo(handler,6);
                }
            } else {
                File file2 = new File(forSaveFloder + File.separator + "backupstem" + File.separator + file.getName());
                if (!file2.exists()) {
                    try {
                        copyFileUsingFileChannels(file, new File(file1.getAbsolutePath() + File.separator + file.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMessageTo(handler,6);
                    }
                }
            }
        }
        sendMessageTo(handler,2);
    }

    public void backupsOne(Handler handler, String fileName) {//备份单个指定文件
        File file1 = new File(forSaveFloder + File.separator + "backupstem");
        File filesave = new File(forSaveFloder + File.separator + "data");
        File file = new File(filesave.getAbsolutePath() + File.separator + fileName);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        try {
            if (file.getName().equals("ForSaveHistory") || file.getName().equals("ForSaveStation")) {
                copyFileUsingFileChannels(file, new File(file1.getAbsolutePath() + File.separator + file.getName()));
                sendMessageTo(handler,5);
            } else {
                File file2 = new File(forSaveFloder + File.separator + "backupstem" + File.separator + fileName);
                if(!file2.exists()){
                    copyFileUsingFileChannels(file,file2);
                    sendMessageTo(handler,5);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMessageTo(handler,6);
        }
    }

    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    public int getbackupsnums() {//获取文件备份数量
        File file1 = new File(forSaveFloder + File.separator + "backupstem");
        if (file1.exists()) {
            return file1.listFiles().length;
        }
        return 0;
    }

    public void getbackups(Handler handler) {//从文件备份恢复
        File file1 = new File(forSaveFloder + File.separator + "backupstem");
        File filesave = new File(forSaveFloder + File.separator + "data");
        if (file1.exists() && file1.length() > 0) {
            int tim = 0;
            for (File file : file1.listFiles()) {
                Message message = Message.obtain();
                message.what = 3;
                tim = tim + 1;
                message.obj = "正在进行备份" + tim + file1.listFiles().length;
                handler.sendMessage(message);
                try {
                    if (!new File(filesave.getAbsoluteFile() + File.separator + file.getName()).exists()) {
                        copyFileUsingFileChannels(file, new File(filesave.getAbsolutePath() + File.separator + file.getName()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        sendMessageTo(handler,4);
    }

    public void recoverOne(Handler handler,String fileName) {//从文件备份恢复
        File file1 = new File(forSaveFloder + File.separator + "backupstem");
        File filesave = new File(forSaveFloder + File.separator + "data");
        if (!file1.exists() || file1.listFiles().length == 0) {
            Message message = Message.obtain();
            message.what = 7;
            handler.sendMessage(message);
        }else {
            File file2 = new File(file1.getAbsolutePath()+File.separator+fileName);
            if(!file2.exists()){
                sendMessageTo(handler,8);
            }else {
                try {
                    copyFileUsingFileChannels(file2,new File(filesave+File.separator+fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessageTo(handler,6);
                }
            }
        }
        sendMessageTo(handler,4);
    }

    private void sendMessageTo(Handler handler,int what) {
        Message message = Message.obtain();
        message.what = what;
        handler.sendMessage(message);
    }
}
