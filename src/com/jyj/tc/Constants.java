package com.jyj.tc;

import android.R.integer;
import android.net.Uri;

public interface Constants {

    static final String PLUGIN_KEY_PARAMETER = "plugin_key";
    
    static final String PLUGIN_KEY = "Sina_Weibo_For_Time_Scape_5783964543459";
    
    static final String CALLBACK_URL = "http://localhost/callback_url";
    
    String AUTHORITY = "com.jyj.tc";
    
    Uri STATUS_DETAIL_PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/status_details");
    Uri RETWEEN_STATUS_PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/retween_statuses");
    
    String DATABASE_NAME = "sina_weibo_tc.db";
    int DATABASE_VERSION = 1;
    
    interface StatusDetailColumn {
	String _ID = "_id";
	String STATUS_ID = "status_id";
	String CREATED_AT = "created_at";
	String TEXT = "text";
	String SOURCE = "source";
	String FAVARITE = "favarite";
	String TRUNCATED = "truncated";
	String BMIDDLE_PIC = "bmiddle_pic";
	String ORIGINAL_PIC = "original_pic";
	String THUMBNAIL_PIC = "thumbnail_pic";
	String REPOSTS_COUNT = "reposts_count";
	String COMMENTS_COUNT = "comments_count";
	String FRIEND_KEY = "friend_key";
	String RETWEEN_STATUS_ID = "retween_status_id";
    }
    
    interface RetweenStatusColumn {
	String _ID = "_id";
	String STATUS_ID = "status_id";
	String CREATED_AT = "created_at";
	String TEXT = "text";
	String BMIDDLE_PIC = "bmiddle_pic";
	String ORIGINAL_PIC = "original_pic";
	String THUMBNAIL_PIC = "thumbnail_pic";
	String REPOSTS_COUNT = "reposts_count";
	String COMMENTS_COUNT = "comments_count";
	String USER_ID = "user_id";
	String USER_SCREEN_NAME = "user_screen_name";
    }
    
    interface FAVARITE_VALUE {
	int NOT_FAVARITE = 0;
	int FAVARITE = 1;
    }
    
    interface TRUNCATED_VALUE {
	int NOT_TRUNCATED = 0;
	int TRUNCATED = 1;
    }
    
    // Shared preference constants, used in settings.
    static final String CONFIG_STORE = "Sina_Weibo_For_TC.conf";
    static final String ACCESS_TOKEN_PREFS_KEY = "Sina_Weibo_For_TC.access_token";
    static final String ACCESS_TOKEN_SECRET_PREFS_KEY = "Sina_Weibo_For_TC.access_token_secret";
    static final String LAST_COMMUNICATION_WITH_SERVER_KEY = "Sina_Weibo_For_TC.communication.lasttime";
    static final String OWN_ID_PREFS_KEY = "Sina_Weibo_For_TC.own.id";
    static final String HAS_ACCEPTED_DISCLAIMER = "Sina_Weibo_For_TC.accepted_disclaimer";
    static final String LOCALE_HASH = "Sina_Weibo_For_TC.locale.hash";
    static final String DISPLAY_NAME_KEY = "Sina_Weibo_For_TC.display.name";
    static final String PAGING_SINCE_MESSAGE_ID = "Sina_Weibo_For_TC.paging.since.message.id";
    static final String PAGING_SINCE_STATUS_ID = "Sina_Weibo_For_TC.paging.since.status.id";
    
    static final int STATUS_TEXT_MAX_LENGTH = 420;

    // Intent constants used internally within the extension when throwng internal events.
    static final String SEND_STATUS_UPDATE_INTENT = "com.jyj.weibotcpulgin.SEND_STATUS_UPDATE";
    static final String REGISTER_PLUGIN_INTENT = "com.jyj.weibotcpulgin.REGISTER_PLUGIN";
    static final String REFRESH_REQUEST_INTENT = "com.jyj.weibotcpulgin.REFRESH_REQUEST";
    static final String AUTHENTICATE_INTENT = "com.jyj.weibotcpulgin.AUTHENTICATE";
    static final String LOGOUT_INTENT = "com.jyj.weibotcpulgin.LOGOUT";
    static final String LAUNCH_BROWSER_INTENT = "com.jyj.weibotcpulgin.VIEW_TILE";
}
