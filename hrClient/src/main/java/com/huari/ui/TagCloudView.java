package com.huari.ui;

/**
 * Copyright © 2016 moxun
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the “Software”),
 * to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.huari.client.R;


public class TagCloudView extends ViewGroup implements Runnable, TagsAdapter.OnDataSetChangeListener {
    private final float TOUCH_SCALE_FACTOR = .8f;
    private final float TRACKBALL_SCALE_FACTOR = 10;
    private float tspeed = 2f;
    private TagCloud mTagCloud;
    private float mAngleX = 0.5f;
    private float mAngleY = 0.5f;
    private float centerX, centerY;
    private float radius;
    private float radiusPercent = 0.9f;

    private float[] darkColor = new float[]{1f, 0f, 0f, 1f};//rgba
    private float[] lightColor = new float[]{0.9412f, 0.7686f, 0.2f, 1f};//rgba

    public static final int MODE_DISABLE = 0;
    public static final int MODE_DECELERATE = 1;
    public static final int MODE_UNIFORM = 2;
    public int mode;
    private int left, right, top, bottom;

    private MarginLayoutParams layoutParams;
    private int minSize;

    private boolean isOnTouch = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private TagsAdapter tagsAdapter;
    private ValueAnimator animator;

    public TagCloudView(Context context) {
        super(context);
        init(context, null);
    }

    public TagCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TagCloudView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setFocusableInTouchMode(true);
        mTagCloud = new TagCloud();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagCloudView);

            String m = typedArray.getString(R.styleable.TagCloudView_autoScrollMode);
            mode = Integer.valueOf(m);

            int light = typedArray.getColor(R.styleable.TagCloudView_lightColor, Color.WHITE);
            setLightColor(light);

            int dark = typedArray.getColor(R.styleable.TagCloudView_darkColor, Color.BLACK);
            setDarkColor(dark);

            float p = typedArray.getFloat(R.styleable.TagCloudView_radiusPercent, radiusPercent);
            setRadiusPercent(p);

            float s = typedArray.getFloat(R.styleable.TagCloudView_scrollSpeed, 2f);
            setScrollSpeed(s);

            typedArray.recycle();
        }

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            wm.getDefaultDisplay().getSize(point);
        } else {
            point.x = wm.getDefaultDisplay().getWidth();
            point.y = wm.getDefaultDisplay().getHeight();
        }
        int screenWidth = point.x;
        int screenHeight = point.y;
        minSize = screenHeight < screenWidth ? screenHeight : screenWidth;
    }

    public void setAutoScrollMode(int mode) {
        this.mode = mode;
    }

    public final void setAdapter(TagsAdapter adapter) {
        tagsAdapter = adapter;
        tagsAdapter.setOnDataSetChangeListener(this);
        onChange();
    }

    public void setLightColor(int color) {
        float[] argb = new float[4];
        argb[3] = Color.alpha(color) / 1.0f / 0xff;
        argb[0] = Color.red(color) / 1.0f / 0xff;
        argb[1] = Color.green(color) / 1.0f / 0xff;
        argb[2] = Color.blue(color) / 1.0f / 0xff;
        lightColor = argb.clone();
        onChange();
    }

    public void setDarkColor(int color) {
        float[] argb = new float[4];
        argb[3] = Color.alpha(color) / 1.0f / 0xff;
        argb[0] = Color.red(color) / 1.0f / 0xff;
        argb[1] = Color.green(color) / 1.0f / 0xff;
        argb[2] = Color.blue(color) / 1.0f / 0xff;
        darkColor = argb.clone();
        onChange();
    }

    public void setRadiusPercent(float percent) {
        if (percent > 1f || percent < 0f) {
            throw new IllegalArgumentException("percent value not in range 0 to 1");
        } else {
            radiusPercent = percent;
            Log.d("xiaoxiao", "setRadiusPercent");
            onChange();
        }
    }

    private void initFromAdapter() {
        this.postDelayed(() -> {
            centerX = (getRight() - getLeft()) / 2;
            centerY = (getBottom() - getTop()) / 2;
            radius = Math.min(centerX * radiusPercent, centerY * radiusPercent);
            mTagCloud.setRadius((int) radius);

            mTagCloud.setTagColorLight(lightColor);//higher color
            mTagCloud.setTagColorDark(darkColor);//lower color

            mTagCloud.clear();
            removeAllViews();
            for (int i = 0; i < tagsAdapter.getCount(); i++) {
                TagCloudView.this.mTagCloud.add(new Tag(tagsAdapter.getPopularity(i)));
                addView(tagsAdapter.getView(getContext(), i, TagCloudView.this));
            }

            mTagCloud.create(true);

            mTagCloud.setAngleX(mAngleX);
            mTagCloud.setAngleY(mAngleY);
            mTagCloud.update();
        }, 20);
    }

    public void setScrollSpeed(float scrollSpeed) {
        tspeed = scrollSpeed;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int contentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        measureChildren(widthMode, heightMode);

        if (layoutParams == null) {
            layoutParams = (MarginLayoutParams) getLayoutParams();
        }

        int dimensionX = widthMode == MeasureSpec.EXACTLY ? contentWidth : minSize - layoutParams.leftMargin - layoutParams.rightMargin;
        int dimensionY = heightMode == MeasureSpec.EXACTLY ? contentHeight : minSize - layoutParams.leftMargin - layoutParams.rightMargin;
        setMeasuredDimension(dimensionX, dimensionY);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("WrongCall")
    private void updateChild() {
        onLayout(false, left, top, right, bottom);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        left = l;
        right = r;
        top = t;
        bottom = b;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final Tag tag = mTagCloud.get(i);
                if (tag.getScale() < 1) {
                    child.setClickable(false);
                }
                if (tag.getScale() > 1) {
                    child.setClickable(true);
                }
                tagsAdapter.onThemeColorChanged(child, tag.getColor());
                child.setScaleX(tag.getScale());
                child.setScaleY(tag.getScale());
                int left, top;
                left = (int) (centerX + tag.getLoc2DX()) - child.getMeasuredWidth() / 2;
                top = (int) (centerY + tag.getLoc2DY()) - child.getMeasuredHeight() / 2;
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            }
        }
    }

    public void reset() {
        mTagCloud.reset();
        updateChild();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        mAngleX = (y) * tspeed * TRACKBALL_SCALE_FACTOR;
        mAngleY = (-x) * tspeed * TRACKBALL_SCALE_FACTOR;

        mTagCloud.setAngleX(mAngleX);
        mTagCloud.setAngleY(mAngleY);
        mTagCloud.update();

        updateChild();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (animator != null && animator.isRunning()) {
                    animator.end();
                }
                isOnTouch = true;
                Log.d("xiao", "down");
                break;
            case MotionEvent.ACTION_MOVE:
                //rotate elements depending on how far the selection point is from center of cloud
                float dx = x - centerX;
                float dy = y - centerY;
                mAngleX = (dy / radius) * tspeed * TOUCH_SCALE_FACTOR;
                mAngleY = (-dx / radius) * tspeed * TOUCH_SCALE_FACTOR;
                processTouch();

                break;
            case MotionEvent.ACTION_UP:
//                if(mAngleX>=0&&mAngleY>=0){
//                    animator = ValueAnimator.ofFloat(mAngleX, 0.5f);
//                }else if(mAngleX<=0&&mAngleY<=0){
//                    animator = ValueAnimator.ofFloat(mAngleX, -0.5f);
//                }
                double scaleMath = Math.sqrt((mAngleX * mAngleX + mAngleY * mAngleY) * 2);
                animator = ValueAnimator.ofFloat(mAngleX, (float) (mAngleX / scaleMath));
                animator.setDuration(1500);
                animator.addUpdateListener(animation -> {
                    float value = (Float) animation.getAnimatedValue();
                    float m = mAngleY / mAngleX;
                    mAngleX = value;
                    mAngleY = m * mAngleX;
                });
                animator.start();
            case MotionEvent.ACTION_CANCEL:
                isOnTouch = false;
                break;
        }
        return true;
    }

    private void processTouch() {
        if (mTagCloud != null) {
            mTagCloud.setAngleX(mAngleX);
            mTagCloud.setAngleY(mAngleY);
            mTagCloud.update();
        }
        ddmamd();
        updateChild();
    }

    @Override
    public void onChange() {
        initFromAdapter();
    }

    @Override
    public void run() {
        if (!isOnTouch && mode != MODE_DISABLE) {
            if (mode == MODE_DECELERATE) {
                if (mAngleX > 0.04f) {
                    mAngleX -= 0.02f;
                }
                if (mAngleY > 0.04f) {
                    mAngleY -= 0.02f;
                }
                if (mAngleX < -0.04f) {
                    mAngleX += 0.02f;
                }
                if (mAngleY < 0.04f) {
                    mAngleY += 0.02f;
                }
            }
            processTouch();
            ddmamd();
        }

        handler.postDelayed(this, 0);
    }
    public void ddmamd(){

    }
}
