package com.huari.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huari.client.HistoryAnalysisActivity;
import com.huari.client.HistoryDFActivity;
import com.huari.client.HistoryPinDuanActivity;
import com.huari.client.PlayerActivity;
import com.huari.client.R;
import com.huari.tools.SysApplication;
import com.huari.dataentry.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.viewholder> {
    private File currentFile = SysApplication.fileOs.getCurrentFloder();
    public List<String> filesname;
    private Context context;
    public List<File> files;
    SharedPreferences sharedPreferences;
    private TextView textView;

    public FileListAdapter(List<String> filesname, Context context, List<File> files, TextView textView) {
        this.filesname = filesname;
        this.files = files;
        this.context = context;
        this.textView = textView;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_show_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public int getItemCount() {
        return filesname.size();
    }

    static class viewholder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        TextView explainText;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.file_box);
            textView = itemView.findViewById(R.id.file_name);
            explainText = itemView.findViewById(R.id.explain);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, final int position) {
        holder.textView.setText(filesname.get(position));
        String s;
        if(filesname.get(position).equals("ForSaveStation")){
            s = "站点设置的存储文件";
        }else if(filesname.get(position).equals("ForSaveHistory")){
            s = "浏览历史记录的存储文件";
        }else if(filesname.get(position).subSequence(0,2).equals("AN")){
            s = "频谱分析类型数据";
        }else if(filesname.get(position).subSequence(0,2).equals("PD")){
            s = "频段扫描类型数据";
        }else if(filesname.get(position).subSequence(0,2).equals("RE")){
            s = "音频类型数据";
        }else if(filesname.get(position).subSequence(0,2).equals("DF")){
            s = "单频测向类型数据";
        }else {
            s = "未知类型";
        }
        holder.explainText.setText(s);
        if (files.get(position).isDirectory()) {
            holder.imageView.setImageResource(R.drawable.floder_icon);
        }
        holder.itemView.setOnClickListener(v -> {
            currentFile = SysApplication.fileOs.getCurrentFloder();
            if (files.get(position).isDirectory()) {
                SysApplication.fileOs.pushStack(currentFile);
                currentFile = files.get(position);
                textView.setText(currentFile.getAbsolutePath());
                refreshList();
            } else {
                Intent intent = null;
                if (files.get(position).getName().contains("DF")) {
                    intent = new Intent(context, HistoryDFActivity.class);
                } else if (files.get(position).getName().contains("AN")) {
                    intent = new Intent(context, HistoryAnalysisActivity.class);
                } else if (files.get(position).getName().contains("PD")) {
                    intent = new Intent(context, HistoryPinDuanActivity.class);
                } else if (files.get(position).getName().contains("RE")) {
                    intent = new Intent(context, PlayerActivity.class);
                    EventBus.getDefault().postSticky(new MessageEvent(files.get(position).getAbsolutePath(), position));
                }
                if (intent != null) {
                    intent.putExtra("filename", files.get(position).getName());
                    intent.putExtra("from", "history");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "无法打开此文件", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
//            if (files.get(position).isDirectory()) {
//                sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("RootDirectory", files.get(position).getAbsolutePath());
//                editor.commit();
//                SysApplication.fileOs.setOsDicteoryPath(files.get(position));
//                SysApplication.fileOs.setCurrentFloder(files.get(position));
//                files = SysApplication.fileOs.getFiles();
//                filesname = SysApplication.fileOs.getFilesName();
//                SysApplication.fileOs.fileStack.clear();
//                notifyDataSetChanged();
//            } else {
//                Toast.makeText(context, "不能使用文件作为主目录", Toast.LENGTH_SHORT).show();
//            }
            deleteListener.delete(files.get(position));
            return true;
        });
    }

    public interface DeleteListener {
        void delete(File file);
    }

    public DeleteListener deleteListener;

    public void setdeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void refreshList() {
        SysApplication.fileOs.setCurrentFloder(currentFile);
        files = SysApplication.fileOs.getFiles();
        filesname = SysApplication.fileOs.getFilesName();
        notifyDataSetChanged();
    }
}
