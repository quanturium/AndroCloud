package com.quanturium.androcloud.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.impl.model.DisplayType;
import com.quanturium.androcloud.R;
import com.quanturium.androcloud.holders.ItemViewHolder;

public class FilesAdapter extends BaseAdapter implements Filterable
{
	private List<CloudAppItem>	displayItems	= new ArrayList<CloudAppItem>();
	private List<CloudAppItem>	files			= new ArrayList<CloudAppItem>();
	private LayoutInflater		inflater;
	public boolean				lastItemLoading	= false;
	private Filter				filter;
	private boolean				multiSelectMode	= false;

	public FilesAdapter(Context context, List<CloudAppItem> items)
	{
		this.inflater = LayoutInflater.from(context);
		this.displayItems.addAll(items);
		this.files.addAll(items);
	}

	@Override
	public int getCount()
	{
		return this.displayItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return this.displayItems.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		return !(this.displayItems.get(position).getDisplayType() == DisplayType.LOAD_MORE && multiSelectMode);
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		switch (displayItems.get(position).getDisplayType())
		{
			case VISIBLE:

				return 1;

			case LOAD_MORE:
			default:

				return 0;
		}
	}

	public void refill(List<CloudAppItem> items)
	{
		this.lastItemLoading = false;
		this.files.clear();
		this.files.addAll(items);
		this.displayItems.clear();
		this.displayItems.addAll(items);
		notifyDataSetChanged();
	}

	public void clear()
	{
		this.displayItems.clear();
		notifyDataSetChanged();
	}

	public void startMultiSelectMode()
	{
		this.multiSelectMode = true;
	}

	public void cancelMultiSelectMode()
	{
		for (CloudAppItem file : displayItems)
			file.setChecked(false);

		this.multiSelectMode = false;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (displayItems.get(position).getDisplayType() == DisplayType.LOAD_MORE)
		{
			View v = convertView;
			ItemViewHolder itemViewHolder;

			if (v == null)
			{
				itemViewHolder = new ItemViewHolder();
				v = inflater.inflate(R.layout.file_more, null);
				itemViewHolder.title = (TextView) v.findViewById(R.id.fileMoreText);
				itemViewHolder.progressBar = (ProgressBar) v.findViewById(R.id.fileMoreProgress);
				v.setTag(itemViewHolder);
			}
			else
			{
				itemViewHolder = (ItemViewHolder) v.getTag();
			}

			if (lastItemLoading)
			{
				itemViewHolder.title.setVisibility(View.GONE);
				itemViewHolder.progressBar.setVisibility(View.VISIBLE);
			}
			else
			{
				itemViewHolder.progressBar.setVisibility(View.GONE);
				itemViewHolder.title.setVisibility(View.VISIBLE);
			}

			return v;
		}
		else
			if (displayItems.get(position).getDisplayType() == DisplayType.VISIBLE)
			{
				View v = convertView;
				ItemViewHolder itemViewHolder;

				if (v == null)
				{
					itemViewHolder = new ItemViewHolder();
					v = inflater.inflate(R.layout.file_item, null);
					itemViewHolder.title = (TextView) v.findViewById(R.id.fileItemTitle);
					itemViewHolder.date = (TextView) v.findViewById(R.id.fileItemDate);
					itemViewHolder.count = (TextView) v.findViewById(R.id.fileItemCount);
					itemViewHolder.icon = (ImageView) v.findViewById(R.id.fileItemIcon);
					itemViewHolder.checkbox = (CheckBox) v.findViewById(R.id.fileItemCheckbox);
					itemViewHolder.layout = (LinearLayout) v.findViewById(R.id.fileItem);
					v.setTag(itemViewHolder);
				}
				else
				{
					itemViewHolder = (ItemViewHolder) v.getTag();
				}

				try
				{

					itemViewHolder.title.setText(displayItems.get(position).getName());
					itemViewHolder.count.setText(displayItems.get(position).getViewCounter() + "");
					itemViewHolder.date.setText(displayItems.get(position).getUpdatedAt().toLocaleString());

					if (this.multiSelectMode)
					{
						itemViewHolder.checkbox.setVisibility(View.VISIBLE);
					}
					else
					{
						itemViewHolder.checkbox.setVisibility(View.GONE);
					}

					if (displayItems.get(position).isChecked())
					{
						itemViewHolder.layout.setBackgroundResource(android.R.color.holo_blue_light);
						itemViewHolder.checkbox.setChecked(true);
					}
					else
					{
						itemViewHolder.layout.setBackgroundResource(android.R.color.transparent);
						itemViewHolder.checkbox.setChecked(false);
					}

					switch (displayItems.get(position).getItemType())
					{

						case AUDIO:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_audio);
							break;

						case BOOKMARK:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_bookmark);
							break;

						case IMAGE:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_image);
							break;

						case VIDEO:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_video);
							break;

						case TEXT:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_text);
							break;

						case ARCHIVE:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_archive);
							break;

						case UNKNOWN:
						default:
							itemViewHolder.icon.setImageResource(R.drawable.ic_itemtype_unknown);
							break;
					}

				} catch (CloudAppException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return v;
			}
			else
			{
				return convertView;
			}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Filter getFilter()
	{

		final String TAG = "aaaa";

		if (filter == null)
		{
			filter = new Filter()
			{
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results)
				{
					// TODO Auto-generated method stub
					Log.d(TAG, "publishResults");

					displayItems.clear();
					displayItems.addAll((ArrayList<CloudAppItem>) results.values);
					notifyDataSetChanged();
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint)
				{
					Log.d(TAG, "performFiltering");

					constraint = constraint.toString().toLowerCase();
					Log.d(TAG, "constraint : " + constraint);

					List<CloudAppItem> tempFiles = new ArrayList<CloudAppItem>();

					for (int i = 0; i < files.size(); i++)
					{
						CloudAppItem item = files.get(i);

						if (item.getDisplayType() != DisplayType.LOAD_MORE)
						{

							try
							{
								if (item.getName().toLowerCase().contains(constraint))
								{
									tempFiles.add(item);
								}
							} catch (CloudAppException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

					tempFiles.add(files.get(files.size() - 1));

					FilterResults newFilterResults = new FilterResults();
					Log.i("items", tempFiles.size() + "");
					newFilterResults.count = tempFiles.size();
					newFilterResults.values = tempFiles;
					return newFilterResults;
				}
			};
		}

		return filter;
	}
}
