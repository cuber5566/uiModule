package com.ui.uimodule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;


public class RaisedButton extends Button {

    static final int ANIMATION_DURATION_DISABLED = 250;
    static final int ANIMATION_DURATION_FOCUS = 2500;
    static final int ANIMATION_DURATION_PRESS = 100;
    static final int ANIMATION_DURATION_UP = 500;

    static final int STATE_DOWN = 0;
    static final int STATE_UP = 1;
    static final int STATE_CANCEL = 2;

    protected int backgroundColor, rippleColor, enableTextColor, enableBackgroundColor;

    private ValueAnimator textColorAnimation, backgroundColorAnimation, rippleRadiusAnimation, radiusAnimation;

    int shadowColor = 0x4C000000;
    int textColor;

    float radius = 48;
    float cur_radius;

    private RectF rectF;
    private Paint ripplePaint, backgroundPaint, shadowPaint;

    float x, y;
    int height, width;

    float cur_shadowWidth;
    float shadowStoke = 2;
    float rectRadius = 3;

    boolean isClicked = false;
    int paddingTop, paddingBottom;

    public RaisedButton(Context context) {
        this(context, null);
    }

    public RaisedButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RaisedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        radius = getResources().getDisplayMetrics().density * radius;
        shadowStoke = getResources().getDisplayMetrics().density * shadowStoke;
        rectRadius = getResources().getDisplayMetrics().density * rectRadius;

        setAttributes(context, attrs, defStyleAttr);
        setPaint();

