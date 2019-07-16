package com.android.jc_banner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-04 20:16
 * @describe 指示器adapter
 * @update
 */
public class IndicatorAdapter extends BaseIndicatorAdapter<IndicatorAdapter.IndicatorViewHolder> {

    private final Context mContext;
    private IndicatorData mIndicatorData;
    private int currentPosition = 0;

    IndicatorAdapter(@NonNull Context context) {
        this.mContext = context;
    }


    @NonNull
    @Override
    public IndicatorAdapter.IndicatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView bannerPoint = new ImageView(mContext);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int indicatorMargin = ConvertUtils.dp2px(8f);
        lp.setMargins(indicatorMargin / 2, indicatorMargin / 2, indicatorMargin / 2, indicatorMargin / 2);
        bannerPoint.setLayoutParams(lp);
        return new IndicatorAdapter.IndicatorViewHolder(bannerPoint);
    }

    @Override
    public void onBindViewHolder(@NonNull IndicatorAdapter.IndicatorViewHolder holder, int position) {
        ImageView bannerPoint = holder.indicatorImageView;
        bannerPoint.setImageDrawable(currentPosition == position ? mIndicatorData.getSelectedDrawable() : mIndicatorData.getUnselectedDrawable());

    }

    @Override
    public int getItemCount() {
        return mIndicatorData.getItemCount();
    }

    @Override
    protected BaseIndicatorAdapter setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        return this;
    }

    @Override
    protected void setIndicatorData(@NonNull IndicatorData indicatorData) {
        this.mIndicatorData = indicatorData;
    }


    static class IndicatorViewHolder extends RecyclerView.ViewHolder {
        private ImageView indicatorImageView;
        private IndicatorViewHolder(@NonNull ImageView indicatorImageView) {
            super(indicatorImageView);
            this.indicatorImageView = indicatorImageView;
        }
    }
}
