package com.android.jc_banner;

import android.graphics.drawable.Drawable;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-05 11:28
 * @describe
 * @update
 */
public class IndicatorData {
    private Drawable selectedDrawable;
    private Drawable unselectedDrawable;
    private int itemCount;
    private int indicatorItemSpace;

    public int getIndicatorItemSpace() {
        return indicatorItemSpace;
    }

    public void setIndicatorItemSpace(int indicatorItemSpace) {
        this.indicatorItemSpace = indicatorItemSpace;
    }

    public IndicatorData(Drawable selectedDrawable, Drawable unselectedDrawable, int itemCount, int indicatorItemSpace) {
        this.selectedDrawable = selectedDrawable;
        this.unselectedDrawable = unselectedDrawable;
        this.itemCount = itemCount;
        this.indicatorItemSpace = indicatorItemSpace;
    }

    public Drawable getSelectedDrawable() {
        return selectedDrawable;
    }

    public void setSelectedDrawable(Drawable selectedDrawable) {
        this.selectedDrawable = selectedDrawable;
    }

    public Drawable getUnselectedDrawable() {
        return unselectedDrawable;
    }

    public void setUnselectedDrawable(Drawable unselectedDrawable) {
        this.unselectedDrawable = unselectedDrawable;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }


}
