package com.mrpoid.mrplist;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.mrpoid.app.EmuPreferenceActivity;
import com.mrpoid.app.HelpActivity;
import com.mrpoid.core.EmuPath;
import com.mrpoid.core.EmuPath.OnPathChangeListener;
import com.mrpoid.core.Emulator;
import com.mrpoid.core.MrpFile;
import com.mrpoid.core.MrpScreen;
import com.mrpoid.core.Prefer;
import com.mrpoid.core.Res;
import com.mrpoid.mrpliset.R;


/**
 * 文件列表
 * 
 * @author Yichou
 * 
 */
public class MrplistActivity extends BaseActivity implements ActionBar.OnNavigationListener {
	public static final String TAG = "MrplistActivity";
	
	public static final String PREF_FILE_NAME = "mrplist";
	public static final String SHARE_URL = "http://www.mrpej.com/bbs/book_list.aspx?action=class&siteid=1000&classid=847";

	private MrpListFragment listFmg;  
	private boolean needRefresh = false;

	private static boolean includeDir;
	private static String mCurPath = "";
	
	static final int[] skinResources = { R.drawable.wallpaper0,
			R.drawable.wallpaper1,
			R.drawable.wallpaper2,
			R.drawable.wallpaper3,
			R.drawable.wallpaper4,
		};
	
	private Drawable oriBgDrawable;
	private String[] screenSizeEnties;
	
	
	private void initSkin() {
		oriBgDrawable = getWindow().getDecorView().getBackground();
		
		SharedPreferences sp = getSharedPreferences(PREF_FILE_NAME, 0);
		setSkin(sp.getInt("skin_index", 0));
	}
	
	private void setSkin(int index) {
		if(index == MENU_BG_ITEMS.length-1){ //无
			getWindow().setBackgroundDrawable(oriBgDrawable);
		}else {
			SharedPreferences sp = getSharedPreferences(PREF_FILE_NAME, 0);
			sp.edit().putInt("skin_index", index).commit();
			
			getWindow().setBackgroundDrawableResource(skinResources[index]);
		}
	}
	
	public boolean isRootDir() {
		return mCurPath.length() == 0;
	}

	public String downDir(String name) {
		mCurPath += name + "/";
		return mCurPath;
	}

	public String upDir() {
		if (isRootDir())
			return mCurPath;

		int i = mCurPath.lastIndexOf('/');
		String s = mCurPath.substring(0, i);
		i = s.lastIndexOf('/');
		if (i == -1) {
			mCurPath = "";
			// 根目录了
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			mCurPath = s.substring(0, i);
			mCurPath += "/";
		}

		return mCurPath;
	}

	public static String getRelativePath(String name) {
		return (mCurPath + name);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		if(Prefer.THEME != R.style.Theme_Sherlock_Light_DarkActionBar && Prefer.THEME != R.style.Theme_Sherlock)
			Prefer.THEME = R.style.Theme_Sherlock;
		setTheme(Prefer.THEME);

		super.onCreate(arg0);
		Log.i(TAG, "onCreate");
		
		SharedPreferences sp = getSharedPreferences(PREF_FILE_NAME, 0);
		includeDir = sp.getBoolean("includeDir", false);

		getSupportActionBar().setDisplayUseLogoEnabled(true);

		FragmentManager fm = getSupportFragmentManager();
		if ((listFmg = (MrpListFragment) fm.findFragmentById(android.R.id.content)) == null) {
			listFmg = new MrpListFragment();
			fm.beginTransaction().add(android.R.id.content, listFmg).commit();
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		EmuPath.getInstance().addOnPathChangeListener(new OnPathChangeListener() {
			@Override
			public void onPathChanged(String newPath, String oldPath) {
				needRefresh = true;
			}
		});
		
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
		
		SharedPreferences sp = getSharedPreferences("mrplist", 0);
		Editor editor = sp.edit();
		editor.putBoolean("includeDir", includeDir);
		editor.commit();
	}

	private boolean againToExit;

	@Override
	public void onBackPressed() {
		if (!isRootDir()) {
			upDir();
			listFmg.refreshList();
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
//		return (Prefer.THEME == R.style.Theme_Sherlock_Light_DarkActionBar);
	}
	
	@Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		MrpScreen.parseScreenSize(screenSizeEnties[itemPosition]);
        return true;
    }
	
	static final String[] MENU_BG_ITEMS = { "背景1", "背景2", "背景3", "背景4", "背景5", "无" };
	
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
		MenuItem item2 = subOptions.add(R.id.mg_option, R.id.mi_include_dir, i++, R.string.include_dir);
		item2.setCheckable(true);
		item2.setChecked(includeDir);

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

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getGroupId() == R.id.mg_option) {
			if (item.getItemId() == android.R.id.home) {
				if (!isRootDir()) {
					upDir();
					if (listFmg != null)
						listFmg.refreshList();
				} else {
					finish();
				}
				return true;
			} else if (item.getItemId() == R.id.mi_refresh) { 
				listFmg.refreshList();
			} else if (item.getItemId() == R.id.mi_include_dir) {
				includeDir = !includeDir;
				item.setChecked(includeDir);
				listFmg.refreshList();
			} else if (item.getItemId() == R.id.mi_pref) {
				startActivity(new Intent(this, EmuPreferenceActivity.class));
			} else if (item.getItemId() == R.id.mi_help) {
				startActivity(new Intent(this, HelpActivity.class).setData(Res.HELP_URI_ASSET));
			} else if (item.getItemId() == R.id.mi_about) {
				new AboutActivity(this).show();
//				startActivity(new Intent(this, HelpActivity.class).setData(Res.ABOUT_URI_ASSET));
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
				Prefer.THEME = R.style.Theme_Sherlock;
				Toast.makeText(this, "设置成功，重启后生效！", Toast.LENGTH_SHORT).show();
			} else if (item.getItemId() == R.id.mi_theme_light) {
				Prefer.THEME = R.style.Theme_Sherlock_Light_DarkActionBar;
				Toast.makeText(this, "设置成功，重启后生效！", Toast.LENGTH_SHORT).show();
			} 
		} else if (item.getGroupId() == R.id.mg_skin_bg) {
			setSkin(item.getItemId());
		}

