package com.jyj.tc;

import static com.jyj.tc.Constants.PLUGIN_KEY;
import static com.jyj.tc.EventStreamConstants.EVENT_KEY_DATA_SEPARATOR;
import static com.jyj.tc.EventStreamConstants.EVENT_PROVIDER_URI;
import static com.jyj.tc.EventStreamConstants.PLUGIN_PROVIDER_URI;
import static com.jyj.tc.EventStreamConstants.SOURCE_PROVIDER_URI;
import static com.jyj.tc.EventStreamConstants.UUID;

import java.util.ArrayList;
import java.util.List;

import weibo4android.Status;
import weibo4android.User;
import android.R.integer;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.jyj.tc.EventStreamConstants.ConfigState;
import com.jyj.tc.EventStreamConstants.EventTable;
import com.jyj.tc.EventStreamConstants.PluginTable;
import com.jyj.tc.EventStreamConstants.SourceTable;
import com.jyj.tc.EventStreamConstants.StatusSupport;
import com.jyj.tc.WeiboForTCService.EventStreamException;

public class DataAccessor {

    private static final String TAG = "DataAccessor";
    
    public static final int ILLEAGAL_PLUGIN_ID = -1;
    
    public static final int ILLEAGAL_SOURCE_ID = -1;
    
    /** Used to indicate that this is a message type in EVENT_KEY */
    private static final String EVENT_KEY_MESSAGE_TYPE = "message";

    /** Used to indicate that this is a status type in EVENT_KEY */
    private static final String EVENT_KEY_STATUS_TYPE = "status";
    
    private static final int BULK_INSERT_MAX_COUNT = 50;

    private static final int BULK_INSERT_DELAY = 20; //ms

    private Context mContext;

    private ContentResolver mResolver;
    
    private long mSourceid;

    public DataAccessor(Context context) {
	mContext = context;
	mResolver = context.getContentResolver();
    }

    public boolean isRegistered() throws EventStreamException {
	return (getPluginId() != ILLEAGAL_PLUGIN_ID);
    }

    public long getPluginId() throws EventStreamException {
	long pluginId = ILLEAGAL_PLUGIN_ID;
	Cursor cursor = null;
	try {
	    cursor = mResolver.query(SOURCE_PROVIDER_URI, 
		    null, SourceTable.UUID + "=?", 
		    new String [] {UUID}, null);
	    
	    if (cursor == null) {
		throw new EventStreamException("null returned when querying source table");
	    }
	    
	    if (cursor.moveToFirst()) {
		pluginId = cursor.getLong(cursor.getColumnIndexOrThrow(SourceTable.PLUGIN_ID));
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
		cursor = null;
	    }
	}
	return pluginId;
    }
    
    public long getSourceId() throws EventStreamException {
	long pluginId = ILLEAGAL_SOURCE_ID;
	Cursor cursor = null;
	try {
	    cursor = mResolver.query(SOURCE_PROVIDER_URI, 
		    null, SourceTable.UUID + "=?", 
		    new String [] {UUID}, null);
	    
	    if (cursor == null) {
		throw new EventStreamException("null returned when querying source table");
	    }
	    
	    if (cursor.moveToFirst()) {
		pluginId = cursor.getLong(cursor.getColumnIndexOrThrow(SourceTable.ID_COLUMN));
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
		cursor = null;
	    }
	}
	return pluginId;
    }
    
    private ContentValues createPluginRegistrationContentValues(int configStatus, String configureText) {
	ContentValues pluginValues = new ContentValues();
	pluginValues.put(PluginTable.PLUGIN_KEY, PLUGIN_KEY);
	pluginValues.put(PluginTable.NAME, mContext.getResources().getString(R.string.service_name));
	pluginValues.put(PluginTable.STATUS_TEXT_MAX_LENGTH, Constants.STATUS_TEXT_MAX_LENGTH);
	pluginValues.put(
		PluginTable.ICON_URI,
		ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
			+ mContext.getPackageName() + "/"
			+ R.drawable.weibo_logo);
	pluginValues.put(PluginTable.API_VERSION, 1);
	ComponentName componentName = new ComponentName(mContext, WeiboForTCActivity.class.getName());
	pluginValues.put(PluginTable.CONFIGURATION_ACTIVITY, componentName.flattenToString());
	pluginValues.put(PluginTable.STATUS_SUPPORT, StatusSupport.HAS_SUPPORT_TRUE);
	pluginValues.put(PluginTable.CONFIGURATION_TEXT, configureText);
	pluginValues.put(PluginTable.CONFIGURATION_STATE, configStatus);
	
	return pluginValues;
    }
    
