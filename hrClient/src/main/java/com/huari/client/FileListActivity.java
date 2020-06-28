package com.huari.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huari.adapter.FileListAdapter;
import com.huari.tools.SysApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListActivity extends AppCompatActivity {

    FileListAdapter fileListAdapter;
    RecyclerView recyclerView;

    List<String> filesname = new ArrayList<>();
    List<File> files = new ArrayList<>();
    private TextView currentFloderName;
    private ImageView searhFile;
    private ImageView addfloderButton;
    private ImageView settingButton;
    private ImageView backupsButton;
    private ImageView back;
    private LinearLayout linearLayout;
    private FloatingActionButton fab;
    private PopupWindow window;
    private TextView textView;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    textView.setText("正在备份" + msg.obj);
                    break;
                case 2:
                    if (window != null && window.isShowing()) {
                        textView.setText("备份完成");
                        window.dismiss();
                    }
                    break;
                case 3:
                    if (window1 != null && window1.isShowing()) {
                        textView1.setText("正在恢复" + msg.obj);
                    }
                case 4:
                    SysApplication.fileOs.setCurrentFloder(new File(SysApplication.fileOs.forSaveFloder + File.separator + "data"));
                    if (window1 != null && window1.isShowing()) {
                        textView1.setText("恢复完成");
                        fileListAdapter.notifyDataSetChanged();
                        window1.dismiss();
                    }
                    break;
                case 5://单个文件备份成功
                    Toast.makeText(FileListActivity.this,"成功备份当前文件",Toast.LENGTH_SHORT).show();
                    break;
                case 6://系统异常，文件备份失败
                    Toast.makeText(FileListActivity.this,"系统异常，文件备份失败",Toast.LENGTH_SHORT).show();
                    break;
                case 7://成功恢复单个文件
                    Toast.makeText(FileListActivity.this,"成功恢复单个文件",Toast.LENGTH_SHORT).show();
                    break;
                case 8://恢复失败
                    Toast.makeText(FileListActivity.this,"恢复失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private PopupWindow window1;
    private TextView textView1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(FileListActivity.this, "未获得文件读取权限", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
    }

    private void init() {
        linearLayout = findViewById(R.id.file_max);
        recyclerView = findViewById(R.id.list_files);
        backupsButton = findViewById(R.id.backups_file);
        back = findViewById(R.id.back);
        fab = findViewById(R.id.fab);
        currentFloderName = findViewById(R.id.file_diecetory);
        searhFile = findViewById(R.id.searh_file);
        addfloderButton = findViewById(R.id.add_floder);
        settingButton = findViewById(R.id.setting);
        fab.setOnClickListener(v -> startActivity(new Intent(FileListActivity.this,BackupActivity.class)));
        backupsButton.setOnClickListener(v -> popWindowbackupsFile(backupsButton));
        addfloderButton.setOnClickListener(v -> popWindowrecover(addfloderButton));
        searhFile.setOnClickListener(v -> startActivity(new Intent(FileListActivity.this, SearhFileActivity.class)));
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(FileListActivity.this, SettingActivity.class);
            startActivity(intent);
        });
        SysApplication.fileOs.setCurrentFloder(new File(SysApplication.fileOs.forSaveFloder + File.separator + "data"));
//        SysApplication.fileOs.setCurrentFloder(SysApplication.fileOs.getOsDicteoryPath(this));
        files = SysApplication.fileOs.getFiles();
        filesname = SysApplication.fileOs.getFilesName();
        recyclerView.setSystemUiVisibility(View.INVISIBLE);
        currentFloderName.setText(SysApplication.fileOs.getCurrentFloder().getAbsolutePath());
        fileListAdapter = new FileListAdapter(filesname, this, files, currentFloderName);
        fileListAdapter.setdeleteListener(file -> popWindowSelect(addfloderButton, file));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileListAdapter);
        back.setOnClickListener(v -> finish());
    }

    private void popWindow(View view) {
        // TODO: 2016/5/17 构建一个popupwindow的布局
        View popupView = FileListActivity.this.getLayoutInflater().inflate(R.layout.popwindow_add_floder, null);
        popupView.setPadding(50, 0, 50, 0);
        // TODO: 2016/5/17 为了演示效果，简单的设置了一些数据，实际中大家自己设置数据即可，相信大家都会。
        // TODO: 2016/5/17 创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.showAtLocation(linearLayout, Gravity.CLIP_VERTICAL, 0, 0);
        // TODO: 2016/5/17 设置动画
        window.setAnimationStyle(R.style.popup_window_anim);
        // TODO: 2016/5/17 设置背景颜色
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        // TODO: 2016/5/17 设置可以获取焦点
        window.setFocusable(true);
        // TODO: 2016/5/17 设置可以触摸弹出框以外的区域
        window.setOutsideTouchable(true);
        // TODO：更新popupwindow的状态
        window.update();
        // TODO: 2016/5/17 以下拉的方式显示，并且可以设置显示的位置
        window.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        EditText filename = popupView.findViewById(R.id.user_edit);
        TextView newFile = popupView.findViewById(R.id.add_button);
        newFile.setOnClickListener(v -> {
            if (filename.getText().toString().length() == 0) {
                Toast.makeText(FileListActivity.this, "文件夹名不能为空", Toast.LENGTH_SHORT).show();
            } else {
                File file = new File(SysApplication.fileOs.getCurrentFloder() + File.separator + filename.getText());
                file.mkdirs();
                window.dismiss();
                fileListAdapter.refreshList();
            }
        });
    }

    private void popWindowDeleteFile(View view, File file) {
        View popupView = FileListActivity.this.getLayoutInflater().inflate(R.layout.delete_floder, null);
        popupView.setPadding(50, 0, 50, 0);
        PopupWindow window = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.showAtLocation(linearLayout, Gravity.CLIP_VERTICAL, 0, 0);
        window.setAnimationStyle(R.style.popup_window_anim);
        window.setWidth((int) getResources().getDimension(R.dimen.dp_200));
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        window.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        TextView cancel = popupView.findViewById(R.id.cancel);
        TextView ensure = popupView.findViewById(R.id.ensure);
        ensure.setOnClickListener(v -> {
            if (file.exists()) {
                file.delete();
            }
            fileListAdapter.refreshList();
            window.dismiss();
        });
        cancel.setOnClickListener(v -> window.dismiss());
    }

    private void popWindowbackupsFile(View view) {
        View popupView = FileListActivity.this.getLayoutInflater().inflate(R.layout.delete_floder, null);
        popupView.setPadding(50, 0, 50, 0);
        window = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        textView = popupView.findViewById(R.id.window_title);
        textView.setText("是否将所有文件进行重新备份");
        window.showAtLocation(linearLayout, Gravity.CLIP_VERTICAL, 0, 0);
        window.setAnimationStyle(R.style.popup_window_anim);
        window.setWidth((int) getResources().getDimension(R.dimen.dp_200));
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        window.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        TextView cancel = popupView.findViewById(R.id.cancel);
        TextView ensure = popupView.findViewById(R.id.ensure);
        ensure.setOnClickListener(v -> {
            SysApplication.fileOs.backups(handler);
            textView.setText("正在备份，请稍后...");
            ensure.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
        });
        cancel.setOnClickListener(v -> window.dismiss());
    }

    private void popWindowrecover(View view) {
        View popupView = FileListActivity.this.getLayoutInflater().inflate(R.layout.delete_floder, null);
        popupView.setPadding(50, 0, 50, 0);
        window1 = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        textView1 = popupView.findViewById(R.id.window_title);
        textView1.setText("是否从备份进行恢复," + "共" + SysApplication.fileOs.getbackupsnums() + "项将恢复到上次备份状态");
        window1.showAtLocation(linearLayout, Gravity.CLIP_VERTICAL, 0, 0);
        window1.setAnimationStyle(R.style.popup_window_anim);
        window1.setWidth((int) getResources().getDimension(R.dimen.dp_200));
        window1.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window1.setFocusable(true);
        window1.setOutsideTouchable(true);
        window1.update();
        window1.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        TextView cancel = popupView.findViewById(R.id.cancel);
        TextView ensure = popupView.findViewById(R.id.ensure);
        ensure.setOnClickListener(v -> {
            SysApplication.fileOs.getbackups(handler);
            textView1.setText("正在从备份进行恢复，请稍后...");
            ensure.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
        });
        cancel.setOnClickListener(v -> window1.dismiss());
    }

    private void popWindowSelect(View view, File file) {
        View popupView = FileListActivity.this.getLayoutInflater().inflate(R.layout.do_for_floder, null);
        int height = R.dimen.dp_100;
        PopupWindow window1 = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 350, true);
        window1.showAtLocation(linearLayout, Gravity.CLIP_VERTICAL, 0, 0);
        window1.setAnimationStyle(R.style.popup_window_anim);
        window1.setWidth((int) getResources().getDimension(R.dimen.dp_200));
        window1.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        window1.setFocusable(true);
        window1.setOutsideTouchable(true);
        window1.update();
        window1.showAsDropDown(view, 0, 0, Gravity.BOTTOM);
        TextView delete = popupView.findViewById(R.id.delete);
        TextView backups = popupView.findViewById(R.id.backups);
        TextView recover = popupView.findViewById(R.id.recover);
        recover.setVisibility(View.GONE);
        delete.setOnClickListener(v -> {
            window1.dismiss();
            popWindowDeleteFile(view,file);
        });
        backups.setOnClickListener(v -> {
            SysApplication.fileOs.backupsOne(handler,file.getName());
            window1.dismiss();
        });
        recover.setOnClickListener(v -> {
            SysApplication.fileOs.recoverOne(handler,file.getName());
            window1.dismiss();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SysApplication.fileOs.pushStack(SysApplication.fileOs.getCurrentFloder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SysApplication.permissionManager.requestPermission(FileListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, () -> init());
        if (!SysApplication.fileOs.fileStack.isEmpty()) {
            SysApplication.fileOs.setCurrentFloder(SysApplication.fileOs.popStack());
        }
    }

    @Override
    public void onBackPressed() {
        if (SysApplication.fileOs.getFileStack().isEmpty() == true) {
            super.onBackPressed();
        } else {
            File file = SysApplication.fileOs.popStack();
            SysApplication.fileOs.setCurrentFloder(file);
            fileListAdapter.files = SysApplication.fileOs.getFiles();
            fileListAdapter.filesname = SysApplication.fileOs.getFilesName();
            currentFloderName.setText(file.getAbsolutePath());
            fileListAdapter.notifyDataSetChanged();
        }
    }
}

