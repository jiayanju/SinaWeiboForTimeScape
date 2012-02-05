package com.jyj.tc;

public interface Constants {

    static final String PLUGIN_KEY_PARAMETER = "PLUGIN_KEY";
    
    static final String PLUGIN_KEY = "Sina_Weibo_For_Time_Scape_5783964543459";
    
    static final String CALLBACK_URL = "http://localhost/callback_url";
    
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
    static final String SEND_STATUS_UPDATE_INTENT = "com.jyj.weibotcpulgin..SEND_STATUS_UPDATE";
    static final String REGISTER_PLUGIN_INTENT = "com.jyj.weibotcpulgin..REGISTER_PLUGIN";
    static final String REFRESH_REQUEST_INTENT = "com.jyj.weibotcpulgin..REFRESH_REQUEST";
    static final String AUTHENTICATE_INTENT = "com.jyj.weibotcpulgin..AUTHENTICATE";
    static final String LOGOUT_INTENT = "com.jyj.weibotcpulgin..LOGOUT";
    static final String LAUNCH_BROWSER_INTENT = "com.jyj.weibotcpulgin..VIEW_TILE";
}
