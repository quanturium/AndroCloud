package com.cloudapp.impl.model;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.webkit.MimeTypeMap;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;

public class CloudAppItemImpl extends CloudAppModel implements CloudAppItem
{
	private boolean		checked	= false;
	private DisplayType	displayType;

	public CloudAppItemImpl(JSONObject json)
	{
		this.json = json;
		this.displayType = DisplayType.VISIBLE;
	}

	public CloudAppItemImpl()
	{
		this.displayType = DisplayType.LOAD_MORE;
	}

	public DisplayType getDisplayType()
	{
		return this.displayType;
	}

	public void setDisplayType(DisplayType displayType)
	{
		this.displayType = displayType;
	}

	public String getHref() throws CloudAppException
	{
		return getString("href");
	}

	public String getName() throws CloudAppException
	{
		return getString("name");
	}

	public void setName(String value) throws CloudAppException
	{
		setString("name", value);
	}

	public boolean isPrivate() throws CloudAppException
	{
		return getBoolean("private");
	}

	public boolean isSubscribed() throws CloudAppException
	{
		return getBoolean("subscribed");
	}

	public boolean isTrashed() throws CloudAppException
	{
		Date d = getDeletedAt();
		return d != null;
	}

	public String getUrl() throws CloudAppException
	{
		return getString("url");
	}

	public String getContentUrl() throws CloudAppException
	{
		return getString("content_url");
	}

	public Type getItemType() throws CloudAppException
	{
		String t = getString("item_type");
		try
		{
			return Type.valueOf(t.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return Type.UNKNOWN;
		}
	}

	public long getViewCounter() throws CloudAppException
	{
		return getLong("view_counter");
	}

	public String getIconUrl() throws CloudAppException
	{
		return getString("icon");
	}

	public String getRemoteUrl() throws CloudAppException
	{
		return getString("remote_url");
	}

	public String getRedirectUrl() throws CloudAppException
	{
		return getString("redirect_url");
	}

	public String getThumbnailUrl() throws CloudAppException
	{
		return getString("thumbnail_url");
	}

	public String getSource() throws CloudAppException
	{
		return getString("source");
	}

	public Date getCreatedAt() throws CloudAppException
	{
		try
		{
			String d = getString("created_at");
			return format.parse(d);
		}
		catch (ParseException e)
		{
			throw new CloudAppException(500, "Could not parse the date.", e);
		}
	}

	public Date getUpdatedAt() throws CloudAppException
	{
		try
		{
			String d = getString("updated_at");
			return format.parse(d);
		}
		catch (ParseException e)
		{
			throw new CloudAppException(500, "Could not parse the date.", e);
		}
	}

	public Date getDeletedAt() throws CloudAppException
	{
		try
		{
			String d = getString("deleted_at");
			return format.parse(d);
		}
		catch (ParseException e)
		{
			throw new CloudAppException(500, "Could not parse the date.", e);
		}
	}

	@Override
	public JSONObject toJson() throws JSONException, CloudAppException
	{
		JSONObject object = new JSONObject();

		object.put("href", getHref());
		object.put("name", getName());
		object.put("private", isPrivate());
		object.put("subscribed", isSubscribed());
		object.put("url", getUrl());
		object.put("content_url", getContentUrl());
		object.put("item_type", getItemType());
		object.put("view_counter", getViewCounter());
		object.put("icon", getIconUrl());
		object.put("remote_url", getRemoteUrl());
		object.put("thumbnail_url", "");
		object.put("redirect_url", getRedirectUrl());
		object.put("source", getSource());
		// object.put("created_at", getCreatedAt());
		// object.put("updated_at", getUpdatedAt());
		// object.put("deleted_at", getDeletedAt());

		return object;
	}

	@Override
	public void setChecked(boolean b)
	{
		this.checked = b;
	}

	@Override
	public boolean isChecked()
	{
		return this.checked;
	}
}
