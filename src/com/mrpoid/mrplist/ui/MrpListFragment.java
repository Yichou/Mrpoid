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

import java.io.File;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.mrpoid.core.Emulator;
import com.mrpoid.mrpliset.R;
import com.mrpoid.mrplist.core.FileType;
import com.mrpoid.mrplist.core.MrpFile;
import com.mrpoid.mrplist.core.MrpListLoader;
import com.mrpoid.mrplist.core.PreferencesProvider;
import com.mrpoid.mrplist.utils.ShortcutUtils;


public class MrpListFragment extends SherlockListFragment 
	implements LoaderCallbacks<List<MrpFile>>, OnItemLongClickListener {
	static final String TAG = "MrpListFragment";
	
	static final char PATH_SEP = File.separatorChar;

	List<MrpFile> mData;
	MrpListAdapter mAdapter;
	MrplistActivity activity;
	int mLongPressIndex;
	int mFocuseIndex;
	
	private final Stack<String> mPathStack = new Stack<String>();
	private final Stack<Integer> mFocuseStack = new Stack<Integer>();
	
	
	private void pushPath(String path, int focuseIndex) {
		mPathStack.push(path);
		
		focuseIndex = getListView().getFirstVisiblePosition();
		mFocuseStack.push(focuseIndex);
	}
	
	private void initPathStack() {
		pushPath("/", 0); //root目录
		
		File sdPath = Environment.getExternalStorageDirectory();
		pushPath(sdPath.getParent() + PATH_SEP, 0); //sd卡所在目录
		pushPath(sdPath.getAbsolutePath() + PATH_SEP, 0); //sd卡根目录
	}
	
	public boolean isRootDir() { //根目录
		return mPathStack.size() == 1;
	}

	public String inDir(String name, int position) {
		String cur = mPathStack.peek() + name + PATH_SEP;
		pushPath(cur, position);
		
		refreshListInner(0); //新进入的页面 焦点在 0

		enableHomeUpBtn();
		
		return cur;
	}

	public String outDir() {
		if (isRootDir()) {
			disableHomeUpBtn();
			return mPathStack.peek();
		}
		
		mPathStack.pop();
		refreshListInner(mFocuseStack.pop());
		
		return mPathStack.peek();
	}
	
	public void refreshList() {
		refreshListInner(getListView().getFirstVisiblePosition());
	}
	
	private void refreshListInner(int position) {
		mFocuseIndex = position;
		setListShownNoAnimation(false); //没有动画隐藏 listView，显示旋转进度
		getLoaderManager().restartLoader(1001, null, this);
	}
	
	public void removeItem(int position) {
		if(position < 0 || position > mData.size())
			return;
		
		MrpFile file = mData.get(position);
		
		Log.d(TAG, "delete file = " + file.getName());
		
		if (file.isFile()) {
			if (file.toFile().delete()) {
				Log.i(TAG, "remove file suc!");
				mData.remove(position);
			}
		} else {
			// 设置该目录为排除
		}

		mAdapter.flush(mData);
	}
	
	private void enableHomeUpBtn() {
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void disableHomeUpBtn() {
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = (MrplistActivity) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initPathStack();
		
		setHasOptionsMenu(true);
		setListShown(false);

		mAdapter = new MrpListAdapter(getActivity());
		setListAdapter(mAdapter);

		ListView listView = getListView();
		registerForContextMenu(listView);
		listView.setOnItemLongClickListener(this);
		
		listView.setFastScrollEnabled(true);
		listView.setCacheColorHint(Color.TRANSPARENT);

		if(PreferencesProvider.Interface.General.getThemeImage(0) >= 2) {
			listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
			listView.setDividerHeight(1);
		}

		getLoaderManager().initLoader(1001, null, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setEmptyText("请将mrp放在\n" 
//				+ EmuPath.getInstance().getFullPath() 
				+ "\n运行。");
	}
	
	@Override
	public void onDestroyView() {
		unregisterForContextMenu(getListView());
		super.onDestroyView();
	}

	@Override
	public Loader<List<MrpFile>> onCreateLoader(int id, Bundle data) {
//		System.out.println("MrpListFragment.onCreateLoader(" + id);
		
		if(id == 1001){
			return new MrpListLoader(getActivity(), mPathStack.peek(), isRootDir());
		}
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<List<MrpFile>> loader, List<MrpFile> list) {
		mData = list;
		mAdapter.flush(mData);

		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

		getListView().setSelection(mFocuseIndex);
	}

	@Override
	public void onLoaderReset(Loader<List<MrpFile>> arg0) {
		System.out.println("MrpListFragment.onLoaderReset()");
		
		mData = null;
		mAdapter.flush(mData);
		getListView().setSelection(mFocuseIndex);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mLongPressIndex = position;
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		MrpFile file = mData.get(position);

		if (file.isParent()) { //返回上级标记
			outDir();
		} else if (file.isDir()) {
			inDir(file.getName(), position);
		} else if (file.getType() == FileType.MRP) {
			Emulator.startMrp(getActivity(), file.getPath());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(mLongPressIndex < 0 || mLongPressIndex >= mData.size())
			return;
		
		MrpFile file = mData.get(mLongPressIndex);

		menu.add(0, R.id.mi_remove, 0, R.string.remove);
		if(file.isFile())
			menu.add(0, R.id.mi_create_shortcut, 0, R.string.create_shortcut);
		// else if(file.isDir())
		// menu.add(0, R.id.mi_remove, 0, R.string.remove);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if(mLongPressIndex < 0 || mLongPressIndex >= mData.size())
			return false;
		
		MrpFile file = mData.get(mLongPressIndex);
		
		if (item.getItemId() == R.id.mi_remove) {
			removeItem(mLongPressIndex);
		} else if (item.getItemId() == R.id.mi_create_shortcut) {
			ShortcutUtils.createShortCut(getActivity(), 
					file.getTtile(), 
					ShortcutUtils.getAppIcon(getActivity()),
					file.toFile());
		} else {
			return super.onContextItemSelected(item);
		}

		return true;
	}
}