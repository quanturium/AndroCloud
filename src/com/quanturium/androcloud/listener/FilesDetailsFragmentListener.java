package com.quanturium.androcloud.listener;

import android.os.Message;

public interface FilesDetailsFragmentListener
{
	void onFileDeleted(Message message);
	void onFileRenamed(Message message);
	boolean isDualView();
	void onStartLoading(int targetRes);
	void onStopLoading();
}
