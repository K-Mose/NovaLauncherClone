package com.java.novalauncher;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

// AppObject에 대해서 어디 붙을지 결정해주는 어댑터
public class AppAdapter extends BaseAdapter {
    Context context;
    List<AppObject> appList;
    int cellHeight;
    public AppAdapter(Context context, List<AppObject> appList, int cellHeight){
        this.context = context;
        this.appList = appList;
        this.cellHeight = cellHeight;
    }
    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        // 실제로 그려지는 아이템을 ConvertView라는 배열로 관리함
        // convertView==null 일 때
        int i = 0;
        if(convertView == null){
            Log.d("ConvertView::","ConvertView - LayoutInflate "+i);
            i++;
            // layoutInflate
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_app, parent, false);
        }else{
            v = convertView;
        }

        LinearLayout mLayout = v.findViewById(R.id.layout);
        ImageView mImage = v.findViewById(R.id.image);
        TextView mLabel = v.findViewById(R.id.appLabel);

        // 아이콘 설정
        mImage.setImageDrawable(appList.get(position).getImage());
        mLabel.setText(appList.get(position).getName());

        // 레이아웃 클릭하면 레이아웃을 가져와서 Intent 시켜줌
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Main의 itemPress로 실행시킴
                // appList에서 메모리에 저장된 주소(position)으로 보내기 떄문에 itemp,
                ((MainActivity) context).itemPress(appList.get(position));
            }
        });
        mLayout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity) context).itemLongPress(appList.get(position));
                return false;
            }
        });
        //

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, cellHeight);
        mLayout.setLayoutParams(lp);
        return v;
    }
}
