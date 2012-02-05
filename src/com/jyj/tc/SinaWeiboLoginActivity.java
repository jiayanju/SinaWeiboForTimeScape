package com.jyj.tc;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import com.jyj.tc.WeiboForTCApplication.State;

import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;
import weibo4android.http.AccessToken;
import weibo4android.http.RequestToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SinaWeiboLoginActivity extends Activity {

    private static final String TAG = "SinaWeiboLoginActivity";
    
    private LinearLayout mContentLayout;
    private TextView mTitle;
    private WebView mWebView;
    private String mUrl;
    private ProgressDialog mProgressDialog;
    private Weibo mWeibo;
    private RequestToken mRequestToken;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mContentLayout = new LinearLayout(this);
        mContentLayout.setOrientation(LinearLayout.VERTICAL);
        mContentLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        
        mWeibo = new Weibo();
        
        setUpTitle();
        setUpWebView();
        setContentView(mContentLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        new RequestTokenTask().execute();
    }
    
    private void setUpTitle() {
	mTitle = new TextView(this);
	mTitle.setText("与新浪连接");
	mTitle.setTextColor(Color.WHITE);
	mTitle.setGravity(Gravity.CENTER_VERTICAL);
	mTitle.setTypeface(Typeface.DEFAULT_BOLD);
	mTitle.setBackgroundColor(Color.BLUE);
	mTitle.setCompoundDrawablePadding(6);
//	mTitle.setCompoundDrawablesWithIntrinsicBounds(
//		getResources().getDrawable(R.drawable.renren_android_title_logo), null, null, null);
	mContentLayout.addView(mTitle);
    }
    
    private void setUpWebView() {
	mWebView = new WebView(this);
	mWebView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	mWebView.setVerticalScrollBarEnabled(false);
	mWebView.setHorizontalScrollBarEnabled(false);
	mWebView.setWebViewClient(new SinaWeiboWebViewClient());
	mWebView.getSettings().setJavaScriptEnabled(true);
//	mWebView.loadUrl(mUrl);
	mContentLayout.addView(mWebView);
    }
    
    private void setState(State state) {
	((WeiboForTCApplication) getApplication()).setState(state);
    }
    
    private class RequestTokenTask extends AsyncTask<Void, Integer, RequestToken> {
	@Override
	protected RequestToken doInBackground(Void... params) {
	    try {
		mRequestToken = mWeibo.getOAuthRequestToken(Constants.CALLBACK_URL);
	    } catch (WeiboException e) {
		e.printStackTrace();
		Log.d(TAG, e.getMessage());
		setState(State.AUTHENTICATION_FAILED);
		finish();
	    }
	    return mRequestToken;
	}
	
	@Override
	protected void onPostExecute(RequestToken result) {
	    mUrl = result.getAuthorizationURL();
	    mWebView.loadUrl(mUrl);
	}
    }
    
    private class RequestAccessTokenTask extends AsyncTask<String, Integer, AccessToken> {
	@Override
	protected AccessToken doInBackground(String... params) {
	    String verifierCode = params [0];
	    AccessToken accessToken = null;
	    try {
		accessToken = mRequestToken.getAccessToken(verifierCode);
	    } catch (WeiboException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    
	    DataAccessor dataAccessor = new DataAccessor(getApplicationContext());
	    WeiboForTC weiboForTC = WeiboForTCFactory.getWeiboForTC(getApplicationContext());
	    weiboForTC.getSettings().setAccessToken(accessToken);
	    
	    String screenName = "";
	    try {
		User user = weiboForTC.getOwnUser();
		if (user != null) {
		    screenName = user.getScreenName();
		}
		Log.v(TAG, "ScreenName : " + screenName);
		dataAccessor.setPluginConfigInEventStream(screenName, dataAccessor.getPluginId());
		
		weiboForTC.refreshWeiboMessage(dataAccessor.getSourceId(), dataAccessor);
		
	    } catch (WeiboException e) {
		e.printStackTrace();
		Log.e(TAG, e.getMessage());
	    } catch (Exception e) {
		e.printStackTrace();
		Log.e(TAG, "error occured.");
	    }
	    
	    return accessToken;
	}
	
	@Override
	protected void onPostExecute(AccessToken result) {
	    setState(State.AUTHENTICATION_SUCCESS);
	    finish();
	}
    }
    
    
    private class SinaWeiboWebViewClient extends WebViewClient {
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
	    Log.v(TAG, "Loaded URL : " + url);
	    if (url != null && url.startsWith(Constants.CALLBACK_URL)) {
		Log.v(TAG, "process callback url");
		String verifierCode = null;
		try {
		    URL u = new URL(url);
		    String query = u.getQuery();
		    String [] arrays = query.split("&");
		    for (String parameter : arrays) {
			if (parameter.contains("oauth_verifier")) {
			    String [] v = parameter.split("=");
			    verifierCode = URLDecoder.decode(v[1]);
			    break;
			}
			
		    }
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}
		
		if (verifierCode != null && verifierCode.length() > 0) {
		    new RequestAccessTokenTask().execute(verifierCode);
		}
		view.stopLoading();
	    }
	    super.onPageStarted(view, url, favicon);
	}
    }
}
