package com.alperez.hyrocam;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by stanislav.perchenko on 10-Nov-15.
 */
public class TargetView extends View {

    private static final double MIN_DISTANCE_TO_TARGET = 0.26;


    private int mPadding = 24;
    private int mGridLineWidth = 2;
    private int mCircleMarkLen = 12;

    private int mSelfSizeVert = 15;
    private int mSelfSizeHor = 10;
    private int mTargSize = 10;
    private int mTargToucheableRadius = 15;


    private int mGridColor;
    private int mSelfColor = 0;
    private int mTargColor = 0;
    private int mTargTouchedColor;
    private int mTargTouchedWrapperColor;
    private int mAllowAreaColor = 0;
    private int mDenyArreaColor = 0;

    //--- Self-to-target line parameters ---
    private int mSelfToTargLineWidth;
    private int mSelfToTargLineColor;
    private int mSelfToTargLineWidthTouched;
    private int mSelfToTargLineColorTouched;


    private int mTextSize = 24;
    private int mTextColor = Color.parseColor("#333333");



    private Paint mGridPaint;
    private Paint mFillPaint;
    private Paint mTargConnectionPaint;
    private Paint mTextPaint;

    private Path mSelfMarkPath;



    private double targStableXnorm = 0;
    private double targStableYnorm = 0.5;

    // These values are tracking target position while in drag mode.
    // These values honor acceptable area bounds. After dragging has been finished, these values are set
    // as stable values
    private double targInDragXnorm = 0;
    private double targInDragYnorm = 0.5;
    private double targInDragDistance = 0.5;


    private boolean targetIsBeingTouched = false;
    private float touchPointerStartX;
    private float touchPointerStartY;


    private boolean wasLayout;


    //--- Real-life parameters ---
    private float mRealLifeMaxTargetRadius = 100; // Maximum horizontal projection of a distance to the target (corresponds to the normalized value of 1.0). The value is set externally via the setter;
    private float mRealLifeTargetX;         // Current X value of the target's position in real measuring points. The value is updated inside this View via touch listener.
    private float mRealLifeTargetY;         // Current Y value of the target's position in real measuring points. The value is updated inside this View via touch listener.
    private float mRealLifeTargetAltitude;  // Altitude of the target. The value is set externally via the setter;
    private float mRealLifeSelfAltitude;    // Altitude of this device. The value is set externally via the setter;



    private OnPositionChecgeListener mPositionChecgeListener;


    public TargetView(Context context) {
        super(context);
        init(context);
    }

