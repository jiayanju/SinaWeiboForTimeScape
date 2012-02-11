package com.jyj.tc;

import static com.jyj.tc.Constants.LOGOUT_INTENT;

import com.jyj.tc.EventStreamConstants.EventstreamIntentData;
import com.jyj.tc.EventStreamConstants.EventstreamIntents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class EventStreamListener extends BroadcastReceiver {
    private static final String TAG = "EventStreamReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
	if (context == null) {
	    return;
	}
	
	if (!validateIntent(intent)) {
	    return;
	}
	
	String action = intent.getAction();
	Log.v(TAG, "Received action " + action);
	Intent serviceiIntent = null;
	if (EventstreamIntents.REGISTER_PLUGINS_REQUEST_INTENT.equals(action)) {
	    serviceiIntent = new Intent(Constants.REGISTER_PLUGIN_INTENT);
	} else if (EventstreamIntents.REFRESH_REQUEST_INTENT.equals(action)) {
	    serviceiIntent = new Intent(Constants.REFRESH_REQUEST_INTENT);
	} else if (EventstreamIntents.STATUS_UPDATE_INTENT.equals(action)){
	    String updateMessage = intent.getExtras().getString(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE);
	    if (updateMessage != null && updateMessage.length() > 0) {
		serviceiIntent = new Intent(Constants.SEND_STATUS_UPDATE_INTENT);
		serviceiIntent.putExtra(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE, updateMessage);
	    }
	}
	
	if (serviceiIntent != null) {
	    serviceiIntent.setClass(context, WeiboForTCService.class);
	    serviceiIntent.putExtra(Constants.PLUGIN_KEY_PARAMETER, Constants.PLUGIN_KEY);
	    context.startService(serviceiIntent);
	}
	
    }
    
    private boolean validateIntent(Intent intent) {
	if (intent == null) {
	    return false;
	}
	
	String action = intent.getAction();
	Log.v(TAG, "Validate Intent Action : " + action);
	if (action == null) {
	    return false;
	}
	
	if (EventstreamIntents.REGISTER_PLUGINS_REQUEST_INTENT.equals(action)) {
	    return true;
	} else {
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
		String key = extras.getString(Constants.PLUGIN_KEY_PARAMETER);
		return (key != null && key.equals(Constants.PLUGIN_KEY));
	    }
	}
	
	return false;
    }

}
