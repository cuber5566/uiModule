package com.ui.uimodule;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLinearLayout extends LinearLayout {

    private float widthWeight, heightWeight;

    public SquareLinearLayout(Context context) {
        this(context, null);
    }

    public SquareLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributes(context, attrs, defStyleAttr);
    }

    private void setAttributes(Context context, AttributeSet attrs, int defStyleAttr) {

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareLayout, defStyleAttr, 0);
        widthWeight = typedArray.getFloat(R.styleable.SquareLayout_widthWeight, 1);
        heightWeight = typedArray.getFloat(R.styleable.SquareLayout_heightWeight, 1);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int measureSpecWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int measureSpecHeight = MeasureSpec.makeMeasureSpec((int) (width * heightWeight / widthWeight), MeasureSpec.EXACTLY);
        super.onMeasure(measureSpecWidth, measureSpecHeight);
    }
}
