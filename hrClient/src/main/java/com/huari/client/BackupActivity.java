package com.huari.client;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huari.adapter.BackupsAdapter;
import com.huari.tools.FileOsImpl;
import com.huari.tools.SysApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import cn.bmob.v3.util.V;

public class BackupActivity extends AppCompatActivity {
    ImageView back;
    TextView title;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    BackupsAdapter adapter;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        back = findViewById(R.id.back);
        back.setSystemUiVisibility(View.INVISIBLE);
        title = findViewById(R.id.file_diecetory);
        recyclerView = findViewById(R.id.list_files);
        linearLayout = findViewById(R.id.linearlayout);
        title.setText("回收箱");
        back.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListRefresh();
    }

    private void ListRefresh() {
        File file = new File(FileOsImpl.forSaveFloder + File.separator + "backupstem");
        List<String> list = new ArrayList<>();
        for (File file1 : file.listFiles()) {
            list.add(file1.getAbsolutePath());
        }
        adapter = new BackupsAdapter(list, this, Arrays.asList(file.listFiles()));
        recyclerView.setAdapter(adapter);
        adapter.setdeleteListener(position -> popWindowSelect(back, new File(list.get(position))));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void popWindowSelect(View view, File file) {
        View popupView = BackupActivity.this.getLayoutInflater().inflate(R.layout.do_for_floder, null);
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
        backups.setVisibility(View.GONE);
        TextView recover = popupView.findViewById(R.id.recover);
        delete.setOnClickListener(v -> {
            file.delete();
            window1.dismiss();
            ListRefresh();
            Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
        });
//        backups.setOnClickListener(v -> {
//            SysApplication.fileOs.backupsOne(handler,file.getName());
//            window1.dismiss();
//        });
        recover.setOnClickListener(v -> {
            SysApplication.fileOs.recoverOne(handler, file.getName());
            window1.dismiss();
            Toast.makeText(this,"恢复成功",Toast.LENGTH_SHORT).show();
        });
    }

}
