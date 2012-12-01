package com.quanturium.androcloud;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.quanturium.androcloud.listener.FilesDetailsFragmentListener;
import com.quanturium.androcloud.listener.FilesFragmentListener;
import com.quanturium.androcloud.tools.Constant;

public class FilesActivity extends SherlockFragmentActivity implements FilesFragmentListener, FilesDetailsFragmentListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i("debug", "Mainativity : create " + this.toString());

		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_layout);
	}

	@Override
	public void onFileSelected(JSONObject file)
	{
		if (findViewById(R.id.fileDetailsFragment) == null)
		{
			Intent intent = new Intent(this, FileDetailsActivity.class);
			intent.putExtra("json", file.toString());
			startActivityForResult(intent, 1);
		}
		else
		{
			FileDetailsFragment newFragment = new FileDetailsFragment();

			Bundle a = new Bundle();
			a.putString("json", file.toString());

			newFragment.setArguments(a);

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.fileDetailsFragment, newFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null)
		{
			if (data.getIntExtra("type", -1) != -1)
			{

				int type = data.getIntExtra("type", -1);
				Message message = new Message();

				if (type == Constant.HANDLER_ACTION_RENAME)
				{
					message.what = Constant.HANDLER_ACTION_RENAME;
					message.obj = new String[] { data.getStringExtra("href"), data.getStringExtra("new_name") };
					onFileRenamed(message);
				}
				else if (type == Constant.HANDLER_ACTION_DELETE)
				{
					message.what = Constant.HANDLER_ACTION_DELETE;
					message.obj = data.getStringExtra("href");
					onFileDeleted(message);
				}
			}
		}

	}

	@Override
	public boolean isDualView()
	{
		return true;
	}
	
	public boolean isDualView2() // uniquement a utiliser dans FileFragment
	{
		return findViewById(R.id.fileDetailsFragment) != null ? true : false; 
	}

	@Override
	public void onFileDeleted(Message message)
	{
		((FilesFragment) getSupportFragmentManager().findFragmentById(R.id.filesFragment)).getHandler().sendMessage(Message.obtain(message));		
	}

	@Override
	public void onFileRenamed(Message message)
	{
		((FilesFragment) getSupportFragmentManager().findFragmentById(R.id.filesFragment)).getHandler().sendMessage(Message.obtain(message));
	}

	@Override
	public void onStartLoading(int targetRes)
	{
		FilesFragment filesFragment = (FilesFragment) getSupportFragmentManager().findFragmentById(R.id.filesFragment);

		filesFragment.currentlyLoading = true;
		filesFragment.showProgressIcon(true);
	}

	@Override
	public void onStopLoading()
	{
		FilesFragment filesFragment = (FilesFragment) getSupportFragmentManager().findFragmentById(R.id.filesFragment);

		filesFragment.currentlyLoading = false;
		filesFragment.showProgressIcon(false);
	}
}
