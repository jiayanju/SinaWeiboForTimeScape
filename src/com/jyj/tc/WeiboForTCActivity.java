package com.jyj.tc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.jyj.tc.EventStreamConstants.Config;
import com.jyj.tc.WeiboForTCApplication.State;
import com.jyj.tc.WeiboForTCApplication.StateListener;

public class WeiboForTCActivity extends Activity implements StateListener {
    
    private static final String TAG = "WeiboForTCActivity";
    
    static final int DIALOG_LOG_OUT = 1;
    
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
	    
	case AUTHENTICATED:
	    showDialog(DIALOG_LOG_OUT);
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
    
    
    public void onStateChangeListener(final State newState) {
	if (Config.DEBUG) {
	    Log.d(TAG, "onStateChangeListener " + newState);
	}

	Runnable runnable = new Runnable() {

	    @Override
	    public void run() {
		switch (newState) {
		case NOT_CONFIGURED:
		    finish();
		    break;

		case NOT_AUTHENTICATED:
		    launchLoginActivity();
		    break;

		case AUTHENTICATED:
		    finish();

		default:
		    break;
		}
	    }
	};
	runOnUiThread(runnable);
    };
    
    private void setState(State state) {
	((WeiboForTCApplication) getApplication()).setState(state);
    }
    
    public void launchLoginActivity() {
	Intent intent = new Intent(WeiboForTCActivity.this, SinaWeiboLoginActivity.class);
	startActivity(intent);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case DIALOG_LOG_OUT:
	    return createLogoutDialog();

	default:
	    break;
	}
	return super.onCreateDialog(id);
    }
    
    public Dialog createLogoutDialog() {
	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.sina_weibo_logout_dialog_title);
        dialog.setMessage(R.string.sina_weibo_logout_dialog_text);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                Intent logoutIntent = new Intent(Constants.LOGOUT_INTENT);
                startService(logoutIntent);
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setResult(RESULT_OK);
                finish();
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                setResult(RESULT_OK);
                finish();
            }
        });
        return dialog.create();
    }
}