package com.huari.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends Fragment {
    boolean is_played = false;
    boolean is_recorded = false;
    boolean visible = true;


//    public interface OnSingleDataListener {
//        void onData(SingleMeasureData data);
//        void onCommand(boolean status);
//        void onItuData(float freq,float strength,float bandwidth,float freq_deviat);
//        void onDemodulationData(String demod_str,float percent);
//        void onSave(boolean status,float time_long);
//    }
//
//    public interface OnScanDataListener {
//
//        void onData(SingleMeasureData data);
//    }

//    public interface OnScanDataListener {
//
//        boolean onData();//ScanData
//    }

    //protected AppCompatActivity activity;
    //public static final ImageLoader imageLoader = ImageLoader.getInstance();

    //These values are used for controlling framgents when they are part of the frontpage
    //@State
    protected boolean useAsFrontPage = false;
    //protected boolean mIsVisibleToUser = false;

    public void useAsFrontPage(boolean value) {
        useAsFrontPage = value;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fragment's Lifecycle
    //////////////////////////////////////////////////////////////////////////*/

    protected DialogFactory mDialogFactory ;

    public BaseDialogFragment.BaseDialogListener getDialogListener(){
        return mDialogFactory.mListenerHolder.getDialogListener();
    }

    /**
     * 清空DialogListenerHolder中的dialog listener
     */
    public void clearDialogListener(){
        mDialogFactory.mListenerHolder.setDialogListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //activity = (AppCompatActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //if (DEBUG) Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDialogFactory.mListenerHolder.saveDialogListenerKey(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDialogFactory = new DialogFactory(getChildFragmentManager(),savedInstanceState);
        mDialogFactory.restoreDialogListener(this);
    }

    boolean get_played(){
        return is_played;
    }

    boolean get_saved(){
        return is_recorded;
    }

    boolean get_visible(){
        return visible;
    }

    void play()
    {
        //is_played = !is_played;
    }

    void save()
    {
        is_recorded = !is_recorded;
    }

    void config()
    {

    }

    void more()
    {

    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
//        if (DEBUG) {
//            Log.d(TAG, "onViewCreated() called with: rootView = [" + rootView + "], savedInstanceState = [" + savedInstanceState + "]");
//        }
        initViews(rootView, savedInstanceState);
        initListeners();
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        RefWatcher refWatcher = App.getRefWatcher(getActivity());
//        if (refWatcher != null) refWatcher.watch(this);
    }



    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    protected void initViews(View rootView, Bundle savedInstanceState) {
    }

    protected void initListeners() {
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

//    public void setTitle(String title) {
//        if (DEBUG) Log.d(TAG, "setTitle() called with: title = [" + title + "]");
//        if((!useAsFrontPage || mIsVisibleToUser)
//            && (activity != null && activity.getSupportActionBar() != null)) {
//            activity.getSupportActionBar().setTitle(title);
//        }
//    }

//    protected FragmentManager getFM() {
//        return getParentFragment() == null
//                ? getFragmentManager()
//                : getParentFragment().getFragmentManager();
//    }
}
