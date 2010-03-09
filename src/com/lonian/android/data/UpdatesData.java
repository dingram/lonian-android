package com.lonian.android.data;

import static android.provider.BaseColumns._ID;
import static com.lonian.android.data.Constants.UPDATES_TABLE_NAME;
import static com.lonian.android.data.Constants.UPDATE_ID;
import static com.lonian.android.data.Constants.CONTENT;
import static com.lonian.android.data.Constants.ACTIONS;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UpdatesData extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "updates.db";
	private static final int DATABASE_VERSION = 1;
	
	public UpdatesData(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + UPDATES_TABLE_NAME + " ("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ UPDATE_ID + " INTEGER, "
				+ CONTENT + " TEXT NOT NULL, "
				+ ACTIONS + " TEXT NOT NULL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nowt to do
	}

}
