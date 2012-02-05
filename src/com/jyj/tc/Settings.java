package com.jyj.tc;

import static com.jyj.tc.Constants.ACCESS_TOKEN_PREFS_KEY;
import static com.jyj.tc.Constants.ACCESS_TOKEN_SECRET_PREFS_KEY;
import static com.jyj.tc.Constants.CONFIG_STORE;
import static com.jyj.tc.Constants.DISPLAY_NAME_KEY;
import static com.jyj.tc.Constants.OWN_ID_PREFS_KEY;
import static com.jyj.tc.Constants.PAGING_SINCE_MESSAGE_ID;
import static com.jyj.tc.Constants.PAGING_SINCE_STATUS_ID;

import com.jyj.tc.EventStreamConstants.WeiboConf;

import weibo4android.http.AccessToken;
import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
    private static final int MODE_PRIVATE = android.content.Context.MODE_PRIVATE;
    private Context mContext;
    
    public Settings(Context context) {
	mContext = context;
    }
    
    public String getAuthenticationToken() {
	return getString(ACCESS_TOKEN_PREFS_KEY, null);
    }

    public void setAuthenticationToken(String token) {
	if (token == null) {
	    removeSetting(ACCESS_TOKEN_PREFS_KEY);
	} else {
	    setString(ACCESS_TOKEN_PREFS_KEY, token);
	}
	
    }
    
    public String getAuthenticationTokenSecret() {
	return getString(ACCESS_TOKEN_SECRET_PREFS_KEY, null);
    }
    
    public void setAuthenticationTokenSecret(String token) {
	if (token == null) {
	    removeSetting(ACCESS_TOKEN_SECRET_PREFS_KEY);
	} else {
	    setString(ACCESS_TOKEN_SECRET_PREFS_KEY, token);
	}
    }
    
    public Long getUserId() {
	return getLong(OWN_ID_PREFS_KEY, 0);
    }
    
    public void setUserId(Long userId) {
	if (userId == null) {
	    removeSetting(OWN_ID_PREFS_KEY);
	} else {
	    setLong(OWN_ID_PREFS_KEY, userId);
	}
    }
    
    public String getDisplayName() {
	return getString(DISPLAY_NAME_KEY, null);
    }
    
    public void setDisplayName(String displayName) {
	if (displayName == null) {
	    removeSetting(DISPLAY_NAME_KEY);
	} else {
	    setString(DISPLAY_NAME_KEY, displayName);
	}
    }
    
    public void setAccessToken(AccessToken token) {
	SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);
	Editor editor = preferences.edit();
	editor.putString(ACCESS_TOKEN_PREFS_KEY, token.getToken());
	editor.putString(ACCESS_TOKEN_SECRET_PREFS_KEY, token.getTokenSecret());
	editor.putLong(OWN_ID_PREFS_KEY, token.getUserId());
	editor.putString(DISPLAY_NAME_KEY, token.getScreenName());
	editor.apply();
    }

    public void setPaginSinceId(String pageType, long sinceId) {
	if (WeiboConf.PAGING_TYPE_MESSAGE.equals(pageType)) {
	    setLong(PAGING_SINCE_MESSAGE_ID, sinceId);
	} else if (WeiboConf.PAGING_TYPE_STATUS.equals(pageType)) {
	    setLong(PAGING_SINCE_STATUS_ID, sinceId);
	}
    }
    
    public long getPaingSinceId(String pageType) {
	if (WeiboConf.PAGING_TYPE_MESSAGE.equals(pageType)) {
	    getLong(PAGING_SINCE_MESSAGE_ID, 0);
	} else if (WeiboConf.PAGING_TYPE_STATUS.equals(pageType)) {
	    getLong(PAGING_SINCE_STATUS_ID, 0);
	}
	return 0;
    }
    
    
    /**
     * Remove a setting
     *
     * @param key the setting to remove
     */
    private void removeSetting(String key) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);
        if (preferences == null) {
            return;
        }
        preferences.edit().remove(key).apply();
    }

    /**
     * Sets a new string setting
     *
     * @param key the key, null not allowed
     * @param value the new value, null not allowed
     */
    private void setString(String key, String value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putString(key, value).apply();
    }

    /**
     * Get a stored string setting
     *
     * @param key the key for the setting
     * @param defaultValue the default value if key not found
     * @return the setting
     */
    private String getString(String key, String defaultValue) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        return preferences.getString(key, defaultValue);
    }

    /**
     * Store a long setting
     *
     * @param key the key for the setting
     * @param value the value for the setting
     */
    private void setLong(String key, Long value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putLong(key, value).apply();
    }

    /**
     * Get a stored long setting
     *
     * @param key the key for the setting
     * @param defaultValue the default value if key not found
     * @return the setting
     */
    private Long getLong(String key, long defaultValue) {
        return mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE).getLong(key, defaultValue);
    }


    /**
     * Get a stored integer setting
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private Integer getInteger(String key, Integer defaultValue) {
        return mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE).getInt(key, defaultValue);
    }

    /**
     * Set an integer
     * @param key
     * @param value
     */
    private void setInteger(String key, int value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putInt(key, value).apply();
    }    
}
