package com.huari.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class ItemView extends LinearLayout {
    static int checkId = -1;
    public ItemView(Context context) {
        super(context);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (deleteOwn != null) {
                    deleteOwn.deleteOwnListener(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface deleteOwn {
        void deleteOwnListener(View view);
    }

    public deleteOwn deleteOwn;

    public void setDeleteOwn(deleteOwn deleteOwn,int position) {
        this.deleteOwn = deleteOwn;
        this.setTag(position);
//        if(position == checkId){
//            this.setBackgroundColor(Color.parseColor("#00FFFFFF"));
//        }else {
//            checkId = position;
            this.setBackgroundColor(Color.parseColor("#004085"));
//        }
    }
}