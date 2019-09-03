package com.android.jc.recyclerview.adapter;

import android.view.View;

/**
 * @author Mr.Hu(Jc) JCFramework
 * @create 2019-07-01 15:00
 * @describe RecyclerView的Adapter监听事件
 * @update
 */
public interface IRvTypeListener {


    /**
     * @param <D>
     */
    interface OnItemClickListener<D> {
        /**
         * item点击监听
         *
         * @param view     view
         * @param holder   RvViewHolder
         * @param data     数据
         * @param position position
         */
        void onItemClick(View view, RvViewHolder holder, D data, int position);
    }

    /**
     * @param <D>
     */
    interface OnItemLongClickListener<D> {
        /**
         * item长按监听
         *
         * @param view     view
         * @param holder   RvViewHolder
         * @param data     数据
         * @param position position
         * @return true if the callback consumed the long click, false otherwise
         */
        boolean onItemLongClick(View view, RvViewHolder holder, D data, int position);
    }
}
