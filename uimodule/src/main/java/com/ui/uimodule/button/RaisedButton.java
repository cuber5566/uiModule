package com.ui.uimodule.button;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.ui.uimodule.R;
import com.ui.uimodule.RippleHelper;
import com.ui.uimodule.ShadowHelper;

public class RaisedButton extends Button {

    final int ANIMATION_DURATION_DISABLED = 250;
    final int ANIMATION_DURATION_PRESS = 300;
    final int ANIMATION_DURATION_UP = 350;

    float rippleRadius = 48 * Resources.getSystem().getDisplayMetrics().density;

    float padding = 4 * Resources.getSystem().getDisplayMetrics().density;
    float elevation = 4 * Resources.getSystem().getDisplayMetrics().density;
    float rectRadius = 2 * Resources.getSystem().getDisplayMetrics().density;

    protected int backgroundColor, rippleColor, enableTextColor, enableBackgroundColor;
    private ValueAnimator enableTextColorAnimator, enableBackgroundColorAnimator, backgroundColorAnimator;

    Paint ripplePaint, backgroundPaint;
    RectF rectF = new RectF();
    ShadowHelper shadowHelper;
    RippleHelper rippleHelper;

    float x, y;
    int textColor;
    int height, width;
    boolean isClicked = false;
    boolean focus;

    public RaisedButton(Context context) {
        this(context, null);
    }

    public RaisedButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RaisedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setAttributes(context, attrs);
        setPaint();
        shadowHelper = new ShadowHelper(this);
        rippleHelper = new RippleHelper(this);
    }

    private void setAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RaisedButton, 0, 0);
        backgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_backgroundColor, Color.WHITE);
        rippleColor = typedArray.getColor(R.styleable.RaisedButton_rb_rippleColor, Color.WHITE);
        enableTextColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableTextColor, Color.WHITE);
        enableBackgroundColor = typedArray.getColor(R.styleable.RaisedButton_rb_enableBackgroundColor, Color.LTGRAY);
        rippleRadius = typedArray.getDimension(R.styleable.RaisedButton_rb_rippleRadius, rippleRadius);
        elevation = typedArray.getDimension(R.styleable.RaisedButton_rb_elevation, elevation);
        padding = elevation + 0.5f * Resources.getSystem().getDisplayMetrics().density;
        rectRadius = typedArray.getDimension(R.styleable.RaisedButton_rb_rectRadius, rectRadius);
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
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled() || isClicked)
            return false;

        x = event.getX();
        y = event.getY();
        rippleHelper.onTouch(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                focus = true;
                changeBackgroundColor(true);
                break;

            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isClicked = true;
                focus = false;
                changeBackgroundColor(false);
                if (0 < x && x < width && 0 < y && y < height) {
                    postDelayed(clickRunnable, ANIMATION_DURATION_UP - 200);
                } else {
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
    protected void onDraw(@NonNull Canvas canvas) {

        height = canvas.getHeight();
        width = canvas.getWidth();

        shadowHelper.onDraw(canvas, rectRadius, elevation, focus);

        rectF.set(padding, padding, width - padding, height - padding - 0.5f * Resources.getSystem().getDisplayMetrics().density);
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, backgroundPaint);

        rippleHelper.onDraw(canvas, rippleRadius, rippleColor, padding, rectRadius);

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

    private void changeBackgroundColor(boolean focus) {

        if (null != backgroundColorAnimator) backgroundColorAnimator.cancel();

        if (focus) {
            backgroundColorAnimator = ValueAnimator.ofFloat(1.0f, 0.95f);
        } else {
            backgroundColorAnimator = ValueAnimator.ofFloat(0.95f, 1.0f);
        }

        backgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                float[] hsv = new float[3];
                Color.colorToHSV(backgroundColor, hsv);
                hsv[2] *= v;
                backgroundPaint.setColor(Color.HSVToColor(hsv));
                invalidate();
            }
        });
        backgroundColorAnimator.setInterpolator(new DecelerateInterpolator());
        backgroundColorAnimator.setDuration(ANIMATION_DURATION_PRESS);
        backgroundColorAnimator.start();
    }

    public void onClick() {
        performClick();
    }
}
