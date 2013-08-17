package com.mrpoid.mrplist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mrpoid.mrpliset.R;

/**
 * “关于”对话框
 * 
 * @author wang jun
 */
public class AboutActivity extends Dialog {

	private TextView mVersionTextView;
	private Button mTermsButton;
	private Button mCompanyButton;
	private Button mCancelButton;
	private ViewFlipper mCompanyViewFlipper;

	public AboutActivity(Activity context) {
		super(context);
		init(context);
	}

	public AboutActivity(Activity context, int theme) {
		super(context, theme);
		init(context);
	}

	public AboutActivity(Activity context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	/**
	 * 构造函数之间的可共享代码
	 */
	private void init(final Activity context) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		String topText = " " + context.getString(R.string.about_note);

		mVersionTextView = (TextView) findViewById(R.id.VersionText);
		mVersionTextView.setText(topText);

		mTermsButton = (Button) findViewById(R.id.TermsButton);
		mTermsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://wap.mrpej.com"));
				context.startActivity(myIntent);
			}

		});

		mCompanyViewFlipper = (ViewFlipper) findViewById(R.id.CompanyViewFlipper);
		mCompanyButton = (Button) findViewById(R.id.AboutCompanyButton);
		mCompanyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int currentCompany = mCompanyViewFlipper.getDisplayedChild();
				if (currentCompany == 0) {
					mCompanyButton.setText(R.string.about_jamendo);
					mCompanyViewFlipper.setDisplayedChild(1);
				} else {
					mCompanyButton.setText(R.string.about_teleca);
					mCompanyViewFlipper.setDisplayedChild(0);
				}
			}

		});

		mCancelButton = (Button) findViewById(R.id.CancelButton);
		mCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AboutActivity.this.dismiss();
			}

		});
	}

}
