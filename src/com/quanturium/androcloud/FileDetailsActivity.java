package com.quanturium.androcloud;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.quanturium.androcloud.listener.FilesDetailsFragmentListener;
import com.quanturium.androcloud.tools.Constant;

public class FileDetailsActivity extends SherlockFragmentActivity implements FilesDetailsFragmentListener
{
	private Bundle	resultBundle	= new Bundle();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("debug", "ativity : create " + this.toString());

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Configuration config = getResources().getConfiguration();
		
		int smallestScreenWidthDp = 0;
		
		if(android.os.Build.VERSION.SDK_INT >= 11)
			smallestScreenWidthDp = config.smallestScreenWidthDp;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && smallestScreenWidthDp >= 600)
		{
			setContentView(R.layout.fragment_layout2);
			finish();
		}

		if (savedInstanceState == null)
		{
			setContentView(R.layout.fragment_layout2);

			// During initial setup, plug in the details fragment.
			FileDetailsFragment details = new FileDetailsFragment();
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().add(R.id.fileDetailsFragment, details).commit();
		}
		else
		{
			setContentView(R.layout.fragment_layout2);
		}
	}

	@Override
	public void finish()
	{
		Intent intent = new Intent();
		intent.putExtras(this.resultBundle);
		setResult(Activity.RESULT_OK, intent);

		super.finish();
	}

	@Override
	public boolean isDualView()
	{
		return false;
	}

	@Override
	public void onFileRenamed(Message message)
	{
		String[] data = (String[]) message.obj;

		this.resultBundle.putInt("type", Constant.HANDLER_ACTION_RENAME);
		this.resultBundle.putString("href", (String) data[0]);
		this.resultBundle.putString("new_name", (String) data[1]);
	}

	@Override
	public void onFileDeleted(Message message)
	{
		this.resultBundle.putInt("type", Constant.HANDLER_ACTION_DELETE);
		this.resultBundle.putString("href", (String) message.obj);
		finish();
	}

	@Override
	public void onStartLoading(int targetRes)
	{
		FileDetailsFragment fileDetailsFragment = (FileDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fileDetailsFragment);
		try
		{
			fileDetailsFragment.currentlyLoading = true;
			fileDetailsFragment.currentlyAction = targetRes;
			invalidateOptionsMenu();
		}
		catch (NullPointerException e)
		{

		}
	}

	@Override
	public void onStopLoading()
	{
		FileDetailsFragment fileDetailsFragment = (FileDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fileDetailsFragment);

		try
		{
			fileDetailsFragment.currentlyLoading = false;
			fileDetailsFragment.currentlyAction = -1;
			invalidateOptionsMenu();
		}
		catch (NullPointerException e)
		{

		}
	}
}
