package com.android.jc.recyclerview.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mr.Hu(Jc) JCFramework
 * @create 2019-07-01 16:36
 * @describe itemType的数据bean相同通用adapter
 * @update
 */
public abstract class BaseSameDataAdapter<D> extends RvTypeAdapter {
    /**
     * 显示暂无数据布局对应的type
     */
    final static int EMPTY_ITEM_TYPE = 100;
    /**
     * 暂无数据对应的布局id
     */
    private final int mEmptyLayoutId;
    /**
     * 正在加载数据对应的type
     */
    final static int LOADING_ITEM_TYPE = 101;
    /**
     * 正在加载数据对应的布局id
     */
    private final int mLoadingLayoutId;
    /**
     * 显示数据对应的type
     */
    final static int DATA_ITEM_TYPE = 1;
    /**
     * 不需要对应的布局 id
     */
    final static  int NO_LAYOUT_ID = -1;

    private List<D> mList;

    /**
     * @param emptyLayout   暂无数据显示的布局
     * @param loadingLayout 加载数据中的布局
     */
    public BaseSameDataAdapter(@LayoutRes int emptyLayout, @LayoutRes int loadingLayout) {
        mLoadingLayoutId = loadingLayout;
        mEmptyLayoutId = emptyLayout;
        showLoading();
    }

    /**
     * 添加itemType
     *
     * @param itemTypeView itemTypeView
     */
    public void addDataItemType(BaseTypeItemView<D> itemTypeView) {
        registerItemType(DATA_ITEM_TYPE, itemTypeView, true);
    }

    /**
     * 显示正在加载
     */
    public void showLoading() {
        if (mLoadingLayoutId == NO_LAYOUT_ID) {
            return;
        }
        removeItemType(LOADING_ITEM_TYPE);
        registerItemType(LOADING_ITEM_TYPE, new BaseTypeItemView<String>() {
            @Override
            public int getLayoutId() {
                return mLoadingLayoutId;
            }

            @Override
            public void onConvertData(RvViewHolder holder, @NonNull String data, int position) {

            }
        });
        getDataArray().clear();
        getDataArray().put(LOADING_ITEM_TYPE, Collections.singletonList("加载中...."));
        notifyDataSetChanged();
    }

    /**
     * 显示暂无数据
     */
    public void showEmpty() {
        if (mEmptyLayoutId == NO_LAYOUT_ID) {
            return;
        }
        removeItemType(EMPTY_ITEM_TYPE);
        registerItemType(EMPTY_ITEM_TYPE, new BaseTypeItemView<String>() {
            @Override
            public int getLayoutId() {
                return mEmptyLayoutId;
            }

            @Override
            public void onConvertData(RvViewHolder holder, @NonNull String data, int position) {

            }
        });
        getDataArray().clear();
        getDataArray().put(EMPTY_ITEM_TYPE, Collections.singletonList("暂无数据"));
        notifyDataSetChanged();
    }

    /**
     * 设置数据
     *
     * @param dataList 数据list
     * @return 当前对象
     */
    public BaseSameDataAdapter setList(List<D> dataList) {
        removeItemType(LOADING_ITEM_TYPE);
        removeItemType(EMPTY_ITEM_TYPE);
        mList = dataList;
        super.setList(DATA_ITEM_TYPE, mList);
        if (dataList == null || dataList.size() < 1) {
            showEmpty();
        }
        return this;
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public List<D> getList() {
        return mList;
    }

    /**
     * 添加数据
     *
     * @param list 新增的数据
     * @return 当前对象
     */
    public BaseSameDataAdapter<D> addList(List<D> list) {
        List<D> oldList = getList();
        if (oldList == null) {
            oldList = new ArrayList<>();
        }
        oldList.addAll(list);
        return this;
    }


    /**
     * 此处只是添加正常数据的监听，
     * 如果需要监听正在加载单击可以调用{@link #setOnItemClickListener(int, IRvTypeListener.OnItemClickListener)}}int->{@link #LOADING_ITEM_TYPE}
     * 如果需要监听暂无数据单击可以调用{@link #setOnItemClickListener(int, IRvTypeListener.OnItemClickListener)}}int->{@link #EMPTY_ITEM_TYPE}
     *
     * @param onItemClickListener 单击item监听
     */
    public void setOnItemClickListener(IRvTypeListener.OnItemClickListener<D> onItemClickListener) {
        setOnItemClickListener(DATA_ITEM_TYPE, onItemClickListener);
    }
    /**
     * 此处只是添加正常数据的监听，
     * 如果需要监听正在加载单击可以调用{@link #setOnItemLongClickListener(int, IRvTypeListener.OnItemLongClickListener)}}int->{@link #LOADING_ITEM_TYPE}
     * 如果需要监听暂无数据单击可以调用{@link #setOnItemLongClickListener(int, IRvTypeListener.OnItemLongClickListener)}}int->{@link #EMPTY_ITEM_TYPE}
     *
     * @param onItemLongClickListener 单击item监听
     */
    public void setOnItemLongClickListener(IRvTypeListener.OnItemLongClickListener<D> onItemLongClickListener) {
        setOnItemLongClickListener(DATA_ITEM_TYPE, onItemLongClickListener);
    }
}
