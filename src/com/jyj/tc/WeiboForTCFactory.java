package com.jyj.tc;

import android.content.Context;

public class WeiboForTCFactory {

    private static WeiboForTC mWeiboForTC;
    
    public static synchronized WeiboForTC getWeiboForTC(Context context) {
	if (mWeiboForTC == null) {
	    mWeiboForTC = new WeiboForTC(new Settings(context));
	}
	
	return mWeiboForTC;
    }
}
