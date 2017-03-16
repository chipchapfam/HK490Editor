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
	/**
	 * 
	 * Enum for sort order
	 *
	 * @author []
	 * @version 1.0
	 * @since 16.03.2017
	 *
	 */
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

	/**
	 * constructor
	 * 
	 * @param bundle
	 *            ResourceBundle
	 * @param programList
	 *            List of programs
	 * @param sortOrder
	 *            SortOrder
	 */
	public SortProgramTask(ResourceBundle bundle, List<ProgramTVObject> programList, SortOrder sortOrder) {
		this.bundle = bundle;
		this.programList = programList;
		this.sortOrder = sortOrder;
	}

	/**
	 * contructor with ASC-Sortorder
	 * 
	 * @param bundle
	 *            ResourceBundle
	 * @param programList
	 *            List of programs
	 */
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

	/**
	 * change sort order
	 * 
	 * @param sortOrder
	 */
	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
}
