package com.quanturium.androcloud.transfert;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.quanturium.androcloud.R;
import com.quanturium.androcloud.tools.Prefs;

public class TransfertNotification
{
	private TransfertTask		transfertTask;
	private int					notificationId					= -2;
	private NotificationManager	notificationManager;
	private Notification		notification;
	public static final String	CANCEL_BROADCAST_ACTION			= "com.quanturium.androcloud.cancel";
	public static final String	NOTIFICATION_BROADCAST_ACTION	= "com.quanturium.androcloud.notification";
	private Object				finishResults					= null;

	public TransfertNotification(TransfertTask transfertTask, String fileName)
	{
		this.transfertTask = transfertTask;

		this.notificationManager = (NotificationManager) transfertTask.context.getSystemService(Context.NOTIFICATION_SERVICE);

		this.notificationId = TransfertTasksStorage.getInstance().addTask(transfertTask);

		if (this.transfertTask.getTypeAction() == -1) // upload
			this.notification = buildNotification(this.notificationId, this.transfertTask.context, fileName, this.transfertTask.getStringAction() + " started", R.drawable.ic_stat_transfert_ticker, R.drawable.ic_stat_upload_running);
		else
			// = 1 : download
			this.notification = buildNotification(this.notificationId, this.transfertTask.context, fileName, this.transfertTask.getStringAction() + " started", R.drawable.ic_stat_transfert_ticker, R.drawable.ic_stat_download_running);

		notificationManager.notify(this.notificationId, this.notification);
	}

	public void update(int progress)
	{
		notification.contentView.setProgressBar(R.id.ProgressBar, 100, progress, false);
		notificationManager.notify(this.notificationId, notification);
	}

	public void cancel(String error)
	{
		this.notificationManager.cancel(this.notificationId);
		Toast.makeText(this.transfertTask.context, error, Toast.LENGTH_SHORT).show();
	}

	public void finish(Object result)
	{
		this.finishResults = result;

		PendingIntent notificationPendingIntent = buildPendingIntent(this.notificationId, transfertTask.context, NOTIFICATION_BROADCAST_ACTION, TransfertEventsReceiver.class);

		notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;

		notification.contentView.setViewVisibility(R.id.ProgressBar, View.GONE);
		notification.contentView.setViewVisibility(R.id.DescriptionText, View.VISIBLE);
		notification.contentView.setViewVisibility(R.id.CancelLayout, View.GONE);
		notification.contentView.setTextViewText(R.id.DescriptionText, getDescription());
		notification.contentIntent = notificationPendingIntent;

		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			notification.contentView.setImageViewResource(R.id.iconImage, R.drawable.ic_stat_transfert_finished);
		}
		else
		{
			if (android.os.Build.VERSION.SDK_INT >= 14)
			{
				notification.largeIcon = BitmapFactory.decodeResource(transfertTask.context.getResources(), R.drawable.ic_stat_transfert_finished);
			}			
		}

		notificationManager.notify(this.notificationId, notification);
	}

	private Notification buildNotification(int id, Context context, String title, String text_ticker, int icon_ticker, int icon_uploading)
	{
		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.upload_notification);
			contentView.setOnClickPendingIntent(R.id.CancelButton, buildPendingIntent(id, context, CANCEL_BROADCAST_ACTION, TransfertEventsReceiver.class));
			contentView.setTextViewText(R.id.TitleText, title);

			Notification notification = new Notification(icon_ticker, title, System.currentTimeMillis());

			notification.setLatestEventInfo(context, title, text_ticker, null);
			notification.contentView = contentView;
			notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE;

			return notification;
		}
		else
		{
			RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.upload_notification);
			contentView.setOnClickPendingIntent(R.id.CancelButton, buildPendingIntent(id, context, CANCEL_BROADCAST_ACTION, TransfertEventsReceiver.class));
			contentView.setViewVisibility(R.id.iconLayout, View.GONE);
			contentView.setTextViewText(R.id.TitleText, title);

			Notification.Builder builder = new Notification.Builder(context);

			builder.setTicker(text_ticker);
			builder.setSmallIcon(icon_ticker);
			
			Log.i("notifSize", context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width) + ";" + context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height));
			
//			builder.setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), icon_uploading), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height), false));
			
			if (android.os.Build.VERSION.SDK_INT >= 14)
			{
				builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon_uploading));
			}
			
			builder.setOngoing(true);
			builder.setAutoCancel(false);
			builder.setOnlyAlertOnce(true);
			builder.setContent(contentView);

			Notification notification = builder.getNotification(); 
			
			return notification;
		}
	}

	private PendingIntent buildPendingIntent(int id, Context context, String action, Class<?> cls)
	{
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		intent.putExtra("id", id);

		if (action == NOTIFICATION_BROADCAST_ACTION && finishResults != null)
		{
			int intentType = transfertTask.getTypeAction(); // -1 : upload ; 1 : download
			int intentAction = -1;
			String intentActionValue1 = null;
			String intentActionValue2 = null;

			if (finishResults != null)
			{

				switch (transfertTask.getTypeAction())
				{
					case -1:

						intentAction = Integer.valueOf(Prefs.getPreferences(transfertTask.context).getString(Prefs.UPLOAD_NOTIFICATION_ACTION, "0"));
						CloudAppItem item = (CloudAppItem) finishResults;

						try
						{
							switch (intentAction)
							{
								case 0:

									intentActionValue1 = item.getUrl();

								break;

								case 1:

									intentActionValue1 = item.getRemoteUrl();

								break;

								case 2:

									intentActionValue1 = item.getUrl();

								break;

								case 3:
									
									intentActionValue1 = item.getRemoteUrl();

								break;
							}
						}
						catch (CloudAppException e)
						{
							e.printStackTrace();
						}

					break;

					case 1:

						intentAction = Integer.valueOf(Prefs.getPreferences(transfertTask.context).getString(Prefs.DOWNLOAD_NOTIFICATION_ACTION, "0"));
						File file = (File) finishResults;

						switch (intentAction)
						{
							case 0:

								String uri = Uri.fromFile(file).toString();
								String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
								MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
								String mimeType = mimeTypeMap.getMimeTypeFromExtension(extension);

								intentActionValue1 = uri;
								intentActionValue2 = mimeType;

							break;
						}

					break;

				}

			}

			intent.putExtra("type", intentType);
			intent.putExtra("action", intentAction);
			intent.putExtra("action_value1", intentActionValue1);
			intent.putExtra("action_value2", intentActionValue2);
		}

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

		return pendingIntent;
	}

	private String getDescription()
	{
		if (this.transfertTask.getTypeAction() == -1) // upload
		{
			String preferencesUploadActionValue = Prefs.getPreferences(transfertTask.context).getString(Prefs.UPLOAD_NOTIFICATION_ACTION, "0");
			String[] preferencesUploadActions = transfertTask.context.getResources().getStringArray(R.array.notificationUploadAction);
			return "On click : " + preferencesUploadActions[Integer.valueOf(preferencesUploadActionValue)];
		}
		else
		// download
		{
			String preferencesDownloadActionValue = Prefs.getPreferences(transfertTask.context).getString(Prefs.DOWNLOAD_NOTIFICATION_ACTION, "0");
			String[] preferencesDownloadActions = transfertTask.context.getResources().getStringArray(R.array.notificationDownloadAction);
			return "On click : " + preferencesDownloadActions[Integer.valueOf(preferencesDownloadActionValue)];
		}
	}
}
