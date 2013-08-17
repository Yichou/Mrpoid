package com.mrpoid.mrplist;

import android.app.Activity;
import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.yichou.sdk.CheckUpdateCallback;
import com.yichou.sdk.DownloadCallback;
import com.yichou.sdk.SdkInterface;

public class SdkImpl implements SdkInterface, UmengUpdateListener, UmengDownloadListener {
	private CheckUpdateCallback checkUpdateCallback;
	private DownloadCallback downloadCallback;
	private Activity mActivity;
	

	public SdkImpl(final Activity activity) {
		this.mActivity = activity;
		
		UmengUpdateAgent.setUpdateListener(this);
		UmengUpdateAgent.setOnDownloadListener(this);
	}
	
	 @Override
     public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
         switch (updateStatus) {
         
         case 0: // has update
        	 if(checkUpdateCallback != null) 
        		 checkUpdateCallback.onCheckUpdateRet(CheckUpdateCallback.RET_HAS_NEW, updateInfo);
             break;
         case 1: // has no update
        	 if(checkUpdateCallback != null) 
        		 checkUpdateCallback.onCheckUpdateRet(CheckUpdateCallback.RET_NO_NEW, updateInfo);
             break;
         case 2: // none wifi
        	 if(checkUpdateCallback != null) 
        		 checkUpdateCallback.onCheckUpdateRet(CheckUpdateCallback.RET_NO_WIFI, updateInfo);
             break;
         case 3: // time out
        	 if(checkUpdateCallback != null) 
        		 checkUpdateCallback.onCheckUpdateRet(CheckUpdateCallback.RET_FAILUE, updateInfo);
             break;
         }
     }

	@Override
	public void setUpdateCfg(boolean wifiOnly, boolean autoPopup) {
		UmengUpdateAgent.setUpdateOnlyWifi(wifiOnly);
		UmengUpdateAgent.setUpdateAutoPopup(autoPopup);
	}

	@Override
	public void updateOnlineParams(Context context) {
		MobclickAgent.updateOnlineConfig(context);
	}

	@Override
	public void showUpdateDialog(Context context, Object data) {
		UmengUpdateAgent.showUpdateDialog(mActivity, (UpdateResponse) data);
	}

	@Override
	public void event(Context context, String id, String data) {
		MobclickAgent.onEvent(context, id, data);
	}

	@Override
	public void checkUpdate(Context context) {
		UmengUpdateAgent.update(context);
	}

	@Override
	public void onPause(Activity context) {
		MobclickAgent.onPause(context);
	}

	@Override
	public void onResume(Activity context) {
		MobclickAgent.onResume(context);
	}

	@Override
	public void enableCrashHandle(Context context, boolean enable) {
		MobclickAgent.onError(context);
	}

	@Override
	public void OnDownloadEnd(int arg0) {
		if(downloadCallback != null){
			if(arg0 == 1)
				downloadCallback.OnDownloadRet(DownloadCallback.RET_SUCCESS);
			else
				downloadCallback.OnDownloadRet(DownloadCallback.RET_FAILUE);
		}
	}

	@Override
	public void setCheckUpdateCallback(CheckUpdateCallback cb) {
		this.checkUpdateCallback = cb;
	}

	@Override
	public void setDownloadCallback(DownloadCallback cb) {
		this.downloadCallback = cb;
	}
}
