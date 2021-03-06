package com.star.miclock;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.Calendar;


public class MiClockView extends View {

    private static final int CIRCLE_STROKE_WIDTH = 2;
    private static final int DEFAULT_MEASURED_DIMENSION = 800;

    private static final float PADDING_RATIO = 0.12f;

    private static final float CIRCLE_START_DEGREE = 0;
    private static final float CIRCLE_END_DEGREE = 360;

    private static final int SCALE_LINE_COUNT = 200;

    private static final float SWEEP_GRADIENT_START = 0.75f;
    private static final float SWEEP_GRADIENT_END = 1;

    private static final float ARC_INTERVAL = 5;

    private static final int MAX_CAMERA_ROTATE = 10;

    private static final String CAMERA_ROTATE_X = "cameraRotateX";
    private static final String CAMERA_ROTATE_Y = "cameraRotateY";
    private static final String CANVAS_TRANSLATE_X = "canvasTranslateX";
    private static final String CANVAS_TRANSLATE_Y = "canvasTranslateY";

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
    private Paint mCoverCirclePaint;

    private int mRadius;

    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;

    private SweepGradient mSweepGradient;
    private Matrix mGradientMatrix;

    private Canvas mCanvas;

    private Rect mTextRect;
    private RectF mCircleRectF;
    private RectF mScaleArcRectF;

    private float mSecondDegree;
    private float mMinuteDegree;
    private float mHourDegree;

    private Path mSecondHandPath;
    private Path mMinuteHandPath;
    private Path mHourHandPath;

    private Matrix mCameraMatrix;
    private Camera mCamera;

    private float mCameraRotateX;
    private float mCameraRotateY;

    private float mCanvasTranslateX;
    private float mCanvasTranslateY;

    private float mMaxCanvasTranslate;

    private ValueAnimator mValueAnimator;

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

        mCoverCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCoverCirclePaint.setStyle(Paint.Style.FILL);

        mTextRect = new Rect();
        mCircleRectF = new RectF();
        mScaleArcRectF = new RectF();

        mGradientMatrix = new Matrix();

        mSecondHandPath = new Path();
        mMinuteHandPath = new Path();
        mHourHandPath = new Path();

