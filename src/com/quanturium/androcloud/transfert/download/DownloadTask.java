package com.quanturium.androcloud.transfert.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.quanturium.androcloud.FileDetailsFragment;
import com.quanturium.androcloud.tools.Constant;
import com.quanturium.androcloud.tools.Prefs;
import com.quanturium.androcloud.transfert.TransfertTask;

public class DownloadTask extends TransfertTask
{

	private CloudAppItem	file;
	private FileDetailsFragment fragment;
	private boolean openWhenSaved;	

	public DownloadTask(Activity a, CloudAppItem file, String fileName, FileDetailsFragment	fragment, boolean openWhenSaved)
	{
		super(a);

		this.file = file;
		this.fileName = fileName;
		this.fragment = fragment;
		this.openWhenSaved = openWhenSaved;
		this.showNotification = Prefs.getPreferences(a).getBoolean(Prefs.DOWNLOAD_NOTIFICATION_SHOW, true);
	}

	@Override
	protected Object doInBackground(String... params)
	{
		try
		{
			long time = 0;
			
			URL url = new URL(file.getContentUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(true);

			int lenghtOfFile = connection.getContentLength();

			File newFile = new File(fragment.getSavingFolder(), fileName);
			newFile.createNewFile();
			FileOutputStream f = new FileOutputStream(newFile);
			Log.i("saved","saved in " + fragment.getSavingFolder() + "/" + fileName);

			InputStream in = connection.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			long total = 0;
			boolean hasCancelled = false;

			while ((len1 = in.read(buffer)) > 0)
			{
				total += len1;								
				
				if(System.currentTimeMillis() > time + 500)
				{
					time = System.currentTimeMillis();
					Log.i("download",total+" bytes (" + (int) ((total / (float) totalSize) * 100) + ") %");
					doPublishProgress((int) ((total * 100) / lenghtOfFile));
				}	
				
				f.write(buffer, 0, len1);
				
				if(isCancelled())
				{
					hasCancelled = true;
					break;
				}
			}
			
			f.close();
			
			if(hasCancelled)
				newFile.delete();
			else
				return newFile;

		}
		catch (MalformedURLException e)
		{
			error = "Can not read file's URL";
			cancel(true);
			e.printStackTrace();
		}
		catch (CloudAppException e)
		{
			error = e.getMessage();
			cancel(true);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			error = "Permission denied : Can not save file in the folder. Please check your preferences";
			cancel(true);
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	protected void onPostExecute(Object result)
	{
		super.onPostExecute(result);
		
		Message message = new Message();
		message.what = Constant.HANDLER_ACTION_SAVE;
		message.arg1 = 1; // success
		message.arg2 = openWhenSaved ? 1 : 0;
		
		fragment.getHandler().sendMessage(message);
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		
		Message message = new Message();
		message.what = Constant.HANDLER_ACTION_SAVE;
		
		if(this.error == null) // si c'est cancel sans error, cela veut dire que c'est l'utilisateur qui a cancel en cliquant sur la croix de la notification
		{
			message.arg1 = 0; // cancel par l'utilisateur
		}
		else
		{
			message.arg1 = -1; // error
		}
		
		fragment.getHandler().sendMessage(message);
	}
	
	@Override
	public String getStringAction()
	{
		return "Download";
	}

	@Override
	public int getTypeAction()
	{
		return 1; // download
	}
}
