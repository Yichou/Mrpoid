package com.mrpoid.mrplist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.mrpoid.mrpliset.R;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sp = getPreferences(0);
		if(!sp.getBoolean("showLogo", true)) {
			startActivity(new Intent(WelcomeActivity.this, MrplistActivity.class));
			finish();
			return;
		}
		
		sp.edit().putBoolean("showLogo", false).commit();
		
		setContentView(R.layout.activity_welcome);
		
//		ImageView imageView = findViewById(R.id.imageView1);
//		Animation a = AnimationDrawable.
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				startActivity(new Intent(WelcomeActivity.this, MrplistActivity.class));
			}
		}, 2000);
	}

}
