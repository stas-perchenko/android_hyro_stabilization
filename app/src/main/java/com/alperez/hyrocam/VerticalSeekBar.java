package com.alperez.hyrocam;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by stanislav.perchenko on 13-Nov-15.
 */
public class VerticalSeekBar extends View {



    // Sizes of content items
    private int mThumbSize = 25;
    private float mThumbSizeHalf = 12.5f;
    private int mNotSelectedBarWidth = 4;
    private int mSelectedBarWidth = 10;

    // Colors of elements
    private int colorNotSelectedBar;
    private int colorSelectedBar;
    private int colorSelectedBarTouched;
    private int colorThumb;
    private int colorThumbTouched;
    private int colorThumbTouchedWrapper;



    // Cached values of paddings
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;

    // Size and start position of content area regarding to View's size and paddings.
    private int contentAreaStartX;
    private int contentAreaStartY;
    private int contentAreaWidth;
    private int contentAreaHeight;
    private float contentAreaCenterHorizontal;


    //----  Status fields  ----
    private boolean targetIsBeingTouched;

    private float mPixelYPosition;


    //----  Real progress values  ----
    private float mMin = 0;
    private float mMax = 100f;
    private float mProgress = 50f;



    private boolean wasLayout;

    public VerticalSeekBar(Context context) {
        super(context);
        init(context);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        extractArguments(attrs, defStyle);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        extractArguments(attrs, defStyleAttr);
        init(context);
    }