    public TargetView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TargetView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TargetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TargetView, defStyleAttr, 0);
        try {
            mPadding = a.getDimensionPixelSize(R.styleable.TargetView_extDenyAreaPadding, mPadding);
            mGridLineWidth = a.getDimensionPixelSize(R.styleable.TargetView_gridLineWidth, mGridLineWidth);
            mCircleMarkLen = a.getDimensionPixelSize(R.styleable.TargetView_angleMarkingsSize, mCircleMarkLen);
            mSelfSizeHor = a.getDimensionPixelSize(R.styleable.TargetView_selfMarkSizeHor, mSelfSizeHor);
            mSelfSizeVert = a.getDimensionPixelSize(R.styleable.TargetView_selfMarkSizeVert, mSelfSizeVert);
            mTargSize = a.getDimensionPixelSize(R.styleable.TargetView_targetMarkSize, mTargSize);
            mTargToucheableRadius = a.getDimensionPixelSize(R.styleable.TargetView_targetToucheableRadius, mTargToucheableRadius);

            mAllowAreaColor = a.getColor(R.styleable.TargetView_allowAreaColor, mAllowAreaColor);
            mDenyArreaColor = a.getColor(R.styleable.TargetView_denyAreaColor, mDenyArreaColor);
            mGridColor = a.getColor(R.styleable.TargetView_gridColor, mGridColor);
            mSelfColor = a.getColor(R.styleable.TargetView_selfMarkColor, mSelfColor);
            mTargColor = a.getColor(R.styleable.TargetView_targetMarkColor, mTargColor);
            mTargTouchedColor = a.getColor(R.styleable.TargetView_targetMarkTouchedColor, mTargTouchedColor);
            mTargTouchedWrapperColor = a.getColor(R.styleable.TargetView_targetTouchWrapperColor, mTargTouchedWrapperColor);

            mSelfToTargLineWidth = a.getDimensionPixelSize(R.styleable.TargetView_selfToTargLineWidth, mSelfToTargLineWidth);
            mSelfToTargLineColor = a.getColor(R.styleable.TargetView_selfToTargLineColor, mSelfToTargLineColor);
            mSelfToTargLineWidthTouched = a.getDimensionPixelSize(R.styleable.TargetView_selfToTargLineTouchedWidth, mSelfToTargLineWidthTouched);
            mSelfToTargLineColorTouched = a.getColor(R.styleable.TargetView_selfToTargLineTouchedColor, mSelfToTargLineColorTouched);

            mTextSize = a.getDimensionPixelSize(R.styleable.TargetView_textSize, mTextSize);
            mTextColor = a.getColor(R.styleable.TargetView_textColor, mTextColor);
        } finally {
            a.recycle();
        }
        init(context);
    }

    private void init(Context c) {
        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(mGridLineWidth);
        mGridPaint.setColor(mGridColor);

        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);

        mTargConnectionPaint = new Paint();
        mTargConnectionPaint.setStyle(Paint.Style.STROKE);
        mTargConnectionPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    /**********************************************************************************************/
    /**********************  Setters section  *****************************************************/
    public void setOnPositionChecgeListener(OnPositionChecgeListener l) {
        mPositionChecgeListener = l;
    }



    public void setRealLifeMaxTargetRadius(float radius) {
        this.mRealLifeMaxTargetRadius = radius;
        invalidate();
        requestReportPositionInDraw();
    }

    public void setRealLifeSelfAltitude(float selfAlt) {
        this.mRealLifeSelfAltitude = selfAlt;
        invalidate();
        requestReportPositionInDraw();
    }

    public void setRealLifeTargetAltitude(float targetAlt) {
        this.mRealLifeTargetAltitude = targetAlt;
        invalidate();
        requestReportPositionInDraw();
    }

    public void setSelfMarkSize(int widthPx, int heightPx) {
        this.mSelfSizeHor = widthPx;
        this.mSelfSizeVert = heightPx;
        this.mSelfMarkPath = null;
        invalidate();
    }
    /**********************************************************************************************/



    @Override
    public void requestLayout() {
        super.requestLayout();
        wasLayout = false;
    }

    /**
     * X-coordinate of the center point of the View
     */
    private float x0px;

    /**
     * Y-coordinate of the center point of the View
     */
    private float y0px;

    /**
     * Maximum allowed target distance of the target from the center in screen pixels (outer denied area)
     */
    private float maxDrawingRadiusPx;

    /**
     * Minimum allowed target distance of the target from the center in screen pixels (inner denied area)
     */
    private float minDrawingRadiusPx;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!wasLayout || changed) {
            wasLayout = true;
            int width = right - left;
            int height = bottom - top;

            x0px = (float) width / 2f;
            y0px = (float) height / 2f;

            if (width <= height) {
                maxDrawingRadiusPx = width - x0px - mPadding;
            } else {
                maxDrawingRadiusPx = height - y0px - mPadding;
            }
            minDrawingRadiusPx = (float)(maxDrawingRadiusPx * MIN_DISTANCE_TO_TARGET);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (wasLayout) {
            final int action = MotionEventCompat.getActionMasked(event);
            final float x = event.getX();
            final float y = event.getY();
            switch(action) {
                case MotionEvent.ACTION_DOWN:
                    if (!targetIsBeingTouched && checkTouchOnTarget(x, y)) {
                        startTouchMode(x, y);
                        invalidate();
                        //ViewCompat.postInvalidateOnAnimation(this);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (targetIsBeingTouched) {
                        updateTargetPositionInTouchMode(x, y);
                        invalidate();
                        //ViewCompat.postInvalidateOnAnimation(this);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (targetIsBeingTouched) {
                        finishTouchMode(x, y);
                        invalidate();
                        //ViewCompat.postInvalidateOnAnimation(this);
                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean checkTouchOnTarget(final float pointerX, final float pointerY) {
        final float dXpx = (x0px + (float) targStableXnorm * maxDrawingRadiusPx) - pointerX;
        final float dYpx = (y0px - (float) targStableYnorm * maxDrawingRadiusPx) - pointerY;

        final double dist = Math.sqrt(dXpx*dXpx + dYpx*dYpx);
        return (dist <= mTargToucheableRadius);
    }

    private void startTouchMode(final float pointerX, final float pointerY) {
        if (targetIsBeingTouched) throw new IllegalStateException("This method can be called only when the View is not in touch mode");

        targetIsBeingTouched = true;
        touchPointerStartX = pointerX;
        touchPointerStartY = pointerY;
        updateTargetPositionInTouchMode(pointerX, pointerY);
    }


    private void finishTouchMode(final float pointerX, final float pointerY) {
        updateTargetPositionInTouchMode(pointerX, pointerY);

        targetIsBeingTouched = false;
        targStableXnorm = targInDragXnorm;
        targStableYnorm = targInDragYnorm;
    }

    private void updateTargetPositionInTouchMode(final float pointerX, final float pointerY) {
        if (!targetIsBeingTouched) throw new IllegalStateException("This method can be called only when the View is in touch mode");

        final double dXnorm = (pointerX - touchPointerStartX) / maxDrawingRadiusPx;
        final double dYnorm = -1 * (pointerY - touchPointerStartY) / maxDrawingRadiusPx;

        final double newRawXnorm = targStableXnorm + dXnorm;
        final double newRawYnorm = targStableYnorm + dYnorm;
        final double newRawXnormSq = newRawXnorm*newRawXnorm;
        final double newRawYnormSq = newRawYnorm*newRawYnorm;

        final double rawTargDist = Math.sqrt(newRawXnormSq + newRawYnormSq);
        if (rawTargDist > 1.0) {
            // Find cross of the target direction line with the outer circle
            targInDragYnorm = Math.sqrt(1.0 / (1.0 + newRawXnormSq/newRawYnormSq));
            targInDragXnorm = targInDragYnorm * newRawXnorm / newRawYnorm;
        } else if (rawTargDist < MIN_DISTANCE_TO_TARGET) {
            // Find cross of the target direction line with the inner circle
            targInDragYnorm = Math.sqrt(MIN_DISTANCE_TO_TARGET / (1.0 + newRawXnormSq/newRawYnormSq));
            targInDragXnorm = targInDragYnorm * newRawXnorm / newRawYnorm;
        } else {
            targInDragXnorm = newRawXnorm;
            targInDragYnorm = newRawYnorm;
            targInDragDistance = rawTargDist;
        }

        updateRealLifePositionValue();
    }

    private void updateRealLifePositionValue() {
        if (targetIsBeingTouched) {
            mRealLifeTargetX = (float)(mRealLifeMaxTargetRadius * targInDragXnorm);
            mRealLifeTargetY = (float)(mRealLifeMaxTargetRadius * targInDragYnorm);
        } else {
            mRealLifeTargetX = (float)(mRealLifeMaxTargetRadius * targStableXnorm);
            mRealLifeTargetY = (float)(mRealLifeMaxTargetRadius * targStableYnorm);
        }
        requestReportPositionInDraw();
    }


    private boolean isReportNeedInDraw = false;
    private void requestReportPositionInDraw() {
        isReportNeedInDraw = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //TODO Implement this


        if (!wasLayout) return;


        //----  Report scene  ----
        if (isReportNeedInDraw) {
            isReportNeedInDraw = false;
            if (mPositionChecgeListener != null) {
                mPositionChecgeListener.onPositionChanged(!targetIsBeingTouched, mRealLifeTargetX, mRealLifeTargetY, mRealLifeTargetAltitude, mRealLifeSelfAltitude);
            }
        }

        final float cWidth = canvas.getWidth();
        final float cHeight = canvas.getHeight();

        //----  Calculate target on-screen coordinates  ----
        final float targetXpx = x0px + (float)(maxDrawingRadiusPx * (targetIsBeingTouched ? targInDragXnorm : targStableXnorm));
        final float targetYpx = y0px - (float)(maxDrawingRadiusPx * (targetIsBeingTouched ? targInDragYnorm : targStableYnorm));

        //----  Fill canvas with deny-area color to create outer denied area  ----
        mFillPaint.setColor(mDenyArreaColor);
        canvas.drawRect(0, 0, cWidth, cHeight, mFillPaint);

        //----  Draw circle with allow-area color to create allowed area  ----
        mFillPaint.setColor(mAllowAreaColor);
        canvas.drawCircle(x0px, y0px,maxDrawingRadiusPx, mFillPaint);

        //----  Draw inner deny area  ----
        mFillPaint.setColor(mDenyArreaColor);
        canvas.drawCircle(x0px, y0px,minDrawingRadiusPx, mFillPaint);

        //----  Draw axises and border circles  ----
        final float vertPadding = y0px - maxDrawingRadiusPx;
        final float horPadding = x0px - maxDrawingRadiusPx;
        canvas.drawLine(x0px, vertPadding, x0px, cHeight-vertPadding, mGridPaint);
        canvas.drawLine(horPadding, y0px, cWidth-horPadding, y0px, mGridPaint);
        canvas.drawCircle(x0px, y0px, maxDrawingRadiusPx, mGridPaint);
        canvas.drawCircle(x0px, y0px, minDrawingRadiusPx, mGridPaint);

        //----  Draw angle markings on the outer circle  ----
        drawAngleMark(canvas, 30.0*Math.PI / 180.0);
        drawAngleMark(canvas, 60.0*Math.PI / 180.0);
        drawAngleMark(canvas, 120.0*Math.PI / 180.0);
        drawAngleMark(canvas, 150.0*Math.PI / 180.0);
        drawAngleMark(canvas, 210.0*Math.PI / 180.0);
        drawAngleMark(canvas, 240.0*Math.PI / 180.0);
        drawAngleMark(canvas, 300.0*Math.PI / 180.0);
        drawAngleMark(canvas, 330.0*Math.PI / 180.0);


        //----  Draw self-to-target line  ----
        boolean needSelfToTargLine = false;
        if (targetIsBeingTouched && mSelfToTargLineWidthTouched > 0) {
            mTargConnectionPaint.setStrokeWidth(mSelfToTargLineWidthTouched);
            mTargConnectionPaint.setColor(mSelfToTargLineColorTouched);
            needSelfToTargLine = true;
        } else if (!targetIsBeingTouched && mSelfToTargLineWidth > 0) {
            mTargConnectionPaint.setStrokeWidth(mSelfToTargLineWidth);
            mTargConnectionPaint.setColor(mSelfToTargLineColor);
            needSelfToTargLine = true;
        }
        if (needSelfToTargLine) {
            canvas.drawLine(x0px, y0px, targetXpx, targetYpx, mTargConnectionPaint);
        }

        //----  Draw target touch wrapper if needed  ----
        if (targetIsBeingTouched) {
            mFillPaint.setColor(mTargTouchedWrapperColor);
            canvas.drawCircle(targetXpx, targetYpx, mTargToucheableRadius, mFillPaint);
        }


        //----  Draw target  ----
        mFillPaint.setColor(targetIsBeingTouched ? mTargTouchedColor : mTargColor);
        canvas.drawCircle(targetXpx, targetYpx, mTargSize/2, mFillPaint);

        //----  Draw self mark  ----
        mFillPaint.setColor(mSelfColor);
        float h_2 = mSelfSizeHor / 2f;
        if (mSelfMarkPath == null) {
            mSelfMarkPath = new Path();
            float v_2 = mSelfSizeVert / 2f;
            mSelfMarkPath.moveTo(x0px, y0px - v_2);
            mSelfMarkPath.lineTo(x0px + h_2, y0px);
            mSelfMarkPath.lineTo(x0px, y0px + v_2);
            mSelfMarkPath.lineTo(x0px - h_2, y0px);
            mSelfMarkPath.close();
        }
        canvas.drawPath(mSelfMarkPath, mFillPaint);

        //----  Draw system text  ----
        canvas.drawText(String.format(SELF_H_TEMPLATE, mRealLifeSelfAltitude), x0px + h_2, y0px + mTextSize, mTextPaint);
        int targLineYpx = mPadding + mTextSize;
        canvas.drawText(TARGET_TITLE_TEXT, mPadding, targLineYpx, mTextPaint);
        targLineYpx += mTextSize;
        canvas.drawText(String.format(TARGET_X_TEMPLATE, mRealLifeTargetX), mPadding, targLineYpx, mTextPaint);
        targLineYpx += mTextSize;
        canvas.drawText(String.format(TARGET_Y_TEMPLATE, mRealLifeTargetY), mPadding, targLineYpx, mTextPaint);
        targLineYpx += mTextSize;
        canvas.drawText(String.format(TARGET_H_TEMPLATE, mRealLifeTargetAltitude), mPadding, targLineYpx, mTextPaint);

    }

    private static final String SELF_H_TEMPLATE = "H=%.1f";
    private static final String TARGET_TITLE_TEXT = "Target:";
    private static final String TARGET_X_TEMPLATE = "x=%.1f";
    private static final String TARGET_Y_TEMPLATE = "y=%.1f";
    private static final String TARGET_H_TEMPLATE = "h=%.1f";

    /**
     *
     * @param c
     * @param angle angle in radians as on compass
     */
    private void drawAngleMark(Canvas c, double angle) {
        final double tgVal = Math.tan(Math.PI/2.0 - angle);
        final double tgSquare = tgVal * tgVal;
        final double x1 = (angle > Math.PI ? -1 : 1) * maxDrawingRadiusPx / Math.sqrt(1 + tgSquare);
        final double y1 = x1 * tgVal;
        final double x2 = (angle > Math.PI ? -1 : 1) * (maxDrawingRadiusPx - mCircleMarkLen) / Math.sqrt(1 + tgSquare);
        final double y2 = x2 * tgVal;
        c.drawLine(x0px+(float)x1, y0px-(float)y1, x0px+(float)x2, y0px-(float)y2, mGridPaint);
    }







    public interface OnPositionChecgeListener {
        void onPositionChanged(boolean isTouchFinished, float targetX, float targetY, float targetZ, float selfZ);
    }
}
