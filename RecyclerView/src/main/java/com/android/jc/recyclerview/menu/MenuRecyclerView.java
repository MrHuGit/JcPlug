package com.android.jc.recyclerview.menu;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.android.jc.recyclerview.R;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-25 15:54
 * @describe 菜单滑动动画, 菜单外层要用FrameLayout包裹，且每个菜单中的每个item均要用FrameLayout包裹
 * @update
 */
public class MenuRecyclerView extends RecyclerView {
    private boolean mVerticalScrollBarEnabled;
    /**
     * 是否允许滑动item
     */
    private boolean mEnableScrollingItem;
    /**
     * 滑动时间
     */
    private int mScrollDurationTime;
    /**
     * 最小手指滑动速度
     */
    private final float mItemMinimumFlingVelocity;

    /**
     * item是否在滑动
     */
    private boolean mItemStartMoving;
    /**
     * 记录当前item的菜单是否已经显示可见
     */
    private boolean mCurrentItemHasOpened;
    /**
     * 可以开始拖动之前的行程距离
     */
    protected final float mTouchSlop;
    /**
     * 记录手指按下的位置
     */
    private final Point mActionDownPoint = new Point();
    /**
     * 记录手指按下的位置,0为之前的 1为当前的位置
     */
    private final float[] mTouchX = new float[2];
    private final float[] mTouchY = new float[2];
    private VelocityTracker mVelocityTracker;

    /**
     * 当前itemView的边界
     */
    private final Rect mCurrentItemBounds = new Rect();
    /**
     * 当前itemView的菜单边界
     */
    private final Rect mCurrentItemMenuBounds = new Rect();
    /**
     * 当前正在操作的itemView
     */
    private ViewGroup mCurrentItemView;
    /**
     * 已经显示菜单的itemView
     */
    private ViewGroup mOpenedItemView;
    private final List<ViewGroup> mOpenedItems = new LinkedList<>();
    /**
     * item菜单总宽度tag
     */
    private static final int ITEM_MENU_TOTAL_WIDTH_TAG = R.id.itemMenuTotalWidthTag;
    /**
     * item菜单宽度数组tag
     */
    private static final int ITEM_MENU_WIDTH_ARRAYS_TAG = R.id.itemMenuWidthArraysTag;
    /**
     * item动画tag
     */
    private static final int ITEM_ANIMATOR_TAG = R.id.itemAnimatorTag;

    public MenuRecyclerView(Context context) {
        this(context, null);
    }

