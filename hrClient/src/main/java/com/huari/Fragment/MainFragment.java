package com.huari.Fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huari.client.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
    List<BaseFragment> fragments = new ArrayList<>();
    //public static Handler handler;

    Context context;

    private TabLayout tabLayout;
    private View view;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    private ImageButton play_button;
    private ImageButton save_button;
    private ImageButton set_button;
    private ImageButton more_button;
    private boolean is_played;
    private boolean is_saved;
    private boolean is_visible;
    private int current;

    public MainFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("ValidFragment")
    public MainFragment(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_measuer, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        //view.setSupportActionBar(toolbar);
        viewPager = view.findViewById(R.id.viewPager1);

        addTabs(viewPager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);

        play_button = view.findViewById(R.id.play_Button);
        play_button.setOnClickListener(v -> {

            fragments.get(viewPager.getCurrentItem()).play();

        });

        save_button = view.findViewById(R.id.save_Button);
        save_button.setOnClickListener(v -> {
            fragments.get(viewPager.getCurrentItem()).save();

        });

        set_button = view.findViewById(R.id.set_Button);
        set_button.setOnClickListener(v -> {
            fragments.get(viewPager.getCurrentItem()).config();
        });

        more_button = view.findViewById(R.id.more_Button);
        more_button.setOnClickListener(v -> {
            fragments.get(viewPager.getCurrentItem()).more();
        });

        fragments = adapter.mFragmentList;
        current = viewPager.getCurrentItem();

        update_titile_icon();

        return view;
    }

    private void addTabs(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getChildFragmentManager(), this);
        adapter.addFrag(Arrays.asList("频段扫描", "单频测量", "离散扫描", "单频测向", "跳频检测", "频率扫描", "多路分析", "电子地图", "设备自检"));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onTabSelected(TabLayout.Tab selectedTab) {
        //if (DEBUG) Log.d(TAG, "onTabSelected() called with: selectedTab = [" + selectedTab + "]");
        update_titile_icon();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //if (DEBUG) Log.d(TAG, "onTabReselected() called with: tab = [" + tab + "]");
        update_titile_icon();
    }

    public void update_titile_icon() {
        current = viewPager.getCurrentItem();
        is_played = fragments.get(current).get_played();
        is_saved = fragments.get(current).get_saved();
        is_visible = fragments.get(current).get_visible();

        if (!is_visible) {
            play_button.setVisibility(View.INVISIBLE);
            save_button.setVisibility(View.INVISIBLE);
            set_button.setVisibility(View.INVISIBLE);
        } else {
            play_button.setVisibility(View.VISIBLE);
            save_button.setVisibility(View.VISIBLE);
            set_button.setVisibility(View.VISIBLE);

            if (is_played) {
                play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white));
            } else {
                play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
            }

            if (is_saved) {
                save_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_nosave_white_24dp));
            } else {
                save_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_white_24dp));
            }
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }

        final List<BaseFragment> mFragmentList = new ArrayList<>();
        List<String> mFragmentTitleList = new ArrayList<>();
        MainFragment fragment;
        Context context;

        ViewPagerAdapter(FragmentManager manager, MainFragment fragment, Context context) {
            super(manager);
            this.fragment = fragment;
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(List<String> stringList) {
//            mFragmentList.add(fragment);
            mFragmentTitleList = stringList;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
