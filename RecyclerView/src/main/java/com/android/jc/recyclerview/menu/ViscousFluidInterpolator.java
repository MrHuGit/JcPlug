package com.android.jc.recyclerview.menu;

import android.view.animation.Interpolator;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-25 16:36
 * @describe 动画插值器
 * @update
 */
public class ViscousFluidInterpolator implements Interpolator {
    private final float mViscousFluidScale;
    private final float mViscousFluidNormalize;
    private final float mViscousFluidOffset;

     ViscousFluidInterpolator(float viscousFluidScale) {
        mViscousFluidScale = viscousFluidScale;
        mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
        mViscousFluidOffset = 1.0f - mViscousFluidNormalize * viscousFluid(1.0f);
    }

    private float viscousFluid(float x) {
        x *= mViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float) Math.exp(-x));
        } else {
            // 1/e == exp(-1)
            float start = 0.36787944117f;
            x = 1.0f - (float) Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        return x;
    }

    /**
     * @param input 时间索引
     * @return
     */
    @Override
    public float getInterpolation(float input) {
        final float interpolated = mViscousFluidNormalize * viscousFluid(input);
        if (interpolated > 0) {
            return interpolated + mViscousFluidOffset;
        }
        return interpolated;
    }
}