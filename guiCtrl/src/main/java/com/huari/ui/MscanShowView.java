package com.huari.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

//import android.util.Log;
//import android.view.MotionEvent;


/**
 * 离散扫描控件
 */
public class MscanShowView extends View {
    private int mywidth, myheight;// 单位: dp
    Context con;
    int no = 0;
    float mk_x = 0, mk_y = 0, inputf = 0;
    boolean mk = false;

    String danwei = "dBuV";// 是场强的dBuV/m还是电平的dBuV
    String danweiName = "幅度";
    EditText inputtext;

    public void setDanwei(String danweiName, String danwei) {
        try {
            this.danweiName = danweiName;
            this.danwei = danwei;
            fudupath.reset();
            postInvalidate();
        } catch (Exception e) {

        }
    }

    private float al = -30f, ah = 80f;// 分别代表y轴上两端的幅度值，即最小值和最大值。默认为-20和80。
    public int leftmargin, upmargin;// 坐标横向和纵向上的起点，即纵轴从x坐标为leftmargin处画起，
    // 左侧有leftmargin宽的空白区，横轴从upmargin处画起，上侧有upmargin高的空白
    private float fl, fh;// 分别代表x轴上两端的频率值，即最小值和最大值
    // private float xpoint;// 每个dp代表多少频率差值

    private Paint wavepaint;

    private Paint mengbanpaint;
    private Paint markpaint;
    private Paint marginpaint, textpaint, p2, shutextpaint, yuzhipaint,
            textpaint2;// 画示波界面四条边框的画笔

    private Paint virtualpaint;// 画示波界面内虚线的画笔
    private PathEffect effects;// 用以制作虚线效果
    private float[] m = null;
    private float[] freqlist;

    private float startx = 0, endx = 0, textx;// 分别记录触屏事件的起点、终点的x坐标,纵坐标标值的x起点
    private float starty = 0, endy = 0;// 分别记录触屏事件的起点、终点的y坐标
    private float headf, tailf, bujin;// 调频率后两端的频率值,bujin单位Hz

    private float xp, yp;// 即x轴和y轴上每个小刻度的宽度,单位：dp
    private float aperdp;// 纵向上每个dp代表多少个幅度值

    private float zeroatdp;
    private float fxlocation;
    private float fylocation;
    private Path fudupath;
    private boolean showyz = true;
    private Rect r;
    private float chufazhi;
    private int startnum, endnum;// 要展示的一串点的第一个点和最后一个点的索引
    private int count;
    private float linex, startlinex;
    private boolean yuzhiboolean;// 当纵向滑动时，是调整阈值，还是调整幅度值
    public float yuzhifudu = 70;// 展示阈值线所标的刻度（幅度），也是阈值。

    private float networkheight;


    private float mItemWidth;
    private int n = 4;

