package com.java.novalauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Boolean isBottom = true;
    ViewPager mViewPager;
    int cellHeight;

    int NUMBER_OF_ROWS = 5;
    int DRAWER_PEEK_HEIGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        initializeHome();
        initializeDrawer();
    }

    ViewPagerAdapter mViewPagerAdapter;
    private void initializeHome() {
        ArrayList<PagerObject> pagerAppList = new ArrayList<>();
        ArrayList<AppObject> appList = new ArrayList<>();
        for(int i = 0; i< 20; i++)
            appList.add(new AppObject("$i", "${i*i}", getResources().getDrawable(R.drawable.ic_launcher_foreground)));

        pagerAppList.add(new PagerObject(appList));
        pagerAppList.add(new PagerObject(appList));
        pagerAppList.add(new PagerObject(appList));

        cellHeight = (getDisplayContentHeight() - DRAWER_PEEK_HEIGHT) / NUMBER_OF_ROWS;

        mViewPager = findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this, pagerAppList, cellHeight);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    private int getDisplayContentHeight() {
        final WindowManager windowManager = getWindowManager();
        final Point size = new Point();
        int screenHeight = 0, actionBarHeight = 0, statusBarHeight = 0;;
        if(getActionBar()!=null){
            actionBarHeight = getActionBar().getHeight();
        }

        // ActionBar의 dimension 가져온다고?
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // android.R.id.content gives you the root element of a view, without having to know its actual name/type/ID. Check out
        // http://stackoverflow.com/questions/4486034/android-how-to-get-root-view-from-current-activity
        int contentTop = (findViewById(android.R.id.content)).getTop();
        // Gets the size of the display, in pixels.
        windowManager.getDefaultDisplay().getSize(size); // @param outSize A {@link Point} object to receive the size information.
        screenHeight = size.y;

        return screenHeight-contentTop-actionBarHeight-statusBarHeight;
    }

    List<AppObject> installedAppList = new ArrayList<>();
    GridView mDrawerGridView;
    BottomSheetBehavior mBottomSheetBehavior;
    // CoordinatorLayout
    // https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout
    private void initializeDrawer() {
        View mBottomSheet = findViewById(R.id.bottomSheet);
        mDrawerGridView = findViewById(R.id.drawerGrid);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(300);
        installedAppList = getInstalledAppList();
        mDrawerGridView.setAdapter(new AppAdapter(getApplicationContext(), installedAppList, cellHeight));

        // 내부 그리드뷰가 위 아래로 확장이 완료 되었을 때만 스크롤링 되도록 설정
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(mAppDrag != null){
                    return;
                }
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

    AppObject mAppDrag = null; // null로 Dragging 인지 아닌지 판별

    // Press : Start Activity
    public  void itemPress(AppObject app){
        if(mAppDrag != null){
            app.setPackageName(mAppDrag.getPackageName());
            app.setImage(mAppDrag.getImage());
            app.setName(mAppDrag.getName());
            mAppDrag = null;
            mViewPagerAdapter.notifyGridChanged();
            return;
        }else{
            Intent launchAppIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName()); // 클릭한 packageName을 Intent에 넘겨줌
            if(launchAppIntent != null){
                getApplicationContext().startActivity(launchAppIntent);
            }
        }

    }
    // LongPress : App Dragging
    public  void itemLongPress(AppObject app){
        collapseDrawer();
        mAppDrag = app;
    }

    private void collapseDrawer() {
        mDrawerGridView.setY(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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