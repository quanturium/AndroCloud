package com.quanturium.androcloud.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.quanturium.androcloud.tools.Tools;
import com.quanturium.androcloud.transfert.TransfertTask;
import com.quanturium.androcloud.transfert.upload.UploadTask;

public class UploadActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		String test = intent.getStringExtra(Intent.EXTRA_TEXT);
		
		if (Intent.ACTION_SEND.equals(action) && type != null)
		{			
			ArrayList<Uri> files = getFile(intent); // Handle single file being sent
	
			if (files == null)
			{
				Log.e("Share action", "Sharing file's uri is null");
				Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show();
			}
			else
			{
				File file = null;
	
				for (Uri uri : files)
				{
					file = Tools.UriToFile(this, uri);
	
					Log.i("Share action", "Sharing file's path : " + file.toString());
					TransfertTask task = new UploadTask(this, file);
					
					if(android.os.Build.VERSION.SDK_INT < 11)
						task.execute("test");
					else
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
				}
			}
		}
		else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
		{
			ArrayList<Uri> files = getFiles(intent); // Handle multiple files being sent
	
			if (files == null)
			{
				Log.e("Share action", "Sharing file's uri is null");
				Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show();
			}
			else
			{
				File file = null;
	
				for (Uri uri : files)
				{
					file = Tools.UriToFile(this, uri);
	
					Log.i("Share action", "Sharing file's path : " + file.toString());
					TransfertTask task = new UploadTask(this, file);
					
					if(android.os.Build.VERSION.SDK_INT < 11)
						task.execute("test");
					else
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
				}
			}
		}
		
		finish();
	}
	
	private ArrayList<Uri> getFile(Intent intent)
	{
		Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

		if (fileUri != null)
		{
			ArrayList<Uri> fileUris = new ArrayList<Uri>();
			fileUris.add(fileUri);

			return fileUris;
		}
		else
		{
			return null;
		}

	}

	private ArrayList<Uri> getFiles(Intent intent)
	{
		ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

		return fileUris;
	}
}
