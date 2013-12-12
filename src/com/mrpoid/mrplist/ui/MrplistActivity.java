/*
 * Copyright (C) 2013 The Mrpoid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mrpoid.mrplist.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.mrpoid.app.EmuPreferenceActivity;
import com.mrpoid.app.HelpActivity;
import com.mrpoid.core.MrpScreen;
import com.mrpoid.core.Res;
import com.mrpoid.mrpliset.R;
import com.mrpoid.mrplist.core.PreferencesProvider;


/**
 * 文件列表
 * 
 * @author Yichou
 * 
 */
public class MrplistActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {
	public static final String TAG = "MrplistActivity";
	public static final String SHARE_URL = "http://www.mrpej.com/bbs/book_list.aspx?action=class&siteid=1000&classid=847";

	private MrpListFragment listFmg;  
	private boolean needRefresh = false;

	
	static final int[] skinResources = {
//		android.R.drawable.screen_background_dark_transparent,
//		android.R.drawable.screen_background_light_transparent,
		android.R.color.transparent,
		android.R.color.transparent,
		R.drawable.wallpaper0,
		R.drawable.wallpaper1,
		R.drawable.wallpaper2,
		R.drawable.wallpaper3,
		R.drawable.wallpaper4,
	};

	static final String[] MENU_BG_ITEMS = { 
		"系统黑",
		"系统白",
		"壁纸1", 
		"壁纸2", 
		"壁纸3", 
		"壁纸4", 
		"壁纸5"
	};
	
	private String[] screenSizeEnties;
	
	
	private void initSkin() {
		setSkin(PreferencesProvider.Interface.General.getThemeImage(2));
	}
	
