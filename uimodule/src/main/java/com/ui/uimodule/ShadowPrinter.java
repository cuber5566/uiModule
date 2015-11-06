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

    private static final float SHADOW_NORMAL = 1 * Resources.getSystem().getDisplayMetrics().density;

    int startColor, midColor, endColor;
    int width, height;

    private float radius, elevation, radiusCenter, shadowNormal, shadowPress, shadowOffset;
    private boolean init, animationStart;
    private int animationDuration = 300;
    private int[] colors = new int[3];
    private float[] positions = new float[3];
    private float[] start = new float[2], end = new float[2];

    private View view;
    private ValueAnimator shadowAnimator;
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

    private void init() {
        startColor = Color.argb((int) (255 * 0.35), 0, 0, 0);
        midColor = Color.argb((int) (255 * 0.35), 0, 0, 0);
        endColor = Color.TRANSPARENT;

        colors[0] = startColor;
        colors[1] = midColor;
        colors[2] = endColor;

        positions[0] = 0;
        positions[1] = 1 - elevation / radius;
        positions[2] = 1;

        start[0] = shadowNormal;
        start[1] = 0;
        end[0] = shadowPress;
        end[1] = elevation / 3;

        verticalLinearGradient = new LinearGradient(0, radiusCenter, 0, radiusCenter - shadowNormal, colors, positions, Shader.TileMode.CLAMP);
        horizontalLinearGradient = new LinearGradient(width - radiusCenter, 0, width - radiusCenter + shadowNormal, 0, colors, positions, Shader.TileMode.CLAMP);
        verticalRadialGradient = new RadialGradient(radiusCenter, radiusCenter, shadowNormal, colors, positions, Shader.TileMode.CLAMP);
        horizontalRadialGradient = new RadialGradient(width - radiusCenter, radiusCenter, shadowNormal, colors, positions, Shader.TileMode.CLAMP);
    }

    public void onDraw(Canvas canvas, float r, float e, boolean press) {

        width = canvas.getWidth();
        height = canvas.getHeight();
        elevation = e;
        radius = r + elevation;

        shadowNormal = radius + SHADOW_NORMAL - elevation;
        shadowPress = radius + elevation * 2 / 3 - elevation;
        radiusCenter = radius;

        if (!init) {
            init();
            init = true;
        }

        if (animationStart != press) {
            startShadowAnimation(press);
            animationStart = press;
        }

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

            rectF.set(width - radiusCenter * 2, 0, width, radiusCenter * 2);
            radiusPaint.setShader(horizontalRadialGradient);
            canvas.drawArc(rectF, 270, 90, true, radiusPaint);

            rectF.set(width - radius, radius, width, height - radius);
            linearPaint.setShader(horizontalLinearGradient);
            canvas.drawRect(rectF, linearPaint);

            canvas.rotate(180, width / 2, height / 2);
        }

        rectF.set(radius, radius, width - radius, height - radius);
        centerPaint.setColor(startColor);
        canvas.drawRect(rectF, centerPaint);

        canvas.restore();

    }

    private void startShadowAnimation(boolean press) {

        if (null != shadowAnimator) shadowAnimator.cancel();

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
                verticalLinearGradient = new LinearGradient(0, radiusCenter, 0, radiusCenter - value[0], colors, positions, Shader.TileMode.CLAMP);

                //R
                horizontalLinearGradient = new LinearGradient(width - radiusCenter, 0, width - radiusCenter + value[0], 0, colors, positions, Shader.TileMode.CLAMP);

                //LT
                verticalRadialGradient = new RadialGradient(radiusCenter, radiusCenter, value[0], colors, positions, Shader.TileMode.CLAMP);

                //RT
                horizontalRadialGradient = new RadialGradient(width - radiusCenter, radiusCenter, value[0], colors, positions, Shader.TileMode.CLAMP);

                view.invalidate();
            }
        });
        shadowAnimator.setInterpolator(new DecelerateInterpolator());
        shadowAnimator.setDuration(animationDuration);
        shadowAnimator.start();
    }
}
