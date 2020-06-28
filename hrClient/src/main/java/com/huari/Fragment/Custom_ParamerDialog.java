package com.huari.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huari.client.R;

import java.util.ArrayList;

public class Custom_ParamerDialog extends BaseDialogFragment {
    private ViewPagerAdapter adapter;
    private ArrayList<Fragment> fragments;
    private int m_type;
    //标题
    String[] title = {
            "参数设置",
            "显控选项"
    };

    Custom_ParamerDialog(int type){
        m_type = type;
    }

    public static Custom_ParamerDialog newInstance(String message, boolean cancelable,int type){

        Custom_ParamerDialog dialog = new Custom_ParamerDialog(type);
        Bundle bundle = new Bundle();
        putMessageParam(bundle,message);
        putCancelableParam(bundle,cancelable);
        dialog.setArguments(bundle);
        return  dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.custom_paramdialog,container,false);

        TabLayout tabLayout = view.findViewById(R.id.tablayout);

        ViewPager viewpager = view.findViewById(R.id.viewpager1);

        fragments = new ArrayList<>();

        switch (m_type){
            case 0:
                fragments.add(new Pscan_Param_Fragment());
                fragments.add(new Pscan_Ctrl_Fragment());
                break;
            case 1:
                fragments.add(new Single_Param_Fragment());
                fragments.add(new Single_Ctrl_Fragment());
                break;
            case 2:
                fragments.add(new Mscan_Param_Fragment());
                fragments.add(new Mscan_Ctrl_Fragment());
                break;
            case 3:
                fragments.add(new Single_Param_Fragment());
                fragments.add(new Ddf_Ctrl_Fragment());
                break;
            case 4:
                fragments.add(new Hope_Param_Fragment());
                fragments.add(new Hope_Ctrl_Fragment());
                break;
            case 5:
                break;
            case 6:
                fragments.add(new Ddc_Param_Fragment());
                fragments.add(new Ddc_Ctrl_Fragment());
                break;
            default:
                break;
        }

        adapter = new ViewPagerAdapter(getChildFragmentManager());

        viewpager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewpager);

//        //设置弹窗的宽度
//        WindowManager m = Objects.requireNonNull(getDialog()).mWindow.getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getDialog().getAttributes();
//        Point size = new Point();
//        d.getSize(size);
//        p.width = (int)(size.x * 0.80);//是dialog的宽度为app界面的80%
//        p.height = (int)(size.y * 0.80);
//        getWindow().setAttributes(p);
        return view;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }


}

