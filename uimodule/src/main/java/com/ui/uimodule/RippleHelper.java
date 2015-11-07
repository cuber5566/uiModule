package com.ui.uimodule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;

public class RippleHelper {

    public static final int STATE_DOWN = 0;
    public static final int STATE_UP = 1;
    public static final int STATE_CANCEL = 2;

    private View view;
    private ValueAnimator rippleColorAnimator, rippleRadiusAnimator, rippleFocusAnimator;

    int animationDurationFocus = 2500;
    int animationDurationPress = 300;
    int animationDurationUp = 350;

    float radius = 48 * Resources.getSystem().getDisplayMetrics().density;
    float focusDelta = 4 * Resources.getSystem().getDisplayMetrics().density;

    float width, height;
    float x, y;
    int color;

    float cur_radius;

    Path clipPath = new Path();
    RectF clipRect = new RectF();
    Paint ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setStyle(Style.FILL);
        }
    };

    public RippleHelper(View view) {
        this.view = view;
    }

    public RippleHelper animationDurationFocus(int animationDurationFocus){
        this.animationDurationFocus = animationDurationFocus;
        return this;
    }

    public RippleHelper animationDurationPress(int animationDurationPress){
        this.animationDurationPress = animationDurationPress;
        return this;
    }

    public RippleHelper animationDurationUp(int animationDurationUp){
        this.animationDurationUp = animationDurationUp;
        return this;
    }

    public void onTouch(MotionEvent event){

        x = event.getX();
        y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeRippleColor(true);
                startRippleRadiusFocus(true);
                changeRippleRadius(STATE_DOWN);
                break;

            case MotionEvent.ACTION_MOVE:
                view.invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                changeRippleColor(false);
                startRippleRadiusFocus(false);
                if (0 < x && x < width && 0 < y && y < height) {
                    changeRippleRadius(STATE_UP);
                } else {
                    changeRippleRadius(STATE_CANCEL);
                    view.invalidate();
                }
                break;
        }
    }

    public void onDraw(Canvas canvas, float rippleRadius, int rippleColor, float padding, float rectRadius) {

        width = canvas.getWidth();
        height = canvas.getHeight();
        radius = rippleRadius;
        color = rippleColor;

        canvas.save();
        clipRect.set(padding, padding, width - padding, height - padding);
        clipPath.addRoundRect(clipRect, rectRadius, rectRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        canvas.clipRect(clipRect);
        canvas.drawCircle(x, y, cur_radius, ripplePaint);
        canvas.restore();
    }

    private void changeRippleColor(final boolean visible) {

        int normalBackgroundColor = Color.argb((int) (255 * 0.0), Color.red(color), Color.green(color), Color.blue(color));
        int pressBackgroundColor = Color.argb((int) (255 * 0.1), Color.red(color), Color.green(color), Color.blue(color));

        if (null != rippleColorAnimator) rippleColorAnimator.cancel();

        if (visible) {
            rippleColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), normalBackgroundColor, pressBackgroundColor);
            rippleColorAnimator.setDuration(animationDurationPress);
        } else {
            rippleColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), pressBackgroundColor, normalBackgroundColor);
            rippleColorAnimator.setDuration(animationDurationUp);
        }

        rippleColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                ripplePaint.setColor(color);
            }
        });
        rippleColorAnimator.setInterpolator(new DecelerateInterpolator());
        rippleColorAnimator.start();
    }

    private void changeRippleRadius(int state) {

        if (null != rippleRadiusAnimator) rippleRadiusAnimator.cancel();

        switch (state) {

            case STATE_DOWN:
                rippleRadiusAnimator = ValueAnimator.ofFloat(0, radius);
                rippleRadiusAnimator.setDuration(animationDurationPress);
                break;

            case STATE_UP:
                rippleRadiusAnimator = ValueAnimator.ofFloat(cur_radius, radius * 2);
                rippleRadiusAnimator.setDuration(animationDurationUp);
                break;

            default:
                rippleRadiusAnimator = ValueAnimator.ofFloat(cur_radius, 0);
                rippleRadiusAnimator.setDuration(animationDurationUp);
                break;
        }
        rippleRadiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cur_radius = (float) valueAnimator.getAnimatedValue();
                view.invalidate();
            }
        });
        rippleRadiusAnimator.setInterpolator(new DecelerateInterpolator());
        rippleRadiusAnimator.start();
    }

    private void startRippleRadiusFocus(final boolean start) {

        if (null != rippleFocusAnimator) rippleFocusAnimator.cancel();
        rippleFocusAnimator = ValueAnimator.ofFloat(0, focusDelta);
        rippleFocusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (start) {
                    cur_radius = radius + (Float) animation.getAnimatedValue();
                    view.invalidate();
                } else {
                    rippleFocusAnimator.cancel();
                }
            }
        });

        rippleFocusAnimator.setInterpolator(new CycleInterpolator(1));
        rippleFocusAnimator.setRepeatCount(Integer.MAX_VALUE);
        rippleFocusAnimator.setStartDelay(animationDurationPress);
        rippleFocusAnimator.setDuration(animationDurationFocus).start();
    }
}