		return true;
	}

	/**
	 * 加载器
	 * 
	 * @author Yichou
	 * 
	 */
	public static class MrpListLoader extends AsyncTaskLoader<List<MrpFile>> {
		List<MrpFile> mMrps = new ArrayList<MrpFile>(100);

		public MrpListLoader(Context context) {
			super(context);
		}

		@Override
		public List<MrpFile> loadInBackground() {
			mMrps.clear();
			findMrpFiles();

			Collections.sort(mMrps);

			return mMrps;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(List<MrpFile> apps) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					onReleaseResources(apps);
				}
			}

			List<MrpFile> oldApps = apps;
			mMrps = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		@Override
		protected void onStartLoading() {
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

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mMrps != null) {
				onReleaseResources(mMrps);
				mMrps = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<MrpFile> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}

		public String getExt(String s1) {
			int i = s1.lastIndexOf('.');
			if (i != -1) {
				return s1.substring(i);
			}

			return ""; // 避免空指针异常
		}

		public FileFilter mrpFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return includeDir? isContainMrp(f) : false;
				} else if (f.isFile()) {
					return getExt(f.getName()).equalsIgnoreCase(".mrp");
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
			File dir = EmuPath.getInstance().getFullFilePath(mCurPath);//new File(getFullPath());
			File[] files;

			files = dir.listFiles(mrpFilter);

			if (files != null) {
				for (File f : files) {
					mMrps.add(new MrpFile(f));
				}
			}
		}
	}

	private static class MrpListAdapter extends BaseAdapter {
		@SuppressWarnings("unused")
		private Context context;
		private LayoutInflater mInflater;
		private Bitmap bmp_dir, bmp_file;
		private List<MrpFile> mFileList = new ArrayList<MrpFile>(100);

		public MrpListAdapter(Activity activity) {
			this.context = activity;
			bmp_dir = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_dir);
			bmp_file = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_file);
			mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		protected void finalize() throws Throwable {
			bmp_dir.recycle();
			bmp_file.recycle();
			super.finalize();
		}

		public void setData(List<MrpFile> data) {
			mFileList.clear();
			if (data != null) {
				mFileList.addAll(data);
			}
			notifyDataSetChanged();
		}

		public void removeItem(int position) {
			mFileList.remove(position);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFileList.size();
		}

		@Override
		public Object getItem(int position) {
			return mFileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHoder hoder;

			MrpFile file = mFileList.get(position);

			if (convertView == null) {
				hoder = new ViewHoder();

				convertView = mInflater.inflate(R.layout.list_item1b, null);
				hoder.tv_appName = (TextView) convertView.findViewById(R.id.textView1);
				hoder.tv_fileName = (TextView) convertView.findViewById(R.id.textView2);
				hoder.tv_size = (TextView) convertView.findViewById(R.id.textView3);
				hoder.icon = (ImageView) convertView.findViewById(R.id.imageView1);

				convertView.setTag(hoder);
			} else {
				hoder = (ViewHoder) convertView.getTag();
			}

			if (file.isFile()) {
				if (file.getAppName() == null) {
					file.setAppName(Emulator.native_getAppName(EmuPath.getInstance().getFullFilePath(mCurPath + file.getName()).getAbsolutePath()));
				}

				hoder.tv_fileName.setText(file.getName());
				hoder.tv_appName.setText(file.getAppName());
				hoder.tv_size.setText(coverSize(file.length));
			} else {
				hoder.tv_appName.setText(file.getName());
				hoder.tv_size.setText("");
				hoder.tv_fileName.setText("文件夹");
			}
			hoder.icon.setImageBitmap(file.isDir ? bmp_dir : bmp_file);

			return convertView;
		}

		private String coverSize(long size) {
			String s = "";

			if (size < 1024)
				s += size + "b";
			else if (size < 1024 * 1024) {
				s = String.format(Locale.US, "%.2f K", size / 1024f);
			} else if (size < 1024 * 1024 * 1024) {
				s = String.format(Locale.US, "%.2f M", size / 1024 / 1024f);
			} else {
				s = String.format(Locale.US, "%.2f G", size / 1024 / 1024 / 1024f);
			}

			return s;
		}

		// 保存视图
		private final class ViewHoder {
			TextView tv_fileName, tv_appName, tv_size;
			ImageView icon;
		}
	}

	public static class MrpListFragment extends SherlockListFragment implements LoaderCallbacks<List<MrpFile>> {
		MrpListAdapter mAdapter;
		MrplistActivity activity;
		private int lastPress;

		@Override
		public void onAttach(Activity activity) {
			this.activity = (MrplistActivity) activity;
			super.onAttach(activity);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			setEmptyText("请将mrp放在\n" + EmuPath.getInstance().getFullPath() + "\n运行。");
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setHasOptionsMenu(true);

			mAdapter = new MrpListAdapter(getActivity());
			setListAdapter(mAdapter);
			setListShown(false);

			getLoaderManager().initLoader(1001, null, this);

			ListView listView = getListView();
			registerForContextMenu(listView);
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					lastPress = position;
					// Log.i("---", "long press item p="+position);
					return false;
				}
			});
			listView.setCacheColorHint(Color.TRANSPARENT);
			listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
			listView.setDividerHeight(1);
			
