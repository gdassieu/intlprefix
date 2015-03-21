package com.boogersoft.intlprefix;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.database.DatabaseUtilsCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * Quick'n dirty content provider for internal use only
 *
 */
public class MainContentProvider extends ContentProvider
{
	private static final String AUTHORITY = "com.boogersoft.intlprefix";

	/**
	 * Contract for Profiles table
	 */
	public static final class Profile
	{
		public static final String NAME = "profile";

		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/" + NAME);

		public static final String CONTENT_TYPE_DIR =
			"vnd.android.cursor.dir/" + AUTHORITY + "." + NAME;
		public static final String CONTENT_TYPE_ITEM =
			"vnd.android.cursor.item/" + AUTHORITY + "." + NAME;

		public static final String COL_ID = "_id";
		public static final String COL_NAME = "name";
		public static final String COL_COUNTRY_CODE = "countryCode";
		public static final String COL_ADD_DOM_PREFIX = "addDomPrefix";
		public static final String COL_DOM_PREFIX = "domPrefix";
		public static final String COL_DOM_SUFFIX = "domSuffix";
		public static final String COL_ADD_INTL_PREFIX = "addIntlPrefix";
		public static final String COL_INTL_PREFIX = "intlPrefix";
		public static final String COL_INTL_SUFFIX = "intlSuffix";

		public static final String DEFAULT_SORT_ORDER = COL_NAME + " ASC";
	}

	private class ProfileManagerOpenHelper extends SQLiteOpenHelper
	{
		private static final int DB_VERSION = 2;
		private static final String DB_NAME = "IntlPrefix";

		ProfileManagerOpenHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE " + Profile.NAME + " ("
				+ Profile.COL_ID + " INTEGER PRIMARY KEY,"
				+ Profile.COL_NAME + " TEXT UNIQUE, "
				+ Profile.COL_COUNTRY_CODE + " TEXT, "
				+ Profile.COL_ADD_DOM_PREFIX + " INTEGER, "
				+ Profile.COL_DOM_PREFIX + " TEXT, "
				+ Profile.COL_DOM_SUFFIX + " TEXT, "
				+ Profile.COL_ADD_INTL_PREFIX + " INTEGER, "
				+ Profile.COL_INTL_PREFIX + " TEXT, "
				+ Profile.COL_INTL_SUFFIX + " TEXT"
				+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.d(getClass().getName(), String.format(
				"Upgrading DB from v%s to v%s", oldVersion, newVersion));
			switch(oldVersion)
			{
				case 1:
					db.execSQL("ALTER TABLE " + Profile.NAME + " ADD Column "
						+ Profile.COL_DOM_SUFFIX + " TEXT");
					db.execSQL("ALTER TABLE " + Profile.NAME + " ADD Column "
						+ Profile.COL_INTL_SUFFIX + " TEXT");
					break;
				default:
					throw new IllegalStateException(String.format(
						"DB upgrade to v%s not implemented!", newVersion));
			}
		}
	}

	private final HashMap<String, String> profileProjectionMap;
	private final UriMatcher uriMatcher;
	private SQLiteOpenHelper openHelper;

	private static final int URIID_PROFILE_DIR = 0;
	private static final int URIID_PROFILE_ITEM = 1;

	public MainContentProvider()
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, Profile.NAME, URIID_PROFILE_DIR);
		uriMatcher.addURI(AUTHORITY, Profile.NAME + "/#", URIID_PROFILE_ITEM);

		profileProjectionMap = new HashMap<String, String>();
		profileProjectionMap.put(Profile.COL_ID, Profile.COL_ID);
		profileProjectionMap.put(Profile.COL_NAME, Profile.COL_NAME);
		profileProjectionMap.put(Profile.COL_COUNTRY_CODE,
			Profile.COL_COUNTRY_CODE);
		profileProjectionMap.put(Profile.COL_ADD_DOM_PREFIX,
			Profile.COL_ADD_DOM_PREFIX);
		profileProjectionMap.put(Profile.COL_DOM_PREFIX,
			Profile.COL_DOM_PREFIX);
		profileProjectionMap.put(Profile.COL_DOM_SUFFIX,
			Profile.COL_DOM_SUFFIX);
		profileProjectionMap.put(Profile.COL_ADD_INTL_PREFIX,
			Profile.COL_ADD_INTL_PREFIX);
		profileProjectionMap.put(Profile.COL_INTL_PREFIX,
			Profile.COL_INTL_PREFIX);
		profileProjectionMap.put(Profile.COL_INTL_SUFFIX,
			Profile.COL_INTL_SUFFIX);
	}

	@Override
	public String getType(Uri uri)
	{
		switch(uriMatcher.match(uri))
		{
			case URIID_PROFILE_DIR:
				return Profile.CONTENT_TYPE_DIR;
			case URIID_PROFILE_ITEM:
				return Profile.CONTENT_TYPE_ITEM;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public boolean onCreate()
	{
		openHelper = new ProfileManagerOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch(uriMatcher.match(uri))
		{
			case URIID_PROFILE_DIR:
				qb.setTables(Profile.NAME);
				qb.setProjectionMap(profileProjectionMap);
				if(TextUtils.isEmpty(sortOrder))
					sortOrder = Profile.DEFAULT_SORT_ORDER;
				break;
			case URIID_PROFILE_ITEM:
				qb.setTables(Profile.NAME);
				qb.setProjectionMap(profileProjectionMap);
				qb.appendWhere(Profile.COL_ID + "=" + uri.getLastPathSegment());
				if(TextUtils.isEmpty(sortOrder))
					sortOrder = Profile.DEFAULT_SORT_ORDER;
				break;
			default:
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}

		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs,
			null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;

		switch(uriMatcher.match(uri))
		{
			case URIID_PROFILE_DIR:
				count = db.delete(Profile.NAME, selection, selectionArgs);
				break;
			case URIID_PROFILE_ITEM:
				selection = DatabaseUtilsCompat.concatenateWhere(
					Profile.COL_ID + "=" + uri.getLastPathSegment(), selection);
				count = db.delete(Profile.NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Uri resultUri;
		long rowId;

		switch(uriMatcher.match(uri))
		{
			case URIID_PROFILE_DIR:
				db.beginTransaction();
				try
				{
					rowId = db.insertOrThrow(Profile.NAME, null, values);
					resultUri = ContentUris.withAppendedId(
						Profile.CONTENT_URI, rowId);
					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}
				break;
			default:
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(resultUri, null);
		return resultUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
		String[] selectionArgs)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;

		switch(uriMatcher.match(uri))
		{
			case URIID_PROFILE_DIR:
				db.beginTransaction();
				try
				{
					count = db.update(Profile.NAME, values,
						selection, selectionArgs);
					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}
				break;
			case URIID_PROFILE_ITEM:
				db.beginTransaction();
				try
				{
					selection = DatabaseUtilsCompat.concatenateWhere(
						Profile.COL_ID + "=" + uri.getLastPathSegment(),
						selection);
					count = db.update(Profile.NAME, values,
						selection, selectionArgs);
					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}
				break;
			default:
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
