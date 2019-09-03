package com.android.jc.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author Mr.Hu(Jc) JCFramework
 * @create 2019-07-01 14:53
 * @describe RecyclerView通用型adapter
 * @update
 */
public class RvTypeAdapter extends RecyclerView.Adapter<RvViewHolder> {
    /**
     * 工具类
     */
    private final RvAdapterTool mRvAdapterTool;

    public RvTypeAdapter() {
        mRvAdapterTool = new RvAdapterTool();
    }


    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup parentView, int viewType) {
        BaseTypeItemView<?> itemView = mRvAdapterTool.getItemView(viewType);
        int layoutId = 0;
        View layoutView = null;
        if (itemView != null) {
            layoutId = itemView.getLayoutId();
            layoutView = itemView.getLayoutView();
        }
        RvViewHolder viewHolder;
        if (layoutView != null) {
            viewHolder = RvViewHolder.create(layoutView);
        } else {
            viewHolder = RvViewHolder.create(parentView, layoutId);

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder holder, int position) {
        mRvAdapterTool.onConvert(holder, position);
    }

    @Override
    public int getItemCount() {
        final SparseArrayCompat<List<?>> dataList = mRvAdapterTool.mDataArray;
        int dataListCount = dataList.size();
        int itemCount = 0;
        for (int i = dataListCount - 1; i >= 0; i--) {
            List<?> list = dataList.valueAt(i);
            itemCount += list.size();
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        return mRvAdapterTool.getItemViewType(position);
    }

    /**
     * 注册itemType
     *
     * @param itemType     itemType
     * @param typeItemView typeItemView
     * @param canRepeated  是否允许重复注册itemType
     * @param <D>          D
     */
    public <D> void registerItemType(int itemType, BaseTypeItemView<D> typeItemView, boolean canRepeated) {
        if (itemType <= 0) {
            throw new RuntimeException("itemType must >0");
        }
        mRvAdapterTool.registerItemType(itemType, typeItemView, canRepeated);
    }

    /**
     * 注册itemType
     *
     * @param itemType     itemType
     * @param typeItemView typeItemView
     * @param <D>          D
     */
    public <D> void registerItemType(int itemType, BaseTypeItemView<D> typeItemView) {
        registerItemType(itemType, typeItemView, false);
    }

    /**
     * 设置对应itemType的数据集合
     *
     * @param itemType itemType
     * @param typeList 数据集合
     * @param <D>      D
     * @return 当前对应
     */
    public <D> RvTypeAdapter setList(int itemType, List<D> typeList) {
        mRvAdapterTool.mDataArray.put(itemType, typeList);
        return this;
    }


    /**
     * 获取对应itemType的数据集合
     * @param itemType itemType
     * @return 对应itemType的数据集合
     */
    public List<?> getList(int itemType) {
        return mRvAdapterTool.mDataArray.get(itemType);
    }

    /**
     * 获取数据集合
     *
     * @return 所有数据集合
     */
    protected SparseArrayCompat<List<?>> getDataArray() {
        return mRvAdapterTool.mDataArray;
    }

    /**
     * 移除对应的itemType
     *
     * @param itemType itemType
     */
    protected void removeItemType(int itemType) {
        mRvAdapterTool.mDataArray.remove(itemType);
        mRvAdapterTool.mItemTypeArray.remove(itemType);
        mRvAdapterTool.mItemLongClickListeners.remove(itemType);
        mRvAdapterTool.mItemClickListeners.remove(itemType);
    }

    /**
     * 设置长按监听
     *
     * @param itemType          itemType
     * @param longClickListener longClickListener
     * @param <D>               数据泛型
     */
    public <D> void setOnItemLongClickListener(int itemType, IRvTypeListener.OnItemLongClickListener<D> longClickListener) {
        if (longClickListener != null) {
            mRvAdapterTool.mItemLongClickListeners.put(itemType, longClickListener);
        }
    }


    /**
     * 设置单击简体
     *
     * @param itemType      itemType
     * @param clickListener clickListener
     * @param <D>           数据泛型
     */
    public <D> void setOnItemClickListener(int itemType, IRvTypeListener.OnItemClickListener<D> clickListener) {
        if (clickListener != null) {
            mRvAdapterTool.mItemClickListeners.put(itemType, clickListener);
        }
    }
}
