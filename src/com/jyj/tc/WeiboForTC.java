package com.jyj.tc;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.nfc.Tag;
import android.util.Log;

import com.jyj.tc.EventStreamConstants.Config;
import com.jyj.tc.EventStreamConstants.WeiboConf;

import weibo4android.Paging;
import weibo4android.Status;
import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;

public class WeiboForTC {
    
    private static final String TAG = "WeiboForTC";
    
    private Settings mSettings;
    private Weibo mWeibo;
    
    public WeiboForTC(Settings setting) {
	mSettings = setting;
    }
    
    public Settings getSettings() {
	return mSettings;
    }
    
    public synchronized boolean isLoginedIn() {
	String accessToken = mSettings.getAuthenticationToken();
	return (accessToken != null);
    }
    
    private synchronized Weibo getWeibo() throws WeiboException {
	if (mWeibo == null) {
	    mWeibo = new Weibo();
	    String token = mSettings.getAuthenticationToken();
	    String tokenSecret = mSettings.getAuthenticationTokenSecret();
	    Log.v(TAG, "AccessToken " + token + " tokenSecret: " + tokenSecret);
	    mWeibo.setOAuthAccessToken(token, tokenSecret);
	    Log.v(TAG, "Begin verify credentials");
	    mWeibo.verifyCredentials();
	}
	return mWeibo;
    }
    
    public void refreshWeiboMessage(long sourceId, DataAccessor dataAccessor) throws WeiboException {
	if (Config.DEBUG) {
	    Log.d(TAG, "Refresh Weibo Message : " + sourceId);
	}
	
	List<Status> messages = getHomeTimeLine();
	
	List<ContentValues> messageValues = dataAccessor.getContentValuesFromMessages(messages, sourceId);
	
	int inserted = dataAccessor.insertMessages(messageValues);
	
	if (inserted != 0) {
	    setPagingSinceIdForMessages(messages.get(0).getId());
	}
    }
    
    public List<Status> getHomeTimeLine() throws WeiboException {
	mWeibo = getWeibo();
	Paging messagePaging = getpPaging(WeiboConf.PAGING_TYPE_MESSAGE);
	List<Status> statuses = mWeibo.getHomeTimeline(messagePaging);
	if (statuses == null) {
	    statuses = new ArrayList<Status>();
	}
	return statuses;
    }
    
    public void setPagingSinceIdForMessages(long sinceId) {
	mSettings.setPaginSinceId(WeiboConf.PAGING_TYPE_MESSAGE, sinceId);
    }
    
    public User getOwnUser() throws WeiboException {
	mWeibo = getWeibo();
	String id = mSettings.getUserId().toString();
	Log.v(TAG, "User ID : " + id);
	User user = mWeibo.showUser(id);
	Log.v(TAG, "User :" + user.toString());
	return user;
    }
    
    private Paging getpPaging(String pageType) {
	Paging paging = new Paging();
	paging.setCount(WeiboConf.PAGING_COUNT);
	if (WeiboConf.PAGING_TYPE_MESSAGE.equals(pageType) || WeiboConf.PAGING_TYPE_STATUS.equals(pageType)) {
	    long sinceId = mSettings.getPaingSinceId(pageType);
	    if (sinceId > 0) {
		paging.setSinceId(sinceId);
	    }
	}
	
	return paging;
    }
}
