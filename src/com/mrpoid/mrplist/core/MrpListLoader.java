package com.mrpoid.mrplist.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


/**
 * mrp加载器
 * 
 * @author Yichou 2013-11-23
 *
 */
public class MrpListLoader extends AsyncTaskLoader<List<MrpFile>> {
	private List<MrpFile> mCacheList = new ArrayList<MrpFile>(128);
	private File mPath;
	private boolean mIsRoot;
	

	public MrpListLoader(Context context, String path, boolean isRoot) {
		super(context);
		mPath = new File(path);
		mIsRoot = isRoot;
	}

	@Override
	public List<MrpFile> loadInBackground() {
		mCacheList.clear();
		findMrpFiles();
		Collections.sort(mCacheList);

		return mCacheList;
	}

	/**
	 * Called when there is new data to deliver to the client. The super
	 * class will take care of delivering it; the implementation here just
	 * adds a little more logic.
	 */
	@Override
	public void deliverResult(List<MrpFile> list) {
//		System.out.println("MrpListLoader.deliverResult()");
		
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (list != null) {
				onReleaseResources(list);
			}
		}

		List<MrpFile> oldList = list;
		mCacheList = list;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(list);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldList != null) {
			onReleaseResources(oldList);
		}
	}

	@Override
	protected void onStartLoading() {
//		System.out.println("MrpListLoader.onStartLoading()");
		
		forceLoad();
	}

	/**
	 * 外部请求停止加载
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * 取消加载的动作
	 */
	@Override
	public void onCanceled(List<MrpFile> apps) {
		super.onCanceled(apps);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(apps);
	}

	/**
	 * 外部请求重置加载器后
	 */
	@Override
	protected void onReset() {
		super.onReset();
		
//		System.out.println("MrpListLoader.onReset()");

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mCacheList != null) {
			onReleaseResources(mCacheList);
			mCacheList = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with
	 * an actively loaded data set.
	 */
	protected void onReleaseResources(List<MrpFile> list) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

	private final FileFilter mrpFilter = new FileFilter() {
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return PreferencesProvider.Interface.General.getShowDir(true)? isContainMrp(f) : false;
			} else if (f.isFile()) {
				String name = f.getName();
				int ss = name.lastIndexOf('.'); //最后一个 .
				if(ss != -1) {
					return name.regionMatches(true, ss, ".mrp", 0, 4);
				}
			}

			return false;
		}
	};

	/**
	 * 检测一个文件夹是否包含MRP文件
	 * 
	 * @param path
	 * @return
	 */
	public boolean isContainMrp(File path) {
		return true;
	}
	
	public boolean isContainMrp1(File path) {
		File[] files = path.listFiles(mrpFilter);

		if (files == null || files.length == 0)
			return false;

		for (File f : files) {
			if (f.isFile()) // 是文件就是 MRP
				return true;

			if (isContainMrp(f)) // 递归子目录
				return true;
		}

		return false;
	}
	
	public void findMrpFiles() {
		if(mPath == null)
			return;
		
		File[] files = mPath.listFiles(/*mrpFilter*/);
		
		if(!mIsRoot)
			mCacheList.add(new MrpFile()); //..

		if (files != null && files.length > 0) {
			for (File f : files) {
				mCacheList.add(new MrpFile(f));
			}
		}
	}
}
