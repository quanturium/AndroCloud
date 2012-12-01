package com.quanturium.androcloud.transfert;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public abstract class TransfertTask extends AsyncTask<String, Integer, Object>
{
	protected Activity					context			= null;
	protected String				fileName		= "n/a";
	protected TransfertNotification	notification	= null;
	protected int					id				= -1;
	protected long					totalSize		= 0;
	protected boolean showNotification = true;
	protected String error = null;

	public TransfertTask(Activity a)
	{
		this.context = a;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	public abstract int getTypeAction();
	public abstract String getStringAction();

	@Override
	protected void onPreExecute()
	{
		Log.i("Task new", "onPreExecute");
		Toast.makeText(this.context, getStringAction() + " started", Toast.LENGTH_SHORT).show();
		
		if(showNotification)
			notification = new TransfertNotification(this, fileName);
	}

	@Override
	protected Object doInBackground(String... params)
	{
		return null;
	}

	public void doPublishProgress(int progress)
	{
		if (progress > 100)
			progress = 100;

		publishProgress(progress);
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		Log.i("Task #" + this.id, "onProgressUpdate : " + values[0]);
		
		if(showNotification)
			notification.update(values[0]);
	}

	@Override
	protected void onPostExecute(Object result)
	{
		Log.i("Task #" + this.id, "onPostExecute");
		
		if(showNotification)
			notification.finish(result);
	}

	@Override
	protected void onCancelled()
	{
		Log.i("Task #" + this.id, "onCancelled");
		
		TransfertTasksStorage.getInstance().removeTask(this);
		
		if(showNotification)
			notification.cancel(error);
		
	}
}
