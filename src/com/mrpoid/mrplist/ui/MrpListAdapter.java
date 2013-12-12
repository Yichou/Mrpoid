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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrpoid.mrpliset.R;
import com.mrpoid.mrplist.core.MrpFile;
import com.mrpoid.mrplist.core.PreferencesProvider;


/**
 * 列表适配器
 * 
 * @author Yichou 2013-11-23
 *
 */
public final class MrpListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater mInflater;
	private List<MrpFile> mData;
//	private Bitmap bmp_dir, bmp_file;

	
	public MrpListAdapter(Activity activity) {
		this.context = activity;
//		bmp_dir = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_folder);
//		bmp_file = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_file2);
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void flush(List<MrpFile> newData) {
		this.mData = newData;
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mData==null? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHoder hoder;

		MrpFile file = mData.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			hoder = new ViewHoder();

			hoder.tv_title = (TextView) convertView.findViewById(R.id.textView1);
			hoder.tv_msg = (TextView) convertView.findViewById(R.id.textView2);
			hoder.tv_size = (TextView) convertView.findViewById(R.id.textView3);
			hoder.icon = (ImageView) convertView.findViewById(R.id.imageView1);
			
			if(PreferencesProvider.Interface.General.getThemeImage(0) >= 2) {
				convertView.setBackgroundColor(0x20000000);
			}

			convertView.setTag(hoder);
		} else {
			hoder = (ViewHoder) convertView.getTag();
		}

		hoder.tv_title.setText(file.getTtile());
		hoder.tv_msg.setText(file.getMsg());
		hoder.tv_size.setText(file.getSizeString());
//		hoder.icon.setImageBitmap(file.getType().getIconBitmap(context.getResources()));
		hoder.icon.setImageResource(file.getType().getIconRes());

		return convertView;
	}

	// 保存视图
	static final class ViewHoder {
		TextView tv_title, tv_msg, tv_size;
		ImageView icon;
	}
}