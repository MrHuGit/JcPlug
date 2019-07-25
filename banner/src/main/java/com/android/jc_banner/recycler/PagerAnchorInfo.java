package com.android.jc_banner.recycler;

import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-18 16:50
 * @describe
 * @update
 */
public class PagerAnchorInfo {
    OrientationHelper mOrientationHelper;
    int mPosition;
    int mCoordinate;
    boolean mLayoutFromEnd;
    boolean mValid;

    PagerAnchorInfo() {
        this.reset();
    }

    void reset() {
        this.mPosition = -1;
        this.mCoordinate = -2147483648;
        this.mLayoutFromEnd = false;
        this.mValid = false;
    }

    void assignCoordinateFromPadding() {
        this.mCoordinate = this.mLayoutFromEnd ? this.mOrientationHelper.getEndAfterPadding() : this.mOrientationHelper.getStartAfterPadding();
    }

    @Override
    @NonNull
    public String toString() {
        return "AnchorInfo{mPosition=" + this.mPosition + ", mCoordinate=" + this.mCoordinate + ", mLayoutFromEnd=" + this.mLayoutFromEnd + ", mValid=" + this.mValid + '}';
    }

    boolean isViewValidAsAnchor(View child, RecyclerView.State state) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        return !lp.isItemRemoved() && lp.getViewLayoutPosition() >= 0 && lp.getViewLayoutPosition() < state.getItemCount();
    }

    public void assignFromViewAndKeepVisibleRect(View child, int position) {
        int spaceChange = this.mOrientationHelper.getTotalSpaceChange();
        if (spaceChange >= 0) {
            this.assignFromView(child, position);
        } else {
            this.mPosition = position;
            int childStart;
            int startMargin;
            int previousEndMargin;
            int childSize;
            int estimatedChildStart;
            int layoutStart;
            int endMargin;
            if (this.mLayoutFromEnd) {
                childStart = this.mOrientationHelper.getEndAfterPadding() - spaceChange;
                startMargin = this.mOrientationHelper.getDecoratedEnd(child);
                previousEndMargin = childStart - startMargin;
                this.mCoordinate = this.mOrientationHelper.getEndAfterPadding() - previousEndMargin;
                if (previousEndMargin > 0) {
                    childSize = this.mOrientationHelper.getDecoratedMeasurement(child);
                    estimatedChildStart = this.mCoordinate - childSize;
                    layoutStart = this.mOrientationHelper.getStartAfterPadding();
                    endMargin = this.mOrientationHelper.getDecoratedStart(child) - layoutStart;
                    int startReference = layoutStart + Math.min(endMargin, 0);
                    startMargin = estimatedChildStart - startReference;
                    if (startMargin < 0) {
                        this.mCoordinate += Math.min(previousEndMargin, -startMargin);
                    }
                }
            } else {
                childStart = this.mOrientationHelper.getDecoratedStart(child);
                startMargin = childStart - this.mOrientationHelper.getStartAfterPadding();
                this.mCoordinate = childStart;
                if (startMargin > 0) {
                    previousEndMargin = childStart + this.mOrientationHelper.getDecoratedMeasurement(child);
                    childSize = this.mOrientationHelper.getEndAfterPadding() - spaceChange;
                    estimatedChildStart = childSize - this.mOrientationHelper.getDecoratedEnd(child);
                    layoutStart = this.mOrientationHelper.getEndAfterPadding() - Math.min(0, estimatedChildStart);
                    endMargin = layoutStart - previousEndMargin;
                    if (endMargin < 0) {
                        this.mCoordinate -= Math.min(startMargin, -endMargin);
                    }
                }
            }

        }
    }

    public void assignFromView(View child, int position) {
        if (this.mLayoutFromEnd) {
            this.mCoordinate = this.mOrientationHelper.getDecoratedEnd(child) + this.mOrientationHelper.getTotalSpaceChange();
        } else {
            this.mCoordinate = this.mOrientationHelper.getDecoratedStart(child);
        }

        this.mPosition = position;
    }
}
