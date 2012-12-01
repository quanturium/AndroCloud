package com.quanturium.androcloud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.quanturium.androcloud.tools.Cache;
import com.quanturium.androcloud.tools.Prefs;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		setClearCacheAction();

		setSummaryCredentials();

		setSummaryDirectoryType();
		setSummaryDirectory(Prefs.DIRECTORY_MOVIES);
		setSummaryDirectory(Prefs.DIRECTORY_MUSICS);
		setSummaryDirectory(Prefs.DIRECTORY_PICTURES);
		setSummaryDirectory(Prefs.DIRECTORY_TEXTS);
		setSummaryDirectory(Prefs.DIRECTORY_UNKOWN);

		setSummaryUploadAction();
		setSummaryDownloadAction();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Prefs.EMAIL) || key.equals(Prefs.PASSWORD))
		{
			setSummaryCredentials();
			Prefs.getPreferences(this).edit().putBoolean(Prefs.CREDENTIALS_CHANGED, true).commit();
		}

		if (key.equals(Prefs.FILES_PER_REQUEST))
		{
			Prefs.getPreferences(this).edit().putBoolean(Prefs.FILES_PER_REQUEST_CHANGED, true).commit();
		}

		if (key.equals(Prefs.DIRECTORY_MOVIES) || key.equals(Prefs.DIRECTORY_MUSICS) || key.equals(Prefs.DIRECTORY_PICTURES) || key.equals(Prefs.DIRECTORY_TEXTS) || key.equals(Prefs.DIRECTORY_UNKOWN))
			setSummaryDirectory(key);

		if (key.equals(Prefs.SAVING_TYPE))
		{
			setSummaryDirectoryType();
			setSummaryDirectory(Prefs.DIRECTORY_MOVIES);
			setSummaryDirectory(Prefs.DIRECTORY_MUSICS);
			setSummaryDirectory(Prefs.DIRECTORY_PICTURES);
			setSummaryDirectory(Prefs.DIRECTORY_TEXTS);
			setSummaryDirectory(Prefs.DIRECTORY_UNKOWN);
		}

		if (key.equals(Prefs.DOWNLOAD_NOTIFICATION_ACTION))
			setSummaryDownloadAction();

		if (key.equals(Prefs.UPLOAD_NOTIFICATION_ACTION))
			setSummaryUploadAction();
	}

	private void setSummaryDownloadAction()
	{
		Preference preferencesDownloadAction = findPreference(Prefs.DOWNLOAD_NOTIFICATION_ACTION);
		String preferencesDownloadActionValue = Prefs.getPreferences(this).getString(Prefs.DOWNLOAD_NOTIFICATION_ACTION, "0");

		String[] preferencesDownloadActions = getResources().getStringArray(R.array.notificationDownloadAction);

		preferencesDownloadAction.setSummary("On click : " + preferencesDownloadActions[Integer.valueOf(preferencesDownloadActionValue)]);
	}

	private void setSummaryUploadAction()
	{
		Preference preferencesUploadAction = findPreference(Prefs.UPLOAD_NOTIFICATION_ACTION);
		String preferencesUploadActionValue = Prefs.getPreferences(this).getString(Prefs.UPLOAD_NOTIFICATION_ACTION, "0");

		String[] preferencesUploadActions = getResources().getStringArray(R.array.notificationUploadAction);

		preferencesUploadAction.setSummary("On click : " + preferencesUploadActions[Integer.valueOf(preferencesUploadActionValue)]);
	}

	private void setSummaryCredentials()
	{
		String preferencesItemEmailValue = Prefs.getPreferences(this).getString(Prefs.EMAIL, "");
		String preferencesItemPasswordValue = Prefs.getPreferences(this).getString(Prefs.PASSWORD, "");

		Preference preferencesItemEmail = findPreference(Prefs.EMAIL);
		Preference preferencesItemPassword = findPreference(Prefs.PASSWORD);

		if (!preferencesItemEmailValue.isEmpty())
			preferencesItemEmail.setSummary(preferencesItemEmailValue);
		else
			preferencesItemEmail.setSummary("Your ClouApp email account");

		if (!preferencesItemPasswordValue.isEmpty())
			preferencesItemPassword.setSummary("**********");
		else
			preferencesItemPassword.setSummary("Your ClouApp password");
	}

	private void setSummaryDirectory(String key)
	{
		Preference preferencesDirectory = findPreference(key);
		String prefType = Prefs.getPreferences(this).getString(Prefs.SAVING_TYPE, "0");

		if (prefType.equals("2")) // custom
		{
			preferencesDirectory.setEnabled(true);
		}
		else if (prefType.equals("1"))
		{
			preferencesDirectory.setEnabled(false);

			Prefs.getPreferences(this).edit().putString(key, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()).commit();
		}
		else
		{
			preferencesDirectory.setEnabled(false);
			String directory = null;

			if (key.equals(Prefs.DIRECTORY_PICTURES))
				directory = Environment.DIRECTORY_PICTURES;
			else if (key.equals(Prefs.DIRECTORY_MUSICS))
				directory = Environment.DIRECTORY_MUSIC;
			else if (key.equals(Prefs.DIRECTORY_MOVIES))
				directory = Environment.DIRECTORY_MOVIES;
			else if (key.equals(Prefs.DIRECTORY_TEXTS))
				directory = Environment.DIRECTORY_DOWNLOADS;
			else if (key.equals(Prefs.DIRECTORY_UNKOWN))
				directory = Environment.DIRECTORY_DOWNLOADS;

			if (directory != null)
				Prefs.getPreferences(this).edit().putString(key, Environment.getExternalStoragePublicDirectory(directory).getPath()).commit();
		}

		String preferencesDirectoryValue = Prefs.getPreferences(this).getString(key, "");

		preferencesDirectory.setSummary(preferencesDirectoryValue);
	}

	private void setSummaryDirectoryType()
	{
		Preference preferencesSavingType = findPreference(Prefs.SAVING_TYPE);
		String preferencesSavingTypeValue = Prefs.getPreferences(this).getString(Prefs.SAVING_TYPE, "0");

		String[] preferencesUploadActions = getResources().getStringArray(R.array.savingType);

		preferencesSavingType.setSummary(preferencesUploadActions[Integer.valueOf(preferencesSavingTypeValue)]);
	}

	private void setClearCacheAction()
	{
		Preference preferencesClearCache = findPreference(Prefs.CLEAR_CACHE);

		preferencesClearCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
				builder.setMessage("Are you sure you want to delete the temporary files ?").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						Cache.removeAll(PreferencesActivity.this);
						dialog.cancel();
						Toast.makeText(PreferencesActivity.this, "Cache cleared", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("No", null);

				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});
	}
}
