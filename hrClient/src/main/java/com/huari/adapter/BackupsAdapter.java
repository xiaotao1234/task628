package com.huari.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huari.client.R;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BackupsAdapter extends RecyclerView.Adapter<BackupsAdapter.viewholder> {

    public List<String> filesname;
    Context context;
    public List<File> files;

    public BackupsAdapter(List<String> filesname, Context context, List<File> files) {
        this.filesname = filesname;
        this.files = files;
        this.context = context;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_show_item, parent, false);
        BackupsAdapter.viewholder viewholder1 = new BackupsAdapter.viewholder(view);
        return viewholder1;
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
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
        holder.imageView.setImageResource(R.drawable.floder_icon);
        holder.textView.setText(new File(filesname.get(position)).getName());
        holder.explainText.setText(s);
        holder.fileitem.setOnClickListener(v -> {
            if(clickListener!=null){
                clickListener.click(position);
            }
        });
    }

    public interface ClickListener {
        void click(int position);
    }

    public BackupsAdapter.ClickListener clickListener;

    public void setdeleteListener(BackupsAdapter.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return filesname.size();
    }

    class viewholder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        TextView explainText;
        LinearLayout fileitem;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.file_box);
            textView = itemView.findViewById(R.id.file_name);
            explainText = itemView.findViewById(R.id.explain);
            fileitem = itemView.findViewById(R.id.file_item);
        }
    }
}