        cur_shadowWidth = shadowStoke;
        rectF = new RectF();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        setPadding(getPaddingLeft() + (int) rectRadius, paddingTop + (int) rectRadius, getPaddingRight() + (int) rectRadius, paddingBottom + (int) rectRadius);
        textColor = getTextColors().getDefaultColor();
    }

    private void setAttributes(Context context, AttributeSet attrs, int defStyleAttr) {

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareLayout, defStyleAttr, 0);
        backgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_backgroundColor, Color.WHITE);
        rippleColor = typedArray.getColor(R.styleable.RaisedButton_rb_rippleColor, Color.WHITE);
        enableTextColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableTextColor, Color.WHITE);
        enableBackgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableBackgroundColor, Color.LTGRAY);
        typedArray.recycle();
    }

    protected void onActionClick() {
    }

    public float getRaisedRadius() {
        return rectRadius;
    }

    public void setRaisedRadius(float rectRadius) {
        this.rectRadius = rectRadius;
        invalidate();
    }

    public void setRaisedButtonBackground(int color) {
        backgroundColor = color;
        backgroundPaint.setColor(backgroundColor);
        invalidate();
    }

    public void setRaisedButtonRipple(int color) {
        rippleColor = color;
    }

    private void setPaint() {
        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
            {
                setStyle(Style.FILL);
                setColor(backgroundColor);
            }
        };

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
            {
                setStyle(Style.FILL);
                setColor(shadowColor);
            }
        };
    }

    public void setEnabledWithAnimation(boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        changeEnableBackgroundColor(enabled);
        changeEnableTextColor(enabled);
        changeShadowWidth(!enabled);

        super.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        if(enabled){
            setTextColor(textColor);
            backgroundPaint.setColor(backgroundColor);
        }else{
            setTextColor(enableTextColor);
            backgroundPaint.setColor(enableBackgroundColor);
        }
        invalidate();

        super.setEnabled(enabled);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled() || isClicked)
            return false;

        x = event.getX();
        y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                changeBackgroundColor(true);
                changeShadowWidth(true);
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

                changeBackgroundColor(false);
                startRippleRadiusFocus(false);
                changeRippleColor(false);
                changeShadowWidth(false);

                if (0 < x && x < width && 0 < y && y < height) {

                    changeRippleRadius(STATE_UP);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            performClick();
                            onActionClick();
                            isClicked = false;
                        }
                    }, ANIMATION_DURATION_UP);

                } else {

                    changeRippleRadius(STATE_CANCEL);
                    isClicked = false;
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        height = canvas.getHeight();
        width = canvas.getWidth();

        //shadow
        rectF.set(0, shadowStoke, width, height);
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, shadowPaint);

        //background
        rectF.set(0, Math.abs(cur_shadowWidth - shadowStoke), width, height - cur_shadowWidth);
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, backgroundPaint);

        //ripple
        canvas.save();
        canvas.clipRect(rectF);
        canvas.drawCircle(x, y, cur_radius, ripplePaint);
        canvas.restore();

        super.onDraw(canvas);
    }

    private void changeEnableTextColor(boolean disable) {

        int enabledTextColor = enableTextColor;
        int normalTextColor = textColor;

        if (null != textColorAnimation) textColorAnimation.removeAllUpdateListeners();

        if (disable) {
            textColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), enabledTextColor, normalTextColor);
        } else {
            textColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), normalTextColor, enabledTextColor);
        }

        textColorAnimation.setDuration(ANIMATION_DURATION_DISABLED).start();
        textColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTextColor((int) animation.getAnimatedValue());
            }
        });
    }

    private void changeEnableBackgroundColor(boolean enabled) {

        int normalBackgroundColor = backgroundColor;

        if (null != backgroundColorAnimation) backgroundColorAnimation.removeAllUpdateListeners();

        if (enabled) {
            backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), enableBackgroundColor, normalBackgroundColor);
        } else {
            backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), normalBackgroundColor, enableBackgroundColor);
        }

        backgroundColorAnimation.setDuration(ANIMATION_DURATION_DISABLED).start();
        backgroundColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                backgroundPaint.setColor((int) animation.getAnimatedValue());
                invalidate();
            }
        });
    }

    private void changeBackgroundColor(boolean focus) {

        ValueAnimator colorAnimation;

        if (focus) {
            colorAnimation = ValueAnimator.ofFloat(1.0f, 0.75f);
        } else {
            colorAnimation = ValueAnimator.ofFloat(0.75f, 1.0f);
        }

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                float[] hsv = new float[3];
                Color.colorToHSV(backgroundColor, hsv);
                hsv[2] *= v;
                backgroundPaint.setColor(Color.HSVToColor(hsv));
                invalidate();
            }
        });
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.setDuration(ANIMATION_DURATION_DISABLED);
        colorAnimation.start();
    }

    private void changeShadowWidth(boolean press) {

        ValueAnimator widthValueAnimator;

        if (press) {
            widthValueAnimator = ValueAnimator.ofFloat(shadowStoke, 0);
        } else {
            widthValueAnimator = ValueAnimator.ofFloat(0, shadowStoke);
        }
        widthValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cur_shadowWidth = (float) animation.getAnimatedValue();
                setPadding(getPaddingLeft(), paddingTop + (int) Math.abs(cur_shadowWidth - shadowStoke), getPaddingRight(), paddingBottom - (int) Math.abs(cur_shadowWidth - shadowStoke));
                invalidate();
            }
        });

        widthValueAnimator.setDuration(ANIMATION_DURATION_PRESS);
        widthValueAnimator.start();
    }

    float start_radius;

    private void changeRippleColor(final boolean visible) {

        final ValueAnimator colorAnimation;

        int normalBackgroundColor = Color.argb((int) (255 * 0.0), Color.red(rippleColor), Color.green(rippleColor), Color.blue(rippleColor));
        int pressBackgroundColor = Color.argb((int) (255 * 0.3), Color.red(rippleColor), Color.green(rippleColor), Color.blue(rippleColor));

        if (visible) {
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), normalBackgroundColor, pressBackgroundColor);
            colorAnimation.setDuration(ANIMATION_DURATION_PRESS);
        } else {
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), pressBackgroundColor, normalBackgroundColor);
            colorAnimation.setDuration(ANIMATION_DURATION_UP);
        }

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ripplePaint.setColor((int) animation.getAnimatedValue());
                invalidate();
            }
        });
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.start();
    }


    private void changeRippleRadius(int state) {

        if (null != rippleRadiusAnimation) rippleRadiusAnimation.removeAllUpdateListeners();

        switch (state) {

            case STATE_DOWN:
                rippleRadiusAnimation = ValueAnimator.ofFloat(0, radius);
                rippleRadiusAnimation.setDuration(ANIMATION_DURATION_PRESS);
                break;

            case STATE_UP:
                rippleRadiusAnimation = ValueAnimator.ofFloat(cur_radius, radius * 2);
                rippleRadiusAnimation.setDuration(ANIMATION_DURATION_UP);
                break;

            default:
                rippleRadiusAnimation = ValueAnimator.ofFloat(cur_radius, 0);
                rippleRadiusAnimation.setDuration(ANIMATION_DURATION_UP);
                break;
        }
        rippleRadiusAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cur_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        rippleRadiusAnimation.setInterpolator(new DecelerateInterpolator());
        rippleRadiusAnimation.start();
    }

    private void startRippleRadiusFocus(final boolean start) {

        if (radiusAnimation != null) radiusAnimation.removeAllUpdateListeners();
        radiusAnimation = ValueAnimator.ofFloat(0, getResources().getDisplayMetrics().density * 4);
        radiusAnimation.setInterpolator(new CycleInterpolator(1));
        radiusAnimation.setRepeatCount(Integer.MAX_VALUE);
        radiusAnimation.setStartDelay(ANIMATION_DURATION_PRESS);
        radiusAnimation.setDuration(ANIMATION_DURATION_FOCUS).start();
        radiusAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (start) {
                    cur_radius = radius + (Float) animation.getAnimatedValue();
                    invalidate();
                } else {
                    radiusAnimation.cancel();
                }
            }
        });
    }
}
