package com.ui.uimodule.button;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.ui.uimodule.R;
import com.ui.uimodule.ShadowMaker;
import com.ui.uimodule.ShadowPrinter;

public class RaisedButton extends Button {

    final int ANIMATION_DURATION_DISABLED = 250;
    final int ANIMATION_DURATION_FOCUS = 2500;
    final int ANIMATION_DURATION_PRESS = 300;
    final int ANIMATION_DURATION_UP = 350;

    final int STATE_DOWN = 0;
    final int STATE_UP = 1;
    final int STATE_CANCEL = 2;

    int shadowColor = Color.BLACK;
    float shadowRadius = 2 * Resources.getSystem().getDisplayMetrics().density;
    float shadowOffsetX = 0 * Resources.getSystem().getDisplayMetrics().density;
    float shadowOffsetY = 1 * Resources.getSystem().getDisplayMetrics().density;

    float rippleRadius = 48 * Resources.getSystem().getDisplayMetrics().density;
    float cur_radius;
    float max_radius;

    float shadowStoke = 3 * Resources.getSystem().getDisplayMetrics().density;
    float rectRadius = 16 * Resources.getSystem().getDisplayMetrics().density;
    float shadowAlpha = 0.3f;

    protected int backgroundColor, rippleColor, enableTextColor, enableBackgroundColor;
    private ValueAnimator enableTextColorAnimator, enableBackgroundColorAnimator, enableShadowAnimator, backgroundColorAnimator, rippleColorAnimator, rippleRadiusAnimator, rippleFocusAnimator;
    int textColor;

    Paint ripplePaint, backgroundPaint;

    float x, y;
    int height, width;

    RectF rectF;
    float padding = 8 * Resources.getSystem().getDisplayMetrics().density;

    boolean isClicked = false;

    ShadowMaker shadowMaker;
    ShadowPrinter shadowPrinter;
    boolean focus;

    Path clipPath = new Path();
    RectF clipRect = new RectF();

    public RaisedButton(Context context) {
        this(context, null);
    }

    public RaisedButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RaisedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        rectF = new RectF();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setAttributes(context, attrs);
        setPaint();
        shadowMaker = new ShadowMaker(this, rectRadius);
        shadowPrinter = new ShadowPrinter(this);
    }

    private void setAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RaisedButton, 0, 0);
        backgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_backgroundColor, Color.WHITE);
        rippleColor = typedArray.getColor(R.styleable.RaisedButton_rb_rippleColor, Color.WHITE);
        enableTextColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableTextColor, Color.WHITE);
        enableBackgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableBackgroundColor, Color.LTGRAY);
        rippleRadius = typedArray.getDimension(R.styleable.RaisedButton_rb_rippleRadius, rippleRadius);
        shadowStoke = typedArray.getDimension(R.styleable.RaisedButton_rb_rippleRadius, shadowStoke);
        rectRadius = typedArray.getDimension(R.styleable.RaisedButton_rb_rippleRadius, rectRadius);
        typedArray.recycle();
    }

    private void setPaint() {
        ripplePaint = new Paint() {
            {
                setAntiAlias(true);
            }
        };

        backgroundPaint = new Paint() {
            {
                setAntiAlias(true);
                setStyle(Style.FILL);
                setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, Color.argb((int) (255 * shadowAlpha), Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor)));
                setColor(backgroundColor);
            }
        };
    }

    public void setEnabledWithAnimation(boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        changeEnableBackgroundColor(enabled);
        changeEnableTextColor(enabled);
        changeEnableShadowColor(enabled);

        super.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        if (enabled) {
            setTextColor(textColor);
            backgroundPaint.setColor(backgroundColor);
        } else {
            setTextColor(enableTextColor);
            backgroundPaint.setColor(enableBackgroundColor);
        }
        invalidate();

        super.setEnabled(enabled);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || isClicked)
            return false;

        x = event.getX();
        y = event.getY();

        if (!isClicked)
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    focus = true;
//                    changeBackgroundColor(true);
                    changeRippleColor(true);
                    startRippleRadiusFocus(true);
                    changeRippleRadius(STATE_DOWN);

                    break;
                case MotionEvent.ACTION_MOVE:

                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    isClicked = true;
                    start_radius = cur_radius;
                    focus = false;
