package com.huari.Fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.huari.client.R;

import java.util.Objects;

public class Pscan_Param_Fragment extends PreferenceFragmentCompat {
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_pscan_param);

        PreferenceManager mPreferenceManager = getPreferenceManager();

        PreferenceScreen prfs = mPreferenceManager.getPreferenceScreen();

        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("StartFreq")));
        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("EndFreq")));
        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("FreqStep")));
        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("Attenuation")));
        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("RFMode")));

    }

//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState)
//    {
//        View view = inflater.inflate(R.layout.fragment_pscan_setting, container, false);
//        addPreferencesFromResource(R.xml.activity_pscan_param);
//
//        PreferenceManager mPreferenceManager = getPreferenceManager();
//
//        PreferenceScreen prfs = mPreferenceManager.getPreferenceScreen();
//
////        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("StartFreq")));
////        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("EndFreq")));
////        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("FreqStep")));
//        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("Attenuation")));
//        bindPreferenceSummaryToValue(Objects.requireNonNull(prfs.findPreference("RFMode")));
//        return view;
//    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        SharedPreferences sp = preference.getSharedPreferences();
        boolean ON_OFF = sp.getBoolean("AudioSwitch", false);
        Log.i("lenve", ON_OFF+"");
        String tmp = sp.getString("CenterFreq", "101.7");
        Log.i("lenve", tmp+"");
        String listtext = sp.getString("DemoBandWidth", "");
        Log.i("lenve", listtext+"");
        boolean next_screen = sp.getBoolean("SpectrumSwitch", false);
        Log.i("lenve", next_screen+"");
        return true;
    }

    /**
     * bindPreferenceSummaryToValue 拷贝至as自动生成的preferences的代码，用于绑定显示实时值
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}

