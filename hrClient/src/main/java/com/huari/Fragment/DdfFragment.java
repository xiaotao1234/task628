package com.huari.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huari.client.R;

public class DdfFragment extends BaseFragment {

    public DdfFragment() {
        is_played = false;
        is_recorded = false;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ddf, container, false);
    }

    void config(){
        mDialogFactory.showCustomDialog("单频测向参数设置及选项",true,3);
    }
}

