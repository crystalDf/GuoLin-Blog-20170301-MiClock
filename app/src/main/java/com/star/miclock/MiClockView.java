package com.star.miclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class MiClockView extends View {

    private static final int CIRCLE_STROKE_WIDTH = 2;
    private static final int DEFAULT_MEASURED_DIMENSION = 800;

    private static final float PADDING_RATIO = 0.12f;

    private int mBackgroundColor;
    private int mLightColor;
    private int mDarkColor;
    private float mTextSize;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;

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

    public MiClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiClockView,
                defStyleAttr, 0);

        mBackgroundColor = typedArray.getColor(R.styleable.MiClockView_backgroundColor,
                Color.parseColor("#237EAD"));
        mLightColor = typedArray.getColor(R.styleable.MiClockView_lightColor,
                Color.parseColor("#FFFFFF"));
        mDarkColor = typedArray.getColor(R.styleable.MiClockView_darkColor,
                Color.parseColor("#80FFFFFF"));
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

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = mPaddingLeft;
        mPaddingTop = getPaddingTop();
        mPaddingBottom = mPaddingTop;
    }
}
