package com.alperez.hyrocam;

import android.graphics.Canvas;
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



    private void validateElementsSize() {
        //TODO Check the validity of sizes of all elements
    }

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




    private void init() {

        validateElementsSize();
        //TODO
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

}