	private void setSkin(int index) {
		PreferencesProvider.Interface.General.setThemeImage(this, index);
		if(index < 2) {
			getTheme().applyStyle(index == 0? R.style.Theme_Sherlock 
					: R.style.Theme_Sherlock_Light_DarkActionBar, 
					true);
			
			getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		} else {
			getWindow().setBackgroundDrawableResource(skinResources[index]);
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		setTheme(PreferencesProvider.Interface.General.getThemeImage(0) == 1? 
				R.style.Theme_Sherlock_Light_DarkActionBar : R.style.Theme_Sherlock );
		
		super.onCreate(arg0);
		Log.i(TAG, "onCreate");
		
		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayUseLogoEnabled(true);

		FragmentManager fm = getSupportFragmentManager();
		if ((listFmg = (MrpListFragment) fm.findFragmentById(android.R.id.content)) == null) {
			listFmg = new MrpListFragment();
			fm.beginTransaction().add(android.R.id.content, listFmg).commit();
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		initSkin();
		
		addScreenSizeNavigation();
	}
	
	private void addScreenSizeNavigation() {
		screenSizeEnties = getResources().getStringArray(R.array.screensize_entries);
		Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, 
        		R.array.screensize_entries, 
        		R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        
        for(int i=0; i<screenSizeEnties.length; ++i){
        	if(screenSizeEnties[i].equals(MrpScreen.getSizeTag())){
        		getSupportActionBar().setSelectedNavigationItem(i);
        		break;
        	}
        }
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();

		againToExit = false;
		if (needRefresh) {
			// 刷新列表
			if (listFmg != null)
				listFmg.refreshList();
			needRefresh = false;
		}
		
//		final ActionBar ab = getSupportActionBar();
//		ab.setTitle(getString(R.string.title_activity_filex) + " " + MrpScreen.getSizeTag());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private boolean againToExit;

	@Override
	public void onBackPressed() {
		if (!listFmg.isRootDir()) {
			listFmg.outDir();
		} else {
			if (!againToExit) {
				againToExit = true;
				Toast.makeText(this, R.string.hint_again_to_exit, Toast.LENGTH_SHORT).show();
			} else {
				finish();
			}
		}
	}

	public static boolean isLightTheme() {
		return false;
	}
	
	@Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		MrpScreen.parseScreenSize(screenSizeEnties[itemPosition]);
        return true;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subSkin = menu.addSubMenu(0, R.id.mi_theme, 0, R.string.theme);
		subSkin.setIcon(isLightTheme() ? R.drawable.ic_theme : R.drawable.ic_theme_drak);
		subSkin.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		SubMenu subColor = subSkin.addSubMenu(0, 0, 0, R.string.color);
		subColor.add(R.id.mg_skin_color, R.id.mi_theme_black, 0, R.string.dark);
		subColor.add(R.id.mg_skin_color, R.id.mi_theme_light, 1, R.string.light);
		
		SubMenu subBg = subSkin.addSubMenu(0, 1, 1, R.string.image);
		for(int i=0; i<MENU_BG_ITEMS.length; ++i){
			subBg.add(R.id.mg_skin_bg, i, i, MENU_BG_ITEMS[i]);
		}
		
		SubMenu subOptions = menu.addSubMenu(0, R.id.mi_menu, 1, R.string.menu);
		subOptions.setIcon(isLightTheme() ? R.drawable.ic_menu : R.drawable.ic_menu_dark);
		subOptions.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		int i = 0;
		MenuItem item2 = subOptions.add(R.id.mg_option, R.id.mi_show_dir, i++, R.string.include_dir);
		item2.setCheckable(true);
		item2.setChecked(PreferencesProvider.Interface.General.getShowDir(false));

		subOptions.add(R.id.mg_option, R.id.mi_refresh, i++, R.string.refresh);
		subOptions.add(R.id.mg_option, R.id.mi_pref, i++, R.string.settings);
		subOptions.add(R.id.mg_option, R.id.mi_about, i++, R.string.about);
		subOptions.add(R.id.mg_option, R.id.mi_shequ, i++, "社区");
		subOptions.add(R.id.mg_option, R.id.mi_share, i++, R.string.share);
		subOptions.add(R.id.mg_option, R.id.mi_exit, i++, R.string.exit);
		
		return true;
	}
	
	Activity getActivity() {
		return this;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getGroupId() == R.id.mg_option) {
			if (item.getItemId() == android.R.id.home) {
				onBackPressed();
				return true;
			} else if (item.getItemId() == R.id.mi_refresh) { 
				listFmg.refreshList();
			} else if (item.getItemId() == R.id.mi_show_dir) {
				boolean b = PreferencesProvider.Interface.General.getShowDir(false);
				PreferencesProvider.Interface.General.setShowDir(this, !b);
				
				listFmg.refreshList();
			} else if (item.getItemId() == R.id.mi_pref) {
				startActivity(new Intent(this, EmuPreferenceActivity.class));
			} else if (item.getItemId() == R.id.mi_help) {
				startActivity(new Intent(this, HelpActivity.class).setData(Res.HELP_URI_ASSET));
			} else if (item.getItemId() == R.id.mi_about) {
				new AboutActivity(this).show();
				startActivity(new Intent(this, HelpActivity.class).setData(Res.ABOUT_URI_ASSET));
			} else if (item.getItemId() == R.id.mi_exit) {
				finish();
			} else if (item.getItemId() == R.id.mi_shequ) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SHARE_URL));
				startActivity(intent);
			} else if (item.getItemId() == R.id.mi_share) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share));
				intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg) + "\n" + SHARE_URL);
				startActivity(Intent.createChooser(intent, getTitle()));
			}
		} else if (item.getGroupId() == R.id.mg_skin_color) {
			if (item.getItemId() == R.id.mi_theme_black) {
				PreferencesProvider.Interface.General.setDarkTheme(this, true);
				Toast.makeText(this, "设置成功，重启后生效！", Toast.LENGTH_SHORT).show();
			} else if (item.getItemId() == R.id.mi_theme_light) {
				PreferencesProvider.Interface.General.setDarkTheme(this, false);
				Toast.makeText(this, "设置成功，重启后生效！", Toast.LENGTH_SHORT).show();
			} 
		} else if (item.getGroupId() == R.id.mg_skin_bg) {
			setSkin(item.getItemId());
		}

		return true;
	}
}
