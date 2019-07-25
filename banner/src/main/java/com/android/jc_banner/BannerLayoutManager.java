package com.android.jc_banner;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-04 20:41
 * @describe
 * @update
 */
public class BannerLayoutManager extends RecyclerView.LayoutManager {

  static final int DETERMINE_BY_MAX_AND_MIN = -1;

  static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

  static final int VERTICAL = OrientationHelper.VERTICAL;

  private static final int DIRECTION_NO_WHERE = -1;

  private static final int DIRECTION_FORWARD = 0;

  private static final int DIRECTION_BACKWARD = 1;

  static final int INVALID_SIZE = Integer.MAX_VALUE;

  private SparseArray<View> positionCache = new SparseArray<>();

  private int mDecoratedMeasurement;

  private int mDecoratedMeasurementInOther;

  /**
   * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  int mOrientation;

  private int mSpaceMain;

  private int mSpaceInOther;


  /**
   * 滚动后改变的偏移量
   */
  float mOffset;


  private OrientationHelper mOrientationHelper;

  /**
   * Defines if layout should be calculated from end to start.
   */
  private boolean mReverseLayout = false;


  /**
   * 是否应该反向布局
   */
  private boolean mShouldReverseLayout = false;

  /**
   * Works the same way as {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}.
   * see {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}
   */
  private boolean mSmoothScrollbarEnabled = true;

  /**
   * When LayoutManager needs to scroll to a position, it sets this variable and requests a layout
   * which will check this variable and re-layout accordingly.
   */
  private int mPendingScrollPosition = NO_POSITION;

  private RecyclerViewState mPendingSavedState = null;
  /**
   * 每个item的间隔偏移量
   */
  float mInterval;

  /* package */ OnPageChangeListener onPageChangeListener;

  private boolean mRecycleChildrenOnDetach;
  /**
   * 无限滚动
   */
  private boolean mInfinite = true;

  private int mLeftItems;

  private int mRightItems;


  /**
   * 最多显示的数量
   */
  private int mMaxVisibleItemCount = 5;

  private Interpolator mSmoothScrollInterpolator;

  private int mDistanceToBottom = INVALID_SIZE;

  /**
   * use for handle focus
   */
  private View currentFocusView;


  /**
   * 每个图片之间间距
   */
  private int itemSpace = 20;
  /**
   * 中间显示的图片放大比例
   */
  private float centerScale = 1.2f;
  private float moveSpeed = 1.0f;

  BannerLayoutManager(Context context) {
    this(context, HORIZONTAL);
  }

  /**
   * @param orientation Layout orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  BannerLayoutManager(Context context, int orientation) {
    setOrientation(orientation);
    setAutoMeasureEnabled(true);
    setItemPrefetchEnabled(false);
  }


  @Override
  public void onLayoutCompleted(RecyclerView.State state) {
    super.onLayoutCompleted(state);
    mPendingSavedState = null;
    mPendingScrollPosition = NO_POSITION;
  }

  @Override
  public boolean onAddFocusables(@NonNull RecyclerView recyclerView, @NonNull ArrayList<View> views,
      int direction, int focusableMode) {
    final int currentPosition = getCurrentPosition();
    final View currentView = findViewByPosition(currentPosition);
    if (currentView == null) {
      return true;
    }
    if (recyclerView.hasFocus()) {
      final int movement = getMovement(direction);
      if (movement != DIRECTION_NO_WHERE) {
        final int targetPosition = movement == DIRECTION_BACKWARD ?
            currentPosition - 1 : currentPosition + 1;
        recyclerView.smoothScrollToPosition(targetPosition);
      }
    } else {
      currentView.addFocusables(views, direction, focusableMode);
    }
    return true;
  }


  /**
   * 这个方法的作用主要是给RecyclerView的ItemView生成LayoutParams
   *
   * @return RecyclerView.LayoutParams
   */
  @Override
  public RecyclerView.LayoutParams generateDefaultLayoutParams() {
    return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
  }


  @Override
  public boolean canScrollHorizontally() {
    return mOrientation == HORIZONTAL;
  }


