package com.quanturium.androcloud.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.quanturium.androcloud.R;
import com.quanturium.androcloud.fragments.FileDetailsFragment;
import com.quanturium.androcloud.listener.FilesDetailsFragmentListener;
import com.quanturium.androcloud.tools.Constant;

public class FileDetailsActivity extends Activity implements FilesDetailsFragmentListener
{
	private final static String TAG = "FileDetailsActivity";
	private Bundle	resultBundle	= new Bundle();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "Created : " + this.toString());

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Configuration config = getResources().getConfiguration();
		
		int smallestScreenWidthDp = config.smallestScreenWidthDp;

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
			
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.add(R.id.fileDetailsFragment, details, "detailsFragment");
			transaction.commit();
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
		FileDetailsFragment fileDetailsFragment = (FileDetailsFragment) getFragmentManager().findFragmentById(R.id.fileDetailsFragment);
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
		FileDetailsFragment fileDetailsFragment = (FileDetailsFragment) getFragmentManager().findFragmentById(R.id.fileDetailsFragment);

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