    public void initialize() {
        leftmargin = 65;
        textx = leftmargin - 5;

        upmargin = 8;

        fl = 88.000f;
        fh = 108.000f;
        tailf = fh;
        headf = fl;
        bujin = 25;
        count = (int) ((fh - fl) * 1000 / bujin);
        wavepaint = new Paint();

        textpaint = new Paint();
        textpaint2 = new Paint();
        shutextpaint = new Paint();
        mengbanpaint = new Paint();
        markpaint = new Paint();
        markpaint.setColor(Color.RED);
        markpaint.setTextSize(40);
        mengbanpaint.setColor(Color.YELLOW);
        mengbanpaint.setAlpha(100);
        wavepaint.setColor(Color.GREEN);
        wavepaint.setStyle(Style.FILL_AND_STROKE);

        mItemWidth = (mywidth - leftmargin - 13) / (2 * n + 1);

        marginpaint = new Paint();
        marginpaint.setColor(Color.parseColor("#70f3ff"));
        marginpaint.setStrokeWidth(0.4f);
        marginpaint.setTextSize(16);
        textpaint.setColor(Color.parseColor("#70f3ff"));
        textpaint.setTextAlign(Align.CENTER);
        textpaint.setTextSize(20);
        textpaint2.setColor(Color.parseColor("#70f3ff"));
        textpaint2.setTextAlign(Align.RIGHT);
        textpaint2.setTextSize(30);
        shutextpaint.setColor(Color.parseColor("#70f3ff"));
        shutextpaint.setTextAlign(Align.RIGHT);
        shutextpaint.setTextSize(16);

        virtualpaint = new Paint();
        virtualpaint.setColor(Color.parseColor("#70f3ff"));
        virtualpaint.setStyle(Paint.Style.STROKE);

        p2 = new Paint();
        p2.setColor(Color.YELLOW);
        p2.setTextSize(16);
        effects = new DashPathEffect(new float[]{1, 2}, 1);// 虚线风格

        virtualpaint.setStyle(Style.STROKE);

        marginpaint.setStrokeWidth(2);

        yuzhipaint = new Paint();
        yuzhipaint.setColor(Color.WHITE);
        yuzhipaint.setStrokeWidth(2);
        yuzhipaint.setTextAlign(Align.LEFT);

        fudupath = new Path();
        startnum = 0;
        endnum = count - 1;

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * 数据按比例转坐标
     */
    private float toY(float dat) {
        float y;
        try {
            y = zeroatdp - dat / aperdp;
        } catch (Exception e) {
            return 0;
        }
        return y;
    }

    public void setlevel(Canvas canvas, float[] data, int num) {
        mItemWidth = (mywidth - leftmargin - 13) / (2 * num + 1);

        float startx = leftmargin;

        canvas.save();
        for (int i = 0; i < num; i++) {

            startx += mItemWidth;

            RectF rect = new RectF(startx, toY(data[i]), startx + mItemWidth, toY(-30));

            startx += mItemWidth;

            try {
                canvas.drawRect(rect, wavepaint);
                canvas.drawText(String.format("%7.1f", data[i]), startx - mItemWidth + mItemWidth / 3, toY(data[i] + 1), markpaint); //标记电平
                canvas.drawText(String.format("%7.2f", freqlist[i]), startx - 6, myheight - 1, textpaint2);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chufazhi = getWidth() / 6;
        mywidth = getWidth();
        myheight = getHeight();

        mItemWidth = (mywidth - leftmargin - 13) / (2 * n + 1);

        xp = ((float) (mywidth - leftmargin - 13)) / 20;// 横向上每个坐标的间距，单位dp。横向上总共有20列。
        // 坐标界面的左边有x1-1个dp的空白，右边有14dp的空白.
        networkheight = myheight - upmargin - 33;
        yp = ((float) networkheight) / 20;// 纵向上每个坐标点间的间距，单位是dp

    }

    /**
     * public void setWavepaint(Paint wavepaint) <br/>
     * <br/>
     * 用来设置画波形的Paint。
     *
     * @param wavepaint 画波形的画笔。
     */
    public void setWavepaint(Paint wavepaint) {
        this.wavepaint = wavepaint;
    }

    /**
     * 用来设置画示波界面的矩形区域边框的画笔。
     *
     * @param marginpaint 画矩形区域边框的画笔。“频率”、“幅度”和坐标的标值等也是用这个画笔画的。
     */
    public void setMarginpaint(Paint marginpaint) {
        this.marginpaint = marginpaint;
    }

    /**
     * 用来设置画坐标网格内虚线的画笔。
     *
     * @param virtualpaint 画虚线的画笔。
     */
    public void setVirtualpaint(Paint virtualpaint) {
        this.virtualpaint = virtualpaint;
    }

    /**
     * 用来设置表示波形幅度值的整型数组。
     *
     * @param m 一个含有count个元素的整型数组，每个元素表示对应点的实时幅度值。
     */
    public void setM(float[] m, float[] freqlist) {
        if (this.m != null && this.m.length == m.length) {
            for (int i = 0; i < m.length; i++) {
                if (m[i] != 0.0) {
                    this.m[i] = m[i];
                }
            }
        } else {
            this.m = new float[m.length];
            for (int i = 0; i < m.length; i++) {
                this.m[i] = m[i];
            }
        }
        this.freqlist = freqlist;
    }

    public MscanShowView(Context context) {
        super(context);
        con = context;
        initialize();
    }

    public MscanShowView(Context context, AttributeSet set) {
        super(context, set);
        con = context;
        initialize();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG)); // 抗锯齿效果
        canvas.drawColor(Color.BLACK);

        //wavexs = (float) ((float) (mywidth - leftmargin - 13) / (showpointcount - 1));// 宽度被分成了若干份
        DrawXY(canvas);

        if (m != null) {
            if (m.length > 0)
                setlevel(canvas, m, m.length);
        }
    }

    private void DrawXY(Canvas canvas)// 画坐标
    {
        canvas.drawColor(Color.BLACK);
        aperdp = (ah - al) / (myheight - upmargin - 33);
        zeroatdp = upmargin + ah / aperdp;
        float ya = (ah - al) / 20;// y轴上每个小刻度代表的幅度差值,单位是dBuV

        // 以下画四条边框和边框指向的坐标值
        canvas.drawLine(leftmargin - 4, upmargin, leftmargin + xp * 20,
                upmargin, marginpaint);// 第一条横框线,左起点稍微突出，是为了标记坐标值，其他情况同理。即要标坐标的地方，都凸出4个宽度
        canvas.drawText(ah + "", textx, 12, shutextpaint);// 标坐标值,即y轴轴端上的最大幅度值，默认值为80

        canvas.drawLine(leftmargin - 4, upmargin + yp * 20, leftmargin + xp
                * 20, upmargin + yp * 20, marginpaint);// 第二条横框线

        canvas.drawText(al + "", textx, upmargin + 5 + yp * 20, shutextpaint);// 标坐标值，即y轴轴端上的最小幅度值，默认值-20
        canvas.drawLine(leftmargin, upmargin, leftmargin, upmargin + 4 + yp
                * 20, marginpaint);// 第一条竖框线
        canvas.drawLine(leftmargin + xp * 20, upmargin, leftmargin + xp * 20,
                upmargin + 4 + yp * 20, textpaint);// 第二条竖框线
        fxlocation = mywidth - 6;
        fylocation = myheight - 3;
        fudupath.moveTo(15, (myheight - 33) / 2 + 80);
        fudupath.lineTo(15, (myheight - 33) / 2 - 84);
        //canvas.drawText("频 率", leftmargin+60, fylocation, textpaint2);
        canvas.drawText("(MHz)", fxlocation, fylocation, textpaint2);
        canvas.drawLine(leftmargin - 4, zeroatdp, leftmargin, zeroatdp,
                marginpaint);
        // canvas.drawText(0 + "", textx, zeroatdp + 3, shutextpaint);// 标0幅度的位置
        canvas.drawTextOnPath(danweiName + "(" + danwei + ")", fudupath, 0, 0,
                textpaint);

        Path pt = new Path();
        pt.moveTo(leftmargin, 20);

        for (int i = 1; i < 20; i++) {
            canvas.drawLine(leftmargin, upmargin + yp * i,
                    leftmargin + xp * 20, upmargin + yp * i, virtualpaint);// 划X向的横虚线
//			canvas.drawLine(leftmargin + xp * i, upmargin, leftmargin + xp * i,
//					upmargin + yp * 20, virtualpaint);// 划Y向的竖虚线
            if (i % 2 == 0) {
                canvas.drawLine(leftmargin - 4, upmargin + yp * i, leftmargin,
                        upmargin + yp * i, marginpaint);
//				canvas.drawLine(leftmargin + xp * i, upmargin + yp * 20,
//						leftmargin + xp * i, upmargin + yp * 20 + 4,
//						marginpaint);

                canvas.drawText((ah - i * ya) + "", textx, upmargin + 5 + yp
                        * i, shutextpaint);// 画y轴上的坐标
            }
        }
    }// DrawXY()结束


    private float yuxianzhitoy(float yuxianzhi) {
        float y = myheight - 33 - (yuzhifudu - al) / (ah - al)
                * (myheight - 33 - upmargin);
        return y;
    }

    public void set_ah_al(float ah, float al) {
        this.ah = ah;
        this.al = al;
        postInvalidate();
    }

}
