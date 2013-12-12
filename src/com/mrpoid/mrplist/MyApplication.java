package com.mrpoid.mrplist;

import com.mrpoid.app.EmulatorApplication;
import com.mrpoid.mrplist.core.FileType;
import com.mrpoid.mrplist.core.PreferencesProvider;

/**
 * 
 * @author Yichou 2013-11-23
 * 
 */
public class MyApplication extends EmulatorApplication {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		PreferencesProvider.load(this);
		
		FileType.loadIcons(getResources());
	}
	
}
