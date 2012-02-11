package com.jyj.tc.db;

import static com.jyj.tc.Constants.AUTHORITY;
import static com.jyj.tc.Constants.DATABASE_NAME;
import static com.jyj.tc.Constants.DATABASE_VERSION;
import static com.jyj.tc.Constants.RETWEEN_STATUS_PROVIDER_URI;
import static com.jyj.tc.Constants.STATUS_DETAIL_PROVIDER_URI;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.jyj.tc.Constants.RetweenStatusColumn;
import com.jyj.tc.Constants.StatusDetailColumn;

public class StatusContentProvider extends ContentProvider {

    private static final String STATUS_DETAIL_TABLE = "status_detail";
    private static final String RETWEEN_STATUS_TABLE = "retween_status";
    
    private SQLiteDatabase db;
    
    private static class StatusDetailDatabaseHelper extends SQLiteOpenHelper {
	
	public StatusDetailDatabaseHelper(Context context, String name,
		CursorFactory cursorFactory, int version) {
	    super(context, name, cursorFactory, version);
	}

	private static final String CREATE_STATUS_DETAIL_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + STATUS_DETAIL_TABLE + "("
		    + StatusDetailColumn._ID + "INTEGER primary key autoincrement, "
		    + StatusDetailColumn.STATUS_ID + " INTEGER, "
		    + StatusDetailColumn.CREATED_AT + " INTEGER, "
		    + StatusDetailColumn.SOURCE + " TEXT, "
		    + StatusDetailColumn.FAVARITE + " INTEGER, "
		    + StatusDetailColumn.TRUNCATED + " INTEGER, "
		    + StatusDetailColumn.BMIDDLE_PIC + " TEXT, "
		    + StatusDetailColumn.ORIGINAL_PIC + " TEXT, "
		    + StatusDetailColumn.THUMBNAIL_PIC + " TEXT, "
		    + StatusDetailColumn.REPOSTS_COUNT + " INTEGER, "
		    + StatusDetailColumn.COMMENTS_COUNT + " INTEGER, "
		    + StatusDetailColumn.FRIEND_KEY + " INTEGER, "
		    + StatusDetailColumn.RETWEEN_STATUS_ID + " INTEGER)";
	
