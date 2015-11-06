package com.ui.uimodule;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ShadowPrinter {

    private LinearGradient verticalLinearGradient, horizontalLinearGradient;
    private RadialGradient verticalRadialGradient, horizontalRadialGradient;

    private static final float SHADOW_NORMAL = 2 * Resources.getSystem().getDisplayMetrics().density;
    private static final float SHADOW_PRESS = 4 * Resources.getSystem().getDisplayMetrics().density;
    private static final float SHADOW_OFFSET = 2 * Resources.getSystem().getDisplayMetrics().density;
    private static final float PADDING = 8 * Resources.getSystem().getDisplayMetrics().density;

    private static final int START_COLOR = Color.argb((int) (255 * 1), 0, 0, 0);
    private static final int END_COLOR = Color.argb(0, 0, 0, 0);

//    private static final float shadowNormalLR = 1 * Resources.getSystem().getDisplayMetrics().density;
//    private static final float shadowPressLR = 3 * Resources.getSystem().getDisplayMetrics().density;
//
//    private static final float shadowNormalBottom = 2 * Resources.getSystem().getDisplayMetrics().density;
//    private static final float shadowPressBottom = 4 * Resources.getSystem().getDisplayMetrics().density;

    private int width, height;
    private float radius, padding, radiusCenter, shadowNormal, shadowPress, shadowOffset;
    private boolean animationStart;

    int animationDuration = 300;
//    private float radius;

    private View view;
    private ValueAnimator shadowAnimator;
    //    private Rect rect = new Rect();
    private RectF rectF = new RectF();
    private Paint centerPaint = new Paint() {
        {
            setStyle(Style.FILL);
        }
    }, linearPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setStyle(Style.FILL);
        }
    }, radiusPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setStyle(Style.FILL);
        }
    };

    public ShadowPrinter(View view) {
        this.view = view;
    }

    public ShadowPrinter duration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public void onDraw(Canvas canvas, float r, float p, boolean press) {

        this.radius = r;
        this.padding = p;
        shadowNormal = radius + SHADOW_NORMAL;
        shadowPress = radius + SHADOW_PRESS;
        radiusCenter = radius + padding;

        if (animationStart != press) {
            startShadowAnimation(press, radius);
            animationStart = press;
        }

        width = canvas.getWidth();
        height = canvas.getHeight();


        canvas.save();
        canvas.translate(0, shadowOffset);

        for (int i = 0; i < 2; i++) {

            rectF.set(0, 0, radiusCenter * 2, radiusCenter * 2);
            radiusPaint.setShader(verticalRadialGradient);
            canvas.drawArc(rectF, 180, 90, true, radiusPaint);

            rectF.set(radiusCenter, 0, width - radiusCenter, radiusCenter);
            linearPaint.setShader(verticalLinearGradient);
            canvas.drawRect(rectF, linearPaint);

            canvas.rotate(180, width / 2, height / 2);
        }

        for (int i = 0; i < 2; i++) {

            rectF.set(width - radiusCenter*2, 0, width, radiusCenter*2);
            radiusPaint.setShader(horizontalRadialGradient);
            canvas.drawArc(rectF, 270, 90, true, radiusPaint);

            rectF.set(width - radius, radius, width, height - radius);
            linearPaint.setShader(horizontalLinearGradient);
            canvas.drawRect(rectF, linearPaint);

            canvas.rotate(180, width / 2, height / 2);
        }

        rectF.set(radius, radius, width - radius, height - radius);
        centerPaint.setColor(START_COLOR);
        canvas.drawRect(rectF, centerPaint);

        canvas.restore();
    }

    private void startShadowAnimation(boolean press, float r) {

        if (null != shadowAnimator) shadowAnimator.cancel();

        float[] start = {shadowNormal, 0};
        float[] end = {shadowPress, SHADOW_OFFSET};

        if (press) {
            shadowAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(), start, end);
        } else {
            shadowAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(), end, start);
        }

        shadowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float[] value = (float[]) valueAnimator.getAnimatedValue();

                //offset
                shadowOffset = value[1];

                //T
                verticalLinearGradient = new LinearGradient(0, radiusCenter, 0, radiusCenter - value[0], START_COLOR, END_COLOR, Shader.TileMode.CLAMP);

                //R
                horizontalLinearGradient = new LinearGradient(width - radiusCenter, 0, width - radiusCenter + value[0], 0, START_COLOR, END_COLOR, Shader.TileMode.CLAMP);

                //LT
                verticalRadialGradient = new RadialGradient(radiusCenter, radiusCenter, value[0], START_COLOR, END_COLOR, Shader.TileMode.CLAMP);

                //RT
                horizontalRadialGradient = new RadialGradient(width - radiusCenter, radiusCenter, value[0], START_COLOR, END_COLOR, Shader.TileMode.CLAMP);

                view.invalidate();
            }
        });
        shadowAnimator.setInterpolator(new DecelerateInterpolator());
        shadowAnimator.setDuration(animationDuration);
        shadowAnimator.start();
    }
}
