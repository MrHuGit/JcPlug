package com.android.jc_banner.recycler;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-18 14:53
 * @describe
 * @update
 */
public class PagerLayoutManager extends RecyclerView.LayoutManager {

    private RecyclerView mRecyclerView;
    /**
     * 每页的item数量
     */
    private int mItemCountOnePager;
    /**
     * 页面数量
     */
    private int mPagerCount;
    private OrientationHelper mOrientationHelper;
    private float mOffset;
    private PagerLayoutManagerState mLayoutState;

    @IntDef({OrientationHelper.VERTICAL, OrientationHelper.HORIZONTAL})
    @interface OrientationType {

    }

    /**
     * 滚动状态
     */
    private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;
    /**
     * 默认水平滚动
     */
    @OrientationType
    private int mOrientation = OrientationHelper.HORIZONTAL;

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mRecyclerView = view;
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {
        calculatePagerCount();
        if (state.getItemCount() == 0) {
            //移除回收当前的View
            removeAndRecycleAllViews(recycler);
            mOffset = 0;
            setPageIndex(0, false);
            return;
        }


        setPageIndex(getPageIndexByOffset(), false);


        // 计算可以滚动的最大数值，并对滚动距离进行修正
        if (mOrientation == OrientationHelper.VERTICAL) {
            mMaxScrollX = (mPagerCount - 1) * getUsableWidth();
            mMaxScrollY = 0;
            if (mOffsetX > mMaxScrollX) {
                mOffsetX = mMaxScrollX;
            }
        } else {
            mMaxScrollX = 0;
            mMaxScrollY = (mPagerCount - 1) * getUsableHeight();
            if (mOffsetY > mMaxScrollY) {
                mOffsetY = mMaxScrollY;
            }
        }

        // 接口回调
        // setPagerCount(mPageCount);
        // setPageIndex(mCurrentPageIndex, false);

        if (mItemWidth <= 0) {
            mItemWidth = getUsableWidth() / mColumns;
        }
        if (mItemHeight <= 0) {
            mItemHeight = getUsableHeight() / mRows;
        }

        mWidthUsed = getUsableWidth() - mItemWidth;
        mHeightUsed = getUsableHeight() - mItemHeight;

        // 预存储两页的View显示区域
        for (int i = 0; i < mOnePageSize * 2; i++) {
            getItemFrameByPosition(i);
        }

        if (mOffsetX == 0 && mOffsetY == 0) {
            // 预存储View
            for (int i = 0; i < mItemCountOnePager; i++) {
                if (i >= getItemCount()) {
                    break; // 防止数据过少时导致数组越界异常
                }
                View view = recycler.getViewForPosition(i);
                addView(view);
                measureChildWithMargins(view, mWidthUsed, mHeightUsed);
            }
        }

        // 回收和填充布局
        recycleAndFillItems(recycler, state, true);
    }

