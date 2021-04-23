package com.java.novalauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Boolean isBottom = true;
    ViewPager mViewPager;
    int cellHeight;

    int NUMBER_OF_ROWS = 5;
    int DRAWER_PEEK_HEIGHT = 100;
    String PREFS_NAME = "NovaPrefs";

    int numRow = 0, numColumn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        getPermissions();
        getData();

        final LinearLayout mTopDrawerLayout = findViewById(R.id.topDrawerLayout);
        // onCreate에서 뷰를 생성하는데 시간이 걸려서 아래와 같이 하면 높이가 0으로 받을 수 있다.
        // mTopDrawerLayout.getHeight();
        mTopDrawerLayout.post(new Runnable(){
            @Override
            public void run() {
                DRAWER_PEEK_HEIGHT = mTopDrawerLayout.getHeight();
                initializeHome();
                initializeDrawer();
            }
        });
        ImageButton mSettings = findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
    }

    ViewPagerAdapter mViewPagerAdapter;
    private void initializeHome() {
        ArrayList<PagerObject> pagerAppList = new ArrayList<>();
        ArrayList<AppObject> appList1 = new ArrayList<>();
        ArrayList<AppObject> appList2 = new ArrayList<>();
        ArrayList<AppObject> appList3 = new ArrayList<>();
        for(int i = 0; i< numColumn*numRow; i++){
            appList1.add(new AppObject("", "", getResources().getDrawable(R.drawable.ic_launcher_foreground), false));
            appList2.add(new AppObject("", "", getResources().getDrawable(R.drawable.ic_launcher_foreground), false));
            appList3.add(new AppObject("", "", getResources().getDrawable(R.drawable.ic_launcher_foreground), false));
        }

        pagerAppList.add(new PagerObject(appList1));
        pagerAppList.add(new PagerObject(appList2));
        pagerAppList.add(new PagerObject(appList3));

        cellHeight = (getDisplayContentHeight() - DRAWER_PEEK_HEIGHT) / numRow;

        mViewPager = findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this, pagerAppList, cellHeight, numColumn);
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
        mDrawerGridView.setAdapter(new AppAdapter(this, installedAppList, cellHeight));

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
        // 홈스크린 차지하고 있는 칸에 옮기는 경우
        if(mAppDrag != null && !app.getName().equals("")){
            Toast.makeText(this, "Cell Already Occupied", Toast.LENGTH_SHORT);
            return;
        }
        // 홈스크린 빈칸에 옮기는 경우, 스크린에 앱 추가
        if(mAppDrag != null && !app.getIsAppInDrawer()){
            app.setPackageName(mAppDrag.getPackageName());
            app.setImage(mAppDrag.getImage());
            app.setName(mAppDrag.getName());
            app.setIsAppInDrawer(false);

            // 앱 삭제
            if(!mAppDrag.getIsAppInDrawer()){
                mAppDrag.setPackageName("");
                mAppDrag.setImage(getResources().getDrawable(R.drawable.ic_launcher_background));
                mAppDrag.setName("");
                mAppDrag.setIsAppInDrawer(false);
            }

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
        mDrawerGridView.setY(DRAWER_PEEK_HEIGHT);
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
            AppObject app = new AppObject(appPackageName, appName, appImage, true);

            if(!list.contains(app))
                list.add(app);
        }
        return list;
    }



    private void getData(){
        ImageView mHomeScreenImage = findViewById(R.id.homeScreenImage);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUri = sharedPreferences.getString("imageUri", null);
        int numRow = sharedPreferences.getInt("numRow", 7);
        int numColumn = sharedPreferences.getInt("numColumn", 4);

        // 변화가 생겼을 때
        if(this.numRow != numRow || this.numColumn != numColumn){
            this.numRow = numRow;
            this.numColumn = numColumn;
            initializeHome();
        }
        if(imageUri != null){
            mHomeScreenImage.setImageURI(Uri.parse(imageUri));
        }
    }

    private void getPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}