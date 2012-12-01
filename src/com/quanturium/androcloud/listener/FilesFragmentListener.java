package com.quanturium.androcloud.listener;

import org.json.JSONObject;

public interface FilesFragmentListener
{
    public void onFileSelected(JSONObject file);
    public boolean isDualView2();
}