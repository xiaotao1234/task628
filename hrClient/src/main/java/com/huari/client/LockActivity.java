package com.huari.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.huari.tools.MD5;
import com.huari.tools.SysApplication;

public class LockActivity extends AppCompatActivity {
    TextView textView;
    EditText editText;
    TextView textViewUnlock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        textView = findViewById(R.id.md5_number);
        editText = findViewById(R.id.md5_edit);
        textView.setText(SysApplication.id);
        textViewUnlock = findViewById(R.id.unlock);
        textViewUnlock.setOnClickListener(v -> {
            if(editText.getText().toString().trim().equals(MD5.getInstance().getMD5(SysApplication.id))){
                Toast.makeText(LockActivity.this,"解密成功",Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = getSharedPreferences("welcome", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("islock", false);
                editor.commit();
                startActivity(new Intent(LockActivity.this, MajorActivity.class));
                finish();
            }else {
                Toast.makeText(LockActivity.this,"解密失败，解密码错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
