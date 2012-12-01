package com.quanturium.androcloud;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.impl.CloudAppImpl;
import com.cloudapp.impl.model.CloudAppItemImpl;
import com.cloudapp.impl.model.DisplayType;
import com.quanturium.androcloud.adapters.FilesAdapter;
import com.quanturium.androcloud.listener.FilesFragmentListener;
import com.quanturium.androcloud.tools.Constant;
import com.quanturium.androcloud.tools.Prefs;

public class FilesFragment extends SherlockFragment implements OnItemClickListener//, MultiChoiceModeListener, OnQueryTextListener
{
	private boolean								isNew				= true;
	private Activity							activity;
	private Handler								handler;
	private com.actionbarsherlock.view.MenuItem	menuItemProgress;
	private com.actionbarsherlock.view.MenuItem	menuItemRefresh;

	private List<CloudAppItem>					files				= new ArrayList<CloudAppItem>();
	public boolean								currentlyLoading	= false;
	private ListView							listView;
	private FilesAdapter						filesAdapter;
	private LinearLayout						emptyView;
	private FilesFragmentListener				listener;

	@Override
	public void onAttach(Activity activity)
	{

		Log.i("debug", "Mainfragment : attach to " + activity.toString());

		super.onAttach(activity);
		try
		{
			listener = (FilesFragmentListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement FilesFragmentListener");
		}
	}

	public void onCreate(Bundle savedInstanceState)
	{
		Log.i("debug", "Mainfragment : create");

		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		setHandler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i("debug", "Mainfragment : create view");

		View mainView = inflater.inflate(R.layout.main, container, false);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		Log.i("debug", "Mainfragment : activity created");

		super.onActivityCreated(savedInstanceState);

		this.activity = getActivity();

		if (getView() != null)
		{
			setListView();
			loadItems();
			isNew = false;
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (isFirstRun() && haveCredentialsChanged()) // Si on est toujours dans le firstRun et que il y a changement d'identifiants, on se trouve dans le cas de la première utilisation avec changement d'identifiant
		{
			setFirstRun(false);
		}

		if (isFirstRun()) // Le client n'a pas defini ses identifiants (premier lancement), du coup on l'oblige a les definir
		{
			firstRun();
		}
		else if (haveCredentialsChanged()) // Modification des identifiants, need reload la liste des fichiers
		{
			setCredentialsChanged(false);
			loadFiles(true, 0);
		}
		else if (hasNumberFilePerRequestChanged()) // Modification des identifiants, need reload la liste des fichiers
		{
			setNumberFilePerRequestChanged(false);
			loadFiles(true, 0);
		}
		else
		{
			if (Prefs.getPreferences(activity).getBoolean(Prefs.REFRESH_AUTO, false))
				loadFiles(false, 0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuItemPreferences:

				startActivity(new Intent(getActivity(), PreferencesActivity.class));

			break;

			case R.id.menuItemRefresh:

				loadFiles(true, 0);

			break;
		}

		return false;
	}

	private boolean isFirstRun()
	{
		return Prefs.getPreferences(getActivity()).getBoolean(Prefs.FIRST_RUN, true);
	}

	private void firstRun()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("First run");
		builder.setCancelable(false);
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				startActivity(new Intent(activity, PreferencesActivity.class));
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setFirstRun(boolean b)
	{
		Prefs.getPreferences(activity).edit().putBoolean(Prefs.FIRST_RUN, b).commit();
	}

	private boolean haveCredentialsChanged()
	{
		return Prefs.getPreferences(activity).getBoolean(Prefs.CREDENTIALS_CHANGED, false);
	}

	private void setCredentialsChanged(boolean b)
	{
		Prefs.getPreferences(activity).edit().putBoolean(Prefs.CREDENTIALS_CHANGED, b).commit();
	}

	private boolean hasNumberFilePerRequestChanged()
	{
		return Prefs.getPreferences(activity).getBoolean(Prefs.FILES_PER_REQUEST_CHANGED, false);
	}

	private void setNumberFilePerRequestChanged(boolean b)
	{
		Prefs.getPreferences(activity).edit().putBoolean(Prefs.FILES_PER_REQUEST_CHANGED, b).commit();
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater)
	{
		inflater.inflate(R.menu.main_activity, menu);

		this.menuItemProgress = menu.findItem(R.id.menuItemProgress);
		this.menuItemRefresh = menu.findItem(R.id.menuItemRefresh);

		if (!currentlyLoading)
			showProgressIcon(false);

//		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//		searchView.setIconifiedByDefault(false);
//		searchView.setOnQueryTextListener(this);
//		searchView.setSubmitButtonEnabled(false);
//		searchView.setQueryHint("Filter files");
//		searchView.setFocusable(false);
//		searchView.setFocusableInTouchMode(false);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void setListView()
	{
		listView = (ListView) activity.findViewById(R.id.listView);
		emptyView = (LinearLayout) activity.findViewById(R.id.emptyView);

		listView.setFocusableInTouchMode(true);
		listView.requestFocus();

		listView.setOnItemClickListener(this);
		listView.setEmptyView(emptyView);
		listView.setTextFilterEnabled(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//		listView.setMultiChoiceModeListener(this);

		filesAdapter = new FilesAdapter(activity, this.files);
		listView.setAdapter(filesAdapter);

		if (android.os.Build.VERSION.SDK_INT < 11) // Verifier si 11 est bien la bonne cible
			registerForContextMenu(listView);
	}

	private void loadItems()
	{
		if (!this.isNew)
		{
			Message message = new Message();
			message.what = Constant.HANDLER_ACTION_LOAD;
			message.arg1 = 1;
			message.obj = files;

			this.handler.sendMessage(message); // Notifier qu'il faut recharger la liste
		}
		else
		{

			if (!isFirstRun())
				loadFiles(true, 0);
		}
	}

	public void showProgressIcon(boolean show)
	{
		if (this.menuItemProgress != null && this.menuItemRefresh != null)
		{
			if (show)
			{
				this.menuItemProgress.setVisible(true);
				this.menuItemRefresh.setVisible(false);
			}
			else
			{
				this.menuItemRefresh.setVisible(true);
				this.menuItemProgress.setVisible(false);
			}
		}
	}

	private void fileRename(String href, String newName)
	{
		for (CloudAppItem file : files)
		{
			try
			{
				if (file.getDisplayType() == DisplayType.VISIBLE && file.getHref().equals(href))
				{
					file.setName(newName);
					filesAdapter.refill(files);
					return;
				}
			}
			catch (CloudAppException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void fileDelete(String href)
	{
		for (CloudAppItem file : files)
		{
			try
			{
				if (file.getDisplayType() == DisplayType.VISIBLE && file.getHref().equals(href))
				{
					files.remove(file);
					filesAdapter.refill(files);
					return;
				}
			}
			catch (CloudAppException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		if (((CloudAppItem) this.filesAdapter.getItem(arg2)).getDisplayType() == DisplayType.LOAD_MORE)
		{
			filesAdapter.lastItemLoading = true;
			filesAdapter.notifyDataSetChanged();

			int page = (int) (this.files.size() / Integer.valueOf(Prefs.getPreferences(activity).getString(Prefs.FILES_PER_REQUEST, "20"))) + 1;
			loadFiles(false, page);
		}
		else
		{
			try
			{
				listener.onFileSelected(((CloudAppItem) this.filesAdapter.getItem(arg2)).toJson());
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (CloudAppException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param reload
	 *            : en cas de reload, tout les elements de la liste seront supprimé avant d'ajouter les nouveaux, en cas de non reload seul les nouveaux elements seront ajoutés
	 * @param page
	 *            : si 0, les elements seront ajouté en haut, sinon en bas
	 */
	private void loadFiles(final boolean reload, final int page)
	{
		if (!currentlyLoading)
		{
			currentlyLoading = true;
			showProgressIcon(true);

			new Thread(new Runnable() {

				@Override
				public void run()
				{
					Message message = new Message();

					if (reload)
						message.what = Constant.HANDLER_ACTION_RELOAD;
					else
						message.what = Constant.HANDLER_ACTION_LOAD;

					message.arg2 = page;

					CloudApp api = new CloudAppImpl(Prefs.getPreferences(activity).getString(Prefs.EMAIL, ""), Prefs.getPreferences(activity).getString(Prefs.PASSWORD, ""));
					List<CloudAppItem> items = null;

					try
					{
						items = api.getItems(page, Integer.valueOf(Prefs.getPreferences(activity).getString(Prefs.FILES_PER_REQUEST, "20")), null, false, null);
						message.arg1 = 1; // success
						message.obj = items;
					}
					catch (CloudAppException e1)
					{
						message.arg1 = 0;
						e1.printStackTrace();
					}
					finally
					{
						handler.sendMessage(message);
					}
				}

			}).start();
		}
		else
		{
			Toast.makeText(activity, "Wait ...", Toast.LENGTH_SHORT).show();
		}
	}

	private void setHandler()
	{
		this.handler = new Handler() {

			@Override
			public void handleMessage(Message msg)
			{

				switch (msg.what)
				{
					case Constant.HANDLER_ACTION_RELOAD: // Si c'est un reload on recharge tout

						@SuppressWarnings("unchecked")
						List<CloudAppItem> items2 = (List<CloudAppItem>) msg.obj;

						if (items2 != null && msg.arg1 == Constant.SUCCESS)
						{
							items2.add(new CloudAppItemImpl());
							FilesFragment.this.files = items2;
							filesAdapter.refill(FilesFragment.this.files);
						}
						else
						{
							Toast.makeText(activity, "Request failed. Check your connection and your credentials", Toast.LENGTH_LONG).show();
						}

						currentlyLoading = false;
						showProgressIcon(false);

					case Constant.HANDLER_ACTION_LOAD: // Si ce n'est pas un reload (juste un load), mais juste un petit rafraichissement, il faut faire la diff avec ce qui est sur l'ecran et ce que l'on a obtenu pour rajouter uniquement ce qui est nouveau

						@SuppressWarnings("unchecked")
						List<CloudAppItem> items = (List<CloudAppItem>) msg.obj;

						if (items != null && msg.arg1 == Constant.SUCCESS)
						{
							int page = msg.arg2;

							if (page == 0)
							{
								FilesFragment.this.files.remove(FilesFragment.this.files.size() - 1);

								OUTERMOST: for (CloudAppItem item : FilesFragment.this.files) // On ajoute uniquement les nouveau
								{
									for (CloudAppItem item2 : items)
									{
										try
										{
											if (item2.getUrl().equals(item.getUrl()))
											{
												continue OUTERMOST;
											}
										}
										catch (CloudAppException e)
										{
											e.printStackTrace();
										}
									}

									items.add(item);
								}

								items.add(new CloudAppItemImpl());
								FilesFragment.this.files = items;
							}
							else
							{
								FilesFragment.this.files.remove(FilesFragment.this.files.size() - 1);

								OUTERMOST: for (CloudAppItem item : items) // On ajoute uniquement les nouveau
								{
									for (CloudAppItem item2 : FilesFragment.this.files)
									{
										try
										{
											if (item2.getUrl().equals(item.getUrl()))
											{
												continue OUTERMOST;
											}
										}
										catch (CloudAppException e)
										{
											e.printStackTrace();
										}
									}

									FilesFragment.this.files.add(item);
								}

								FilesFragment.this.files.add(new CloudAppItemImpl());
							}

							filesAdapter.refill(FilesFragment.this.files);
						}
						else
						{
							Toast.makeText(activity, "Request failed. Check your connection and your credentials", Toast.LENGTH_LONG).show();
						}

						currentlyLoading = false;
						showProgressIcon(false);

					break;

					case Constant.HANDLER_ACTION_RENAME:

						String[] data = (String[]) msg.obj;
						fileRename(data[0], data[1]);

					break;

					case Constant.HANDLER_ACTION_DELETE:

						fileDelete((String) msg.obj);

					break;
				}
			}
		};
	}

	public Handler getHandler()
	{
		return this.handler;
	}

//	@Override
//	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
//	{
//		long[] checkedItems;
//
//		switch (item.getItemId())
//		{
//			case R.id.multiItemSave:
//
//				checkedItems = listView.getCheckedItemIds();
//				Toast.makeText(activity, "multi save", Toast.LENGTH_SHORT).show();
//
//			break;
//
//			case R.id.multiItemDelete:
//
//				checkedItems = listView.getCheckedItemIds();
//				Toast.makeText(activity, "multi delete", Toast.LENGTH_SHORT).show();
//				listView.setItemChecked(0, false);
//
//			break;
//		}
//
//		return true;
//	}

//	@Override
//	public boolean onCreateActionMode(ActionMode mode, Menu menu)
//	{
//		MenuInflater inflater = activity.getMenuInflater();
//		inflater.inflate(R.menu.action_select, menu);
//
//		mode.setTitle("Select Items");
//
//		return true;
//	}
//
//	@Override
//	public void onDestroyActionMode(ActionMode mode)
//	{
//		this.filesAdapter.cancelMultiSelectMode();
//	}
//
//	@Override
//	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
//	{
//		this.filesAdapter.startMultiSelectMode();
//		return true;
//	}
//
//	@Override
//	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
//	{
//		((CloudAppItem) this.filesAdapter.getItem(position)).setChecked(checked);
//
//		int checkedCount = listView.getCheckedItemCount();
//
//		mode.setSubtitle("" + checkedCount + " item(s) selected");
//	}
//
//	@Override
//	public boolean onQueryTextChange(String newText)
//	{
//		if (filesAdapter != null && newText != null && files.size() > 0)
//		{
//			filesAdapter.getFilter().filter(newText);
//			return true;
//		}
//		else
//		{
//			return false;
//		}
//	}
//
//	@Override
//	public boolean onQueryTextSubmit(String query)
//	{
//		return false;
//	}
}
