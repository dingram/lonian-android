package com.lonian.android.data;

import android.net.Uri;

public interface Constants {
	public static final String CONTENT_AUTHORITY = "com.lonian.data";
	public static final String UPDATES_TABLE_NAME = "updates";
	public static final Uri UPDATES_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + UPDATES_TABLE_NAME);
	
	// columns
	public static final String UPDATE_ID = "update_id";
	public static final String CONTENT = "content";
	public static final String ACTIONS = "actions";
}
