package com.chipchap.task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import com.chipchap.model.ProgramTVObject;

import javafx.concurrent.Task;

/**
 * 
 * The sorting task for the listview
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 15.03.2017
 *
 */
public class SortProgramTask extends Task<Void> {
	public static enum SortOrder {
		ASC(1), DESC(-1);

		private int order;

		private SortOrder(int order) {
			this.order = order;
		}

		public int getOrderValue() {
			return order;
		}
	}

	private SortOrder sortOrder;
	private List<ProgramTVObject> programList;
	private ResourceBundle bundle;

	public SortProgramTask(ResourceBundle bundle, List<ProgramTVObject> programList, SortOrder sortOrder) {
		this.bundle = bundle;
		this.programList = programList;
		this.sortOrder = sortOrder;
	}

	public SortProgramTask(ResourceBundle bundle, List<ProgramTVObject> programList) {
		this(bundle, programList, SortOrder.ASC);
	}

	@Override
	protected Void call() throws Exception {
		updateMessage(bundle.getString("SortProgramTask.msg"));

		Collections.sort(programList, new Comparator<ProgramTVObject>() {
			@Override
			public int compare(ProgramTVObject o1, ProgramTVObject o2) {
				return sortOrder.getOrderValue() * o1.getName().compareTo(o2.getName());
			}
		});
		updateProgress(1, 1);
		return null;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
}
