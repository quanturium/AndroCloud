package com.quanturium.androcloud.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs
{
	public final static String	FIRST_RUN						= "first_run";
	public final static String	EMAIL							= "email";
	public final static String	PASSWORD						= "password";
	public final static String	CREDENTIALS_CHANGED				= "credentials_changed";
	public final static String	FILES_PER_REQUEST				= "files_per_request";
	public final static String	FILES_PER_REQUEST_CHANGED		= "files_per_request_changed";
	public final static String	REFRESH_AUTO					= "refresh_auto";

	public final static String	DOWNLOAD_NOTIFICATION_SHOW		= "download_notification_show";
	public final static String	DOWNLOAD_NOTIFICATION_ACTION	= "download_notification_action";
	public final static String	UPLOAD_NOTIFICATION_SHOW		= "upload_notification_show";
	public final static String	UPLOAD_NOTIFICATION_ACTION		= "upload_notification_action";

	public final static String	SAVING_TYPE						= "saving_type";
	public final static String	DIRECTORY_PICTURES				= "directory_pictures";
	public final static String	DIRECTORY_MUSICS				= "directory_musics";
	public final static String	DIRECTORY_MOVIES				= "directory_movies";
	public final static String	DIRECTORY_TEXTS					= "directory_texts";
	public final static String	DIRECTORY_UNKOWN				= "directory_unknown";

	public final static String	CLEAR_CACHE						= "clear_cache";

	public static SharedPreferences getPreferences(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
