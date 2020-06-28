package com.huari.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.huari.Base.AnalysisBase;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.LogicParameter;
import com.huari.dataentry.MyDevice;
import com.huari.dataentry.Parameter;
import com.huari.dataentry.SimpleStation;
import com.huari.dataentry.Station;
import com.huari.tools.FileOsImpl;
import com.huari.tools.SysApplication;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

//频谱分析功能界面
@SuppressLint("NewApi")
public class PPFXsetActivity extends AppCompatActivity implements
		OnClickListener, OnPageChangeListener {
	ViewPager vp;
	List<View> mylist;
	View ger, adv;
	LinearLayout gerLinearLayout, advLinearLayout;
	int offset, displaywidth, barwidth;
	ImageView imageview;
	int currentpage;
	TextView normaltextview, advancedtextview;

	String[] namesofitems, advanceditems, generalparent, generaletdata,
			advancedparent;// 每个设置选项的名字,常规、高级。
	private int generalindex;

	SpinnerAdapter[] generalspter;

	Button bn;
	View titlebar;
	ActionBar actionbar;

	String[] generalBig = null, advancedBig = null;
	ArrayList<String> gBigList, aBigList;
	String logicId;
	String stationName, deviceName, stationId;

	ArrayList<Parameter> generalparameters;
	ArrayList<Parameter> advancedparameters;

	HashMap<String, String> oldValues;
	HashMap<String, String> newValues;
	boolean pcm2gsm = false;

	Dialog dialog;

	class MyTextWatcher implements TextWatcher {
		String key;

		public MyTextWatcher(String s) {
			key = s;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String m = s.toString().trim();
			if (newValues.containsKey(key)) {
				newValues.remove(key);
			}
			newValues.put(key, m);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinpufenximenu);
		SysApplication.getInstance().addActivity(this);
		Thread.setDefaultUncaughtExceptionHandler(GlobalData.myExceptionHandler);
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		vp = (ViewPager) findViewById(R.id.generalandadvancedset);
		imageview = (ImageView) findViewById(R.id.menubar);
		ger = (View) getLayoutInflater().inflate(R.layout.generalset, null);
		adv = (View) getLayoutInflater().inflate(R.layout.advancedset, null);

		gerLinearLayout = (LinearLayout) ger.findViewById(R.id.myppfxgeneral);
		advLinearLayout = (LinearLayout) adv.findViewById(R.id.myppfxadvanced);

		normaltextview = (TextView) findViewById(R.id.normal);
		advancedtextview = (TextView) findViewById(R.id.advanced);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		displaywidth = metric.widthPixels;
		barwidth = displaywidth / 6 * 2;
		offset = displaywidth / 6;
		mylist = new ArrayList<View>();
		mylist.add(ger);
		mylist.add(adv);
		vp.setAdapter(new MyPagerAdapter(mylist));
		vp.setOnPageChangeListener(this);
		normaltextview.setOnClickListener(this);
		advancedtextview.setOnClickListener(this);

		generalparameters = new ArrayList<Parameter>();
		advancedparameters = new ArrayList<Parameter>();

		gBigList = new ArrayList<String>();
		aBigList = new ArrayList<String>();

		oldValues = new HashMap<String, String>();
		newValues = new HashMap<String, String>();

		Intent intent = getIntent();
		Bundle mybundle = intent.getExtras();
		stationName = mybundle.getString("sname");// 该设置界面所属于的设备的名字、台站的名字、台站的id、逻辑数据的id
		deviceName = mybundle.getString("dname");
		stationId = mybundle.getString("stakey");
		logicId = mybundle.getString("lids");

		loadParameters();

		dialog = new AlertDialog.Builder(PPFXsetActivity.this)
				.setTitle("确定要保存参数的更改吗？")
				.setNegativeButton("取消", (arg0, arg1) -> {
                    loadParameters();
                    PPFXsetActivity.this.finish();
                })
				.setPositiveButton("确定", (arg0, arg1) -> {
                    //GlobalData.isFirstAudio = true;
                    boolean b = refreshParameters();

                    if (b) {
                        PPFXsetActivity.this.finish();
                    }

                }).create();
	}

	private void loadParameters()// 加载参数，显示在参数列表界面
	{
		try {
			gerLinearLayout.removeAllViewsInLayout();
		} catch (Exception e) {

		}
		try {
			advLinearLayout.removeAllViewsInLayout();
		} catch (Exception e) {

		}

		MyDevice iDevice = null;
		for (MyDevice myd : GlobalData.stationHashMap.get(stationId).showdevicelist) {
			if (myd.name.equals(deviceName)) {
				iDevice = myd;
			}
		}
		LogicParameter currentLogic = iDevice.logic.get(logicId);

		for (Parameter p : currentLogic.parameterlist) {
			if (p.isAdvanced == 0) {
				advancedparameters.add(p);
				if (!aBigList.contains(p.displayType)) {
					aBigList.add(p.displayType);
				}
			} else {
				generalparameters.add(p);
				if (!gBigList.contains(p.displayType)) {
					gBigList.add(p.displayType);
				}
			}
		}

		for (String s : gBigList) {
			if (s.equals("数字对讲机解调"))
				continue;
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.big_parameter, null);
			TextView tv = (TextView) ll.findViewById(R.id.bigparamtertextview);
			tv.setText(s);
			gerLinearLayout.addView(ll);// 将大类参数显示在界面上，下面开始加载附挂到它下面的子选项
			for (Parameter p : currentLogic.parameterlist) {
				if (p.displayType.equals(s) && p.isEditable == 1
						&& p.isAdvanced == 1) {
					LinearLayout ly = (LinearLayout) getLayoutInflater()
							.inflate(R.layout.ppfxchildwithet, null);
					TextView tiew = (TextView) ly
							.findViewById(R.id.exchilditemtv);
					EditText ext = (EditText) ly
							.findViewById(R.id.exchilditemet);
					ext.addTextChangedListener(new MyTextWatcher(p.name));
					ext.setText(p.defaultValue);
					tiew.setText(p.dispname);
					gerLinearLayout.addView(ly);
				} else if (p.displayType.equals(s) && p.isEditable == 0
						&& p.isAdvanced == 1) {
					LinearLayout ly = (LinearLayout) getLayoutInflater()
							.inflate(R.layout.ppfxwithspinner, null);
					TextView tiew = (TextView) ly
							.findViewById(R.id.exchilditemttv);
					final Spinner sp = (Spinner) ly
							.findViewById(R.id.exchildsp);
					Spinner tempsp = sp;
					if (p.enumValues != null) {
						ArrayAdapter<String> adapter = null;
						adapter = new ArrayAdapter<String>(
								PPFXsetActivity.this,
								R.layout.customspinnertextview, p.enumValues);

						final String[] temp = p.enumValues;
						final String tempname = p.name;
						sp.setAdapter(adapter);
						int temps = 0;
						for (String st : p.enumValues) {
							if (p.defaultValue.trim().equals(st)) {
								break;
							} else {
								temps++;
							}
						}
						if (temps == p.enumValues.length)
							temps = 0;
						sp.setSelection(temps);
						sp.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent,View view, int position, long id) {
								String value = sp.getSelectedItem().toString();
								if (newValues.containsKey(tempname)) {
									newValues.remove(tempname);
								}
								newValues.put(tempname, value);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {

							}
						});
					}
					if (p.dispname.length()>0)
						tiew.setText(p.dispname);
					else
						tiew.setText(p.name);
					gerLinearLayout.addView(ly);
				}

			}
		}

		for (String s : aBigList) {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.big_parameter, null);
			TextView tv = (TextView) ll.findViewById(R.id.bigparamtertextview);
			tv.setText("高级设置");
			advLinearLayout.addView(ll);// 将大类参数显示在界面上，下面开始加载附挂到它下面的子选项
			for (Parameter p : currentLogic.parameterlist) {
				if (p.displayType.equals(s) && p.isEditable == 1
						&& p.isAdvanced == 0) {
					LinearLayout ly = (LinearLayout) getLayoutInflater()
							.inflate(R.layout.ppfxchildwithet, null);
					TextView tiew = (TextView) ly
							.findViewById(R.id.exchilditemtv);
					EditText ext = (EditText) ly
							.findViewById(R.id.exchilditemet);
					ext.addTextChangedListener(new MyTextWatcher(p.name));
					ext.setText(p.defaultValue);
					if (p.dispname.length()>0)
						tiew.setText(p.dispname);
					else
						tiew.setText(p.name);
					gerLinearLayout.addView(ly);
				} else if (p.displayType.equals(s) && p.isEditable == 0
						&& p.isAdvanced == 0) {
					LinearLayout ly = (LinearLayout) getLayoutInflater()
							.inflate(R.layout.ppfxwithspinner, null);
					TextView tiew = (TextView) ly
							.findViewById(R.id.exchilditemttv);
					final Spinner sp = (Spinner) ly
							.findViewById(R.id.exchildsp);
					Spinner tempsp = sp;
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							PPFXsetActivity.this,
							R.layout.customspinnertextview, p.enumValues);
					final String[] temp = p.enumValues;
					final String tempname = p.name;
					sp.setAdapter(adapter);
					int temps = 0;
					for (String st : p.enumValues) {
						if (p.defaultValue.trim().equals(st.trim())) {
							break;
						} else {
							temps++;
						}
					}
					if (temps == p.enumValues.length)
						temps = 0;
					sp.setSelection(temps);
					sp.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
												   View view, int position, long id) {
							String value = sp.getSelectedItem().toString();
							if (newValues.containsKey(tempname)) {
								newValues.remove(tempname);
							}
							newValues.put(tempname, value);
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {

						}
					});
					if (p.dispname.length()>0)
						tiew.setText(p.dispname);
					else
						tiew.setText(p.name);
					advLinearLayout.addView(ly);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			// Intent intent=getIntent();
			// Bundle bd=intent.getExtras();
			// intent.putExtra("stname",bd.getString("sname") );
			// intent.putExtra("dename", bd.getString("dname"));
			// PPFXsetActivity.this.setResult(1, intent);
			// SpectrumsAnalysisActivity.handle.sendEmptyMessage(0x10);
			// PPFXsetActivity.this.finish();
			dialog.show();
		}
		// else if(item.getItemId()==R.id.saveparameterschange)
		// {
		// refreshParameters();
		// }
		// else if(item.getItemId()==R.id.canclepachange)
		// {
		// loadParameters();
		// }
		// haveMenu=false;
		// invalidateOptionsMenu();
		return true;
	}

	private boolean refreshParameters()// 更新该设备的参数
	{
		Station station = GlobalData.stationHashMap.get(stationId);
		MyDevice md = null;
		for (MyDevice mydevice : station.showdevicelist) {
			if (mydevice.name.equals(deviceName)) {
				md = mydevice;
			}
		}
		LogicParameter logicparameter = md.logic.get(logicId);
		ArrayList<Parameter> pa = logicparameter.parameterlist;
		float max = 0;
		float min = 0;
		boolean will = false;
		for (Parameter p : pa) {
			if (p.name.equals("CenterFreq")) {
				max = p.maxValue;
				min = p.minValue;
				break;
			}
		}
		try {
			if ((Float.parseFloat(Objects.requireNonNull(newValues.get("CenterFreq"))) <= max)
                    && (Float.parseFloat(Objects.requireNonNull(newValues.get("CenterFreq"))) >= min)) {
				will = true;
			}
		} catch (Exception e) {
		    e.printStackTrace();
		}

		if (will) {
			for (Parameter p : pa) {
				if (newValues.containsKey(p.name)) {
				    if (p.name.contains("AudioType"))
                    {
                        if (!Objects.equals(newValues.get(p.name), p.defaultValue))
                            AnalysisBase.handle.sendEmptyMessage(AnalysisBase.FINISH);
                    }
					p.defaultValue = newValues.get(p.name);
				}
			}
			saveSetting(station);
			loadParameters();
			if (SpectrumsAnalysisActivity.handle != null)
				SpectrumsAnalysisActivity.handle
						.sendEmptyMessage(SpectrumsAnalysisActivity.PARAMETERREFRESH);
			return true;
		} else {
			Toast.makeText(PPFXsetActivity.this, "有参数输入不在正确范畴或格式不正确，请检查并重新输入",
					Toast.LENGTH_SHORT).show();
			loadParameters();
			return false;
		}
	}

	private void saveSetting(Station station) {
		for (MyDevice mydevice : station.showdevicelist) {
			if (mydevice.name.equals(deviceName)) {
				SimpleStation simpleStation = new SimpleStation(station.name, station.id, mydevice);
				if (FileOsImpl.simpleStations.contains(simpleStation)) {
					FileOsImpl.simpleStations.set(FileOsImpl.simpleStations.indexOf(simpleStation), simpleStation);
				} else {
					FileOsImpl.simpleStations.add(new SimpleStation(station.name, station.id, mydevice));
				}
				File filebase = new File(SysApplication.fileOs.forSaveFloder + File.separator + "data" + File.separator + "ForSaveStation");
				if (!filebase.getParentFile().exists()) {
					filebase.getParentFile().mkdirs();
				}
				try {
					filebase.delete();
					FileOutputStream fileOutputStream = new FileOutputStream(filebase);
					ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
					oos.writeObject(FileOsImpl.simpleStations);
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MyPagerAdapter extends PagerAdapter {
		List<View> list;

		public MyPagerAdapter(List<View> l) {
			list = l;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return "我的标题";
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(list.get(position), 0);
			return list.get(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		int single = displaywidth / 2;
		TranslateAnimation ta = new TranslateAnimation(currentpage * single,
				single * arg0, 0, 0);
		ta.setFillAfter(true);
		ta.setDuration(200);
		imageview.startAnimation(ta);
		currentpage = arg0;
	}

	@Override
	public void onClick(View v) {
		int single = displaywidth / 2;
		if (v.getId() == R.id.normal) {
			vp.setCurrentItem(0);
			if (currentpage != 0) {
				TranslateAnimation ta = new TranslateAnimation(currentpage
						* single, 0, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				imageview.startAnimation(ta);
			}
			currentpage = 0;
		}
		if (v.getId() == R.id.advanced) {
			vp.setCurrentItem(1);
			if (currentpage != 1) {
				TranslateAnimation ta = new TranslateAnimation(currentpage
						* single, single, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				imageview.startAnimation(ta);
			}
			currentpage = 1;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			dialog.show();
		}
		return true;
	}

}