  @Override
  public boolean canScrollVertically() {
    return mOrientation == VERTICAL;
  }

  /**
   * 回复状态
   *
   * @param state 状态
   */
  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof RecyclerViewState) {
      mPendingSavedState = new RecyclerViewState((RecyclerViewState) state);
      requestLayout();
    }
  }

  /**
   * 保存状态
   *
   * @return 状态
   */
  @Override
  public Parcelable onSaveInstanceState() {
    if (mPendingSavedState != null) {
      return new RecyclerViewState(mPendingSavedState);
    }
    RecyclerViewState savedState = new RecyclerViewState();
    savedState.setPosition(mPendingScrollPosition);
    savedState.setOffset(mOffset);
    savedState.setReverseLayout(mShouldReverseLayout);
    return savedState;
  }

  @Override
  public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
    super.onDetachedFromWindow(view, recycler);
    if (mRecycleChildrenOnDetach) {
      removeAndRecycleAllViews(recycler);
      recycler.clear();
    }
  }


  float getDistanceRatio() {
    if (moveSpeed == 0) {
      return Float.MAX_VALUE;
    }
    return 1 / moveSpeed;
  }


  /**
   *
   */
  private float getInterval() {
    //返回view在水平方向上所占位置的大小（包括view的左右外边距）
    return mDecoratedMeasurement * ((centerScale - 1) / 2 + 1) + itemSpace;
  }

  void setItemSpace(int itemSpace) {
    this.itemSpace = itemSpace;
  }

  /**
   * 设置当前显示的item的放大比列
   *
   * @param centerScale 放大比列
   */
  void setCenterScale(float centerScale) {
    this.centerScale = centerScale;
  }

  /**
   * 跟随手指的移动速度
   *
   * @param moveSpeed 移动速度
   */
  void setMoveSpeed(float moveSpeed) {
    assertNotInLayoutOrScroll(null);
    if (this.moveSpeed == moveSpeed) {
      return;
    }
    this.moveSpeed = moveSpeed;
  }

  private void setItemViewProperty(View itemView, float targetOffset) {
    float scale = calculateScale(targetOffset + mSpaceMain);
    itemView.setScaleX(scale);
    itemView.setScaleY(scale);
  }

  /**
   * @param x start positon of the view you want scale
   * @return the scale rate of current scroll mOffset
   */
  private float calculateScale(float x) {
    //返回RecycleView水平内容区空间大小（宽度，除去左右内边距）
    float deltaX = Math.abs(x - (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2f);
    float diff = 0f;
    if ((mDecoratedMeasurement - deltaX) > 0) {
      diff = mDecoratedMeasurement - deltaX;
    }

    return (centerScale - 1f) / mDecoratedMeasurement * diff + 1;
  }

  /**
   * cause elevation is not support below api 21, so you can set your elevation here for supporting
   * it below api 21 or you can just setElevation in
   */
  protected float setViewElevation(View itemView, float targetOffset) {
    return 0;
  }


  /**
   * Returns whether LayoutManager will recycle its children when it is detached from RecyclerView.
   *
   * @return true if LayoutManager will recycle its children when it is detached from RecyclerView.
   */
  public boolean getRecycleChildrenOnDetach() {
    return mRecycleChildrenOnDetach;
  }

  /**
   * Set whether LayoutManager will recycle its children when it is detached from RecyclerView.
   * <p>
   * If you are using a {@link RecyclerView.RecycledViewPool}, it might be a good idea to set this
   * flag to <code>true</code> so that views will be available to other RecyclerViews immediately.
   * <p>
   * Note that, setting this flag will result in a performance drop if RecyclerView is restored.
   *
   * @param recycleChildrenOnDetach Whether children should be recycled in detach or not.
   */
  public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
    mRecycleChildrenOnDetach = recycleChildrenOnDetach;
  }


  /**
   * Returns the current orientation of the layout.
   *
   * @return Current orientation,  either {@link #HORIZONTAL} or {@link #VERTICAL}
   * @see #setOrientation(int)
   */
  public int getOrientation() {
    return mOrientation;
  }

  /**

   *
   * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  public void setOrientation(int orientation) {
    if (orientation != HORIZONTAL && orientation != VERTICAL) {
      throw new IllegalArgumentException("invalid orientation:" + orientation);
    }
    assertNotInLayoutOrScroll(null);
    if (orientation == mOrientation) {
      return;
    }
    mOrientation = orientation;
    mOrientationHelper = null;
    mDistanceToBottom = INVALID_SIZE;
    removeAllViews();
  }

  /**
   * Returns the max visible item count, {@link #DETERMINE_BY_MAX_AND_MIN} means it haven't been set
   * now And it will use {@link #maxRemoveOffset()} and {@link #minRemoveOffset()} to handle the
   * range
   *
   * @return Max visible item count
   */
  public int getMaxVisibleItemCount() {
    return mMaxVisibleItemCount;
  }


  boolean getReverseLayout() {
    return mReverseLayout;
  }

  /**
   * Used to reverse item traversal and layout order. This behaves similar to the layout change for
   * RTL views. When set to true, first item is laid out at the end of the UI, second item is laid
   * out before it etc.
   * <p>
   * For horizontal layouts, it depends on the layout direction. When set to true, If {@link
   * android.support.v7.widget.RecyclerView} is LTR, than it will layout from RTL, if {@link
   * android.support.v7.widget.RecyclerView}} is RTL, it will layout from LTR.
   */
  public void setReverseLayout(boolean reverseLayout) {
    assertNotInLayoutOrScroll(null);
    if (reverseLayout == mReverseLayout) {
      return;
    }
    mReverseLayout = reverseLayout;
    removeAllViews();
  }

  public void setSmoothScrollInterpolator(Interpolator smoothScrollInterpolator) {
    this.mSmoothScrollInterpolator = smoothScrollInterpolator;
  }

  @Override
  public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
      int position) {
    final int offsetPosition = getOffsetToPosition(position);
    if (mOrientation == VERTICAL) {
      recyclerView.smoothScrollBy(0, offsetPosition, mSmoothScrollInterpolator);
    } else {
      recyclerView.smoothScrollBy(offsetPosition, 0, mSmoothScrollInterpolator);
    }
  }

  @Override
  public void scrollToPosition(int position) {
    if (!mInfinite && (position < 0 || position >= getItemCount())) {
      return;
    }
    mPendingScrollPosition = position;
    mOffset = mShouldReverseLayout ? position * -mInterval : position * mInterval;
    requestLayout();
  }

  /**
   * 相当于ViewGroup的 onLayout.一开始的界面构建就是这个入口 是 LayoutManager 的主入口。 它会在初始化布局时调用，
   * 当适配器的数据改变时(或者整个适配器被换掉时)会再次调用。 它的作用就是在初始化的时候放置item，直到填满布局为止。 1 在RecyclerView初始化时，会被调用两次。 2
   * 在调用adapter.notifyDataSetChanged()时，会被调用。 3 在调用setAdapter替换Adapter时,会被调用。 4
   * 在RecyclerView执行动画时，它也会被调用。
   * <p>
   * 获取对应位置的子view 添加进RecyclerView 测量子View的宽高 根据测量的宽高,给他们排列好位置
   *
   * @param recycler recycler
   * @param state state
   */
  @Override
  public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (state.getItemCount() == 0) {
      //移除回收当前的View
      removeAndRecycleAllViews(recycler);
      mOffset = 0;
      return;
    }
    ensureLayoutState();
    resolveShouldLayoutReverse();
    View scrap = recycler.getViewForPosition(0);
    measureChildWithMargins(scrap, 0, 0);
    //返回view在水平方向上()所占位置的大小（包括view的左右外边距）
    mDecoratedMeasurement = mOrientationHelper.getDecoratedMeasurement(scrap);
     //返回view在竖直方向上所占位置的大小（包括view的上下外边距）
    mDecoratedMeasurementInOther = mOrientationHelper.getDecoratedMeasurementInOther(scrap);
    //返回RecycleView水平(垂直)内容区空间大小（宽度，除去左右内边距）
    //根据RecyclerView宽度（高度）-当前显示的View的宽度（高度）/2 =剩余显示的item的宽度（高度）
    mSpaceMain = (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2;
    if (mDistanceToBottom == INVALID_SIZE) {
      mSpaceInOther = (getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2;
    } else {
      mSpaceInOther = getTotalSpaceInOther() - mDecoratedMeasurementInOther - mDistanceToBottom;
    }
    mInterval = getInterval();
    setUp();
    mLeftItems = (int) Math.abs(minRemoveOffset() / mInterval) + 1;
    mRightItems = (int) Math.abs(maxRemoveOffset() / mInterval) + 1;

    if (mPendingSavedState != null) {
      mShouldReverseLayout = mPendingSavedState.isReverseLayout();
      mPendingScrollPosition = mPendingSavedState.getPosition();
      mOffset = mPendingSavedState.getOffset();
    }
    if (mPendingScrollPosition != NO_POSITION) {
      mOffset = mShouldReverseLayout ?
          mPendingScrollPosition * -mInterval : mPendingScrollPosition * mInterval;
    }
    detachAndScrapAttachedViews(recycler);
    layoutItems(recycler);
  }

  private void ensureLayoutState() {
    if (mOrientationHelper == null) {
      mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
    }
  }


  /**
   * 轮播的时候如果是到第一个或者是最后一个需要翻转
   */
  private void resolveShouldLayoutReverse() {
    if (mOrientation == HORIZONTAL && getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
      mReverseLayout = !mReverseLayout;
    }
  }

  public int getTotalSpaceInOther() {
    if (mOrientation == HORIZONTAL) {
      return getHeight() - getPaddingTop()
          - getPaddingBottom();
    } else {
      return getWidth() - getPaddingLeft()
          - getPaddingRight();
    }
  }


  private int getMovement(int direction) {
    if (mOrientation == VERTICAL) {
      if (direction == View.FOCUS_UP) {
        return mShouldReverseLayout ? DIRECTION_FORWARD : DIRECTION_BACKWARD;
      } else if (direction == View.FOCUS_DOWN) {
        return mShouldReverseLayout ? DIRECTION_BACKWARD : DIRECTION_FORWARD;
      } else {
        return DIRECTION_NO_WHERE;
      }
    } else {
      if (direction == View.FOCUS_LEFT) {
        return mShouldReverseLayout ? DIRECTION_FORWARD : DIRECTION_BACKWARD;
      } else if (direction == View.FOCUS_RIGHT) {
        return mShouldReverseLayout ? DIRECTION_BACKWARD : DIRECTION_FORWARD;
      } else {
        return DIRECTION_NO_WHERE;
      }
    }
  }


  /**
   * You can set up your own properties here or change the exist properties like mSpaceMain and
   * mSpaceInOther
   */
  protected void setUp() {

  }

  private float getProperty(int position) {
    return mShouldReverseLayout ? position * -mInterval : position * mInterval;
  }

  @Override
  public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
    removeAllViews();
    mOffset = 0;
  }


  @Override
  public int computeHorizontalScrollOffset(@NonNull RecyclerView.State state) {
    return computeScrollOffset();
  }

  @Override
  public int computeVerticalScrollOffset(@NonNull RecyclerView.State state) {
    return computeScrollOffset();
  }

  @Override
  public int computeHorizontalScrollExtent(@NonNull RecyclerView.State state) {
    return computeScrollExtent();
  }

  @Override
  public int computeVerticalScrollExtent(@NonNull RecyclerView.State state) {
    return computeScrollExtent();
  }

  @Override
  public int computeHorizontalScrollRange(@NonNull RecyclerView.State state) {
    return computeScrollRange();
  }

  @Override
  public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
    return computeScrollRange();
  }

  /**
   * 在这里实现滚动的逻辑。RecyclerView 已经处理了触摸事件的那些事情， 当你上下左右滑动的时候scrollHorizontallyBy() &
   * scrollVerticallyBy()会传入此时的位移偏移量dy（或者dx）， 根据这个dy你需要完成下面这三个任务： 将所有的子视图移动适当的位置 (对的，你得自己做这个)。
   * 决定移动视图后 添加/移除 视图。 返回滚动的实际距离。框架会根据它判断你是否触碰到边界。
   *
   * @param dx 位移偏移量
   * @param recycler recycler
   * @param state state
   * @return 滚动的实际距离
   */
  @Override
  public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
      RecyclerView.State state) {
    if (mOrientation == VERTICAL) {
      return 0;
    }
    return scrollBy(dx, recycler, state);
  }

  /**
   * 纵向滑动调用此方法
   *
   * @param dy 位移偏移量
   * @param recycler recycler
   * @param state state
   * @return 滚动的实际距离
   */
  @Override
  public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (mOrientation == HORIZONTAL) {
      return 0;
    }
    return scrollBy(dy, recycler, state);
  }

  /**
   * 滑动处理
   *
   * @param dy 位移偏移量
   * @param recycler recycler
   * @param state state
   * @return 滚动的实际距离
   */
  private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (getChildCount() == 0 || dy == 0) {
      return 0;
    }
    ensureLayoutState();
    int willScroll = dy;

    float realDx = dy / getDistanceRatio();
    if (Math.abs(realDx) < 0.00000001f) {
      return 0;
    }
    float targetOffset = mOffset + realDx;

    //handle the boundary
    if (!mInfinite && targetOffset < getMinOffset()) {
      willScroll -= (targetOffset - getMinOffset()) * getDistanceRatio();
    } else if (!mInfinite && targetOffset > getMaxOffset()) {
      willScroll = (int) ((getMaxOffset() - mOffset) * getDistanceRatio());
    }

    realDx = willScroll / getDistanceRatio();

    mOffset += realDx;

    layoutItems(recycler);

    return willScroll;
  }

  @Override
  public View findViewByPosition(int position) {
    final int itemCount = getItemCount();
    if (itemCount == 0) {
      return null;
    }
    for (int i = 0; i < positionCache.size(); i++) {
      final int key = positionCache.keyAt(i);
      if (key >= 0) {
        if (position == key % itemCount) {
          return positionCache.valueAt(i);
        }
      } else {
        int delta = key % itemCount;
        if (delta == 0) {
          delta = -itemCount;
        }
        if (itemCount + delta == position) {
          return positionCache.valueAt(i);
        }
      }
    }
    return null;
  }

  private int computeScrollOffset() {
    if (getChildCount() == 0) {
      return 0;
    }

    if (!mSmoothScrollbarEnabled) {
      return !mShouldReverseLayout ?
          getCurrentPosition() : getItemCount() - getCurrentPosition() - 1;
    }

    final float realOffset = getOffsetOfRightAdapterPosition();
    return !mShouldReverseLayout ? (int) realOffset
        : (int) ((getItemCount() - 1) * mInterval + realOffset);
  }

  private int computeScrollExtent() {
    if (getChildCount() == 0) {
      return 0;
    }

    if (!mSmoothScrollbarEnabled) {
      return 1;
    }

    return (int) mInterval;
  }

  private int computeScrollRange() {
    if (getChildCount() == 0) {
      return 0;
    }

    if (!mSmoothScrollbarEnabled) {
      return getItemCount();
    }

    return (int) (getItemCount() * mInterval);
  }


  /**
   * @param recycler recycler
   */
  private void layoutItems(RecyclerView.Recycler recycler) {
    detachAndScrapAttachedViews(recycler);
    positionCache.clear();
    final int itemCount = getItemCount();
    if (itemCount == 0) {
      return;
    }
    final int currentPos = mShouldReverseLayout ?
        -getCurrentPositionOffset() : getCurrentPositionOffset();
    int start = currentPos - mLeftItems;
    int end = currentPos + mRightItems;
    if (useMaxVisibleCount()) {
      boolean isEven = mMaxVisibleItemCount % 2 == 0;
      if (isEven) {
        int offset = mMaxVisibleItemCount / 2;
        start = currentPos - offset + 1;
        end = currentPos + offset + 1;
      } else {
        int offset = (mMaxVisibleItemCount - 1) / 2;
        start = currentPos - offset;
        end = currentPos + offset + 1;
      }
    }

    //不无限滚动的话，从0开始，结束为item数量
    if (!mInfinite) {
      if (start < 0) {
        start = 0;
        if (useMaxVisibleCount()) {
          end = mMaxVisibleItemCount;
        }
      }
      if (end > itemCount) {
        end = itemCount;
      }
    }

    float lastOrderWeight = Float.MIN_VALUE;
    for (int i = start; i < end; i++) {
      if (useMaxVisibleCount() || !removeCondition(getProperty(i) - mOffset)) {
        int adapterPosition = i;
        if (i >= itemCount) {
          adapterPosition %= itemCount;
        } else if (i < 0) {
          int delta = (-adapterPosition) % itemCount;
          if (delta == 0) {
            delta = itemCount;
          }
          adapterPosition = itemCount - delta;
        }
        final View scrap = recycler.getViewForPosition(adapterPosition);
        measureChildWithMargins(scrap, 0, 0);
        resetViewProperty(scrap);
        final float targetOffset = getProperty(i) - mOffset;
        layoutScrap(scrap, targetOffset);
        final float orderWeight = setViewElevation(scrap, targetOffset);
        if (orderWeight > lastOrderWeight) {
          addView(scrap);
        } else {
          addView(scrap, 0);
        }
        if (i == currentPos) {
          currentFocusView = scrap;
        }
        lastOrderWeight = orderWeight;
        positionCache.put(i, scrap);
      }
    }

    currentFocusView.requestFocus();
  }

  /**
   * 是否移除
   *
   * @param targetOffset 偏移量
   * @return 是否移除
   */
  private boolean removeCondition(float targetOffset) {
    return targetOffset > maxRemoveOffset() || targetOffset < minRemoveOffset();
  }

  /**
   * 最大移除偏移量，如果实际偏移量大于此值时，视图将被删除并走{@link #layoutItems(Recycler)}
   */
  private float maxRemoveOffset() {
    return mOrientationHelper.getTotalSpace() - mSpaceMain;
  }


  /**
   * 最小移除偏移量，如果实际偏移量小于此值时，视图将被删除并走{@link #layoutItems(Recycler)}
   */
  private float minRemoveOffset() {
    //获取RecycleView左侧内边距（paddingLeft）
    return -mDecoratedMeasurement - mOrientationHelper.getStartAfterPadding() - mSpaceMain;
  }

  private boolean useMaxVisibleCount() {
    return mMaxVisibleItemCount != DETERMINE_BY_MAX_AND_MIN;
  }


  private void resetViewProperty(View v) {
    v.setRotation(0);
    v.setRotationY(0);
    v.setRotationX(0);
    v.setScaleX(1f);
    v.setScaleY(1f);
    v.setAlpha(1f);
  }

  float getMaxOffset() {
    return !mShouldReverseLayout ? (getItemCount() - 1) * mInterval : 0;
  }

  float getMinOffset() {
    return !mShouldReverseLayout ? 0 : -(getItemCount() - 1) * mInterval;
  }

  private void layoutScrap(View scrap, float targetOffset) {
    final int left = calItemLeft(scrap, targetOffset);
    final int top = calItemTop(scrap, targetOffset);
    if (mOrientation == VERTICAL) {
      layoutDecorated(scrap, mSpaceInOther + left, mSpaceMain + top,
          mSpaceInOther + left + mDecoratedMeasurementInOther,
          mSpaceMain + top + mDecoratedMeasurement);
    } else {
      layoutDecorated(scrap, mSpaceMain + left, mSpaceInOther + top,
          mSpaceMain + left + mDecoratedMeasurement,
          mSpaceInOther + top + mDecoratedMeasurementInOther);
    }
    setItemViewProperty(scrap, targetOffset);
  }

  private int calItemLeft(View itemView, float targetOffset) {
    return mOrientation == VERTICAL ? 0 : (int) targetOffset;
  }

  private int calItemTop(View itemView, float targetOffset) {
    return mOrientation == VERTICAL ? (int) targetOffset : 0;
  }


  int getCurrentPosition() {
    if (getItemCount() == 0) {
      return 0;
    }
    int position = getCurrentPositionOffset();
    if (!mInfinite) {
      return Math.abs(position);
    }
    position = !mShouldReverseLayout ?
        (position >= 0 ?
            position % getItemCount() :
            getItemCount() + position % getItemCount()) :
        (position > 0 ?
            getItemCount() - position % getItemCount() :
            -position % getItemCount());
    return position == getItemCount() ? 0 : position;
  }


  private int getCurrentPositionOffset() {
    return Math.round(mOffset / mInterval);
  }

  /**
   * Sometimes we need to get the right offset of matching adapter position cause when {@link
   * #mInfinite} is set true, there will be no limitation of {@link #mOffset}
   */
  private float getOffsetOfRightAdapterPosition() {
    if (mShouldReverseLayout) {
      return mInfinite ?
          (mOffset <= 0 ?
              (mOffset % (mInterval * getItemCount())) :
              (getItemCount() * -mInterval + mOffset % (mInterval * getItemCount()))) :
          mOffset;
    } else {
      return mInfinite ?
          (mOffset >= 0 ?
              (mOffset % (mInterval * getItemCount())) :
              (getItemCount() * mInterval + mOffset % (mInterval * getItemCount()))) :
          mOffset;
    }
  }


  /**
   * 获取距离当前显示的item的偏移量
   *
   * @return 距离当前显示的item的偏移量
   */
  int getOffsetToCenter() {
    if (mInfinite) {
      return (int) ((getCurrentPositionOffset() * mInterval - mOffset) * getDistanceRatio());
    }
    return (int) ((getCurrentPosition() *
        (!mShouldReverseLayout ? mInterval : -mInterval) - mOffset) * getDistanceRatio());
  }

  private int getOffsetToPosition(int position) {
    if (mInfinite) {
      return (int) (((getCurrentPositionOffset() +
          (!mShouldReverseLayout ? position - getCurrentPosition()
              : getCurrentPosition() - position)) *
          mInterval - mOffset) * getDistanceRatio());
    }
    return (int) ((position *
        (!mShouldReverseLayout ? mInterval : -mInterval) - mOffset) * getDistanceRatio());
  }

  public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
    this.onPageChangeListener = onPageChangeListener;
  }

  /**
   * 设置是否无限播放
   *
   * @param enable 是否无限播放
   */
  void setInfinite(boolean enable) {
    assertNotInLayoutOrScroll(null);
    if (enable == mInfinite) {
      return;
    }
    mInfinite = enable;
    requestLayout();
  }

  /**
   * 获取无限播放状态
   *
   * @return 无限播放状态
   */
  boolean getInfinite() {
    return mInfinite;
  }

  public int getDistanceToBottom() {
    return mDistanceToBottom == INVALID_SIZE ?
        (getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2 : mDistanceToBottom;
  }

  public void setDistanceToBottom(int mDistanceToBottom) {
    assertNotInLayoutOrScroll(null);
    if (this.mDistanceToBottom == mDistanceToBottom) {
      return;
    }
    this.mDistanceToBottom = mDistanceToBottom;
    removeAllViews();
  }


  public void setSmoothScrollbarEnabled(boolean enabled) {
    mSmoothScrollbarEnabled = enabled;
  }


  /**
   * Returns the current state of the smooth scrollbar feature. It is enabled by default.
   *
   * @return True if smooth scrollbar is enabled, false otherwise.
   * @see #setSmoothScrollbarEnabled(boolean)
   */
  public boolean getSmoothScrollbarEnabled() {
    return mSmoothScrollbarEnabled;
  }


  public interface OnPageChangeListener {

    void onPageSelected(int position);

    void onPageScrollStateChanged(int state);
  }
}
