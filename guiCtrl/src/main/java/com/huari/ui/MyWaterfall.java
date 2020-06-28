package com.huari.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyWaterfall extends View {
    private final static String TAG = MyWaterfall.class.getSimpleName();
    private Paint mPaint;
    private Bitmap bm;

    int[] srcbuf;
    int[] dstbuf;
    short[] vals;
    private int[] m_ColorTbl;
    boolean m_Running = false;

    int leftmargin = 60;
    int rightmargin = 13;
    int m_ZAxisMin = -30;
    int m_ZAxisMax = 80;

    int m_Position = 0;
    int m_Scaling = 1;
    int d_len = 0;

    int plotHeight;
    int plotWidth;

    Lock lock1 = new ReentrantLock();

    public MyWaterfall(Context context) {
        super(context);
        init();
    }

    public MyWaterfall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyWaterfall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.BLACK);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        create_colortbl();
        set_ColorTbl(ColorTbl);
    }

    public void set_ColorTbl(int[] colorTbl) {
        m_ColorTbl = colorTbl;
    }

    public void update_data(short[] data, int len) {
        lock1.lock();
        if (d_len != len) {
            vals = null;
            vals = new short[len];
            d_len = len;
        }
        System.arraycopy(data, 0, vals, 0, len);
        lock1.unlock();
        m_Running = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        plotWidth = getWidth() - leftmargin - rightmargin;
        plotHeight = getHeight();

        bm = Bitmap.createBitmap(plotWidth, plotHeight, Bitmap.Config.ARGB_8888);

        srcbuf = new int[plotWidth * plotHeight];
        dstbuf = new int[plotWidth * plotHeight];
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG)); // 抗锯齿效果

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        int h = plotHeight;
        int w = plotWidth;
        if (m_Running) {
            lock1.lock();
            try {
                bm.getPixels(srcbuf, 0, w, 0, 0, w, h);

                System.arraycopy(srcbuf, 0, dstbuf, w, w * (h - 1));

                int FreqIndex = 0;//d_len * m_Position;
                int FreqLength = d_len;//* m_Scaling;
                if (FreqLength <= 0) FreqLength = 1;

                if (FreqLength > w) {
                    double PerPixelFreqN = (double) FreqLength / w;
                    double FreqN = FreqIndex + PerPixelFreqN;

                    if (vals != null)
                        for (int j = 0; j < w; j++) {
                            short MaxValue = (short) (vals[FreqIndex++] / 10f);

                            int k = (int) (Math.round(FreqN));
                            for (; FreqIndex < k; FreqIndex++)
                                if (MaxValue < vals[FreqIndex] / 10)
                                    MaxValue = (short) (vals[FreqIndex] / 10);

                            dstbuf[j] = ConvertToColorIndex(MaxValue);

                            FreqN += PerPixelFreqN;
                        }
                } else {
                    double PerFreqPixelN = (double) w / FreqLength;
                    double PixelN = PerFreqPixelN;

                    for (int k = 0, j = FreqIndex; j < FreqIndex + FreqLength; j++) {
                        int ColorIndexValue = ConvertToColorIndex((short) (vals[j] / 10));

                        int EndPixel = (int) (Math.round(PixelN));
                        for (; k < EndPixel; k++)
                            dstbuf[k] = ColorIndexValue;

                        PixelN += PerFreqPixelN;
                    }
                }

                bm.setPixels(dstbuf, 0, w, 0, 0, w, h);
                canvas.drawBitmap(bm, leftmargin, 0, mPaint);

            } finally {
                lock1.unlock();
            }

        }
    }

    public static int[] ColorTbl;

    public static void create_colortbl() {
        if (ColorTbl == null) {
            ColorTbl = new int[256];
            //生成颜色对应表
            for (int i = 0; i < 256; i++) {
                if (i < 43)
                    ColorTbl[i] = Color.rgb(0, 0, 255 * (i / 43));
                if (i >= 43 && i < 87)
                    ColorTbl[i] = Color.rgb(0, 255 * (i - 43) / 43, 255);
                if (i >= 87 && i < 120)
                    ColorTbl[i] = Color.rgb(0, 255, 255 - (255 * (i - 87) / 32));
                if (i >= 120 && i < 154)
                    ColorTbl[i] = Color.rgb((255 * (i - 120) / 33), 255, 0);
                if (i >= 154 && i < 217)
                    ColorTbl[i] = Color.rgb(255, 255 - (255 * (i - 154) / 62), 0);
                if (i >= 217)
                    ColorTbl[i] = Color.rgb(255, 0, 128 * (i - 217) / 38);
            }
        }
    }

    private int ConvertToColorIndex(short val) {
        //如果没有数据就是黑色
        if (val == -10000)
            return 0;

        int ret;
        if (val <= m_ZAxisMin)
            ret = 255;
        else if (val >= m_ZAxisMax)
            ret = 1;
        else
            ret = m_ColorTbl[(int) ((double) (val - m_ZAxisMin) / (m_ZAxisMax - m_ZAxisMin) * 255.0)];

        return ret;
    }


}
