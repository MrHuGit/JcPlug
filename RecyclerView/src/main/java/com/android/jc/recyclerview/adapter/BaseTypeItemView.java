package com.android.jc.recyclerview.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * @author Mr.Hu(Jc) JCFramework
 * @create 2019-07-01 14:57
 * @describe itemView基类
 * @update
 */
public abstract class BaseTypeItemView<D> {

    /**
     * item布局资源id
     *
     * @return 布局资源id
     */
    public abstract @LayoutRes
    int getLayoutId();

    /**
     * item布局
     *
     * @return item布局
     */
    public View getLayoutView() {
        return null;
    }



    /**
     * 绑定数据
     * @param holder viewHolder
     * @param data 数据
     * @param position position
     * @param onItemClickListener 监听
     * @param onItemLongClickListener 长按监听
     */
    void onConvert(RvViewHolder holder, @NonNull Object data, int position,
                   IRvTypeListener.OnItemClickListener<D> onItemClickListener,
                   IRvTypeListener.OnItemLongClickListener<D> onItemLongClickListener) {
        D d;
        try {
            //noinspection unchecked
            d = (D) data;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("无法将" + data.getClass().toString() + "绑定到" + this.getClass().toString());
        }
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, holder, d, position));
        }

        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> onItemLongClickListener.onItemLongClick(v, holder, d, position));
        }
        onConvertData(holder, d, position);

    }

    /**
     * 绑定数据
     *
     * @param holder   viewHolder
     * @param data     数据
     * @param position position
     */
    public abstract void onConvertData(RvViewHolder holder, @NonNull D data, int position);

    /**
     * 检测当前数据是否属于当前itemType
     *
     * @param data     数据
     * @param position position
     * @return 是否属于当前itemType
     */
    boolean checkType(@NonNull Object data, int position) {
        D d;
        try {
            //noinspection unchecked
            d = (D) data;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("无法将" + data.getClass().toString() + "绑定到" + this.getClass().toString());
        }
        return checkViewType(d, position);
    }

    /**
     * 检测当前数据是否属于当前itemType
     *
     * @param item     数据
     * @param position position
     * @return 是否属于当前itemType
     */
    public boolean checkViewType(D item, int position) {
        return true;
    }
}
