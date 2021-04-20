package com.java.novalauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    ArrayList<PagerObject> pagerAppList;
    ArrayList<AppAdapter> appAdapterList = new ArrayList<>();
    int cellHeight;
    public ViewPagerAdapter(Context context, ArrayList<PagerObject> pagerAppList, int cellHeight){
        this.context = context;
        this.pagerAppList = pagerAppList;
        this.cellHeight = cellHeight;
    }

    // where we will control the items that are to be shown the app
    //
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.pager_layout, container, false);

        final GridView mGridView = layout.findViewById(R.id.grid);
        AppAdapter mGridAdapter = new AppAdapter(context, pagerAppList.get(position).getAppList(), cellHeight);
        // Page별로 리스트를 받기 위해
        mGridView.setAdapter(mGridAdapter);


        appAdapterList.add(mGridAdapter);

        container.addView(layout);
        return layout;
    }


    @Override
    public int getCount() {
        return pagerAppList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return false;
    }

    public void notifyGridChanged(){
        for(int i = 0; i < appAdapterList.size(); i++){
            appAdapterList.get(i).notifyDataSetChanged();
        }
    }
}
