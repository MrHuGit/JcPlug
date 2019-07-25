package com.android.jc_banner;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-04 18:29
 * @describe
 * @update
 */
public class BannerLayout extends FrameLayout {
    /**
     * 轮播时间间隔
     */
    private int playingIntervalTime;
    /**
     * 是否自动轮播
     */
    private boolean autoPlaying = true;
    /**
     * 是否在滚动
     */
    private boolean isPlaying = false;
    /**
     * 是否显示指示器
     */
    private boolean showIndicator = true;
    /**
     * 指示器间隔
     */
    private int indicatorMarginLeft;
    private int indicatorMarginRight;
    private int indicatorMarginBottom;
    /**
     * 指示器选中的Drawable
     */
    private Drawable indicatorSelectedDrawable;
    /**
     * 指示器未选中Drawable
     */
    private Drawable indicatorUnselectedDrawable;
    /**
     * 方向
     */
    private int mOrientation;
    private final Context mContext;
    /**
     * banner图片的recyclerView
     */
    private RecyclerView mImageRecyclerView;

    /**
     * 指示器RecyclerView
     */
    private RecyclerView mIndicatorRecyclerView;
    private BaseIndicatorAdapter mIndicatorAdapter;
    /**
     * 图片数量
     */
    private int mImageItemCount;
    /**
     * 当前显示的图片下标
     */
    private int mCurrentItemIndex;
    /**
     * 居中显示放大比例
     */
    private float centerScale;
    /**
     * 移动速度
     */
    private float moveSpeed;
    /**
     * image item间距
     */
    private int imageItemSpace;
    private int indicatorGravity;
    private int indicatorItemSpace;
    private final static int AUTO_PLAY_WHAT = 1;
    private BannerLayoutManager mLayoutManager;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == AUTO_PLAY_WHAT) {
                if (mCurrentItemIndex == mLayoutManager.getCurrentPosition()) {
                    ++mCurrentItemIndex;
                    mImageRecyclerView.smoothScrollToPosition(mCurrentItemIndex);
                    mHandler.sendEmptyMessageDelayed(AUTO_PLAY_WHAT, playingIntervalTime);
                    refreshIndicator();
                }
            }
            return false;
        }
    });

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            int first = mLayoutManager.getCurrentPosition();
            if (mCurrentItemIndex != first) {
                mCurrentItemIndex = first;
            }
            if (newState == SCROLL_STATE_IDLE) {
                setPlaying(true);
            }
            refreshIndicator();
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dx != 0) {
                setPlaying(false);
            }
        }
    };


    public BannerLayout(@NonNull Context context) {
        this(context, null);
    }

    public BannerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context, attrs);
    }

    /**
     * 初始化view
     *
     * @param context
     *         context
     * @param attrs
     *         attrs
     */
    private void initView(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerLayout);
        showIndicator = a.getBoolean(R.styleable.BannerLayout_showIndicator, true);
        playingIntervalTime = a.getInt(R.styleable.BannerLayout_playingIntervalTime, 2000);
        autoPlaying = a.getBoolean(R.styleable.BannerLayout_autoPlaying, true);
        indicatorSelectedDrawable = a.getDrawable(R.styleable.BannerLayout_indicatorSelectedDrawable);
        indicatorUnselectedDrawable = a.getDrawable(R.styleable.BannerLayout_indicatorUnselectedDrawable);
        indicatorGravity = a.getInt(R.styleable.BannerLayout_indicatorGravity,Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        imageItemSpace = a.getDimensionPixelSize(R.styleable.BannerLayout_imageItemSpace, ConvertUtils.dp2px(10f));
        indicatorItemSpace = a.getDimensionPixelSize(R.styleable.BannerLayout_indicatorItemSpace, ConvertUtils.dp2px(10f));
        centerScale = a.getFloat(R.styleable.BannerLayout_centerScale, 1.4f);
        moveSpeed = a.getFloat(R.styleable.BannerLayout_moveSpeed, 1.0f);
        int orientation = a.getInt(R.styleable.BannerLayout_orientation, OrientationHelper.HORIZONTAL);
        indicatorMarginLeft = a.getDimensionPixelSize(R.styleable.BannerLayout_indicatorMarginLeft, ConvertUtils.dp2px(16f));
        indicatorMarginRight = a.getDimensionPixelSize(R.styleable.BannerLayout_indicatorMarginRight, ConvertUtils.dp2px(0f));
        indicatorMarginBottom = a.getDimensionPixelSize(R.styleable.BannerLayout_indicatorMarginBottom, ConvertUtils.dp2px(11f));
        if (orientation == OrientationHelper.HORIZONTAL) {
            mOrientation = OrientationHelper.HORIZONTAL;
        } else if (orientation == OrientationHelper.VERTICAL) {
            mOrientation = OrientationHelper.VERTICAL;
        }
        a.recycle();
        initData();
        setListener();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        //轮播图部分
        mImageRecyclerView = new RecyclerView(mContext);
        LayoutParams imageRvLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mImageRecyclerView, imageRvLayoutParams);
        mLayoutManager = new BannerLayoutManager(mContext, mOrientation);
        mLayoutManager.setItemSpace(imageItemSpace);
        mLayoutManager.setCenterScale(centerScale);
        mLayoutManager.setMoveSpeed(moveSpeed);
        mImageRecyclerView.setLayoutManager(mLayoutManager);
        new CenterSnapHelper().attachToRecyclerView(mImageRecyclerView);
        //指示器部分
        mIndicatorRecyclerView = new RecyclerView(mContext);
        LinearLayoutManager indicatorLayoutManager = new LinearLayoutManager(mContext);
        indicatorLayoutManager.setOrientation(mOrientation);
        mIndicatorRecyclerView.setLayoutManager(indicatorLayoutManager);
        if (indicatorSelectedDrawable == null) {
            //绘制默认选中状态图形
            GradientDrawable selectedGradientDrawable = new GradientDrawable();
            selectedGradientDrawable.setShape(GradientDrawable.OVAL);
            selectedGradientDrawable.setColor(Color.RED);
            selectedGradientDrawable.setSize(ConvertUtils.dp2px(5f), ConvertUtils.dp2px(5f));
            selectedGradientDrawable.setCornerRadius(ConvertUtils.dp2px(5f) / 2f);
            indicatorSelectedDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
        }
        if (indicatorUnselectedDrawable == null) {
            //绘制默认未选中状态图形
            GradientDrawable unSelectedGradientDrawable = new GradientDrawable();
            unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
            unSelectedGradientDrawable.setColor(Color.GRAY);
            unSelectedGradientDrawable.setSize(ConvertUtils.dp2px(5f), ConvertUtils.dp2px(5f));
            unSelectedGradientDrawable.setCornerRadius(ConvertUtils.dp2px(5f) / 2f);
            indicatorUnselectedDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
        }

        LayoutParams indicatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        indicatorLayoutParams.gravity = indicatorGravity;
        indicatorLayoutParams.setMargins(indicatorMarginLeft, 0, indicatorMarginRight, indicatorMarginBottom);
        addView(mIndicatorRecyclerView, indicatorLayoutParams);
        if (!showIndicator) {
            mIndicatorRecyclerView.setVisibility(GONE);
        }

    }


    /**
     * 设置是否自动轮播
     *
     * @param autoPlaying
     *         是否自动轮播
     */
    public void setAutoPlaying(boolean autoPlaying) {
        this.autoPlaying = autoPlaying;
        setPlaying(this.autoPlaying);
    }

    public boolean isPlaying() {
        return isPlaying;
    }


    /**
     * 设置是否显示指示器
     *
     * @param showIndicator
     *         是否显示指示器
     */
    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
        mIndicatorRecyclerView.setVisibility(showIndicator ? VISIBLE : GONE);
    }


    /**
     * 设置当前图片缩放系数
     *
     * @param centerScale
     *         缩放系数
     */
    public void setCenterScale(float centerScale) {
        this.centerScale = centerScale;
        mLayoutManager.setCenterScale(centerScale);
    }


    /**
     * 设置跟随手指的移动速度
     *
     * @param moveSpeed
     *         移动速度
     */
    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
        mLayoutManager.setMoveSpeed(moveSpeed);
    }


    /**
     * 设置图片间距
     *
     * @param imageItemSpace
     *         图片间距
     */
    public void setImageItemSpace(int imageItemSpace) {
        this.imageItemSpace = imageItemSpace;
        mLayoutManager.setItemSpace(imageItemSpace);
    }

    /**
     * 设置轮播间隔时间
     *
     * @param playingIntervalTime
     *         时间毫秒
     */
    public void setAutoPlayingTime(int playingIntervalTime) {
        this.playingIntervalTime = playingIntervalTime;
    }

    /**
     * 设置方向
     *
     * @param orientation
     *         方向{@link OrientationHelper#HORIZONTAL} or {@link OrientationHelper#VERTICAL}
     */
    public void setOrientation(int orientation) {
        mLayoutManager.setOrientation(orientation);
    }

    /**
     * 设置是否自动播放（上锁）
     *
     * @param playing
     *         开始播放
     */
    private synchronized void setPlaying(boolean playing) {
        if (autoPlaying && mImageItemCount > 1) {
            if (!isPlaying && playing) {
                mHandler.sendEmptyMessageDelayed(AUTO_PLAY_WHAT, playingIntervalTime);
                isPlaying = true;
            } else if (isPlaying && !playing) {
                mHandler.removeMessages(AUTO_PLAY_WHAT);
                isPlaying = false;
            }
        }
    }

    /**
     * 改变导航的指示点
     */
    private synchronized void refreshIndicator() {
        if (showIndicator && mImageItemCount > 1) {
            mIndicatorAdapter.setCurrentPosition(mCurrentItemIndex % mImageItemCount).notifyDataSetChanged();
        }
    }

    /**
     * 设置轮播数据集
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        setAdapter(adapter, new IndicatorAdapter(mContext));


    }

    public void setAdapter(@NonNull RecyclerView.Adapter imageAdapter, @NonNull BaseIndicatorAdapter indicatorAdapter) {
        mImageRecyclerView.removeOnScrollListener(mOnScrollListener);
        mImageRecyclerView.setAdapter(imageAdapter);
        mImageItemCount = imageAdapter.getItemCount();
        mLayoutManager.setInfinite(mImageItemCount >= 3);
        setPlaying(true);
        mImageRecyclerView.addOnScrollListener(mOnScrollListener);

        mIndicatorAdapter = indicatorAdapter;
        IndicatorData indicatorData = new IndicatorData(indicatorSelectedDrawable, indicatorUnselectedDrawable, mImageItemCount,indicatorItemSpace);
        mIndicatorAdapter.setIndicatorData(indicatorData);
        mIndicatorRecyclerView.setAdapter(mIndicatorAdapter);
        mIndicatorAdapter.notifyDataSetChanged();
    }

    /**
     * 修改数据后的更新
     */
    public void notifyDataSetChanged() {
        RecyclerView.Adapter imageAdapter = mImageRecyclerView.getAdapter();
        if (imageAdapter != null) {
            mImageItemCount = imageAdapter.getItemCount();
            mLayoutManager.setInfinite(mImageItemCount >= 3);
            setPlaying(true);
            IndicatorData indicatorData = new IndicatorData(indicatorSelectedDrawable, indicatorUnselectedDrawable, mImageItemCount,indicatorItemSpace);
            mIndicatorAdapter.setIndicatorData(indicatorData);
            mIndicatorAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPlaying(false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPlaying(true);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPlaying(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setPlaying(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            setPlaying(true);
        } else {
            setPlaying(false);
        }
    }


    /**
     * 设置监听
     */
    private void setListener() {

    }
}
