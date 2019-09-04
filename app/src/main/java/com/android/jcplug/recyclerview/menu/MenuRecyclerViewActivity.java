package com.android.jcplug.recyclerview.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.jc.recyclerview.decoration.RvDividerItemDecoration;
import com.android.jc.recyclerview.menu.MenuRecyclerView;
import com.android.jcplug.R;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-29 15:11
 * @describe 左滑菜单recyclerView
 * @update
 */
public class MenuRecyclerViewActivity extends AppCompatActivity {
//    private SlidingItemMenuRecyclerView slidingMenuRecyclerView;
    private MenuRecyclerView slidingMenuRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_recyclerview);
        slidingMenuRecyclerView =  findViewById(R.id.sliding_menu_recycler_view);
        initData();
        setListener();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        slidingMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        slidingMenuRecyclerView.setAdapter(new MenuAdapter());
        slidingMenuRecyclerView.addItemDecoration(RvDividerItemDecoration.builder()
                .setDividerDistance(5)
//                .setOffsetLeft(50)
//                .setOffsetRight(50)
                .setOrientation(LinearLayoutManager.VERTICAL)
                .setDividerType(RvDividerItemDecoration.DividerType.MID)
                .setDividerColor(Color.parseColor("#f3f345"))
                .build(this));
    }

    /**
     * 设置监听
     */
    private void setListener() {
    }



    private static class MenuAdapter extends RecyclerView.Adapter<MenuAdapterHolder>{
        int itemCount =100;
        @NonNull
        @Override
        public MenuAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MenuAdapterHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_menu_recycler_view_adapter, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MenuAdapterHolder menuAdapterHolder, int position) {
            menuAdapterHolder.text.setText("ItemView " + position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }


    private static class MenuAdapterHolder extends RecyclerView.ViewHolder{
        final TextView text;
        final TextView deleteButton;

        private MenuAdapterHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }

    }
}
