package com.jyj.tc;


import weibo4android.WeiboException;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.jyj.tc.EventStreamConstants.Config;
import com.jyj.tc.EventStreamConstants.EventstreamIntentData;
import com.jyj.tc.EventStreamConstants.EventstreamIntents;
import com.jyj.tc.WeiboForTCApplication.State;

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
	} else if (Constants.SEND_STATUS_UPDATE_INTENT.equals(action)) {
	    try {
		String updateMessage = intent.getExtras().getString(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE);
		sendUpdateMessage(updateMessage);
	    } catch (Exception e) {
		// TODO: handle exception
	    }
	} else if (Constants.LOGOUT_INTENT.equals(action)) {
	    try {
		logout();
	    } catch (Exception e) {
		// TODO: handle exception
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
    
    private void sendUpdateMessage(String updateMessage) throws WeiboException,
	    EventStreamException {
	DataAccessor dataAccessor = new DataAccessor(getApplicationContext());
	WeiboForTC weiboForTC = WeiboForTCFactory
		.getWeiboForTC(getApplicationContext());
	if (weiboForTC.isLoginedIn() && dataAccessor.isRegistered()
		&& updateMessage.length() <= Constants.STATUS_TEXT_MAX_LENGTH) {
	    if (Config.DEBUG) {
		Log.d(TAG, "Post " + updateMessage);
	    }
	    weiboForTC.sendUpdateMessage(updateMessage);
	    dataAccessor.insertUpdateStatusIntoEventStream(updateMessage,
		    System.currentTimeMillis());
	}
    }
    
    private void logout() throws WeiboException, EventStreamException {
	DataAccessor dataAccessor = new DataAccessor(getApplicationContext());
	WeiboForTC weiboForTC = WeiboForTCFactory.getWeiboForTC(getApplicationContext());
	if (weiboForTC.isLoginedIn() && dataAccessor.isRegistered()) {
	    weiboForTC.logout();
	    dataAccessor.setLogoutInEventStream();
	    dataAccessor.clearStatusInSourceTable();
	    ((WeiboForTCApplication)getApplication()).setState(State.NOT_AUTHENTICATED);
	}
    }
    
    public static class EventStreamException extends Exception {
	
	private static final long serialVersionUID = 7610569405079218488L;

	public EventStreamException(String message) {
	    super(message);
	}
    }
    
}
