package com.java.novalauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.GridView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Boolean isBottom = true;
    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeHome();
        initializeDrawer();
    }

    private void initializeHome() {
        ArrayList<PagerObject> pagerAppList = new ArrayList<>();

        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new ViewPagerAdapter(this,pagerAppList));
    }

    List<AppObject> installedAppList = new ArrayList<>();

    // CoordinatorLayout
    // https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout
    private void initializeDrawer() {
        View mBottomSheet = findViewById(R.id.bottomSheet);
        final GridView mDrawerGridView = findViewById(R.id.drawerGrid);
        final BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(300);
        installedAppList = getInstalledAppList();
        mDrawerGridView.setAdapter(new AppAdapter(getApplicationContext(), installedAppList));

        // 내부 그리드뷰가 위 아래로 확장이 완료 되었을 때만 스크롤링 되도록 설정
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // BottomSheet이 숨겨져 있을 떄
                // gridVIew의 Y위치가 0이 아니면 드래그(행동)했을 때 EXPANDED로 설정
                if(newState == BottomSheetBehavior.STATE_HIDDEN && mDrawerGridView.getChildAt(0).getY()!=0){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                // BottomSheet이 드래그 중일 떄
                //
                if(newState == BottomSheetBehavior.STATE_DRAGGING && mDrawerGridView.getChildAt(0).getY()!=0){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private List<AppObject> getInstalledAppList() {
        List<AppObject> list = new ArrayList<>();
        // Info about Action MAIN and Category Launcher in Android Manifest
        // https://stackoverflow.com/questions/6730982/info-about-action-main-and-category-launcher-in-android-manifest

        // https://developer.android.com/reference/android/content/Intent#ACTION_MAIN
        // Start as a main entry point, does not expect to receive data.
        // 메인 시작점으로 만듬, 데이터 받지 않음.
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        // App type 지정 . category는 Intent 수행에 있어서 추가적인 디테일을 제공함.

        // https://developer.android.com/reference/android/content/Intent#addCategory(java.lang.String)
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // https://developer.android.com/reference/android/content/pm/PackageManager
        // 아직 처리되지 않은 앱 리스트를 받음.
        // ApplicationContext - Application lifeCycle에 묶여있는 Context
        // getPackageManager - Class for retrieving various kinds of information related to the application packages that are currently installed on the device.
        // queryIntentActivity - Retrieve all activities that can be performed for the given intent. /
        List<ResolveInfo> untreatedAppList = getApplicationContext()
                .getPackageManager()
                .queryIntentActivities(intent, 0); // flag 0 - INSTALL_SCENARIO_DEFAULT : A value to indicate the lack of CUJ information, disabling all installation scenario logic.

        // 객체 생성 후 리스트에 추가
        for(ResolveInfo UntreatedApp : untreatedAppList){
            String appName = UntreatedApp.activityInfo.loadLabel(getPackageManager()).toString();
            String appPackageName = UntreatedApp.activityInfo.packageName;
            Drawable appImage = UntreatedApp.activityInfo.loadIcon(getPackageManager());
            AppObject app = new AppObject(appPackageName, appName, appImage);

            if(!list.contains(app))
                list.add(app);
        }
        return list;
    }
}