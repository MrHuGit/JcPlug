package com.android.jcplug;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.jc_banner.BannerLayout;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BannerLayout bannerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bannerLayout = (BannerLayout) findViewById(R.id.banner_layout);
        List<String> list = new ArrayList<>();
        list.add("http://img0.imgtn.bdimg.com/it/u=1352823040,1166166164&fm=27&gp=0.jpg");
        list.add("http://img3.imgtn.bdimg.com/it/u=2293177440,3125900197&fm=27&gp=0.jpg");
        list.add("http://img3.imgtn.bdimg.com/it/u=3967183915,4078698000&fm=27&gp=0.jpg");
        list.add("http://img0.imgtn.bdimg.com/it/u=3184221534,2238244948&fm=27&gp=0.jpg");
        list.add("http://img4.imgtn.bdimg.com/it/u=1794621527,1964098559&fm=27&gp=0.jpg");
        list.add("http://img4.imgtn.bdimg.com/it/u=1243617734,335916716&fm=27&gp=0.jpg");
        WebBannerAdapter webBannerAdapter=new WebBannerAdapter(this,list);
        bannerLayout.setAdapter(webBannerAdapter);
    }


    class WebBannerAdapter extends RecyclerView.Adapter<WebBannerAdapter.MzViewHolder> {

        private Context context;
        private List<String> urlList;

        public WebBannerAdapter(Context context, List<String> urlList) {
            this.context = context;
            this.urlList = urlList;
        }



        @NonNull
        @Override
        public WebBannerAdapter.MzViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MzViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WebBannerAdapter.MzViewHolder holder, final int position) {
            if (urlList == null || urlList.isEmpty()) {
                return;
            }
            final int P = position % urlList.size();
            String url = urlList.get(P);
            Glide.with(context).load(url).into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("url",url);
                }
            });

        }

        @Override
        public int getItemCount() {
            if (urlList != null) {
                return urlList.size();
            }
            return 0;
        }


        class MzViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            MzViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image);
            }
        }

    }
}
