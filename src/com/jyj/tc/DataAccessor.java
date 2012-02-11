package com.jyj.tc;

import static com.jyj.tc.Constants.PLUGIN_KEY;
import static com.jyj.tc.Constants.RETWEEN_STATUS_PROVIDER_URI;
import static com.jyj.tc.Constants.STATUS_DETAIL_PROVIDER_URI;
import static com.jyj.tc.Constants.STATUS_TEXT_MAX_LENGTH;
import static com.jyj.tc.EventStreamConstants.EVENT_KEY_DATA_SEPARATOR;
import static com.jyj.tc.EventStreamConstants.EVENT_PROVIDER_URI;
import static com.jyj.tc.EventStreamConstants.FRIEND_PROVIDER_URI;
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

import com.jyj.tc.Constants.FAVARITE_VALUE;
import com.jyj.tc.Constants.RetweenStatusColumn;
import com.jyj.tc.Constants.StatusDetailColumn;
import com.jyj.tc.Constants.TRUNCATED_VALUE;
import com.jyj.tc.EventStreamConstants.ConfigState;
import com.jyj.tc.EventStreamConstants.EventTable;
import com.jyj.tc.EventStreamConstants.FriendTable;
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

    public static final int BULK_INSERT_MAX_COUNT = 50;

    private static final int BULK_INSERT_DELAY = 20; // ms

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
	    cursor = mResolver.query(SOURCE_PROVIDER_URI, null,
		    SourceTable.UUID + "=?", new String[] { UUID }, null);

	    if (cursor == null) {
		throw new EventStreamException(
			"null returned when querying source table");
	    }

	    if (cursor.moveToFirst()) {
		pluginId = cursor.getLong(cursor
			.getColumnIndexOrThrow(SourceTable.PLUGIN_ID));
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
	    cursor = mResolver.query(SOURCE_PROVIDER_URI, null,
		    SourceTable.UUID + "=?", new String[] { UUID }, null);

	    if (cursor == null) {
		throw new EventStreamException(
			"null returned when querying source table");
	    }

	    if (cursor.moveToFirst()) {
		pluginId = cursor.getLong(cursor
			.getColumnIndexOrThrow(SourceTable.ID_COLUMN));
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
		cursor = null;
	    }
	}
	return pluginId;
    }

    private ContentValues createPluginRegistrationContentValues(
	    int configStatus, String configureText) {
	ContentValues pluginValues = new ContentValues();
	pluginValues.put(PluginTable.PLUGIN_KEY, PLUGIN_KEY);
	pluginValues.put(PluginTable.NAME,
		mContext.getResources().getString(R.string.service_name));
	pluginValues.put(PluginTable.STATUS_TEXT_MAX_LENGTH,
		Constants.STATUS_TEXT_MAX_LENGTH);
	pluginValues.put(
		PluginTable.ICON_URI,
		ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
			+ mContext.getPackageName() + "/"
			+ R.drawable.weibo_logo);
	pluginValues.put(PluginTable.API_VERSION, 1);
	ComponentName componentName = new ComponentName(mContext,
		WeiboForTCActivity.class.getName());
	pluginValues.put(PluginTable.CONFIGURATION_ACTIVITY,
		componentName.flattenToString());
	pluginValues.put(PluginTable.STATUS_SUPPORT,
		StatusSupport.HAS_SUPPORT_TRUE);
	pluginValues.put(PluginTable.CONFIGURATION_TEXT, configureText);
	pluginValues.put(PluginTable.CONFIGURATION_STATE, configStatus);

	return pluginValues;
    }

    public void setPluginConfigInEventStream(String userName, long pluginId) {
	Log.v(TAG, "userName: " + userName + " pluginId " + pluginId);
	String configTxt = mContext.getResources().getString(
		R.string.logout_label);
	configTxt += " " + userName;
	ContentValues contentValues = new ContentValues();
	contentValues.put(PluginTable.CONFIGURATION_TEXT, configTxt);
	contentValues.put(PluginTable.CONFIGURATION_STATE,
		ConfigState.CONFIGURED);
	contentValues.put(PluginTable.NAME,
		mContext.getResources().getString(R.string.service_name));

	mResolver.update(PLUGIN_PROVIDER_URI, contentValues, PluginTable._ID
		+ "=?", new String[] { String.valueOf(pluginId) });
    }

    public long registerPlugin() throws EventStreamException {
	ContentValues pluginValues = createPluginRegistrationContentValues(
		ConfigState.NOT_CONFIGURED,
		mContext.getResources().getString(R.string.register_txt));

	Uri pluginUri = mResolver.insert(PLUGIN_PROVIDER_URI, pluginValues);

	if (pluginUri == null) {
	    throw new EventStreamException(
		    "insert into plugin table return null ");
	}

	return ContentUris.parseId(pluginUri);
    }

    public ContentValues createSourceRegistrationValues(long pluginId) {
	ContentValues sourceRegistrationValues = new ContentValues();

	sourceRegistrationValues.put(SourceTable.NAME, mContext.getResources()
		.getString(R.string.service_name));

	Builder iconUriBuilder = new Uri.Builder()
		.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
		.authority(mContext.getPackageName())
		.appendPath(Integer.toString(R.drawable.weibo_logo));
	sourceRegistrationValues.put(SourceTable.ICON_URI,
		iconUriBuilder.toString());

	sourceRegistrationValues.put(SourceTable.ENABLED, (short) 1);
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

    private ContentValues createContentValuesFromMessage(Status message,
	    long sourceId) {
	User user = message.getUser();
	ContentValues event = new ContentValues();
	String profileImageUrl = user.getProfileImageURL() != null ? user
		.getProfileImageURL().toString() : "";

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
    
    private ContentValues createStatusDetailValuesFromMessage(Status status) {
	User user = status.getUser();
	ContentValues statusDetail = new ContentValues();
	statusDetail.put(StatusDetailColumn.STATUS_ID, status.getId());
	statusDetail.put(StatusDetailColumn.CREATED_AT, status.getCreatedAt()
		.getTime());
	statusDetail.put(StatusDetailColumn.TEXT, status.getText());
	statusDetail.put(StatusDetailColumn.SOURCE, status.getSource());
	statusDetail.put(StatusDetailColumn.FAVARITE,
		status.isFavorited() ? FAVARITE_VALUE.FAVARITE
			: FAVARITE_VALUE.NOT_FAVARITE);
	statusDetail.put(StatusDetailColumn.TRUNCATED,
		status.isTruncated() ? TRUNCATED_VALUE.TRUNCATED
			: TRUNCATED_VALUE.NOT_TRUNCATED);
	statusDetail.put(StatusDetailColumn.BMIDDLE_PIC,
		status.getBmiddle_pic());
	statusDetail.put(StatusDetailColumn.ORIGINAL_PIC,
		status.getOriginal_pic());
	statusDetail.put(StatusDetailColumn.THUMBNAIL_PIC,
		status.getThumbnail_pic());
	statusDetail.put(StatusDetailColumn.FRIEND_KEY, user.getId());
	if (status.isRetweet()) {
	    statusDetail.put(StatusDetailColumn.RETWEEN_STATUS_ID, status.getRetweeted_status().getId());
	}
	return statusDetail;
    }
    
    private ContentValues createRetweentedStatusValuesFromMessage(Status status) {
	User user = status.getUser();
	ContentValues statusDetail = new ContentValues();
	statusDetail.put(RetweenStatusColumn.STATUS_ID, status.getId());
	statusDetail.put(RetweenStatusColumn.CREATED_AT, status.getCreatedAt()
		.getTime());
	statusDetail.put(RetweenStatusColumn.TEXT, status.getText());
	statusDetail.put(RetweenStatusColumn.BMIDDLE_PIC,
		status.getBmiddle_pic());
	statusDetail.put(RetweenStatusColumn.ORIGINAL_PIC,
		status.getOriginal_pic());
	statusDetail.put(RetweenStatusColumn.THUMBNAIL_PIC,
		status.getThumbnail_pic());
	statusDetail.put(RetweenStatusColumn.USER_ID, user.getId());
	statusDetail.put(RetweenStatusColumn.USER_SCREEN_NAME, user.getScreenName());
	return statusDetail;
    }

    public int insertMessages(List<Status> statuses,long sourceId) {
	
	List<ContentValues> messageValues = new ArrayList<ContentValues>();
	List<ContentValues> statusDetailValues = new ArrayList<ContentValues>();
	List<ContentValues> retweentedStatusValues = new ArrayList<ContentValues>();
	for (Status status : statuses) {
	    messageValues.add(createContentValuesFromMessage(status, sourceId));
	    statusDetailValues.add(createStatusDetailValuesFromMessage(status));
	    if (status.isRetweet()) {
		retweentedStatusValues.add(createRetweentedStatusValuesFromMessage(status));
	    }
	}
	
	int inserted = bulkInsertedValues(EVENT_PROVIDER_URI, messageValues);
	bulkInsertedValues(STATUS_DETAIL_PROVIDER_URI, statusDetailValues);
	bulkInsertedValues(RETWEEN_STATUS_PROVIDER_URI, retweentedStatusValues);

	return inserted;
    }
    
    private int bulkInsertedValues(Uri uri, List<ContentValues> messageValues) {
	ArrayList<ContentValues> bulkValues = new ArrayList<ContentValues>();
	int inserted = 0;
	for (int i = 0; i < messageValues.size(); i++) {
	    bulkValues.add(messageValues.get(i));

	    if (bulkValues.size() >= BULK_INSERT_MAX_COUNT) {
		inserted += mResolver.bulkInsert(uri,
			(ContentValues[]) bulkValues
				.toArray(new ContentValues[bulkValues.size()]));
		bulkValues.clear();

		try {
		    Thread.sleep(BULK_INSERT_DELAY);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	if (bulkValues.size() > 0) {
	    inserted += mResolver.bulkInsert(uri,
		    (ContentValues[]) bulkValues
			    .toArray(new ContentValues[bulkValues.size()]));
	    bulkValues.clear();
	}
	return inserted;
    }
    

    public List<Long> getFriendListFromEventStream(long sourceId) {
	ArrayList<Long> friends = new ArrayList<Long>();
	Cursor cursor = null;
	try {
	    cursor = mResolver.query(FRIEND_PROVIDER_URI, null,
		    FriendTable.SOURCE_ID + "=?",
		    new String[] { String.valueOf(sourceId) }, null);

	    if (cursor != null && cursor.moveToFirst()) {
		do {
		    String idStr = cursor.getString(cursor
			    .getColumnIndexOrThrow(FriendTable.FRIEND_KEY));
		    friends.add(Long.valueOf(idStr));
		} while (cursor.moveToNext());
	    }
	} catch (Exception e) {
	    // TODO: handle exception
	} finally {
	    if (cursor != null) {
		cursor.close();
		cursor = null;
	    }
	}
	return friends;
    }

    public ContentValues createContentValuesForFriend(User updatedFriend,
	    long sourceId) {
	ContentValues values = new ContentValues();
	String profileImageUrl = updatedFriend.getProfileImageURL() != null ? updatedFriend
		.getProfileImageURL().toString() : "";

	values.put(FriendTable.FRIEND_KEY, updatedFriend.getId());
	values.put(FriendTable.DISPLAY_NAME, updatedFriend.getName());
	values.put(FriendTable.PROFILE_IMAGE_URI, profileImageUrl);
	values.put(FriendTable.SOURCE_ID, sourceId);

	return values;

    }

    public void bulkInsertFriends(List<ContentValues> values) {
	mResolver.bulkInsert(FRIEND_PROVIDER_URI, (ContentValues[]) values
		.toArray(new ContentValues[values.size()]));
	values.clear();
	try {
	    Thread.sleep(BULK_INSERT_DELAY);
	} catch (Exception e) {
	    // TODO: handle exception
	}
    }

    public void insertUpdateStatusIntoEventStream(String status, long timeStamp) throws EventStreamException {
	ContentValues contentValues = new ContentValues();
	contentValues.put(SourceTable.CURRENT_STATUS, status);
	contentValues.put(SourceTable.STATUS_TIMESTAMP, timeStamp);
	mResolver.update(SOURCE_PROVIDER_URI, contentValues,
		SourceTable.ID_COLUMN + "=?",
		new String[] { String.valueOf(getSourceId()) });
    }
    
    public void setLogoutInEventStream() throws EventStreamException {
	ContentValues contentValues = new ContentValues();
	contentValues.put(PluginTable.CONFIGURATION_STATE,
		ConfigState.NOT_CONFIGURED);
	contentValues.put(PluginTable.CONFIGURATION_TEXT, mContext
		.getResources().getString(R.string.register_txt));
	mResolver.update(PLUGIN_PROVIDER_URI, contentValues, PluginTable._ID
		+ "=?", new String[] { String.valueOf(getPluginId()) });
    }
    
    public void clearStatusInSourceTable() throws EventStreamException {
	ContentValues values = new ContentValues();
	values.putNull(SourceTable.CURRENT_STATUS);
	values.putNull(SourceTable.STATUS_TIMESTAMP);
	mResolver.update(SOURCE_PROVIDER_URI, values, SourceTable.ID_COLUMN
		+ "=?", new String[] { String.valueOf(getSourceId()) });
    }
    
    public void clearData() throws EventStreamException {
	long sourceId = getSourceId();
	if (sourceId != ILLEAGAL_SOURCE_ID) {
	    mResolver.delete(FRIEND_PROVIDER_URI, SourceTable.ID_COLUMN + "=?",
		    new String[] { String.valueOf(sourceId) });
	    mResolver.delete(EVENT_PROVIDER_URI, SourceTable.ID_COLUMN + "=?",
		    new String[] { String.valueOf(sourceId) });
	}
    }
}
