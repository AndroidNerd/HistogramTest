package com.example.nerd.histogramtest.view;

import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;


import com.example.nerd.histogramtest.R;

import java.util.Calendar;
import java.util.List;

public class PerformanceHistogram extends View implements Runnable {

    private static final int SALESVOLUMESANIMATORDURATION = 1200;

    private int xAxis = 50;
    private int dashLineMargin = 60;
    private boolean isMeasureViewSize = true;
    private int viewHeight = 290;
    private int viewWidth = 934;
    private Paint paintYearText;
    private Paint paintYearLine;
    private Paint paintXAxis;
    private Paint paintXDashLine;
    private Path xDashLinePath;
    private int currentSelectedMonthIndex;

    private int yearTextColor;
    private int yearTextSize;
    private int yearLineColor;
    private int textColorNormal;
    private int textColorSelected;
    private int textSize;
    private Paint paintText;
    private int textRectWidth;
    private int textRectHeight;

    private Calendar calendar;

    private int salesVolumesDefaultHeight = 5;
    private double salesVolumesScale = 1;

    private List<PerformanceMonth> year;
    private int valueRectMargin;
    private int valueRectWidth;
    private Drawable salesVolumesDrawableNormal;
    private Drawable salesVolumesDrawableSelected;

    private Rect yearTextRect;
    private Rect[] monthRects = new Rect[12];

    private boolean[] salesVolumesDrawStatus = new boolean[12]; // draw finish status == true
    private Rect[] rects = new Rect[12];

    private boolean drawText = false;
    private int monthXDefauleOffSet;// animate
    private int startDrawMonthIndex;// animate
    private long startDrawsalesVolumesTime;
    private long drawsalesVolumesTimeOffset;

    private OnMonthSelectedListener onMonthSelectedListener;

    public void setOnMonthSelectedListener(OnMonthSelectedListener onMonthSelectedListener) {
        this.onMonthSelectedListener = onMonthSelectedListener;
    }

    public PerformanceHistogram(Context context) {
        super(context);
        init();
    }

