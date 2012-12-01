package com.quanturium.androcloud.transfert;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class TransfertTasksStorage
{
	private static TransfertTasksStorage	instance;
	private List<TransfertTask>				tasks	= new ArrayList<TransfertTask>();

	private TransfertTasksStorage()
	{
	}

	public static TransfertTasksStorage getInstance()
	{
		if (null == instance)
		{
			instance = new TransfertTasksStorage();
		}

		return instance;
	}

	public TransfertTask getTask(int i)
	{
		return tasks.get(i);
	}

	public int addTask(TransfertTask task)
	{
		this.tasks.add(task);
		int id = this.tasks.indexOf(task);

		task.setId(id);
		Log.i("Task #" + id, "added to storage");
		return id;
	}

	public void removeTask(TransfertTask uploadTask)
	{
		int i = tasks.indexOf(uploadTask);
		tasks.set(i, null);
	}
}
