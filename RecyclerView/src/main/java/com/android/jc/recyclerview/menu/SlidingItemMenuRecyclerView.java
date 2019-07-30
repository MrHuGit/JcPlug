//package com.android.jc.recyclerview.menu;
//
//import android.animation.Animator;
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Rect;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.view.ViewCompat;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.view.ViewGroup;
//import android.view.ViewParent;
//import android.view.animation.Interpolator;
//import android.view.animation.OvershootInterpolator;
//import android.widget.FrameLayout;
//
//import com.android.jc.recyclerview.R;
//
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * @author Mr.Hu(Jc) JcPlug
// * @create 2019-07-25 15:23
// * @describe 左滑菜单栏的RecyclerView（仿照微信的IOS版本）
// * @update
// */
//public class SlidingItemMenuRecyclerView extends RecyclerView {
//    /**
//     * 记录按下的x,y位置
//     */
//    private int mActionDownx, mActionDowny;
//    private final float[] mTouchX = new float[2];
//    private final float[] mTouchY = new float[2];
//    /**
//     * 是否允许滑动item
//     */
//    private boolean mEnableScrollingItem;
//    /**
//     * 滑动时间
//     */
//    private int mScrollDurationTime;
//    /**
//     * 最小手指滑动速度
//     */
//    private final float mItemMinimumFlingVelocity;
//    /**
//     * item是否在滑动
//     */
//    private boolean mItemStartMoving;
//    /**
//     * 记录当前item的菜单是否已经显示可见
//     */
//    private boolean mCurrentItemHasOpened;
//
//
//    /**
//     * 可以开始拖动之前的行程距离
//     */
//    private final float mTouchSlop;
//    /**
//     * item菜单总宽度tag
//     */
//    private static final int ITEM_MENU_TOTAL_WIDTH_TAG = R.id.itemMenuTotalWidthTag;
//    /**
//     * item菜单宽度数组tag
//     */
//    private static final int ITEM_MENU_WIDTH_ARRAYS_TAG =  R.id.itemMenuWidthArraysTag;
//    /**
//     * item动画tag
//     */
//    private static final int ITEM_ANIMATOR_TAG =  R.id.itemAnimatorTag;
//    /**
//     * 当前正在操作的itemView
//     */
//    private ViewGroup mCurrentItemView;
//    /**
//     * 已经显示菜单的itemView
//     */
//    private ViewGroup mOpenedItemView;
//    private VelocityTracker mVelocityTracker;
//    /**
//     * 当前itemView的边界
//     */
//    private final Rect mCurrentItemBounds = new Rect();
//    /**
//     * 当前itemView的菜单边界
//     */
//    private final Rect mCurrentItemMenuBounds = new Rect();
//    private final List<ViewGroup> mOpenedItems = new LinkedList<>();
//    private final Interpolator sViscousFluidInterpolator = new ViscousFluidInterpolator(6.66f);
//    private final Interpolator sOvershootInterpolator = new OvershootInterpolator(1.0f);
//    private boolean mVerticalScrollBarEnabled;
//
//    public SlidingItemMenuRecyclerView(@NonNull Context context) {
//        this(context, null);
//    }
//
//    public SlidingItemMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public SlidingItemMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        final float density = context.getResources().getDisplayMetrics().density;
//        mTouchSlop = ViewConfiguration.getTouchSlop() * density;
//        mItemMinimumFlingVelocity = 200f * density;
//        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingItemMenuRecyclerView, defStyle, 0);
//        setItemScrollingEnabled(ta.getBoolean(R.styleable.SlidingItemMenuRecyclerView_itemMenuScrollingEnabled, true));
//        setItemScrollDuration(ta.getInteger(R.styleable.SlidingItemMenuRecyclerView_itemMenuScrollDuration, 500));
//        ta.recycle();
//
//    }
//
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent e) {
//        final int action = e.getAction();
//        if (action == MotionEvent.ACTION_DOWN) {
//            resetTouch();
//        }
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(e);
//        boolean intercept = false;
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mActionDownx = (int) e.getX();
//                mActionDowny = (int) e.getY();
//                markCurrTouchPoint(mActionDownx, mActionDowny);
//                calculateItemMenuWidth();
//                if (mOpenedItems.size() == 0) {
//                    break;
//                }
//                requestParentDisallowInterceptTouchEvent();
//                if (mOpenedItemView != null) {
//                    mCurrentItemHasOpened = true;
//                    if (mCurrentItemView == mOpenedItemView) {
//                        calculateItemMenuBounds();
//                        if (mCurrentItemMenuBounds.contains(mActionDownx, mActionDowny)) {
//                            break;
//                        } else if (mCurrentItemBounds.contains(mActionDownx, mActionDowny)) {
//                            return true;
//                        }
//                    }
//                    closeItemMenu(mOpenedItemView, mScrollDurationTime);
//                }
//                return true;
//
//            case MotionEvent.ACTION_MOVE:
//                markCurrTouchPoint(e.getX(), e.getY());
//                intercept = tryHandleItemScrollingEvent();
//                if (mCurrentItemHasOpened && mCurrentItemMenuBounds.contains(mActionDownx, mActionDowny)) {
//                    return intercept;
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                if (mCurrentItemHasOpened && mCurrentItemMenuBounds.contains(mActionDownx, mActionDowny)) {
//                    closeItemMenu(mItemStartMoving ? mCurrentItemView : mOpenedItemView, mScrollDurationTime);
//                }
//                clearTouch();
//                break;
//            default:
//                break;
//        }
//        return intercept || super.onInterceptTouchEvent(e);
//    }
//
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        if (mVerticalScrollBarEnabled) {
//            super.setVerticalScrollBarEnabled(!mItemStartMoving);
//        }
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(e);
//        int action = e.getAction() & MotionEvent.ACTION_MASK;
//        switch (action) {
//            case MotionEvent.ACTION_POINTER_DOWN:
//            case MotionEvent.ACTION_POINTER_UP:
//                if (mItemStartMoving || mCurrentItemHasOpened || mOpenedItems.size() > 0) {
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                markCurrTouchPoint(e.getX(), e.getY());
//                if (!mEnableScrollingItem && cancelTouch()) {
//                    return true;
//                }
//                if (mItemStartMoving) {
//                    float dx = mTouchX[mTouchX.length - 1] - mTouchX[mTouchX.length - 2];
//                    final float translationx = mCurrentItemView.getChildAt(0).getTranslationX();
//                    final boolean rtl = isLayoutRtl(mCurrentItemView);
//                    final int itemMenuWidth = (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
//                    final int finalXFromEndToStart = rtl ? itemMenuWidth : -itemMenuWidth;
//                    if (!rtl && dx + translationx < finalXFromEndToStart || rtl && dx + translationx > finalXFromEndToStart) {
//                        dx = dx / 3f;
//                    } else if (!rtl && dx + translationx > 0 || rtl && dx + translationx < 0) {
//                        dx = 0 - translationx;
//                    }
//                    cancelTranslateAnimator(mCurrentItemView);
//                    translateItemView(mCurrentItemView, dx);
//                    return true;
//                } else {
//                    if (mCurrentItemHasOpened | tryHandleItemScrollingEvent()) {
//                        return true;
//                    }
//                    if (mOpenedItems.size() > 0) {
//                        return true;
//                    }
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//                if (mEnableScrollingItem && mItemStartMoving) {
//                    changeItemState(e);
//                    return true;
//                }
//            case MotionEvent.ACTION_CANCEL:
//                cancelTouch();
//                break;
//            default:
//                break;
//        }
//        return super.onTouchEvent(e);
//    }
//
//    @Override
//    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
//        mVerticalScrollBarEnabled = verticalScrollBarEnabled;
//        super.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        closeItemMenu(mOpenedItemView, 0);
//        if (mOpenedItems.size() > 0) {
//            final ViewGroup[] openedItems = mOpenedItems.toArray(new ViewGroup[0]);
//            for (ViewGroup openedItem : openedItems) {
//                final Animator animator = (Animator) openedItem.getTag(ITEM_ANIMATOR_TAG);
//                if (animator != null && animator.isRunning()) {
//                    animator.end();
//                }
//            }
//            mOpenedItems.clear();
//        }
//    }
//
//    private void changeItemState(MotionEvent e) {
//        final boolean rtl = isLayoutRtl(mCurrentItemView);
//        final float translationx = mCurrentItemView.getChildAt(0).getTranslationX();
//        final int itemMenuWidth = (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
//        boolean isSameItem = !rtl && translationx == -itemMenuWidth;
//        isSameItem |= rtl && translationx == itemMenuWidth;
//        if (translationx != 0) {
//            if (isSameItem) {
//                mOpenedItemView = mCurrentItemView;
//            } else {
//                float dx = rtl ? mTouchX[mTouchX.length - 2] - mTouchX[mTouchX.length - 1] : mTouchX[mTouchX.length - 1] - mTouchX[mTouchX.length - 2];
//                mVelocityTracker.computeCurrentVelocity(1000);
//                final float velocityx = Math.abs(mVelocityTracker.getXVelocity());
//                if (dx < 0 && velocityx >= mItemMinimumFlingVelocity) {
//                    smoothTranslateItemView(mCurrentItemView, rtl ? itemMenuWidth : -itemMenuWidth, mScrollDurationTime);
//                    mOpenedItemView = mCurrentItemView;
//                } else if (dx > 0 && velocityx >= mItemMinimumFlingVelocity) {
//                    closeItemMenu(mCurrentItemView, mScrollDurationTime);
//                } else {
//                    //此处仿照微信的IOS版本中，只有当滑动的距离大于item菜单宽度的一半才打开菜单
//                    final float middle = itemMenuWidth / 2f;
//                    if (Math.abs(translationx) < middle) {
//                        closeItemMenu(mCurrentItemView, mScrollDurationTime);
//                    } else {
//                        smoothTranslateItemView(mCurrentItemView, rtl ? itemMenuWidth : -itemMenuWidth, mScrollDurationTime);
//                        mOpenedItemView = mCurrentItemView;
//                    }
//                    return;
//                }
//
//            }
//        }
//        clearTouch();
//        cancelParentTouch(e);
//    }
//
//    /**
//     * 更新item的位置及item的menu位置(此方法不会取消动画,仅仅只是更新位置)
//     *
//     * @param itemView item
//     * @param dx       平移的距离
//     */
//    void translateItemView(ViewGroup itemView, float dx) {
//        if (dx == 0) {
//            return;
//        }
//        final float translationx = itemView.getChildAt(0).getTranslationX() + dx;
//        final int itemMenuWidth = (int) itemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
//        final boolean rtl = isLayoutRtl(itemView);
//        boolean needRemoveItem = !rtl && translationx > -itemMenuWidth * 0.05f;
//        needRemoveItem &= translationx > -itemMenuWidth * 0.05f;
//        needRemoveItem |= rtl;
//        needRemoveItem &= translationx < itemMenuWidth * 0.05f;
//        if (needRemoveItem) {
//            mOpenedItems.remove(itemView);
//        } else if (!mOpenedItems.contains(itemView)) {
//            mOpenedItems.add(itemView);
//        }
//        //itemView的子View平移
//        final int itemChildCount = itemView.getChildCount();
//        for (int i = 0; i < itemChildCount; i++) {
//            itemView.getChildAt(i).setTranslationX(translationx);
//        }
//        //itemView的menu中的item平移
//        final FrameLayout itemMenu = (FrameLayout) itemView.getChildAt(itemChildCount - 1);
//        final int[] menuItemWidths = (int[]) itemView.getTag(ITEM_MENU_WIDTH_ARRAYS_TAG);
//        float menuItemFrameDx = 0;
//        for (int i = 1, menuItemCount = itemMenu.getChildCount(); i < menuItemCount; i++) {
//            final FrameLayout menuItemFrame = (FrameLayout) itemMenu.getChildAt(i);
//            menuItemFrameDx -= dx * (float) menuItemWidths[i - 1] / (float) itemMenuWidth;
//            menuItemFrame.setTranslationX(menuItemFrame.getTranslationX() + menuItemFrameDx);
//        }
//    }
//
//    private boolean tryHandleItemScrollingEvent() {
//        if (mCurrentItemView == null || !mEnableScrollingItem || getScrollState() != SCROLL_STATE_IDLE) {
//            return false;
//        }
//        if (getLayoutManager() != null && getLayoutManager().canScrollHorizontally()) {
//            return false;
//        }
//
//        final float absDy = Math.abs(mTouchY[mTouchY.length - 1] - mActionDownx);
//        if (absDy <= mTouchSlop) {
//            final float dx = mTouchX[mTouchX.length - 1] - mActionDownx;
//            if (mOpenedItems.size() == 0) {
//                final boolean rtl = isLayoutRtl(mCurrentItemView);
//                mItemStartMoving = rtl && dx > mTouchSlop || !rtl && dx < -mTouchSlop;
//            } else {
//                mItemStartMoving = Math.abs(dx) > mTouchSlop;
//            }
//            if (mItemStartMoving) {
//                requestParentDisallowInterceptTouchEvent();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 拦截父类的事件分发
//     */
//    private void requestParentDisallowInterceptTouchEvent() {
//        final ViewParent parent = getParent();
//        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(true);
//        }
//    }
//
//
//    /**
//     * 计算菜单的边界
//     */
//    private void calculateItemMenuBounds() {
//        int itemTotalWidth = (int) mCurrentItemView.getTag(ITEM_MENU_TOTAL_WIDTH_TAG);
//        final int left = isLayoutRtl(mCurrentItemView) ? 0 : mCurrentItemView.getRight() - itemTotalWidth;
//        final int right = left + itemTotalWidth;
//        mCurrentItemMenuBounds.set(left, mCurrentItemBounds.top, right, mCurrentItemBounds.bottom);
//    }
//
//    /**
//     * 计算当前手指按下的item的菜单宽度,并绑定到tag上，记录当前按下的item
//     */
//    private void calculateItemMenuWidth() {
//        for (int i = getChildCount() - 1; i >= 0; i--) {
//            final View child = getChildAt(i);
//            //当前不可见的item
//            if (child.getVisibility() != VISIBLE) {
//                continue;
//            }
//            //不包含子View的item
//            if (!(child instanceof ViewGroup)) {
//                continue;
//            }
//            //手指当前按下的位置不在item范围
//            final ViewGroup itemView = (ViewGroup) child;
//            itemView.getHitRect(mCurrentItemBounds);
//            if (!mCurrentItemBounds.contains(mActionDownx, mActionDowny)) {
//                continue;
//            }
//            //判断当前item的最后一个子View是否是FrameLayout
//            final int itemChildCount = itemView.getChildCount();
//            final View itemLastChild = itemView.getChildAt(itemChildCount >= 2 ? itemChildCount - 1 : 1);
//            if (!(itemLastChild instanceof FrameLayout)) {
//                break;
//            }
//            final FrameLayout itemMenu = (FrameLayout) itemLastChild;
//            //计算当前itemView的菜单中每个view的宽度,itemView的菜单总宽度并绑定到itemView tag上
//            final int menuItemCount = itemMenu.getChildCount();
//            final int[] menuItemWidths = new int[menuItemCount];
//            int itemMenuWidth = 0;
//            for (int j = 0; j < menuItemCount; j++) {
//                menuItemWidths[j] = ((FrameLayout) itemMenu.getChildAt(j)).getChildAt(0).getWidth();
//                itemMenuWidth += menuItemWidths[j];
//            }
//            if (itemMenuWidth > 0) {
//                itemView.setTag(ITEM_MENU_TOTAL_WIDTH_TAG, itemMenuWidth);
//                itemView.setTag(ITEM_MENU_WIDTH_ARRAYS_TAG, menuItemWidths);
//                mCurrentItemView = itemView;
//            }
//            break;
//        }
//    }
//
//    private void markCurrTouchPoint(float x, float y) {
//        System.arraycopy(mTouchX, 1, mTouchX, 0, mTouchX.length - 1);
//        mTouchX[mTouchX.length - 1] = x;
//        System.arraycopy(mTouchY, 1, mTouchY, 0, mTouchY.length - 1);
//        mTouchY[mTouchY.length - 1] = y;
//    }
//
//
//    /**
//     * itemView平移
//     *
//     * @param itemView     itemView
//     * @param dx           平移的距离
//     * @param durationTime 动画间隔时间,如果时间>0执行动画，否则直接调用{@link #translateItemView(ViewGroup, float)}
//     */
//    private void smoothTranslateItemView(ViewGroup itemView, float dx, int durationTime) {
//        dx = dx - itemView.getChildAt(0).getTranslationX();
//        if (dx == 0) {
//            return;
//        }
//        MenuItemTranslateAnimator animator = (MenuItemTranslateAnimator) itemView.getTag(ITEM_ANIMATOR_TAG);
//        if (durationTime > 0) {
//            boolean canceled = false;
//            if (animator == null) {
//                animator = new MenuItemTranslateAnimator(this, itemView);
//                itemView.setTag(ITEM_ANIMATOR_TAG, animator);
//            } else if (animator.isRunning()) {
//                animator.removeListener(animator);
//                animator.cancel();
//                canceled = true;
//            }
//            animator.setFloatValues(0, dx);
//            final boolean rtl = isLayoutRtl(itemView);
//            boolean useOvershootInterpolator = !rtl && dx < 0;
//            useOvershootInterpolator |= rtl && dx > 0;
//            Interpolator interpolator = useOvershootInterpolator ? sOvershootInterpolator : sViscousFluidInterpolator;
//            animator.setInterpolator(interpolator);
//            animator.setDuration(durationTime);
//            animator.start();
//            if (canceled) {
//                animator.addListener(animator);
//            }
//        } else {
//            if (animator != null && animator.isRunning()) {
//                animator.cancel();
//            }
//            translateItemView(itemView, dx);
//        }
//    }
//
//    /**
//     * 取消动画
//     *
//     * @param itemView itemView
//     */
//    private void cancelTranslateAnimator(ViewGroup itemView) {
//        if (itemView == null) {
//            return;
//        }
//        final MenuItemTranslateAnimator animator = (MenuItemTranslateAnimator) itemView.getTag(ITEM_ANIMATOR_TAG);
//        if (animator != null && animator.isRunning()) {
//            animator.cancel();
//        }
//    }
//
//    private boolean cancelTouch() {
//        if (mItemStartMoving) {
//            closeItemMenu(mCurrentItemView, mScrollDurationTime);
//            clearTouch();
//            return true;
//        }
//
//        if (mCurrentItemHasOpened) {
//            if (mCurrentItemView == mOpenedItemView) {
//                closeItemMenu(mOpenedItemView, mScrollDurationTime);
//            }
//            clearTouch();
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 清除状态
//     */
//    private void clearTouch() {
//        if (mVelocityTracker != null) {
//            mVelocityTracker.recycle();
//            mVelocityTracker = null;
//        }
//        resetTouch();
//    }
//
//    private void cancelParentTouch(MotionEvent e) {
//        final int action = e.getAction();
//        e.setAction(MotionEvent.ACTION_CANCEL);
//        super.onTouchEvent(e);
//        e.setAction(action);
//    }
//
//    /**
//     * 收起item菜单
//     *
//     * @param itemView     itemView
//     * @param durationTime 动画执行时间
//     */
//    private void closeItemMenu(ViewGroup itemView, int durationTime) {
//        if (itemView != null) {
//            if (durationTime > 0) {
//                smoothTranslateItemView(itemView, 0, durationTime);
//            } else {
//                cancelTranslateAnimator(itemView);
//                translateItemView(itemView, -itemView.getChildAt(0).getTranslationX());
//            }
//            if (mOpenedItemView == itemView) {
//                mOpenedItemView = null;
//            }
//        }
//    }
//
//    /**
//     * 重置状态
//     */
//    private void resetTouch() {
//        mCurrentItemView = null;
//        mCurrentItemHasOpened = false;
//        mCurrentItemBounds.setEmpty();
//        mCurrentItemMenuBounds.setEmpty();
//        mItemStartMoving = false;
//        if (mVelocityTracker != null) {
//            mVelocityTracker.clear();
//        }
//    }
//
//    /**
//     * 视图的表现形式是从右开始向左结束
//     *
//     * @param view 对应的view
//     * @return 是否是从右开始向左结束
//     */
//    private static boolean isLayoutRtl(@NonNull View view) {
//        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
//    }
//
//
//    public boolean getEnableScrollingItem() {
//        return mEnableScrollingItem;
//    }
//
//
//    public void setItemScrollingEnabled(boolean enabled) {
//        mEnableScrollingItem = enabled;
//    }
//
//
//    public int getScrollDurationTime() {
//        return mScrollDurationTime;
//    }
//
//    public void setItemScrollDuration(int durationTime) {
//        if (durationTime < 0) {
//            throw new IllegalArgumentException("The animators for opening/closing the item views " +
//                    "cannot have negative duration: " + durationTime);
//        }
//        mScrollDurationTime = durationTime;
//    }
//}