    public MenuRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final float density = context.getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfiguration.getTouchSlop() * density;
        mItemMinimumFlingVelocity = 200f * density;
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MenuRecyclerView, defStyle, 0);
        setItemScrollingEnabled(ta.getBoolean(R.styleable.MenuRecyclerView_itemScrollingEnabled, true));
        setScrollDurationTime(ta.getInteger(R.styleable.MenuRecyclerView_itemScrollDuration, 500));
        ta.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        final int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            resetData();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);
        boolean intercept = false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownPoint.x = (int) e.getX();
                mActionDownPoint.y = (int) e.getY();
                markMovePoint(mActionDownPoint.x, mActionDownPoint.y);
                calculateItemMenuWidth();
                if (mOpenedItems.size() == 0) {
                    break;
                }
                requestParentDisallowInterceptTouchEvent();
                if (mOpenedItemView != null) {
                    mCurrentItemHasOpened = true;
                    if (mCurrentItemView == mOpenedItemView) {
                        calculateCurrentItemMenuBounds();
                        if (mCurrentItemMenuBounds.contains(mActionDownPoint.x, mActionDownPoint.y)) {
                            break;
                        } else if (mCurrentItemBounds.contains(mActionDownPoint.x, mActionDownPoint.y)) {
                            return true;
                        }
                    }
                    releaseItemViewInternal(mOpenedItemView, mScrollDurationTime);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                markMovePoint(e.getX(), e.getY());
                intercept = tryItemIsMoving();
                if (mCurrentItemHasOpened && mCurrentItemMenuBounds.contains(mActionDownPoint.x, mActionDownPoint.y)) {
                    return intercept;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentItemHasOpened && mCurrentItemMenuBounds.contains(mActionDownPoint.x, mActionDownPoint.y)) {
                    releaseItemView(true);
                }
                clearTouch();
                break;
            default:
                break;
        }
        return intercept || super.onInterceptTouchEvent(e);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mVerticalScrollBarEnabled) {
            super.setVerticalScrollBarEnabled(!mItemStartMoving);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                if (mItemStartMoving || mCurrentItemHasOpened || mOpenedItems.size() > 0) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                markMovePoint(e.getX(), e.getY());
                if (!mEnableScrollingItem && cancelTouch()) {
                    return true;
                }
                if (mItemStartMoving) {
                    float touchMoveDx = mTouchX[1] - mTouchX[0];
                    final float translationDx = mCurrentItemView.getChildAt(0).getTranslationX();
                    final boolean rtl = isLayoutRtl(mCurrentItemView);
                    final int menuNeedMoveDx = rtl ? (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG) : -(int) (mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG));
                    if (!rtl && touchMoveDx + translationDx < menuNeedMoveDx || rtl && touchMoveDx + translationDx > menuNeedMoveDx) {
                        touchMoveDx = touchMoveDx / 3f;
                    } else if (!rtl && touchMoveDx + translationDx > 0 || rtl && touchMoveDx + translationDx < 0) {
                        touchMoveDx = 0 - translationDx;
                    }
                    cancelAnimator(mCurrentItemView);
                    translateItemView(mCurrentItemView, touchMoveDx);
                    return true;
                } else {
                    if (mCurrentItemHasOpened | tryItemIsMoving()) {
                        return true;
                    }
                    if (mOpenedItems.size() > 0) {
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mEnableScrollingItem && mItemStartMoving) {
                    changeItemState(e);
                    return true;
                }
            case MotionEvent.ACTION_CANCEL:
                cancelTouch();
                break;
            default:
                break;
        }

        return super.onTouchEvent(e);
    }

    /**
     * 修改itemView状态
     *
     * @param e MotionEvent
     */
    private void changeItemState(MotionEvent e) {
        final boolean rtl = isLayoutRtl(mCurrentItemView);
        final float translationDx = mCurrentItemView.getChildAt(0).getTranslationX();
        final int itemMenuWidth = (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
        boolean isSameItem = !rtl && translationDx == -itemMenuWidth;
        isSameItem |= rtl && translationDx == itemMenuWidth;
        if (translationDx != 0) {
            if (isSameItem) {
                mOpenedItemView = mCurrentItemView;
            } else {
                final float dx = rtl ? mTouchX[0] - mTouchX[1] : mTouchX[1] - mTouchX[0];
                mVelocityTracker.computeCurrentVelocity(1000);
                final float absVelocity = Math.abs(mVelocityTracker.getXVelocity());
                final float itemMenuDx = (rtl ? itemMenuWidth : -itemMenuWidth) - translationDx;
                if (dx < 0 && absVelocity >= mItemMinimumFlingVelocity) {
                    smoothTranslateItemView(mCurrentItemView, itemMenuDx, mScrollDurationTime);
                    mOpenedItemView = mCurrentItemView;
                } else if (dx > 0 && absVelocity >= mItemMinimumFlingVelocity) {
                    releaseItemView(true);
                } else {
                    //此处仿照微信的IOS版本中，只有当滑动的距离大于item菜单宽度的一半才打开菜单
                    if (Math.abs(translationDx) < itemMenuWidth / 2f) {
                        releaseItemView(true);
                    } else {
                        smoothTranslateItemView(mCurrentItemView, itemMenuDx, mScrollDurationTime);
                        mOpenedItemView = mCurrentItemView;
                    }
                }
            }
        }
        clearTouch();
        cancelParentTouch(e);
    }


    /**
     * 记录当前手指按下的位置
     *
     * @param x x
     * @param y y
     */
    private void markMovePoint(float x, float y) {
        mTouchX[0]=mTouchX[1];
        mTouchX[1] = x;
        mTouchY[0]=mTouchY[1];
        mTouchY[1] = y;
    }

    /**
     * 计算当前手指按下的item的菜单宽度,并绑定到tag上，记录当前按下的item
     */
    private void calculateItemMenuWidth() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            //当前不可见的item
            if (child.getVisibility() != VISIBLE) {
                continue;
            }
            //不包含子View的item
            if (!(child instanceof ViewGroup)) {
                continue;
            }
            //手指当前按下的位置不在item范围
            final ViewGroup itemView = (ViewGroup) child;
            itemView.getHitRect(mCurrentItemBounds);
            if (!mCurrentItemBounds.contains(mActionDownPoint.x, mActionDownPoint.y)) {
                continue;
            }
            //判断当前item的最后一个子View是否是FrameLayout
            final int itemChildCount = itemView.getChildCount();
            final View itemLastChild = itemView.getChildAt(itemChildCount >= 2 ? itemChildCount - 1 : 1);
            if (!(itemLastChild instanceof FrameLayout)) {
                break;
            }
            final FrameLayout itemMenu = (FrameLayout) itemLastChild;
            //计算当前itemView的菜单中每个view的宽度,itemView的菜单总宽度并绑定到itemView tag上
            final int menuItemCount = itemMenu.getChildCount();
            final int[] menuItemWidths = new int[menuItemCount];
            int itemMenuWidth = 0;
            for (int j = 0; j < menuItemCount; j++) {
                menuItemWidths[j] = ((FrameLayout) itemMenu.getChildAt(j)).getChildAt(0).getMeasuredWidth();
                itemMenuWidth += menuItemWidths[j];
            }
            if (itemMenuWidth > 0) {
                itemView.setTag(ITEM_MENU_TOTAL_WIDTH_TAG, itemMenuWidth);
                itemView.setTag(ITEM_MENU_WIDTH_ARRAYS_TAG, menuItemWidths);
                mCurrentItemView = itemView;
            }
            break;
        }
    }

    private boolean tryItemIsMoving() {
        if (mCurrentItemView == null || !mEnableScrollingItem || getScrollState() != SCROLL_STATE_IDLE) {
            return false;
        }
        if (getLayoutManager() != null && getLayoutManager().canScrollHorizontally()) {
            return false;
        }

        final float absDy = Math.abs(mTouchY[1] - mActionDownPoint.y);
        if (absDy <= mTouchSlop) {
            final float dx = mTouchX[1] - mActionDownPoint.x;
            if (mOpenedItems.size() == 0) {
                final boolean rtl = isLayoutRtl(mCurrentItemView);
                mItemStartMoving = rtl && dx > mTouchSlop || !rtl && dx < -mTouchSlop;
            } else {
                mItemStartMoving = Math.abs(dx) > mTouchSlop;
            }
            if (mItemStartMoving) {
                requestParentDisallowInterceptTouchEvent();
                return true;
            }
        }
        return false;
    }

    private void requestParentDisallowInterceptTouchEvent() {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private boolean cancelTouch() {
        if (mItemStartMoving) {
            releaseItemView(true);
            clearTouch();
            return true;
        }
        if (mCurrentItemHasOpened) {
            if (mCurrentItemView == mOpenedItemView) {
                releaseItemView(true);
            }
            clearTouch();
            return true;
        }
        return false;
    }

    private void clearTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        resetData();
    }

    /**
     * 重置数据
     */
    private void resetData() {
        mCurrentItemView = null;
        mCurrentItemHasOpened = false;
        mCurrentItemBounds.setEmpty();
        mCurrentItemMenuBounds.setEmpty();
        mItemStartMoving = false;
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
    }

    private void cancelParentTouch(MotionEvent e) {
        final int action = e.getAction();
        e.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(e);
        e.setAction(action);
    }

    /**
     * 释放itemView
     *
     * @param animate 是否需要执行动画效果
     */
    public void releaseItemView(boolean animate) {
        releaseItemViewInternal(mItemStartMoving ? mCurrentItemView : mOpenedItemView, animate ? mScrollDurationTime : 0);
    }

    /**
     * 释放itemView
     *
     * @param itemView itemView
     * @param duration 动画时间
     */
    private void releaseItemViewInternal(ViewGroup itemView, int duration) {
        if (itemView != null) {
            if (duration > 0) {
                smoothTranslateItemView(itemView, -itemView.getChildAt(0).getTranslationX(), duration);
            } else {
                cancelAnimator(itemView);
                translateItemView(itemView, -itemView.getChildAt(0).getTranslationX());
            }
            if (mOpenedItemView == itemView) {
                mOpenedItemView = null;
            }
        }
    }


    private void smoothTranslateItemView(ViewGroup itemView, float dx, int duration) {
        if (dx == 0) {
            return;
        }
        MenuItemTranslateAnimator animator = (MenuItemTranslateAnimator) itemView.getTag(ITEM_ANIMATOR_TAG);
        if (duration > 0) {
            boolean canceled = false;
            if (animator == null) {
                animator = new MenuItemTranslateAnimator(this, itemView);
                itemView.setTag(ITEM_ANIMATOR_TAG, animator);
            } else if (animator.isRunning()) {
                animator.removeListener(animator);
                animator.cancel();
                canceled = true;
            }
            animator.setFloatValues(0, dx);
            final boolean rtl = isLayoutRtl(itemView);
            Interpolator interpolator;
            if (!rtl && dx < 0 || rtl && dx > 0) {
                interpolator = new OvershootInterpolator(1.0f);
            } else {
                interpolator = new ViscousFluidInterpolator(6.66f);
            }
            animator.setInterpolator(interpolator);
            animator.setDuration(duration);
            animator.start();
            if (canceled) {
                animator.addListener(animator);
            }
        } else {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            translateItemView(itemView, dx);
        }
    }

    /**
     * 取消动画
     *
     * @param itemView itemView
     */
    private void cancelAnimator(ViewGroup itemView) {
        if (itemView == null) {
            return;
        }
        MenuItemTranslateAnimator animator = (MenuItemTranslateAnimator) itemView.getTag(ITEM_ANIMATOR_TAG);
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }


    /**
     * 更新item的位置及item的menu位置(此方法不会取消动画,仅仅只是更新位置)
     *
     * @param itemView item
     * @param dx       平移的距离
     */
    void translateItemView(ViewGroup itemView, float dx) {
        if (dx == 0) {
            return;
        }
        final float translationx = itemView.getChildAt(0).getTranslationX() + dx;
        final int itemMenuWidth = (int) itemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
        final boolean rtl = isLayoutRtl(itemView);
        boolean needRemoveItem = !rtl && translationx > -itemMenuWidth * 0.05f;
        needRemoveItem |= rtl && translationx < itemMenuWidth * 0.05f;
        if (needRemoveItem) {
            mOpenedItems.remove(itemView);
        } else if (!mOpenedItems.contains(itemView)) {
            mOpenedItems.add(itemView);
        }
        final int itemChildCount = itemView.getChildCount();
        for (int i = 0; i < itemChildCount; i++) {
            itemView.getChildAt(i).setTranslationX(translationx);
        }
        final FrameLayout itemMenu = (FrameLayout) itemView.getChildAt(itemChildCount - 1);
        final int[] menuItemWidths = (int[]) itemView.getTag(ITEM_MENU_WIDTH_ARRAYS_TAG);
        float menuItemFrameDx = 0;
        for (int i = 1, menuItemCount = itemMenu.getChildCount(); i < menuItemCount; i++) {
            final FrameLayout menuItemFrame = (FrameLayout) itemMenu.getChildAt(i);
            menuItemFrameDx -= dx * (float) menuItemWidths[i - 1] / (float) itemMenuWidth;
            menuItemFrame.setTranslationX(menuItemFrame.getTranslationX() + menuItemFrameDx);
        }
    }


    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        mVerticalScrollBarEnabled = verticalScrollBarEnabled;
        super.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
    }

    /**
     * 计算菜单的边界
     */
    private void calculateCurrentItemMenuBounds() {
        int itemMenuWidth = (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
        int left = isLayoutRtl(mCurrentItemView) ? 0 : mCurrentItemView.getRight() - itemMenuWidth;
        int right = left + itemMenuWidth;
        mCurrentItemMenuBounds.set(left, mCurrentItemBounds.top, right, mCurrentItemBounds.bottom);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseItemViewInternal(mOpenedItemView, 0);
        if (mOpenedItems.size() > 0) {
            ViewGroup[] openedItems = mOpenedItems.toArray(new ViewGroup[0]);
            for (ViewGroup openedItem : openedItems) {
                Animator animator = (Animator) openedItem.getTag(ITEM_ANIMATOR_TAG);
                if (animator != null && animator.isRunning()) {
                    animator.end();
                }
            }
            mOpenedItems.clear();
        }
    }

    public boolean getItemScrollingEnabled() {
        return mEnableScrollingItem;
    }

    public void setItemScrollingEnabled(boolean enabled) {
        mEnableScrollingItem = enabled;
    }

    public int getScrollDurationTime() {
        return mScrollDurationTime;
    }

    public void setScrollDurationTime(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("The animators for opening/closing the item views " +
                    "cannot have negative duration: " + duration);
        }
        mScrollDurationTime = duration;
    }

    /**
     * 视图的表现形式是从右开始向左结束
     *
     * @param view 对应的view
     * @return 是否是从右开始向左结束
     */
    private static boolean isLayoutRtl(@NonNull View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }
}