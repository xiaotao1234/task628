package com.huari.client;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huari.Fragment.LineFragment;
import com.huari.Fragment.MainFragment;
import com.huari.Fragment.OfflineFragment2;
import com.huari.Fragment.StationShowFragment;
import com.huari.adapter.DzPagerAdapter;
import com.huari.dataentry.SocketStopEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MajorActivity extends AppCompatActivity {
    ViewPager viewPager;
    DzPagerAdapter dzPagerAdapter;
    TabLayout tabLayout;
    List<Fragment> fragments = new ArrayList<>();

    OfflineFragment2 offlineFragment2;
    boolean back = false;
    long time;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (time == 0) {
            time = System.currentTimeMillis();
            Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
        } else {
            if (System.currentTimeMillis() - time < 2000) {
                super.onBackPressed();

            }else {
                time = System.currentTimeMillis();
                Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        back = false;
//        if (Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            //getWindow().setStatusBarColor(Color.TRANSPARENT);
//
////            View decorView = getWindow().getDecorView();
////            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
////            decorView.setSystemUiVisibility(uiOptions);
//            //Objects.requireNonNull(getSupportActionBar()).hide();
//        }

//        if (0 != MyPushMessageReceiver.count) {
//            //角标清空
//            MyPushMessageReceiver.count = 0;
//            AppShortCutUtil.setCount(MyPushMessageReceiver.count, MajorActivity.this);
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_major);
        viewPager = findViewById(R.id.major_viewpager);

        offlineFragment2 = new OfflineFragment2(this,MajorActivity.this);

//        fragments.add(new LineFragment(this));
        fragments.add(new MainFragment(this));
        fragments.add(offlineFragment2);

        dzPagerAdapter = new DzPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(dzPagerAdapter);
        tabLayout = findViewById(R.id.tl_tabs);
        tabLayout.setupWithViewPager(viewPager);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("数据回放");
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("实时测量");
        Log.d("xiao", String.valueOf(viewPager.getCurrentItem()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void FileListCome(SocketStopEvent socketStopEvent) {
        setFragmentLine();
    }

    public void setFragment() {
        fragments.clear();
        fragments.add(new OfflineFragment2(this,MajorActivity.this));
        fragments.add(new StationShowFragment(this));
        DzPagerAdapter dzPagerAdapter = new DzPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.removeAllViews();
        viewPager.removeAllViewsInLayout();
        viewPager.setAdapter(dzPagerAdapter);
        viewPager.setCurrentItem(1);
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.setupWithViewPager(viewPager);
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("实时测量");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("数据回放");
    }

    public void setFragmentLine() {
        fragments.clear();
        fragments.add(new OfflineFragment2(this,MajorActivity.this));
        fragments.add(new LineFragment(this));
        DzPagerAdapter dzPagerAdapter = new DzPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.removeAllViews();
        viewPager.removeAllViewsInLayout();
        viewPager.setAdapter(dzPagerAdapter);
        viewPager.setCurrentItem(1);
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("实时测量");
        tabLayout.getTabAt(1).setText("数据回放");
    }

}