    public PerformanceHistogram(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PerformanceHistogram(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PerformanceHistogram(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        xAxis = getResources().getDimensionPixelSize(R.dimen.PerformanceHistogram_xAxis_Height);
        dashLineMargin = getResources().getDimensionPixelSize(R.dimen.PerformanceHistogram_xDashLine_margin);
        int xAxisColor = ContextCompat.getColor(getContext(), R.color.PerformanceHistogram_xAxisColor);

        valueRectMargin = getResources().getDimensionPixelSize(R.dimen.PerformanceHistogram_valueRectMargin);
        valueRectWidth = getResources().getDimensionPixelSize(R.dimen.PerformanceHistogram_valueRectWidth);
        salesVolumesDrawableNormal = ContextCompat.getDrawable(getContext(), R.drawable.shape_sales_volumes_normal);
        salesVolumesDrawableSelected = ContextCompat.getDrawable(getContext(), R.drawable.shape_sales_volumes_selected);

        yearLineColor = ContextCompat.getColor(getContext(), R.color.PerformanceHistogram_yearLine);
        yearTextColor = ContextCompat.getColor(getContext(), R.color.PerformanceHistogram_yearText);
        yearTextSize = getResources().getDimensionPixelSize(R.dimen.s5);

        int xDashLineColor = xAxisColor;
        textColorNormal = xAxisColor;
        textColorSelected = ContextCompat.getColor(getContext(), R.color.selectedColor);
        textSize = getResources().getDimensionPixelSize(R.dimen.s5);

        paintYearText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintYearText.setTextSize(yearTextSize);
        paintYearText.setTextAlign(Paint.Align.CENTER);
        paintYearText.setStrokeWidth(1);

        paintYearLine = new Paint();
        paintYearLine.setStyle(Paint.Style.STROKE);
        paintYearLine.setAntiAlias(true);
        paintYearLine.setColor(yearLineColor);
        paintYearLine.setStrokeWidth(1);

        paintXAxis = new Paint();
        paintXAxis.setStyle(Paint.Style.STROKE);
        paintXAxis.setAntiAlias(true);
        paintXAxis.setColor(xAxisColor);
        paintXAxis.setStrokeWidth(1);

        paintXDashLine = new Paint();
        paintXDashLine.setStyle(Paint.Style.STROKE);
        paintXDashLine.setAntiAlias(true);
        paintXDashLine.setColor(xDashLineColor);
        paintXDashLine.setStrokeWidth(1);
        PathEffect dashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
        paintXDashLine.setPathEffect(dashPathEffect);
        xDashLinePath = new Path();

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStrokeWidth(1);
        paintText.setTextSize(textSize);
        paintText.setTextAlign(Paint.Align.CENTER);
        textRectHeight = xAxis;

        calendar = Calendar.getInstance();
    }

    public void setData(List<PerformanceMonth> year) {
        if (year == null || year.size() != 12) {
            return;
        }
        this.year = year;
        currentSelectedMonthIndex = calendar.get(Calendar.MONTH);
        int lastMonthOfYearIndex = 0;
        double max = 0;
        if (year != null) {
            for (int i = 0; i < 12; i++) {
                PerformanceMonth performanceMonth = year.get(i);
                if (performanceMonth == null) {
                    continue;
                }
                if (performanceMonth.getSalesVolumes() > max) {
                    max = performanceMonth.getSalesVolumes();
                }
                if (performanceMonth.getMonth() == 12) {
                    lastMonthOfYearIndex = i;
                }
            }
        }
        salesVolumesScale = max == 0 ? 1 : (viewHeight - xAxis) / max;

        if (lastMonthOfYearIndex != 11) {
            Paint.FontMetrics fontMetrics = paintYearText.getFontMetrics();
            int height = (int) (fontMetrics.bottom - fontMetrics.top);
            yearTextRect = new Rect((lastMonthOfYearIndex + 1) * textRectWidth - 80, 0, (lastMonthOfYearIndex + 1) * textRectWidth + 80, height);
        }
        for (int i = 0; i < salesVolumesDrawStatus.length; i++) {
            salesVolumesDrawStatus[i] = false;
        }
        startDrawsalesVolumesTime = 0;
        runAnimate();
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isMeasureViewSize) {
            return;
        }
        isMeasureViewSize = false;
        viewHeight = bottom - top;
        viewWidth = right - left;

        textRectWidth = viewWidth / 12;

        for (int i = 0; i < 12; i++) {
            Rect rect = new Rect(i * textRectWidth, viewHeight - textRectHeight, (i + 1) * textRectWidth, viewHeight);
            monthRects[i] = rect;
        }


        int offSet = textRectWidth;
        for (int i = 0; i < 12; i++) {
            Rect rect = new Rect(valueRectMargin + offSet * i, viewHeight - xAxis, valueRectMargin + offSet * i + valueRectWidth, viewHeight - xAxis);
            rects[i] = rect;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, viewHeight - xAxis, viewWidth, viewHeight - xAxis, paintXAxis);

        for (int i = 1; i < 4; i++) {
            int h = viewHeight - (xAxis + i * dashLineMargin);
            xDashLinePath.moveTo(0, h);
            xDashLinePath.lineTo(viewWidth, h);
            canvas.drawPath(xDashLinePath, paintXDashLine);
        }

        drawYear(canvas);//暂时无用

        if (year != null) {
            for (int i = 0; i < year.size(); i++) {
                if (drawText) {
                    drawText(canvas, i);
                }
                drawPerformance(canvas, i);
            }
        }
    }

    private void drawYear(Canvas canvas) {
        if (yearTextRect == null) {
            return;
        }
        String y = getResources().getString(R.string.mYear, calendar.get(Calendar.YEAR));
        paintYearText.setColor(Color.TRANSPARENT);
        canvas.drawRect(yearTextRect.left - monthXDefauleOffSet, yearTextRect.top, yearTextRect.right - monthXDefauleOffSet, yearTextRect.bottom, paintYearText);
        paintYearText.setColor(yearTextColor);
        Paint.FontMetrics fontMetrics = paintYearText.getFontMetrics();
        int baseLine = (int) (yearTextRect.bottom + yearTextRect.top - fontMetrics.bottom - fontMetrics.top) >> 1;
        canvas.drawText(y, yearTextRect.centerX() - monthXDefauleOffSet, baseLine, paintYearText);

        canvas.drawLine(yearTextRect.centerX() - monthXDefauleOffSet, viewHeight - xAxis, yearTextRect.centerX() - monthXDefauleOffSet, yearTextRect.bottom - yearTextRect.top, paintYearLine);

    }

    private void drawText(Canvas canvas, int index) {
        int textColor = textColorNormal;
        if (index == currentSelectedMonthIndex) {
            textColor = textColorSelected;
        }
        paintText.setColor(Color.TRANSPARENT);

        Rect rect = monthRects[index];
        int m = year != null ? year.get(index) == null ? 0 : year.get(index).getMonth() : 0;
        String month = getResources().getString(R.string.mMonth, m);
//        if (m == calendar.get(Calendar.MONTH) + 1) {
//            month = getResources().getString(R.string.performance_currentMonth);
//        }
        canvas.drawRect(rect.left - monthXDefauleOffSet, rect.top, rect.right - monthXDefauleOffSet, rect.bottom, paintText);
        paintText.setColor(textColor);
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        int baseLine = (int) (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) >> 1;
        canvas.drawText(month, rect.centerX() - monthXDefauleOffSet, baseLine, paintText);
    }

    private void drawPerformance(Canvas canvas, int index) {
        PerformanceMonth performanceMonth = year.get(index);
        if (performanceMonth == null) {
            return;
        }
        Drawable drawable = index == currentSelectedMonthIndex ? salesVolumesDrawableSelected : salesVolumesDrawableNormal;
        drawable.setBounds(rects[index]);
        drawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (boolean status : salesVolumesDrawStatus) {
            if (!status) {
                return super.onTouchEvent(event);
            }
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (rects == null || monthRects == null) {
            return super.onTouchEvent(event);
        }
        for (int i = 0; i < 12; i++) {
            if (rects[i].contains(x, y) || monthRects[i].contains(x, y)) {
                currentSelectedMonthIndex = i;
                if (onMonthSelectedListener != null) {
                    onMonthSelectedListener.onMonthSelected(currentSelectedMonthIndex);
                }
                postInvalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void runAnimate() {
        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator monthAnimator = ValueAnimator.ofInt(viewWidth, 0);
        monthAnimator.setEvaluator(new IntEvaluator());
        monthAnimator.setDuration(viewWidth >> 1);
        monthAnimator.setInterpolator(new LinearInterpolator());
        monthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                monthXDefauleOffSet = (Integer) animation.getAnimatedValue();
                drawText = true;
                postInvalidate();
            }
        });

        ValueAnimator salesVolumesAnimator = ValueAnimator.ofInt(11, 0);
        salesVolumesAnimator.setEvaluator(new IntEvaluator());
        salesVolumesAnimator.setDuration(SALESVOLUMESANIMATORDURATION);
        salesVolumesAnimator.setInterpolator(new LinearInterpolator());
        salesVolumesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startDrawMonthIndex = (Integer) animation.getAnimatedValue();
                if (startDrawsalesVolumesTime == 0) {
                    startDrawsalesVolumesTime = System.currentTimeMillis();
                    PerformanceHistogram.this.post(PerformanceHistogram.this);
                }
            }
        });

