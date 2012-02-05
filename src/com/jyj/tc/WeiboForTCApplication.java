package com.jyj.tc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;

public class WeiboForTCApplication extends Application {
    
    public enum State {
        NOT_CONFIGURED,
        NOT_AUTHENTICATED,
        AUTHENTICATION_IN_PROGRESS,
        AUTHENTICATION_FAILED,
        AUTHENTICATION_SUCCESS,
        AUTHENTICATED,
        AUTHENTICATION_BAD_CREDENTIALS,
        AUTHENTICATION_NETWORK_FAILED,
        STATUS_UPDATE_FAILED,
        INVALID_ACCOUNT
    };

    private static final String TAG = "WeiboForTCApplication";
    
    private State mCurrentState = State.NOT_CONFIGURED;
    
    private List<StateListener> mStateListeners;
    
    public interface StateListener {
	public void onStateChangeListener(State state);
    }
    
    @Override
    public void onCreate() {
	
        WeiboForTC weiboForTC = WeiboForTCFactory.getWeiboForTC(getApplicationContext());
        
        synchronized (this) {
            mStateListeners = new ArrayList<StateListener>();
	}
        
        if (weiboForTC.isLoginedIn()) {
            setState(State.AUTHENTICATED);
        }
    }
    
    public synchronized void addListener(StateListener stateListener) {
	if (mStateListeners != null) {
	    mStateListeners.add(stateListener);
	}
    }
    
    public synchronized void removeListener(StateListener stateListener) {
	if (stateListener !=  null) {
	    mStateListeners.remove(stateListener);
	}
    }
    
    public synchronized void setState(State state) {
	
	mCurrentState = state;
	
	if (mStateListeners != null) {
	    if (mStateListeners.isEmpty()) {
		
	    } else {
		for (StateListener listener : mStateListeners) {
		    listener.onStateChangeListener(state);
		}
	    }
	}
    }
    
    public synchronized State getState() {
	return mCurrentState;
    }
}
