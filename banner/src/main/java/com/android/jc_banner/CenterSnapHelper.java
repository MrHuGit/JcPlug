package com.android.jc_banner;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-04 20:57
 * @describe {@link LinearSnapHelper}
 * @update
 */
public class CenterSnapHelper extends RecyclerView.OnFlingListener {
    private RecyclerView mRecyclerView;
    private Scroller mGravityScroller;
    private boolean snapToCenter = false;
    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                private boolean mScrolled = false;

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    final BannerLayoutManager layoutManager =
                            (BannerLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager==null){
                        return;
                    }
                    final BannerLayoutManager.OnPageChangeListener onPageChangeListener =
                            layoutManager.onPageChangeListener;
                    if (onPageChangeListener != null) {
                        onPageChangeListener.onPageScrollStateChanged(newState);
                    }
                    //滑动停止
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && mScrolled) {
                        mScrolled = false;
                        if (!snapToCenter) {
                            snapToCenter = true;
                            snapToCenterView(layoutManager, onPageChangeListener);
                        } else {
                            snapToCenter = false;
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dx != 0 || dy != 0) {
                        mScrolled = true;
                    }
                }
            };

    /**
     * 手指在屏幕上滑动 RecyclerView然后松手，RecyclerView中的内容会顺着惯性继续往手指滑动的方向继续滚动直到停止，这个过程叫做 Fling 。
     * Fling 操作从手指离开屏幕瞬间被触发，在滚动停止时结束
     *
     * @param velocityX
     *         x方向速度
     * @param velocityY
     *         y方向速度
     *
     * @return
     */
    @Override
    public boolean onFling(int velocityX, int velocityY) {
        BannerLayoutManager layoutManager = (BannerLayoutManager) mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }

        if (!layoutManager.getInfinite() &&
                (layoutManager.mOffset == layoutManager.getMaxOffset()
                        || layoutManager.mOffset == layoutManager.getMinOffset())) {
            return false;
        }

        final int minFlingVelocity = mRecyclerView.getMinFlingVelocity();
        mGravityScroller.fling(0, 0, velocityX, velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (layoutManager.mOrientation == BannerLayoutManager.VERTICAL
                && Math.abs(velocityY) > minFlingVelocity) {
            final int currentPosition = layoutManager.getCurrentPosition();
            final int offsetPosition = (int) (mGravityScroller.getFinalY() /
                    layoutManager.mInterval / layoutManager.getDistanceRatio());
            mRecyclerView.smoothScrollToPosition(layoutManager.getReverseLayout() ?
                    currentPosition - offsetPosition : currentPosition + offsetPosition);
            return true;
        } else if (layoutManager.mOrientation == BannerLayoutManager.HORIZONTAL
                && Math.abs(velocityX) > minFlingVelocity) {
            final int currentPosition = layoutManager.getCurrentPosition();
             int offsetPosition = (int) (mGravityScroller.getFinalX() /
                    layoutManager.mInterval / layoutManager.getDistanceRatio());
            if (offsetPosition>1){
                offsetPosition=1;
            }
            mRecyclerView.smoothScrollToPosition(layoutManager.getReverseLayout() ?
                    currentPosition - offsetPosition : currentPosition + offsetPosition);
            return true;
        }

        return true;
    }

    void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        if (mRecyclerView == recyclerView) {
            return;
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (!(layoutManager instanceof BannerLayoutManager)) {
                return;
            }
            //设置监听
            setupCallbacks();
            mGravityScroller = new Scroller(mRecyclerView.getContext(), new DecelerateInterpolator());

            snapToCenterView((BannerLayoutManager) layoutManager, ((BannerLayoutManager) layoutManager).onPageChangeListener);
        }
    }

    /**
     * 添加监听
     * @throws IllegalStateException 重复添加异常
     */
    private void setupCallbacks() throws IllegalStateException {
        if (mRecyclerView.getOnFlingListener() != null) {
            throw new IllegalStateException("An instance of OnFlingListener already set.");
        }
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(this);
    }

    /**
     * 移除所有监听
     */
    private void destroyCallbacks() {
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(null);
    }

    private void snapToCenterView(BannerLayoutManager layoutManager, BannerLayoutManager.OnPageChangeListener listener) {
        final int delta = layoutManager.getOffsetToCenter();
        if (delta != 0) {
            if (layoutManager.getOrientation() == BannerLayoutManager.VERTICAL) {
                mRecyclerView.smoothScrollBy(0, delta);
            } else {
                mRecyclerView.smoothScrollBy(delta, 0);
            }
        } else {
            snapToCenter = false;
        }

        if (listener != null) {
            listener.onPageSelected(layoutManager.getCurrentPosition());
        }
    }
}
