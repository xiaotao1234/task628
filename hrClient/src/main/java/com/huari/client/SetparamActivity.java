package com.huari.client;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huari.Fragment.Single_Ctrl_Fragment;
import com.huari.Fragment.System_Param_Fragment;

import java.util.ArrayList;

public class SetparamActivity extends AppCompatActivity {
    private ArrayList<Fragment> fragments;
    ViewPager viewpager;
    System_Param_Fragment settingfragment1;
    Single_Ctrl_Fragment settingfragment2;

    //标题
    String[] title = {
            "参数设置",
            "显控选项"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_paramdialog2);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        viewpager = findViewById(R.id.viewpager1);

        fragments = new ArrayList<>();
        settingfragment1 = new System_Param_Fragment();
        settingfragment2 = new Single_Ctrl_Fragment();
        fragments.add(settingfragment1);
        fragments.add(settingfragment2);

        viewpager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewpager);
    }

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

}
