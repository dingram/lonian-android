package com.lonian.android.data;

import static android.provider.BaseColumns._ID;
import static com.lonian.android.data.Constants.CONTENT_AUTHORITY;
import static com.lonian.android.data.Constants.UPDATES_TABLE_NAME;
import static com.lonian.android.data.Constants.UPDATES_CONTENT_URI;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class UpdatesProvider extends ContentProvider {
	private static final int UPDATES = 1;
	private static final int UPDATES_ID = 2;
	
	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lonian.update";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lonian.update";
	
	private UpdatesData updates;
	private UriMatcher uriMatcher;

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(CONTENT_AUTHORITY, "updates", UPDATES);
		uriMatcher.addURI(CONTENT_AUTHORITY, "updates/#", UPDATES_ID);
		updates = new UpdatesData(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case UPDATES:
				return CONTENT_TYPE;
			case UPDATES_ID:
				return CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = updates.getWritableDatabase();
		
		if (uriMatcher.match(uri) != UPDATES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		long id = db.insertOrThrow(UPDATES_TABLE_NAME, null, values);
		
		Uri newUri = ContentUris.withAppendedId(UPDATES_CONTENT_URI, id);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (uriMatcher.match(uri) == UPDATES_ID) {
			long id = Long.parseLong(uri.getPathSegments().get(1));
			selection = appendRowId(selection, id);
		}
		
		SQLiteDatabase db = updates.getReadableDatabase();
		Cursor cursor = db.query(UPDATES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = updates.getWritableDatabase();
		int count = 0;
		
		switch (uriMatcher.match(uri)) {
			case UPDATES:
				count = db.update(UPDATES_TABLE_NAME, values, selection, selectionArgs);
				break;
			case UPDATES_ID:
				long id = Long.parseLong(uri.getPathSegments().get(1)); 
				count = db.update(UPDATES_TABLE_NAME, values, appendRowId(selection, id), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = updates.getWritableDatabase();
		int count = 0;
		
		switch (uriMatcher.match(uri)) {
			case UPDATES:
				count = db.delete(UPDATES_TABLE_NAME, selection, selectionArgs);
				break;
			case UPDATES_ID:
				long id = Long.parseLong(uri.getPathSegments().get(1)); 
				count = db.delete(UPDATES_TABLE_NAME, appendRowId(selection, id), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	protected String appendRowId(String selection, long id) {
		return _ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
	}
}
