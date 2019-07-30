package com.android.jc.recyclerview.menu;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-25 15:54
 * @describe 菜单滑动动画
 * @update
 */
public class MenuItemTranslateAnimator extends ValueAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private final ViewGroup mItemView;
    private final SimpleArrayMap<View, Integer> mMenuChildrenLayerTypes;
    private final MenuRecyclerView mMenuRecyclerView;
    private float mCacheAnimatedValue=0;

      MenuItemTranslateAnimator(final MenuRecyclerView slidingItemMenuRecyclerView, final ViewGroup itemView) {
        this.mItemView = itemView;
        this.mMenuChildrenLayerTypes = new SimpleArrayMap<>(0);
        this.mMenuRecyclerView = slidingItemMenuRecyclerView;
        addListener(this);
        addUpdateListener(this);
    }

    @Override
    public void start() {
        mCacheAnimatedValue = 0;
        super.start();
    }

    @Override
    public void onAnimationStart(Animator animation) {
        ensureChildrenLayerTypes();
        for (int i = mMenuChildrenLayerTypes.size() - 1; i >= 0; i--) {
            final View child = mMenuChildrenLayerTypes.keyAt(i);
            child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (ViewCompat.isAttachedToWindow(child)) {
                child.buildLayer();
            }
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        for (int i = mMenuChildrenLayerTypes.size() - 1; i >= 0; i--) {
            mMenuChildrenLayerTypes.keyAt(i).setLayerType(
                    mMenuChildrenLayerTypes.valueAt(i), null);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        final float animatedValue = (float) animation.getAnimatedValue();
        mMenuRecyclerView.translateItemView(mItemView, animatedValue - mCacheAnimatedValue);
        mCacheAnimatedValue = animatedValue;
    }


    private void ensureChildrenLayerTypes() {
        final int itemChildCount = mItemView.getChildCount();
        final ViewGroup itemMenu = (ViewGroup) mItemView.getChildAt(itemChildCount - 1);
        final int menuItemCount = itemMenu.getChildCount();
        mMenuChildrenLayerTypes.clear();
        mMenuChildrenLayerTypes.ensureCapacity(itemChildCount - 1 + menuItemCount);
        for (int i = 0; i < itemChildCount - 1; i++) {
            final View itemChild = mItemView.getChildAt(i);
            mMenuChildrenLayerTypes.put(itemChild, itemChild.getLayerType());
        }
        for (int i = 0; i < menuItemCount; i++) {
            final View menuItemFrame = itemMenu.getChildAt(i);
            mMenuChildrenLayerTypes.put(menuItemFrame, menuItemFrame.getLayerType());
        }
    }
}
