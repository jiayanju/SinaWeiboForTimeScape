package com.jyj.tc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.jyj.tc.EventStreamConstants.Config;
import static com.jyj.tc.WeiboForTCApplication.State;
import com.jyj.tc.WeiboForTCApplication.StateListener;

public class WeiboForTCActivity extends Activity implements StateListener {
    
    private static final String TAG = "WeiboForTCActivity";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        if (Config.DEBUG) {
            Log.d(TAG, "onCreate called");
        }
    }
    
    @Override
    protected void onResume() {
	super.onResume();
	
	WeiboForTCApplication application = (WeiboForTCApplication) getApplication();
	application.addListener(this);
	
	State state = application.getState();
	
	if (Config.DEBUG) {
	    Log.d(TAG, "onResume " + state);
	}
	
	switch (state) {
	case NOT_CONFIGURED:
	    setState(State.NOT_AUTHENTICATED);
	    break;

	case NOT_AUTHENTICATED:
	    break;
	    
	case AUTHENTICATION_SUCCESS:
	    setState(State.AUTHENTICATED);
	    break;
	    
	default:
	    break;
	}
    };
    
    @Override
    protected void onPause() {
        super.onPause();
        WeiboForTCApplication application = (WeiboForTCApplication) getApplication();
        application.removeListener(this);
    }
    
    
    public void onStateChangeListener(State newState) {
	if (Config.DEBUG) {
	    Log.d(TAG, "onStateChangeListener " + newState);
	}
	
	switch (newState) {
	case NOT_AUTHENTICATED:
	    Intent intent = new Intent(WeiboForTCActivity.this, SinaWeiboLoginActivity.class);
	    startActivity(intent);
	    break;
	    
	case AUTHENTICATED:
	    finish();

	default:
	    break;
	}
    };
    
    private void setState(State state) {
	((WeiboForTCApplication) getApplication()).setState(state);
    }
}