    /**
     * 布局结束
     *
     * @param state State
     */
    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        if (state.isPreLayout()) {
            return;
        }
        // 页面状态回调
        setPageCount(getTotalPageCount());
        setPageIndex(getPageIndexByOffset(), false);
    }

    /**
     * 创建默认布局参数
     *
     * @return 默认布局参数
     */
    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == OrientationHelper.HORIZONTAL;
    }

    /**
     * 横向滑动
     *
     * @param dx       滚动距离
     * @param recycler 回收器
     * @param state    滚动状态
     * @return 实际滚动距离
     */
    @Override
    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == OrientationHelper.VERTICAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    /**
     * 滑动到指定的item position位置
     *
     * @param position item对应位置
     */
    @Override
    public void scrollToPosition(int position) {
        int pagerIndex = getPagerIndexByItemPosition(position);
        scrollToPage(pagerIndex);
    }


    /**
     * 根据item所在position获取对应页码
     *
     * @param position position
     * @return 页面的页码
     */
    private int getPagerIndexByItemPosition(int position) {
        return position / mItemCountOnePager;
    }


    /**
     * 根据 offset 获取页面Index
     *
     * @return 页面 Index
     */
    private int getPagerIndexByOffset() {
        int pagerIndex;
        //纵向
        if (mOrientation == OrientationHelper.VERTICAL) {
            int pageHeight = getUsableHeight();
            if (mOffsetY <= 0 || pageHeight <= 0) {
                pagerIndex = 0;
            } else {
                pagerIndex = mOffsetY / pageHeight;
                if (mOffsetY % pageHeight > pageHeight / 2) {
                    pagerIndex++;
                }
            }
        } else {
            int pageWidth = getUsableWidth();
            if (mOffsetX <= 0 || pageWidth <= 0) {
                pagerIndex = 0;
            } else {
                pagerIndex = mOffsetX / pageWidth;
                if (mOffsetX % pageWidth > pageWidth / 2) {
                    pagerIndex++;
                }
            }
        }
        return pagerIndex;
    }

    /**
     * 计算页面数量
     */
    private void calculatePagerCount() {
        // 计算页面数量
        int pagerCount = getItemCount() / mItemCountOnePager;
        if (getItemCount() % mItemCountOnePager != 0) {
            pagerCount++;
        }
        //监听页面数量发生改变
        if (mPagerCount != pagerCount) {
//            mPageListener.onPageSizeChanged(mPagerCount);
        }
        mPagerCount = pagerCount;

    }

    /**
     * 获取可用的宽度
     *
     * @return 宽度 - padding
     */
    private int getUsableWidth() {
        return mRecyclerView.getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * 获取可用的高度
     *
     * @return 高度 - padding
     */
    private int getUsableHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * 回收和填充布局
     *
     * @param recycler Recycler
     * @param state    State
     * @param isStart  是否从头开始，用于控制View遍历方向，true 为从头到尾，false 为从尾到头
     */
    @SuppressLint("CheckResult")
    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state, boolean isStart) {
        // 计算显示区域区前后多存储一列或则一行
        Rect displayRect = new Rect(mOffsetX - mItemWidth, mOffsetY - mItemHeight,
                getUsableWidth() + mOffsetX + mItemWidth, getUsableHeight() + mOffsetY + mItemHeight);
        // 对显显示区域进行修正(计算当前显示区域和最大显示区域对交集)
        displayRect.intersect(0, 0, mMaxScrollX + getUsableWidth(), mMaxScrollY + getUsableHeight());
        // 获取第一个条目的Pos
        int startPos = 0;
        int pageIndex = getPageIndexByOffset();
        startPos = pageIndex * mItemCountOnePager;
        startPos = startPos - mItemCountOnePager * 2;
        if (startPos < 0) {
            startPos = 0;
        }
        int stopPos = startPos + mItemCountOnePager * 4;
        if (stopPos > getItemCount()) {
            stopPos = getItemCount();
        }
        // 移除所有View
        detachAndScrapAttachedViews(recycler);

        if (isStart) {
            for (int i = startPos; i < stopPos; i++) {
                addOrRemove(recycler, displayRect, i);
            }
        } else {
            for (int i = stopPos - 1; i >= startPos; i--) {
                addOrRemove(recycler, displayRect, i);
            }
        }
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    public void setOrientation(@OrientationType int orientation) {
        if (orientation != 0 && orientation != 1) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        } else {
            this.assertNotInLayoutOrScroll((String) null);
            if (orientation != this.mOrientation || this.mOrientationHelper == null) {
                this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);
//                this.mAnchorInfo.mOrientationHelper = this.mOrientationHelper;
                this.mOrientation = orientation;
                this.requestLayout();
            }

        }
    }

    /**
     * 添加或者移除条目
     *
     * @param recycler    RecyclerView
     * @param displayRect 显示区域
     * @param i           条目下标
     */
    private void addOrRemove(RecyclerView.Recycler recycler, Rect displayRect, int i) {
        View child = recycler.getViewForPosition(i);
        Rect rect = getItemFrameByPosition(i);
        if (!Rect.intersects(displayRect, rect)) {
            // 回收入暂存区
            removeAndRecycleView(child, recycler);
        } else {
            addView(child);
            measureChildWithMargins(child, mWidthUsed, mHeightUsed);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            layoutDecorated(child,
                    rect.left - mOffsetX + lp.leftMargin + getPaddingLeft(),
                    rect.top - mOffsetY + lp.topMargin + getPaddingTop(),
                    rect.right - mOffsetX - lp.rightMargin + getPaddingLeft(),
                    rect.bottom - mOffsetY - lp.bottomMargin + getPaddingTop());
        }
    }

    /**
     * 检测状态
     */
    private void ensureLayoutState() {
        if (this.mLayoutState == null) {
            this.mLayoutState = new PagerLayoutManagerState();
        }

    }

    private void resolveShouldLayoutReverse() {
        if (this.mOrientation != 1 && this.isLayoutRTL()) {
            this.mShouldReverseLayout = !this.mReverseLayout;
        } else {
            this.mShouldReverseLayout = this.mReverseLayout;
        }

        if (mOrientation == OrientationHelper.HORIZONTAL && getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
            mReverseLayout = !mReverseLayout;
        }

    }
}
