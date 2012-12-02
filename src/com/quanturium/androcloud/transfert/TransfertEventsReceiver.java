package com.quanturium.androcloud.transfert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.Toast;

public class TransfertEventsReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(TransfertNotification.CANCEL_BROADCAST_ACTION))
		{
			Log.i("Task #" + intent.getIntExtra("id", -1), "received cancel broadcast action");
			TransfertTask transfertTask = TransfertTasksStorage.getInstance().getTask(intent.getIntExtra("id", -1));
			transfertTask.cancel(true);
		}
		else
			if (intent.getAction().equals(TransfertNotification.NOTIFICATION_BROADCAST_ACTION))
			{
				int type = intent.getIntExtra("type", 0); // -1 : upload ; 1 : download
				int action = intent.getIntExtra("action", -1);
				String actionValue1 = intent.getStringExtra("action_value1");
				String actionValue2 = intent.getStringExtra("action_value2");

				Log.i("Task #" + intent.getIntExtra("id", -1), "received notification broadcast action : " + type);

				switch (type)
				{
					case -1:

						switch (action)
						{
							case 0:

								ClipboardManager cm1 = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
								cm1.setText(actionValue1);

								Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();

								break;

							case 1:

								ClipboardManager cm2 = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
								cm2.setText(actionValue1);

								Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();

								break;

							case 2:

								Intent i2 = new Intent("android.intent.action.VIEW", Uri.parse(actionValue1));
								i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(i2);

								break;

							case 3:

								Intent i3 = new Intent("android.intent.action.VIEW", Uri.parse(actionValue1));
								i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(i3);

								break;
						}

						break;

					case 1:

						switch (action)
						{
							case 0:

								Intent i0 = new Intent();
								i0.setAction(android.content.Intent.ACTION_VIEW);
								i0.setDataAndType(Uri.parse(actionValue1), actionValue2);
								i0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								context.startActivity(i0);

								break;
						}

						break;

					case 0:
					default:

						Toast.makeText(context, "error type action", Toast.LENGTH_SHORT).show();

						break;
				}
			}
	}
}
