package com.star.miclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


public class MiClockView extends View {

    private static final int CIRCLE_STROKE_WIDTH = 2;
    private static final int DEFAULT_MEASURED_DIMENSION = 800;

    private static final float PADDING_RATIO = 0.12f;

    private float SWEEP_GRADIENT_START = 0.75f;
    private float SWEEP_GRADIENT_END = 1;

    private float ARC_INTERVAL = 5;

    private int mBackgroundColor;
    private int mLightColor;
    private int mDarkColor;
    private float mTextSize;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;

    private float mScaleLineLength;
    private Paint mScaleLinePaint;
    private Paint mScaleArcPaint;

    private Paint mTextPaint;

    private Paint mCirclePaint;

    private int mRadius;

    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;

    private SweepGradient mSweepGradient;

    private Canvas mCanvas;

    private Rect mTextRect;
    private RectF mCircleRectF;

    public MiClockView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiClockView,
                0, 0);

        mBackgroundColor = typedArray.getColor(R.styleable.MiClockView_backgroundColor,
                ContextCompat.getColor(context, R.color.colorBackground));
        mDarkColor = typedArray.getColor(R.styleable.MiClockView_darkColor,
                ContextCompat.getColor(context, R.color.colorDark));
        mLightColor = typedArray.getColor(R.styleable.MiClockView_lightColor,
                ContextCompat.getColor(context, R.color.colorLight));
        mTextSize = typedArray.getDimension(R.styleable.MiClockView_textSize,
                DensityUtils.sp2px(context, 14));

        typedArray.recycle();

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mDarkColor);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mMinuteHandPaint.setColor(mLightColor);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(mLightColor);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mDarkColor);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);
        mScaleArcPaint.setColor(mDarkColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mDarkColor);
        mTextPaint.setTextSize(mTextSize);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(mDarkColor);
        mCirclePaint.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        mTextRect = new Rect();
        mCircleRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasuredDimension(DEFAULT_MEASURED_DIMENSION, widthMeasureSpec),
                getMeasuredDimension(DEFAULT_MEASURED_DIMENSION, heightMeasureSpec));
    }

    private int getMeasuredDimension(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
        }

        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2;

        mDefaultPadding = PADDING_RATIO * mRadius;

        mPaddingLeft = mDefaultPadding + getPaddingLeft();
        mPaddingTop = mDefaultPadding + getPaddingTop();
        mPaddingRight = mPaddingLeft;
        mPaddingBottom = mPaddingTop;

        mScaleLineLength = mRadius * PADDING_RATIO;
        mScaleArcPaint.setStrokeWidth(mScaleLineLength);
        mScaleLinePaint.setStrokeWidth(mRadius * PADDING_RATIO * PADDING_RATIO);

        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[] {mDarkColor, mLightColor},
                new float[] {SWEEP_GRADIENT_START, SWEEP_GRADIENT_END});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCanvas = canvas;

        drawTimeText();
    }

    private void drawTimeText() {

        mCircleRectF.set(mPaddingLeft + CIRCLE_STROKE_WIDTH / 2,
                mPaddingTop + CIRCLE_STROKE_WIDTH / 2,
                getWidth() - mPaddingRight  - CIRCLE_STROKE_WIDTH / 2,
                getHeight() - mPaddingBottom  - CIRCLE_STROKE_WIDTH / 2);

        for (int i = 0; i < 4; i++) {
            String timeText = (i + 1) * 3 + "";

            mTextPaint.getTextBounds(timeText, 0, timeText.length(), mTextRect);

            float centerX = (float) (getWidth() / 2 +
                    Math.cos(i * Math.PI / 2) * (mRadius - mDefaultPadding));
            float centerY = (float) (getHeight() / 2 +
                    Math.sin(i * Math.PI / 2) * (mRadius - mDefaultPadding));

            mCanvas.drawText(timeText, centerX - mTextRect.width() / 2,
                    centerY + mTextRect.height() / 2, mTextPaint);

            mCanvas.drawArc(mCircleRectF, ARC_INTERVAL + 90 * i, 90 - 2 * ARC_INTERVAL, false,
                    mCirclePaint);
        }
    }

    private void drawScaleLine() {

    }
}
