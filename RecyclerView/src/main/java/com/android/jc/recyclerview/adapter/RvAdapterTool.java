package com.android.jc.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import java.util.List;

/**
 * @author Mr.Hu(Jc) EXX_Android
 * @create 2019-07-10 16:13
 * @describe 通用RecyclerView的adapter的工具类
 * @update
 */
class RvAdapterTool {
    /**
     * 外层不同itemType,内层为注册相同的itemType,交给上层去区分
     * {@link BaseTypeItemView#checkViewType(Object, int)}
     */
    final SparseArrayCompat<SparseArrayCompat<BaseTypeItemView<?>>> mItemTypeArray;
    /**
     * 数据map,key为itemType
     */
    final SparseArrayCompat<List<?>> mDataArray;
    /**
     * item单击监听集合 key为itemType
     */
    final SparseArrayCompat<IRvTypeListener.OnItemClickListener> mItemClickListeners;
    /**
     * item长按监听集合 key为itemType
     */
    final SparseArrayCompat<IRvTypeListener.OnItemLongClickListener> mItemLongClickListeners;

    /**
     * 临时保存itemType对应布局数据
     */
    private final SparseArrayCompat<BaseTypeItemView<?>> itemTypeArrayList = new SparseArrayCompat<>();

    RvAdapterTool() {
        mItemTypeArray = new SparseArrayCompat<>();
        mDataArray = new SparseArrayCompat<>();
        mItemClickListeners = new SparseArrayCompat<>();
        mItemLongClickListeners = new SparseArrayCompat<>();
    }

    /**
     * @param itemViewType
     *         itemType
     * @param itemView
     *         itemView
     * @param canRepeated
     *         是否允许重复注册同一类型itemType
     * @param <D>
     *         数据类型
     */
    <D> void registerItemType(int itemViewType, BaseTypeItemView<D> itemView, boolean canRepeated) {
        if (mItemTypeArray.indexOfKey(itemViewType) < 0) {
            SparseArrayCompat<BaseTypeItemView<?>> itemViewTypeArray = new SparseArrayCompat<>();
            addItemViewType(itemViewTypeArray, itemView, itemViewType);
            mItemTypeArray.put(itemViewType, itemViewTypeArray);
        } else if (canRepeated) {
            addItemViewType(mItemTypeArray.get(itemViewType), itemView, itemViewType);
        } else {
            throw new RuntimeException("重复注册itemViewType=" + itemViewType);
        }
    }

    /**
     * 通过itemViewType获取对应的BaseTypeItemView
     * @param itemViewType itemType
     * @return BaseTypeItemView
     */
    BaseTypeItemView<?> getItemView(int itemViewType) {
        return itemTypeArrayList.get(itemViewType);
    }

    /**
     * 注册同一类型itemType的时候添加到一起
     * 一般出现这种情况是因为数据类型相同,需要展示不同的样式
     *
     * @param typeArray
     *         同一类型itemType itemView集合
     * @param itemView
     *         itemView
     */
    private static void addItemViewType(SparseArrayCompat<BaseTypeItemView<?>> typeArray, BaseTypeItemView<?> itemView, int itemViewType) {
        if (typeArray == null || itemView == null) {
            return;
        }
        typeArray.put(itemViewType * 10 + typeArray.size(), itemView);
    }


    /**
     * 如果注册了同一类型itemType的多itemView,具体区分要根据上层用户itemView.checkType(data, position)区分
     * {@link BaseTypeItemView#checkType(Object, int)}
     *
     * @param typeArray
     *         同一类型itemType itemView集合
     * @param data
     *         当前数据
     * @param position
     *         index
     *
     * @return 返回itemType
     */
    private int getItemViewType(SparseArrayCompat<BaseTypeItemView<?>> typeArray, Object data, int position) {
        int typeArraySize = typeArray.size();
        for (int index = typeArraySize - 1; index >= 0; index--) {
            BaseTypeItemView<?> itemView = typeArray.valueAt(index);
            if (itemView.checkType(data, position)) {
                int itemType = typeArray.keyAt(index);
                itemTypeArrayList.put(itemType, typeArray.get(itemType));
                return itemType;
            }
        }
        throw new IllegalArgumentException(
                "No RvItemView added that matches position=" + position + " in data source");

    }

    /**
     * 获取item的viewType
     *
     * @param position
     *         下标
     *
     * @return viewType
     */
    int getItemViewType(int position) {
        int dataListCount = mDataArray.size();
        int itemCount = 0;
        for (int i = dataListCount - 1; i >= 0; i--) {
            List<?> list = mDataArray.valueAt(i);
            itemCount += list.size();
            //如果当前下标在当前过滤的所有list长度以内
            if (position < itemCount) {
                int itemType = mDataArray.keyAt(i);
                SparseArrayCompat<BaseTypeItemView<?>> typeArray = mItemTypeArray.get(itemType);
                if (typeArray == null) {
                    throw new RuntimeException("未添加对应itemType类型的布局，itemType=" + itemType);
                }
                //如果添加的itemType数据只有一种类型，直接返回
                else if (typeArray.size() <= 1) {
                    itemTypeArrayList.put(itemType, typeArray.get(typeArray.keyAt(0)));
                    return itemType;
                } else {
                    return getItemViewType(typeArray, list.get(position - (itemCount - list.size())), position);
                }
            }
        }

        throw new IllegalArgumentException(
                "No RvItemView added that matches position=" + position + " in data source");
    }

    void onConvert(@NonNull RvViewHolder holder, int position) {
        int dataListCount = mDataArray.size();
        int itemCount = 0;
        for (int i = dataListCount - 1; i >= 0; i--) {
            List<?> list = mDataArray.valueAt(i);
            itemCount += list.size();
            //如果当前下标在当前过滤的所有list长度以内
            if (position < itemCount) {
                int itemType = mDataArray.keyAt(i);
                SparseArrayCompat<BaseTypeItemView<?>> typeArray = mItemTypeArray.get(itemType);
                if (typeArray != null) {
                    onConvert(typeArray, itemType, list.get(position - (itemCount - list.size())), position, holder);
                    return;
                }
            }
        }


        throw new IllegalArgumentException(
                "No RvItemView added that matches position=" + position + " in data source");
    }

    private void onConvert(SparseArrayCompat<BaseTypeItemView<?>> typeArray, int itemType, @NonNull Object data, int position, @NonNull RvViewHolder holder) {
        int typeArraySize = typeArray.size();
        for (int index = typeArraySize - 1; index >= 0; index--) {
            BaseTypeItemView<?> itemView = typeArray.valueAt(index);
            if (itemView.checkType(data, position)) {
                //noinspection unchecked
                itemView.onConvert(holder, data, position, mItemClickListeners.get(itemType), mItemLongClickListeners.get(itemType));
                return;
            }
        }
        throw new IllegalArgumentException(
                "No RvItemView added that matches position=" + position + " in data source");
    }
}
