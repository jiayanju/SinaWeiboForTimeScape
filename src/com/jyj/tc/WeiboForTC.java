package com.jyj.tc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import weibo4android.Paging;
import weibo4android.Status;
import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;
import android.R.integer;
import android.content.ContentValues;
import android.util.Log;

import com.jyj.tc.EventStreamConstants.Config;
import com.jyj.tc.EventStreamConstants.WeiboConf;

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
	
	int inserted = dataAccessor.insertMessages(messages, sourceId);
	
	if (inserted != 0) {
	    setPagingSinceIdForMessages(messages.get(0).getId());
	}
	
	refreshFriendsListInEventStream(messages, sourceId, dataAccessor);
    }
    
    public List<Status> getHomeTimeLine() throws WeiboException {
	mWeibo = getWeibo();
	Paging messagePaging = getPaging(WeiboConf.PAGING_TYPE_MESSAGE);
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
    
    private Paging getPaging(String pageType) {
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
    
    private void refreshFriendsListInEventStream(List<Status> statuses, long sourceId, DataAccessor dataAccessor) {
	List<Long> friendIds = dataAccessor.getFriendListFromEventStream(sourceId);
	Collection<User> senders = getSenders(statuses);
	
	User updatedFriend = null;
	Object [] userArray = senders.toArray();
	ArrayList<ContentValues> bulkInsertedValues = new ArrayList<ContentValues>();
	
	for (int i = 0; i < userArray.length; i++) {
	    updatedFriend = (User) userArray[i];
	    long updatedFriendId = updatedFriend.getId();
	    if (friendIds.contains(updatedFriendId)) {
		friendIds.remove(updatedFriendId);
	    } else {
		bulkInsertedValues.add(dataAccessor.createContentValuesForFriend(updatedFriend, sourceId));
		if (bulkInsertedValues.size() >= DataAccessor.BULK_INSERT_MAX_COUNT) {
		    dataAccessor.bulkInsertFriends(bulkInsertedValues);
		}
	    }
	}
	
	if (bulkInsertedValues.size() > 0) {
	    dataAccessor.bulkInsertFriends(bulkInsertedValues);
	}
	
    }
    
    
    private Collection<User> getSenders(List<Status> statuses) {
	HashMap<Long, User> users = new HashMap<Long, User>();
	for (Status status : statuses) {
	    User user = status.getUser();
	    users.put(user.getId(), user);
	}
	return users.values();
    }
    
    public void sendUpdateMessage(String updateMessage) throws WeiboException {
	mWeibo = getWeibo();
	mWeibo.updateStatus(updateMessage);
    }
    
    public void logout() throws WeiboException {
	mWeibo = getWeibo();
	mWeibo.endSession();
	mSettings.logout();
    }
}