    private void extractArguments(@Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar, defStyleAttr, 0);
            try {
                mThumbSize = a.getDimensionPixelSize(R.styleable.VerticalSeekBar_thumbSize, mThumbSize);
                mThumbSizeHalf = (float)mThumbSize / 2f;
                mNotSelectedBarWidth = a.getDimensionPixelSize(R.styleable.VerticalSeekBar_notSelectedBarWidth, mNotSelectedBarWidth);
                mSelectedBarWidth = a.getDimensionPixelSize(R.styleable.VerticalSeekBar_selectedBarWidth, mSelectedBarWidth);

                colorNotSelectedBar = a.getColor(R.styleable.VerticalSeekBar_notSelectedBarColor, colorNotSelectedBar);
                colorSelectedBar = a.getColor(R.styleable.VerticalSeekBar_selectedBarColor, colorSelectedBar);
                colorSelectedBarTouched = a.getColor(R.styleable.VerticalSeekBar_selectedBarColorTouched, colorSelectedBarTouched);
                colorThumb = a.getColor(R.styleable.VerticalSeekBar_thumbColor, colorThumb);
                colorThumbTouched = a.getColor(R.styleable.VerticalSeekBar_thumbTouchedColor, colorThumbTouched);
                colorThumbTouchedWrapper = a.getColor(R.styleable.VerticalSeekBar_thumbTouchedWrapperColor, colorThumbTouchedWrapper);
            } finally {
                a.recycle();
            }
        }
    }



    private void init(Context context) {

        validateElementsSize();
        updatePaddingInternal();
    }

    /*********************************  Layout-related  *******************************************/

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updatePaddingInternal();
        if (wasLayout) {
            updateContentAreaSize(getWidth(), getHeight());
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        wasLayout = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!wasLayout || changed) {
            targetIsBeingTouched = false;
            wasLayout = true;
            final int width = right - left;
            final int height = bottom - top;

            updateContentAreaSize(width, height);
            updateYPixelPositionByProgress();
        }
    }

    private void updatePaddingInternal() {
            paddingLeft = getPaddingLeft();
            paddingTop = getPaddingTop();
            paddingRight = getPaddingRight();
            paddingBottom = getPaddingBottom();
    }

    private void updateContentAreaSize(int viewWidth, int viewHeight) {
        contentAreaStartX = paddingLeft;
        contentAreaStartY = paddingTop;
        contentAreaWidth = viewWidth - paddingLeft - paddingRight;
        if (contentAreaWidth < 0) contentAreaWidth = 0;
        contentAreaHeight = viewHeight - paddingTop - paddingBottom;
        if (contentAreaHeight < 0) contentAreaHeight = 0;
        contentAreaCenterHorizontal = (float)(2*contentAreaStartX + contentAreaWidth) / 2f;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (wasLayout) {
            final int action = MotionEventCompat.getActionMasked(event);
            final float x = event.getX();
            final float y = event.getY();
            switch(action) {
                case MotionEvent.ACTION_DOWN:
                    if (!targetIsBeingTouched && isTouchValid(x, y)) {
                        startTouchMode(y);
                        super.invalidate();
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (targetIsBeingTouched) {
                        updateThumbPositionInTouchMode(y);
                        super.invalidate();
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (targetIsBeingTouched) {
                        targetIsBeingTouched = false;
                        super.invalidate();
                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchValid(float touchX, float touchY) {
        if ((touchX >= contentAreaCenterHorizontal - mThumbSizeHalf) && (touchX < contentAreaCenterHorizontal + mThumbSizeHalf)) {
            return true;
        }

        final float halfContentWidth = (float)contentAreaWidth / 2f;
        float minY = mPixelYPosition - halfContentWidth;
        if (minY < contentAreaStartY) minY = contentAreaStartY;
        float maxY = mPixelYPosition + halfContentWidth;
        if (maxY >= contentAreaStartY+contentAreaHeight) maxY = contentAreaStartY+contentAreaHeight;
        if ((touchY >= minY) && (touchY < maxY)) {
            if ((touchX >= contentAreaStartX) && (touchX < contentAreaStartX*contentAreaWidth)) {
                return true;
            }
        }

        return false;
    }

    private void startTouchMode(final float pointerY) {
        if (targetIsBeingTouched) throw new IllegalStateException("This method can be called only when the View is not in touch mode");

        targetIsBeingTouched = true;
        updateThumbPositionInTouchMode(pointerY);
    }

    private void updateThumbPositionInTouchMode(final float pointerY) {
        if (!targetIsBeingTouched)
            throw new IllegalStateException("This method can be called only when the View is in touch mode");

        if (pointerY < contentAreaStartY) {
            mPixelYPosition = contentAreaStartY;
        }  else if (pointerY > contentAreaStartY+contentAreaHeight) {
            mPixelYPosition = contentAreaStartY+contentAreaHeight;
        } else {
            mPixelYPosition = pointerY;
        }

        final float pxProgress = contentAreaStartY+contentAreaHeight - mPixelYPosition;
        mProgress = (mMax - mMin) * pxProgress / contentAreaHeight;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        if (contentAreaWidth == 0 || contentAreaHeight == 0) return;
        canvas.save();
        canvas.clipRect(contentAreaStartX, contentAreaStartY, contentAreaStartX+contentAreaWidth, contentAreaStartY+contentAreaHeight);
        //TODO Draw here
        canvas.restore();
    }





    /**********************************************************************************************/
    /**********************************  Setters for parameters  **********************************/
    /**********************************************************************************************/
    private void setElementsSize(int thumbSize, int notSelectedBarWidth, int selectedBarWidth) {
        mThumbSize = thumbSize;
        mThumbSizeHalf = (float) thumbSize / 2f;
        mNotSelectedBarWidth = notSelectedBarWidth;
        mSelectedBarWidth = selectedBarWidth;
        validateElementsSize();
    }

    private void setBarColors(int colorNotSelected, int colorSelected, int colorSelectedTouched) {
        this.colorNotSelectedBar = colorNotSelected;
        this.colorSelectedBar = colorSelected;
        this.colorSelectedBarTouched = colorSelectedTouched;
    }

    private void setThumbColors(int colorThumb, int colorThumbTouched, int colorThumbTouchedWrapper) {
        this.colorThumb = colorThumb;
        this.colorThumbTouched = colorThumbTouched;
        this.colorThumbTouchedWrapper = colorThumbTouchedWrapper;
    }

    private void validateElementsSize() {
        if (mSelectedBarWidth > mThumbSize)
            throw new IllegalArgumentException("Selected bar width must not exceed thumb size");
        if (mNotSelectedBarWidth > mSelectedBarWidth)
            throw new IllegalArgumentException("Not selected bar width must not exceed selected bar width");
    }


    public void setProgress(float progress) {
        if (progress > mMax) {
            mProgress = mMax;
        } else if (progress < mMin) {
            mProgress = mMin;
        } else {
            mProgress = progress;
        }
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setRange(float min, float max) {
        if (min >= max) throw new IllegalArgumentException("Min value must be less then Max value");
        mMin = min;
        mMax = max;
        if (mProgress < mMin) {
            mProgress = mMin;
        } else if (mProgress > mMax) {
            mProgress = mMax;
        }
        invalidate();
    }

    public float getMin() {
        return mMin;
    }

    public float getMax() {
        return mMax;
    }


    @Override
    public void invalidate() {
        updateYPixelPositionByProgress();
        super.invalidate();
    }

    private void updateYPixelPositionByProgress() {
        if (wasLayout) {
            mPixelYPosition = contentAreaStartY + contentAreaHeight * (mMax - mProgress) / (mMax - mMin);
        }
    }
}