        animatorSet.play(salesVolumesAnimator).after(monthAnimator);
        animatorSet.start();
    }

    private void updateSalesVolumes() {
        drawsalesVolumesTimeOffset = System.currentTimeMillis() - startDrawsalesVolumesTime;
        for (int i = rects.length - 1; i >= 0; i--) {
            double salesVolumes = year.get(i) == null ? 0 : year.get(i).getSalesVolumes();
            if (salesVolumes * salesVolumesScale < salesVolumesDefaultHeight) {
                salesVolumes = 0;
            }
            int realHeight = salesVolumes == 0 ? salesVolumesDefaultHeight : (int) (salesVolumes * salesVolumesScale);
            int virtureH = i >= startDrawMonthIndex ? (int) (drawsalesVolumesTimeOffset - (11 - i) * SALESVOLUMESANIMATORDURATION / 12) * 2 : 0; // v = 2;
            int displayHeight = 0;
            if (virtureH > realHeight) {
                displayHeight = realHeight;
                salesVolumesDrawStatus[i] = true;
            } else {
                displayHeight = virtureH;
                salesVolumesDrawStatus[i] = false;
            }
            Rect rect = rects[i];
            rect.top = viewHeight - xAxis - displayHeight;
        }
        postInvalidate();
    }

    @Override
    public void run() {
        for (int i = 11; i >= 0; i--) {
            if (!salesVolumesDrawStatus[i]) {
                updateSalesVolumes();
                postDelayed(this, 10);
                return;
            }
        }
        removeCallbacks(this);
    }

    public static interface OnMonthSelectedListener {

        /**
         * @param index index of month in array/collections
         */
        public void onMonthSelected(int index);
    }
}

