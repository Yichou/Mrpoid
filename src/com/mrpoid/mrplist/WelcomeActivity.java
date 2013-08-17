package com.mrpoid.mrplist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.mrpoid.mrpliset.R;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