	private static final String CREATE_RETWEEN_STATUS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + RETWEEN_STATUS_TABLE + "("
		    + RetweenStatusColumn._ID + "INTEGER primary key autoincrement, "
		    + RetweenStatusColumn.STATUS_ID + " INTEGER, "
		    + RetweenStatusColumn.CREATED_AT + " INTEGER, "
		    + RetweenStatusColumn.BMIDDLE_PIC + " TEXT, "
		    + RetweenStatusColumn.ORIGINAL_PIC + " TEXT, "
		    + RetweenStatusColumn.THUMBNAIL_PIC + " TEXT, "
		    + RetweenStatusColumn.REPOSTS_COUNT + " INTEGER, "
		    + RetweenStatusColumn.COMMENTS_COUNT + " INTEGER, "
		    + RetweenStatusColumn.USER_ID + " INTEGER, "
		    + RetweenStatusColumn.USER_SCREEN_NAME + " TEXT)";
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(CREATE_STATUS_DETAIL_TABLE_SQL);
	    db.execSQL(CREATE_RETWEEN_STATUS_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + STATUS_DETAIL_TABLE);
	    db.execSQL("DROP TABLE IF EXISTS " + RETWEEN_STATUS_TABLE);
	    onCreate(db);
	}
	
    }
    
    private static final int STATUSES = 1;
    private static final int STATUS_ID = 2;
    private static final int RETWEEN_STATUS = 3;
    private static final int RETWEEN_STATUS_ID = 4;
    
    private static final UriMatcher URI_MATCHER;
    
    static {
	URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	URI_MATCHER.addURI(AUTHORITY, "status_details", STATUSES);
	URI_MATCHER.addURI(AUTHORITY, "status_details/#", STATUS_ID);
	URI_MATCHER.addURI(AUTHORITY, "retween_statuses", RETWEEN_STATUS);
	URI_MATCHER.addURI(AUTHORITY, "retween_statuses/#", RETWEEN_STATUS_ID);
    }
    
    @Override
    public boolean onCreate() {
	Context context = getContext();
	SQLiteOpenHelper dbHelper = new StatusDetailDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	db = dbHelper.getWritableDatabase();
	return (db != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
	    String[] selectionArgs, String sortOrder) {
	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	switch (URI_MATCHER.match(uri)) {
	case STATUS_ID:
	    queryBuilder.setTables(STATUS_DETAIL_TABLE);
	    queryBuilder.appendWhereEscapeString(StatusDetailColumn._ID + "="
		    + uri.getPathSegments().get(1));
	    break;
	
	case RETWEEN_STATUS_ID:
	    queryBuilder.setTables(RETWEEN_STATUS_TABLE);
	    queryBuilder.appendWhereEscapeString(RetweenStatusColumn._ID + "="
	    	 + uri.getPathSegments().get(1));
	    break;
	    
	case STATUSES:
	    queryBuilder.setTables(STATUS_DETAIL_TABLE);
	    break;
	    
	case RETWEEN_STATUS:
	    queryBuilder.setTables(RETWEEN_STATUS_TABLE);
	    break;

	default:
	    throw new IllegalArgumentException("Unsupported Uri" + uri);
	}
	
	String orderBy = sortOrder;
	if (TextUtils.isEmpty(sortOrder)) {
	    orderBy = StatusDetailColumn.CREATED_AT;
	}
	
	Cursor cursor = queryBuilder.query(db, projection, selection,
		selectionArgs, null, null, orderBy);
	cursor.setNotificationUri(getContext().getContentResolver(), uri);
	return cursor;
    }

    @Override
    public String getType(Uri uri) {
	switch (URI_MATCHER.match(uri)) {
	case STATUSES:
	    return "vnd.android.cursor.dir/vnd.paad.status_details";
	    
	case STATUS_ID:
	    return "vnd.android.cursor.dir/vnd.paad.status_detail";
	    
	case RETWEEN_STATUS:
	    return "vnd.android.cursor.dir/vnd.paad.retween_statuses";
	    
	case RETWEEN_STATUS_ID:
	    return "vnd.android.cursor.dir/vnd.paad.retween_status";

	default:
	    throw new IllegalArgumentException("Unsupported Uri : " + uri);
	}
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
	long rowId = -1;
	switch (URI_MATCHER.match(uri)) {
	case STATUS_ID:
	case STATUSES:
	    rowId = db.insert(STATUS_DETAIL_TABLE, "", values);
	    if (rowId > 0) {
		Uri newUri = ContentUris.withAppendedId(STATUS_DETAIL_PROVIDER_URI, rowId);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	    }
	    
	case RETWEEN_STATUS:
	case RETWEEN_STATUS_ID:
	    rowId = db.insert(RETWEEN_STATUS_TABLE, "", values);
	    if (rowId > 0) {
		Uri newUri = ContentUris.withAppendedId(RETWEEN_STATUS_PROVIDER_URI, rowId);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	    }
	}
	
	throw new SQLException("Failed to insert " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
	int count = 0;
	String segment = null;
	String where = null;
	switch (URI_MATCHER.match(uri)) {
	case STATUS_ID:
	    segment = uri.getPathSegments().get(1);
	    where = StatusDetailColumn._ID + "=" + segment;
	    if (!TextUtils.isEmpty(selection)) {
		where += " AND (" + selection + ")";
	    }
	    count = db.delete(STATUS_DETAIL_TABLE, where, selectionArgs);
	    break;
	    
	case STATUSES:
	    count = db.delete(STATUS_DETAIL_TABLE, selection, selectionArgs);
	    break;
	    
	case RETWEEN_STATUS_ID:
	    segment = uri.getPathSegments().get(1);
	    where = RetweenStatusColumn._ID + "=" + segment;
	    if (!TextUtils.isEmpty(selection)) {
		where += " AND (" + selection + ")";
	    }
	    count = db.delete(RETWEEN_STATUS_TABLE, where, selectionArgs);
	    break;

	case RETWEEN_STATUS:
	    count = db.delete(RETWEEN_STATUS_TABLE, selection, selectionArgs);
	    break;
	    
	default:
	    throw new IllegalArgumentException("Unsupported Uri " + uri);
	}
	getContext().getContentResolver().notifyChange(uri, null);
	return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
	    String[] selectionArgs) {
	int count = 0;
	String segment = null;
	String where = null;
	switch (URI_MATCHER.match(uri)) {
	case STATUS_ID:
	    segment = uri.getPathSegments().get(1);
	    where = StatusDetailColumn._ID + "=" + segment;
	    if (!TextUtils.isEmpty(selection)) {
		where += " AND (" + selection + ")";
	    }
	    count = db.update(STATUS_DETAIL_TABLE, values, where, selectionArgs);
	    break;
	    
	case STATUSES:
	    count = db.update(STATUS_DETAIL_TABLE, values, selection, selectionArgs);
	    break;
	    
	case RETWEEN_STATUS_ID:
	    segment = uri.getPathSegments().get(1);
	    where = RetweenStatusColumn._ID + "=" + segment;
	    if (!TextUtils.isEmpty(selection)) {
		where += " AND (" + selection + ")";
	    }
	    count = db.update(RETWEEN_STATUS_TABLE, values, where, selectionArgs);
	    break;
	    
	case RETWEEN_STATUS:
	    count = db.update(RETWEEN_STATUS_TABLE, values, selection, selectionArgs);
	    break;
	    
	default:
	    throw new IllegalArgumentException("Unsuppoerted Uri " + uri);
	}
	getContext().getContentResolver().notifyChange(uri, null);
	return count;
    }

}