//			LayoutParams p = listView.getLayoutParams();
//			p.
		}

		@Override
		public void onDestroyView() {
			unregisterForContextMenu(getListView());
			super.onDestroyView();
		}

		@Override
		public Loader<List<MrpFile>> onCreateLoader(int arg0, Bundle arg1) {
			return new MrpListLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<MrpFile>> loader, List<MrpFile> arg1) {
			mAdapter.setData(arg1);

			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<List<MrpFile>> arg0) {
			mAdapter.setData(null);
		}

		public void refreshList() {
			//没有动画隐藏 listView 
			setListShownNoAnimation(false);
			getLoaderManager().restartLoader(1001, null, this);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			MrpFile file = (MrpFile) mAdapter.getItem(position);

			lastPress = position;
			// Log.i("---", "item press p="+position);

			if (file.isDir) {
				activity.downDir(file.getName());
				activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				refreshList();
			} else {
				Emulator.startMrp(activity, getRelativePath(file.getName()), file);
			}
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			MrpFile file = (MrpFile) mAdapter.getItem(lastPress);

			// if(file.isFile())
			menu.add(0, R.id.mi_remove, 0, R.string.remove);
			if(file.isFile())
				menu.add(0, R.id.mi_create_shortcut, 0, R.string.create_shortcut);
			// else if(file.isDir())
			// menu.add(0, R.id.mi_remove, 0, R.string.remove);
		}

		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			MrpFile file = (MrpFile) mAdapter.getItem(lastPress);
			
			if (item.getItemId() == R.id.mi_remove) {
				if (file.isFile()) {
					if (file.toFile(EmuPath.getInstance().getFullPath()).delete()) {
						mAdapter.removeItem(lastPress);
						Log.d(Emulator.TAG, "delete mrp = " + file.getName());
					}
				} else {
					// 设置该目录为排除
				}
			} else if (item.getItemId() == R.id.mi_create_shortcut) {
				ShortcutUtils.createShortCut(getActivity(), 
						file.getAppName(), 
						ShortcutUtils.getAppIcon(getActivity()),
						file.toFile(EmuPath.getInstance().getFullPath()));
			} else {
				return super.onContextItemSelected(item);
			}

			return true;
		}
	}
}