//                    changeBackgroundColor(false);
                    startRippleRadiusFocus(false);
                    changeRippleColor(false);

                    if (0 < x && x < width && 0 < y && y < height) {

                        changeRippleRadius(STATE_UP);
                        postDelayed(clickRunnable, ANIMATION_DURATION_UP - 200);

                    } else {

                        changeRippleRadius(STATE_CANCEL);
                        isClicked = false;
                        invalidate();
                    }
                    break;
            }
        return true;
    }

    Runnable clickRunnable = new Runnable() {
        @Override
        public void run() {
            onClick();
            isClicked = false;
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {

        height = canvas.getHeight();
        width = canvas.getWidth();

        shadowPrinter.onDraw(canvas, rectRadius, padding,focus);
//        shadowMaker.onDraw(canvas, focus);

        rectF.set(0 + padding, 0 + padding, width - padding, height - padding - shadowOffsetY);

//        canvas.drawRoundRect(rectF, rectRadius, rectRadius, backgroundPaint);

        canvas.save();
        clipRect.set(padding, padding, width - padding, height - padding);
        clipPath.addRoundRect(clipRect, rectRadius, rectRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        canvas.clipRect(rectF);
//        canvas.drawCircle(x, y, cur_radius, ripplePaint);
        canvas.restore();

        super.onDraw(canvas);
    }

    private void changeEnableTextColor(boolean enabled) {

        int disabledTextColor = enableTextColor;
        int normalTextColor = textColor;

        if (null != enableTextColorAnimator) enableTextColorAnimator.cancel();

        if (enabled) {
            enableTextColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), disabledTextColor, normalTextColor);
        } else {
            enableTextColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), normalTextColor, disabledTextColor);
        }

        enableTextColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setTextColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        enableTextColorAnimator.setInterpolator(new DecelerateInterpolator());
        enableTextColorAnimator.setDuration(ANIMATION_DURATION_DISABLED);
        enableTextColorAnimator.start();
    }

    private void changeEnableBackgroundColor(boolean enabled) {

        int disabledTextColor = Color.argb((int) (255 * 0.3), Color.red(enableBackgroundColor), Color.green(enableBackgroundColor), Color.blue(enableBackgroundColor));
        int normalTextColor = backgroundColor;

        if (null != enableBackgroundColorAnimator)
            enableBackgroundColorAnimator.cancel();

        if (enabled) {
            enableBackgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), disabledTextColor, normalTextColor);
        } else {
            enableBackgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), normalTextColor, disabledTextColor);
        }

        if (null != enableBackgroundColorAnimator)
            enableBackgroundColorAnimator.cancel();

        enableBackgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                backgroundPaint.setColor((Integer) valueAnimator.getAnimatedValue());
                invalidate();
            }
        });
        enableBackgroundColorAnimator.setInterpolator(new DecelerateInterpolator());
        enableBackgroundColorAnimator.setDuration(ANIMATION_DURATION_DISABLED);
        enableBackgroundColorAnimator.start();
    }

    private void changeEnableShadowColor(boolean enabled) {

        if (null != enableShadowAnimator) enableShadowAnimator.cancel();

        if (enabled) {
            enableShadowAnimator = ValueAnimator.ofFloat(0.0f, 0.75f);
        } else {
            enableShadowAnimator = ValueAnimator.ofFloat(0.75f, 0.0f);
        }

        enableShadowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                backgroundPaint.setShadowLayer(shadowRadius * v * 100 / 75, shadowOffsetX, shadowOffsetY, Color.argb((int) (255 * shadowAlpha), Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor)));
                invalidate();
            }
        });
        enableShadowAnimator.setInterpolator(new DecelerateInterpolator());
        enableShadowAnimator.setDuration(ANIMATION_DURATION_DISABLED);
        enableShadowAnimator.start();
    }

    private void changeBackgroundColor(boolean focus) {

        if (null != backgroundColorAnimator) backgroundColorAnimator.cancel();

        if (focus) {
            backgroundColorAnimator = ValueAnimator.ofFloat(1.0f, 0.90f);
        } else {
            backgroundColorAnimator = ValueAnimator.ofFloat(0.90f, 1.0f);
        }

        backgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                float[] hsv = new float[3];
                Color.colorToHSV(backgroundColor, hsv);
                hsv[2] *= (v * 0.96 / 0.9) > 1 ? 1 : (v * 0.96 / 0.9);
//                backgroundPaint.setColor(Color.HSVToColor(hsv));
                backgroundPaint.setShadowLayer(shadowRadius + (1.0f - v) * 10 * padding / 2, shadowOffsetX, shadowOffsetY + (1.0f - v) * 10 * padding / 4, Color.argb((int) (255 * Math.abs(v * 2 - 2.3)), Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor)));
                invalidate();
            }
        });
        backgroundColorAnimator.setInterpolator(new DecelerateInterpolator());
        backgroundColorAnimator.setDuration(ANIMATION_DURATION_PRESS);
        backgroundColorAnimator.start();
    }

    float start_radius;

    private void changeRippleColor(final boolean visible) {


        int normalBackgroundColor = Color.argb((int) (255 * 0.0), Color.red(rippleColor), Color.green(rippleColor), Color.blue(rippleColor));
        int pressBackgroundColor = Color.argb((int) (255 * 0.1), Color.red(rippleColor), Color.green(rippleColor), Color.blue(rippleColor));

        if (null != rippleColorAnimator) rippleColorAnimator.cancel();

        if (visible) {
            rippleColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), normalBackgroundColor, pressBackgroundColor);
            rippleColorAnimator.setDuration(ANIMATION_DURATION_PRESS);
        } else {
            rippleColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), pressBackgroundColor, normalBackgroundColor);
            rippleColorAnimator.setDuration(ANIMATION_DURATION_UP);
        }

        rippleColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                ripplePaint.setColor(color);
                invalidate();
            }
        });
        rippleColorAnimator.setInterpolator(new DecelerateInterpolator());
        rippleColorAnimator.start();
    }


    private void changeRippleRadius(int state) {
        final float max_x = x > width / 2 ? x : width - x;
        final float max_y = y > width / 2 ? y : width - y;
        max_radius = (float) Math.sqrt(max_x * max_x + max_y * max_y);

        if (null != rippleRadiusAnimator) rippleRadiusAnimator.cancel();

        switch (state) {

            case STATE_DOWN:
                rippleRadiusAnimator = ValueAnimator.ofFloat(0, rippleRadius);
                rippleRadiusAnimator.setDuration(ANIMATION_DURATION_PRESS);
                break;

            case STATE_UP:
                rippleRadiusAnimator = ValueAnimator.ofFloat(cur_radius, rippleRadius * 2);
                rippleRadiusAnimator.setDuration(ANIMATION_DURATION_UP);
                break;

            default:
                rippleRadiusAnimator = ValueAnimator.ofFloat(cur_radius, 0);
                rippleRadiusAnimator.setDuration(ANIMATION_DURATION_UP);
                break;
        }
        rippleRadiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();

                cur_radius = value;
                invalidate();
            }
        });
        rippleRadiusAnimator.setInterpolator(new DecelerateInterpolator());
        rippleRadiusAnimator.start();
    }

    private void startRippleRadiusFocus(final boolean start) {

        if (null != rippleFocusAnimator) rippleFocusAnimator.cancel();
        rippleFocusAnimator = ValueAnimator.ofFloat(0, getResources().getDisplayMetrics().density * 4);
        rippleFocusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (start) {
                    cur_radius = rippleRadius + (Float) animation.getAnimatedValue();
                    invalidate();
                } else {
                    rippleFocusAnimator.cancel();
                }
            }
        });

        rippleFocusAnimator.setInterpolator(new CycleInterpolator(1));
        rippleFocusAnimator.setRepeatCount(Integer.MAX_VALUE);
        rippleFocusAnimator.setStartDelay(ANIMATION_DURATION_PRESS);
        rippleFocusAnimator.setDuration(ANIMATION_DURATION_FOCUS);
        rippleFocusAnimator.start();
    }

    public void onClick() {
        performClick();
    }
}
