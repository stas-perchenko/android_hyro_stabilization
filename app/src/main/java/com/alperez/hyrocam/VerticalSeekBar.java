package com.alperez.hyrocam;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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


    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updatePaddingInternal();
        updateContentAreaSize(getWidth(), getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateContentAreaSize(w, h);
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

    }



    private boolean isTouchValid(float touchX, float touchY) {
        //TODO Validating touch
        return false;
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
        //TODO Check the validity of sizes of all elements
        if (mSelectedBarWidth > mThumbSize)
            throw new IllegalArgumentException("Selected bar width must not exceed thumb size");
        if (mNotSelectedBarWidth > mSelectedBarWidth)
            throw new IllegalArgumentException("Not selected bar width must not exceed selected bar width");
    }

}
