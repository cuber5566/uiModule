package com.ui.uimodule;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ShadowMaker {

    int shadowColor = Color.BLACK;
    float shadowRadius = 2 * Resources.getSystem().getDisplayMetrics().density;
    float shadowOffsetX = 0 * Resources.getSystem().getDisplayMetrics().density;
    float shadowOffsetY = 1 * Resources.getSystem().getDisplayMetrics().density;
    float shadowAlpha = 0.3f;

    float padding = 4 * Resources.getSystem().getDisplayMetrics().density;
    float shadowBluer = 1 * Resources.getSystem().getDisplayMetrics().density;

    int animationDuration = 300;

    View view;

    int height, width;
    boolean animationStart;
    RectF rectF = new RectF();
    Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ValueAnimator shadowAnimator;
    Path clipPath = new Path();
    RectF clipRect = new RectF();

    public ShadowMaker(View view, float rectRadius) {

        this.view = view;
        this.shadowRadius = rectRadius + 1 * Resources.getSystem().getDisplayMetrics().density;

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, Color.argb((int) (255 * shadowAlpha), Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor)));
    }

    public ShadowMaker duration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public void onDraw(Canvas canvas, boolean focus) {

        if (animationStart != focus) {
            shadowAnimation(focus);
            animationStart = focus;
        }

        height = canvas.getHeight();
        width = canvas.getWidth();

        rectF.set(0 + padding + shadowRadius, 0 + padding, width - padding, height - padding - shadowOffsetY);
        clipRect.set(0, 0, width, height);
        clipPath.addRoundRect(clipRect, shadowRadius, shadowRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        canvas.drawRoundRect(rectF, shadowRadius, shadowRadius, shadowPaint);
    }

    private void shadowAnimation(boolean focus) {

        if (null != shadowAnimator) shadowAnimator.cancel();

        float[] start = {0, 0.2f};
        float[] end = {shadowBluer, 0.35f};

        if (focus) {
            shadowAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(), start, end);
        } else {
            shadowAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(), end, start);
        }

        shadowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float[] value = (float[]) valueAnimator.getAnimatedValue();
                shadowPaint.setShadowLayer(shadowRadius + value[0], shadowOffsetX, shadowOffsetY + value[0], Color.argb((int) (255 * value[1]), Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor)));
                view.invalidate();
            }
        });
        shadowAnimator.setInterpolator(new DecelerateInterpolator());
        shadowAnimator.setDuration(animationDuration);
        shadowAnimator.start();
    }
}
