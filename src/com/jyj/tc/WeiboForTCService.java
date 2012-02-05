package com.jyj.tc;


import weibo4android.WeiboException;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.jyj.tc.EventStreamConstants.Config;

public class WeiboForTCService extends IntentService {
    
    private static final String TAG = "WeiboForTCService";

    public WeiboForTCService() {
	super("WeiboForTCService");
    }
    
    public WeiboForTCService(String name) {
	super(name);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	if (intent == null) {
	    return;
	}
	
	if (!intent.hasExtra(Constants.PLUGIN_KEY_PARAMETER) || 
		! Constants.PLUGIN_KEY.equals(intent.getStringExtra(Constants.PLUGIN_KEY_PARAMETER))) {
	    if (Config.DEBUG) {
		Log.d(TAG, "Plugin key is " + intent.getStringExtra(Constants.PLUGIN_KEY_PARAMETER));
	    }
	}
	
	String action = intent.getAction();
	if (Constants.REGISTER_PLUGIN_INTENT.equals(action)) {
	    try {
		register();
	    } catch (EventStreamException e) {
		e.printStackTrace();
	    }
	} else if (Constants.REFRESH_REQUEST_INTENT.equals(action)) {
	    try {
		refresh();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    
    private long register() throws EventStreamException {
	if (Config.DEBUG) {
	    Log.d(TAG, "Register");
	}
	DataAccessor dataAccessor = new DataAccessor(getApplicationContext());
	
	long pluginId = dataAccessor.getPluginId();
	if (pluginId == DataAccessor.ILLEAGAL_PLUGIN_ID) {
	    pluginId = dataAccessor.registerPlugin();
	}
	
	long sourceId = dataAccessor.getSourceId();
	if (sourceId == DataAccessor.ILLEAGAL_SOURCE_ID) {
	    sourceId = dataAccessor.registerInSourceTable(pluginId);
	}
	return sourceId;
    }
    
    private void refresh() throws WeiboException, EventStreamException {
	DataAccessor dataAccessor = new DataAccessor(getApplicationContext());
	WeiboForTC weiboForTC = WeiboForTCFactory.getWeiboForTC(getApplicationContext());
	if (weiboForTC.isLoginedIn() && dataAccessor.isRegistered()) {
	    long sourceId = register();
	    if (sourceId != DataAccessor.ILLEAGAL_SOURCE_ID) {
		weiboForTC.refreshWeiboMessage(sourceId, dataAccessor);
	    }
	}
    }
    
    public static class EventStreamException extends Exception {
	
	private static final long serialVersionUID = 7610569405079218488L;

	public EventStreamException(String message) {
	    super(message);
	}
    }
}
