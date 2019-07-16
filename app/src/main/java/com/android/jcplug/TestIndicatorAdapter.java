package com.android.jcplug;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.android.jc_banner.BaseIndicatorAdapter;
import com.android.jc_banner.IndicatorData;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-05 15:17
 * @describe
 * @update
 */
public class TestIndicatorAdapter extends BaseIndicatorAdapter {
    @Override
    protected BaseIndicatorAdapter setCurrentPosition(int currentPosition) {
        return null;
    }

    @Override
    protected void setIndicatorData(@NonNull IndicatorData indicatorData) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
