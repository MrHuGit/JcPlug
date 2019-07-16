package com.android.jc_banner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-05 15:12
 * @describe
 * @update
 */
public abstract class BaseIndicatorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * 设置当前展示下标
     *
     * @param currentPosition
     *         currentPosition
     *
     * @return 当前对象
     */
    protected abstract BaseIndicatorAdapter setCurrentPosition(int currentPosition);

    /**
     * 设置
     * @param indicatorData
     */
    protected abstract void setIndicatorData(@NonNull IndicatorData indicatorData);
}