    public void setPluginConfigInEventStream(String userName, long pluginId) {
	Log.v(TAG, "userName: " + userName + " pluginId " + pluginId);
	String configTxt = mContext.getResources().getString(R.string.logout_label);
	configTxt += " " + userName;
	ContentValues contentValues = new ContentValues();
	contentValues.put(PluginTable.CONFIGURATION_TEXT, configTxt);
	contentValues.put(PluginTable.CONFIGURATION_STATE, ConfigState.CONFIGURED);
	contentValues.put(PluginTable.NAME, mContext.getResources().getString(R.string.service_name));
	
	mResolver.update(PLUGIN_PROVIDER_URI, contentValues, PluginTable._ID + "=?", new String [] {String.valueOf(pluginId)});
    }
    
    public long registerPlugin() throws EventStreamException {
	ContentValues pluginValues = createPluginRegistrationContentValues(ConfigState.NOT_CONFIGURED, 
		mContext.getResources().getString(R.string.register_txt));
	
	Uri pluginUri = mResolver.insert(PLUGIN_PROVIDER_URI, pluginValues);
	
	if (pluginUri == null) {
	    throw new EventStreamException("insert into plugin table return null ");
	}
	
	return ContentUris.parseId(pluginUri);
    }
    
    public ContentValues createSourceRegistrationValues(long pluginId) {
	ContentValues sourceRegistrationValues = new ContentValues();

        sourceRegistrationValues.put(SourceTable.NAME, mContext.getResources().getString(R.string.service_name));

        Builder iconUriBuilder = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(mContext.getPackageName()).appendPath(Integer.toString(R.drawable.weibo_logo));
        sourceRegistrationValues.put(SourceTable.ICON_URI, iconUriBuilder.toString());

        sourceRegistrationValues.put(SourceTable.ENABLED, (short)1);
        sourceRegistrationValues.put(SourceTable.UUID, UUID);
        sourceRegistrationValues.put(SourceTable.PLUGIN_ID, pluginId);

        return sourceRegistrationValues;
    }
    
    public long registerInSourceTable(long pluginId) {
	ContentValues sourceValues = createSourceRegistrationValues(pluginId);
	
	Uri uri = mResolver.insert(SOURCE_PROVIDER_URI, sourceValues);
	
	long sourceId = ILLEAGAL_SOURCE_ID;
	if (uri != null) {
	    sourceId = ContentUris.parseId(uri);
	}
	
	return sourceId;
    }
    
    public List<ContentValues> getContentValuesFromMessages(List<Status> messages, long sourceId) {
	List<ContentValues> result = new ArrayList<ContentValues>();
	for (Status message : messages) {
	    result.add(createContentValuesFromMessage(message, sourceId));
	}
	return result;
    }
    
    private ContentValues createContentValuesFromMessage(Status message, long sourceId) {
	User user = message.getUser();
        ContentValues event = new ContentValues();
        String profileImageUrl = user.getProfileImageURL() != null ? user.getProfileImageURL()
                .toString() : "";

        event.put(EventTable.SOURCE_ID, sourceId);
        event.put(EventTable.FRIEND_KEY, user.getId());
        event.put(EventTable.MESSAGE, message.getText());
        event.put(EventTable.IMAGE_URI, profileImageUrl);
        event.put(EventTable.PUBLISHED_TIME, message.getCreatedAt().getTime());
        String uniqueKey = user.getScreenName() + EVENT_KEY_DATA_SEPARATOR
                + EVENT_KEY_MESSAGE_TYPE + EVENT_KEY_DATA_SEPARATOR
                + message.getId();
        event.put(EventTable.EVENT_KEY, uniqueKey);
        event.put(EventTable.PERSONAL, 0);
        event.put(EventTable.OUTGOING, 0);
        return event;
    }
    
    public int insertMessages(List<ContentValues> messageValues) {
	ArrayList<ContentValues> bulkValues = new ArrayList<ContentValues>();
	int inserted = 0;
	for (int i = 0; i < messageValues.size(); i++) {
	    bulkValues.add(messageValues.get(i));
	    
	    if (bulkValues.size() >= BULK_INSERT_MAX_COUNT) {
		inserted += mResolver.bulkInsert(EVENT_PROVIDER_URI, 
			(ContentValues []) bulkValues.toArray(new ContentValues[bulkValues.size()]));
		bulkValues.clear();
		
		try {
		    Thread.sleep(BULK_INSERT_DELAY);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
	
	if (bulkValues.size() > 0) {
	    inserted += mResolver.bulkInsert(EVENT_PROVIDER_URI, 
			(ContentValues []) bulkValues.toArray(new ContentValues[bulkValues.size()]));
		bulkValues.clear();
	}
	
	return inserted;
    }
}
