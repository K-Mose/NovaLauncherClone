package com.java.novalauncher;

import java.util.ArrayList;

public class PagerObject {
    private ArrayList<AppObject> appList;

    public PagerObject(ArrayList<AppObject> appList){
        this.appList = appList;
    }

    // 원하는 위치에 있는 appList를 반환하는 함수
    public ArrayList<AppObject> getAppList(){return appList;}
}
