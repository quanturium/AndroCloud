package com.quanturium.androcloud.transfert.upload;

import java.io.File;

import android.R;
import android.app.Activity;
import android.util.Log;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.impl.CloudAppImpl;
import com.quanturium.androcloud.tools.Prefs;
import com.quanturium.androcloud.transfert.TransfertTask;

public class UploadTask extends TransfertTask
{

	private File file;
	
	public UploadTask(Activity a, File file)
	{
		super(a);
		
		this.file = file;
		this.fileName = this.file.getName();
		this.showNotification = Prefs.getPreferences(a).getBoolean(Prefs.UPLOAD_NOTIFICATION_SHOW, true);
	}
	
	@Override
	protected Object doInBackground(String... params)
	{
		CloudApp api = new CloudAppImpl(Prefs.getPreferences(context).getString(Prefs.EMAIL, ""), Prefs.getPreferences(context).getString(Prefs.PASSWORD, ""));

		try
		{
			CloudAppItem item = api.upload(this.file, this);
			Log.i("Task #" + this.id, "url : " + item.getUrl());
			
			return item;
		}
		catch (CloudAppException e)
		{
			error = e.getMessage();
			cancel(true);
		}
		catch (InterruptedException e) // Interrupted by the user, nothing to do here
		{
//			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getStringAction()
	{
		return "Upload";
	}

	@Override
	public int getTypeAction()
	{
		return -1; // upload
	}

}