        mCameraMatrix = new Matrix();
        mCamera = new Camera();
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
        mScaleLinePaint.setStrokeWidth(mRadius * PADDING_RATIO * PADDING_RATIO / 2);

        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[] {mDarkColor, mLightColor},
                new float[] {SWEEP_GRADIENT_START, SWEEP_GRADIENT_END});

        mMaxCanvasTranslate = 0.12f * mRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCanvas = canvas;

        setCameraRotate();

        getTimeDegree();

        drawTimeText();
        drawScaleLine();
        drawHourHand();
        drawMinuteHand();
        drawSecondHand();
        drawCoverCircle();

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mValueAnimator != null && mValueAnimator.isRunning()) {
                    mValueAnimator.cancel();
                }
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;

            case MotionEvent.ACTION_MOVE:
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;

            case MotionEvent.ACTION_UP:
                startValueAnimator();
                break;
        }

        return true;
    }

    private void startValueAnimator() {

        PropertyValuesHolder cameraRotateXHolder = PropertyValuesHolder.ofFloat(
                CAMERA_ROTATE_X, mCameraRotateX, 0
        );
        PropertyValuesHolder cameraRotateYHolder = PropertyValuesHolder.ofFloat(
                CAMERA_ROTATE_Y, mCameraRotateY, 0
        );
        PropertyValuesHolder canvasTranslateXHolder = PropertyValuesHolder.ofFloat(
                CANVAS_TRANSLATE_X, mCanvasTranslateX, 0
        );
        PropertyValuesHolder canvasTranslateYHolder = PropertyValuesHolder.ofFloat(
                CANVAS_TRANSLATE_Y, mCanvasTranslateY, 0
        );

        mValueAnimator = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder,
                cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder);

        mValueAnimator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) *
                        Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });

        mValueAnimator.setDuration(1000);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCameraRotateX = (float) animation.getAnimatedValue(CAMERA_ROTATE_X);
                mCameraRotateY = (float) animation.getAnimatedValue(CAMERA_ROTATE_Y);
                mCanvasTranslateX = (float) animation.getAnimatedValue(CANVAS_TRANSLATE_X);
                mCanvasTranslateY = (float) animation.getAnimatedValue(CANVAS_TRANSLATE_Y);
            }
        });

        mValueAnimator.start();
    }

    private void getTimeDegree() {

        Calendar calendar = Calendar.getInstance();

        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float mSecond = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float mMinute = calendar.get(Calendar.MINUTE) + mSecond / 60;
        float mHour = calendar.get(Calendar.HOUR) + mMinute / 60;

        mSecondDegree = mSecond / 60 * 360;
        mMinuteDegree = mMinute / 60 * 360;
        mHourDegree = mHour / 12 * 360;
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

        mCanvas.save();

        mScaleArcRectF.set(mPaddingLeft + mScaleLineLength,
                mPaddingTop + mScaleLineLength,
                getWidth() - mPaddingRight  - mScaleLineLength,
                getHeight() - mPaddingBottom  - mScaleLineLength);

        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        mScaleArcPaint.setShader(mSweepGradient);

        mCanvas.drawArc(mScaleArcRectF, CIRCLE_START_DEGREE, CIRCLE_END_DEGREE, false,
                mScaleArcPaint);

        for (int i = 0; i < SCALE_LINE_COUNT; i++) {
            mCanvas.drawLine(getWidth() / 2, (float) (mPaddingTop + 0.5 * mScaleLineLength),
                    getWidth() / 2, (float) (mPaddingTop + 1.5 * mScaleLineLength),
                    mScaleLinePaint);
            mCanvas.rotate(CIRCLE_END_DEGREE / SCALE_LINE_COUNT, getWidth() / 2, getHeight() / 2);
        }

        mCanvas.restore();
    }

    private void drawSecondHand() {

        mCanvas.save();

        mCanvas.rotate(mSecondDegree, getWidth() / 2, getHeight() / 2);

        mSecondHandPath.reset();

        float offset = mPaddingTop;

        mSecondHandPath.moveTo(getWidth() / 2, offset + 0.27f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 - 0.05f * mRadius, offset + 0.35f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 + 0.05f * mRadius, offset + 0.35f * mRadius);
        mSecondHandPath.close();

        mCanvas.drawPath(mSecondHandPath, mSecondHandPaint);

        mCanvas.restore();
    }

    private void drawMinuteHand() {

        mCanvas.save();

        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);

        mMinuteHandPath.reset();

        float offset = mPaddingTop;

        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * mRadius, getHeight() / 2);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * mRadius, offset + 0.38f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.36f * mRadius,
                getWidth() / 2 + 0.008f * mRadius, offset + 0.38f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * mRadius, getHeight() / 2);
        mMinuteHandPath.close();

        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        mCanvas.restore();
    }

    private void drawHourHand() {

        mCanvas.save();

        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);

        mHourHandPath.reset();

        float offset = mPaddingTop;

        mHourHandPath.moveTo(getWidth() / 2 - 0.02f * mRadius, getHeight() / 2);
        mHourHandPath.lineTo(getWidth() / 2 - 0.01f * mRadius, offset + 0.5f * mRadius);
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.48f * mRadius,
                getWidth() / 2 + 0.01f * mRadius, offset + 0.5f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.02f * mRadius, getHeight() / 2);
        mHourHandPath.close();

        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        mCanvas.restore();
    }

    private void drawCoverCircle() {

        mCoverCirclePaint.setColor(mLightColor);
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, 0.05f * mRadius, mCoverCirclePaint);

        mCoverCirclePaint.setColor(mBackgroundColor);
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, 0.025f * mRadius, mCoverCirclePaint);
    }

    private void setCameraRotate() {

        mCameraMatrix.reset();

        mCamera.save();
        mCamera.rotateX(mCameraRotateX);
        mCamera.rotateY(mCameraRotateY);
        mCamera.getMatrix(mCameraMatrix);
        mCamera.restore();

        mCameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mCameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);

        mCanvas.concat(mCameraMatrix);
    }

    private void getCameraRotate(MotionEvent event) {

        float rotateX = -(event.getY() - getHeight() / 2);
        float rotateY = event.getX() - getWidth() / 2;

        mCameraRotateX = getPercent(rotateX) * MAX_CAMERA_ROTATE;
        mCameraRotateY = getPercent(rotateY) * MAX_CAMERA_ROTATE;
    }

    private void getCanvasTranslate(MotionEvent event) {

        float translateX = event.getX() - getWidth() / 2;
        float translateY = event.getY() - getHeight() / 2;

        mCanvasTranslateX = getPercent(translateX) * mMaxCanvasTranslate;
        mCanvasTranslateY = getPercent(translateY) * mMaxCanvasTranslate;
    }

    private float getPercent(float rotate) {

        return Math.max(Math.min(rotate / mRadius, 1), -1);
    }

}
