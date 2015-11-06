package com.ui.uimodule;

import android.animation.TypeEvaluator;

public class FloatArrayEvaluator implements TypeEvaluator<float[]> {

    private float[] mArray;

    public FloatArrayEvaluator() {
    }

    public FloatArrayEvaluator(float[] reuseArray) {
        mArray = reuseArray;
    }

    @Override
    public float[] evaluate(float fraction, float[] startValue, float[] endValue) {
        float[] array = mArray;
        if (array == null) {
            array = new float[startValue.length];
        }

        for (int i = 0; i < array.length; i++) {
            float start = startValue[i];
            float end = endValue[i];
            array[i] = start + (fraction * (end - start));
        }
        return array;
    }
}
