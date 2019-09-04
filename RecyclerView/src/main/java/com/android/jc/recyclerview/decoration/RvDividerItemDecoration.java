package com.android.jc.recyclerview.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-09-03 14:03
 * @describe 仿造系统的DividerItemDecoration改造通用分割线
 * @update
 */
public class RvDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    /**
     * 自定义分割线的画笔
     */
    private Paint mPaint;

    private final Builder mBuilder;
    private final Rect mBounds = new Rect();

    public static Builder builder() {
        return new Builder();
    }

    private RvDividerItemDecoration(Context context, Builder builder) {
        mBuilder = builder;
        if (builder.dividerDrawableId != View.NO_ID) {
            mDivider = ContextCompat.getDrawable(context, builder.dividerDrawableId);
        }
        if (mDivider != null) {
            if (builder.orientation == LinearLayoutManager.VERTICAL) {
                builder.dividerDistance = mDivider.getIntrinsicHeight();
            } else {
                builder.dividerDistance = mDivider.getIntrinsicWidth();
            }
        } else {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(builder.dividerColor);
            mPaint.setStyle(Paint.Style.FILL);
        }
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter != null) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            int childPosition = parent.getChildAdapterPosition(view);
            int itemCount = adapter.getItemCount();
            if (layoutManager instanceof GridLayoutManager) {

            } else if (layoutManager instanceof LinearLayoutManager) {
                setLinearItemOffset(outRect, childPosition, itemCount);
            }

        }
    }

    private void setLinearItemOffset(Rect outRect, int childPosition, int itemCount) {
        final int itemSpace = mBuilder.dividerDistance;
        int left = 0, top = 0, right = 0, bottom = 0;
        //需要起始位置分割线的时候
        if (childPosition == 0) {
            if (mBuilder.orientation == LinearLayoutManager.HORIZONTAL) {
                right = itemSpace;
            } else {
                bottom = itemSpace;
            }
            if (mBuilder.dividerType == DividerType.START_END | mBuilder.dividerType == DividerType.START) {
                if (mBuilder.orientation == LinearLayoutManager.HORIZONTAL) {
                    left = itemSpace;
                } else {
                    top = itemSpace;
                }
            }
        }
        //需要最后一个分割线
        else if (childPosition == itemCount - 1 & (mBuilder.dividerType == DividerType.START_END | mBuilder.dividerType == DividerType.END)) {
            if (mBuilder.orientation == LinearLayoutManager.HORIZONTAL) {
                right = itemSpace;
            } else {
                bottom = itemSpace;
            }
        }
        //中间位置的分割线
        else if (childPosition < itemCount - 1 && childPosition > 0) {
            if (mBuilder.orientation == LinearLayoutManager.HORIZONTAL) {
                right = itemSpace;
            } else {
                bottom = itemSpace;
            }
        }
        outRect.set(left, top, right, bottom);
    }

    /**
     * 设置分割线的方向
     *
     * @param orientation LinearLayoutManager.HORIZONTAL|LinearLayoutManager.VERTICAL
     */
    public void setOrientation(int orientation) {
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        } else {
            this.mBuilder.orientation = orientation;
        }
    }


    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            if (this.mBuilder.orientation == LinearLayoutManager.VERTICAL) {
                this.drawVertical(c, parent);
            } else {
                this.drawHorizontal(c, parent);
            }

        }
    }

    /**
     * 绘制横向列表时的分隔线  这时分隔线是竖着的
     * l、r 变化； t、b 不变
     *
     * @param canvas Canvas
     * @param parent parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        int top;
        int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        int childCount = parent.getChildCount();
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        //绘制第一个item之前的分割线
        if (mBuilder.dividerType == DividerType.START || mBuilder.dividerType == DividerType.START_END) {
            drawDivider(canvas, 0, top, mBuilder.dividerDistance, bottom);
        }
        //绘制最后一个item之后的分割线
        if (mBuilder.dividerType == DividerType.END | mBuilder.dividerType == DividerType.START_END) {
            childCount++;
        }
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            if (layoutManager != null) {
                layoutManager.getDecoratedBoundsWithMargins(child, this.mBounds);
            }
            int right = this.mBounds.right + Math.round(child.getTranslationX());
            int left = right - mBuilder.dividerDistance;
            drawDivider(canvas, left, top, right, bottom);

        }
        canvas.restore();
    }

    /**
     * 绘制纵向列表时的分隔线  这时分隔线是横着的
     * 每次 left相同，top根据child变化，right相同，bottom也变化
     *
     * @param canvas Canvas
     * @param parent parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        int left;
        int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        int childCount = parent.getChildCount();
        //绘制第一个item之前的分割线
        if (mBuilder.dividerType == DividerType.START || mBuilder.dividerType == DividerType.START_END) {
            drawDivider(canvas, left, 0, right, mBuilder.dividerDistance);
        }
        //绘制最后一个item之后的分割线
        if (mBuilder.dividerType == DividerType.END | mBuilder.dividerType == DividerType.START_END) {
            childCount++;
        }
        //绘制中间部分的分割线
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, this.mBounds);
            int bottom = this.mBounds.bottom + Math.round(child.getTranslationY());
            int top = bottom - mBuilder.dividerDistance;
            drawDivider(canvas, left, top, right, bottom);

        }
        canvas.restore();
    }

    /**
     * 绘制分割线
     *
     * @param canvas 画布
     * @param left   left
     * @param top    top
     * @param right  right
     * @param bottom bottom
     */
    private void drawDivider(Canvas canvas, int left, int top, int right, int bottom) {
        if (mDivider != null) {
            this.mDivider.setBounds(left, top, right, bottom);
            this.mDivider.draw(canvas);
        } else if (mPaint != null) {
            if (mBuilder.orientation == LinearLayoutManager.VERTICAL) {
                canvas.drawRect(left + mBuilder.offsetLeft, top, right - mBuilder.offsetRight, bottom, mPaint);
            } else {
                canvas.drawRect(left, top + mBuilder.offsetTop, right, bottom - mBuilder.offsetBottom, mPaint);
            }
        }
    }

    public static class Builder {
        /**
         * 分割线的布局
         */
        private @DrawableRes
        int dividerDrawableId = View.NO_ID;

        /**
         * {@link LinearLayoutManager#VERTICAL}分割线的高
         * {@link LinearLayoutManager#HORIZONTAL}分割线的宽度
         */
        private int dividerDistance = 2;
        /**
         * 列表的方向
         * {@link LinearLayoutManager#VERTICAL}
         * {@link LinearLayoutManager#HORIZONTAL}
         */
        private int orientation = LinearLayoutManager.VERTICAL;
        /**
         * 间隔左端的距离
         * orientation=LinearLayoutManager#VERTICAL
         */
        private int offsetLeft;
        /**
         * 间隔右端的距离
         * orientation=LinearLayoutManager#VERTICAL
         */
        private int offsetRight;
        /**
         * 间隔顶端的距离
         * orientation=LinearLayoutManager#HORIZONTAL
         */
        private int offsetTop;
        /**
         * 间隔底部的距离
         * orientation=LinearLayoutManager#HORIZONTAL
         */
        private int offsetBottom;
        /**
         * 分割线的颜色
         */
        private @ColorInt
        int dividerColor = Color.parseColor("#FF000000");


        private DividerType dividerType = DividerType.MID;

        /**
         * 设置分割线的shape,如果设置过此Drawable,颜色，具体等参数不生效
         *
         * @param dividerDrawableId 分割线的shape
         * @return Builder
         */
        public Builder setDividerDrawableId(@DrawableRes int dividerDrawableId) {
            this.dividerDrawableId = dividerDrawableId;
            return this;
        }

        /**
         * {@link LinearLayoutManager#VERTICAL}分割线的高
         * {@link LinearLayoutManager#HORIZONTAL}分割线的宽度
         *
         * @param dividerDistance 分割线的高|分割线的宽度
         * @return Builder
         */
        public Builder setDividerDistance(int dividerDistance) {
            this.dividerDistance = dividerDistance;
            return this;
        }

        /**
         * 列表的方向
         *
         * @param orientation {@link LinearLayoutManager#VERTICAL}|{@link LinearLayoutManager#HORIZONTAL}
         * @return Builder
         */
        public Builder setOrientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        /**
         * 间隔左端的距离
         * orientation=LinearLayoutManager#VERTICAL
         *
         * @param offsetLeft 间隔左端的距离
         * @return Builder
         */
        public Builder setOffsetLeft(int offsetLeft) {
            this.offsetLeft = offsetLeft;
            return this;
        }

        /**
         * 间隔右端的距离
         * orientation=LinearLayoutManager#VERTICAL
         *
         * @param offsetRight 间隔右端的距离
         * @return Builder
         */
        public Builder setOffsetRight(int offsetRight) {
            this.offsetRight = offsetRight;
            return this;
        }

        /**
         * 间隔顶端的距离
         * orientation=LinearLayoutManager#HORIZONTAL
         *
         * @param offsetTop 间隔顶端的距离
         * @return Builder
         */
        public Builder setOffsetTop(int offsetTop) {
            this.offsetTop = offsetTop;
            return this;
        }

        /**
         * 间隔底部的距离
         * orientation=LinearLayoutManager#HORIZONTAL
         *
         * @param offsetBottom 间隔底部的距离
         * @return Builder
         */
        public Builder setOffsetBottom(int offsetBottom) {
            this.offsetBottom = offsetBottom;
            return this;
        }

        /**
         * 分割线的颜色
         *
         * @param dividerColor 分割线的颜色
         * @return Builder
         */
        public Builder setDividerColor(@ColorInt int dividerColor) {
            this.dividerColor = dividerColor;
            return this;
        }


        /**
         * 分割线添加类型
         *
         * @param dividerType 分割线添加类型
         * @return Builder
         */
        public Builder setDividerType(DividerType dividerType) {
            this.dividerType = dividerType;
            return this;
        }

        public RvDividerItemDecoration build(Context context) {
            return new RvDividerItemDecoration(context, this);
        }
    }


    public enum DividerType {
        /**
         * 只有中间才有分割线
         */
        MID,
        /**
         * 顶部包含中间
         */
        START,
        /**
         * 底部包含中间，也就是默认的
         */
        END,
        /**
         * 两端加中间都要
         */
        START_END,
    }
}
