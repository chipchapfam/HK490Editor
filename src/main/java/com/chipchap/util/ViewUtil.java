package com.chipchap.util;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.scene.control.ListView;

/**
 * 
 * util class for javafx view
 *
 * @author []
 * @version 1.0
 * @since 15.03.2017
 *
 */
@SuppressWarnings("restriction")
public class ViewUtil {
	@SuppressWarnings({ "unchecked" })
	public static <T> void scrollTo(ListView<T> listView, T scrollingObject) {
		ListViewSkin<T> ts = (ListViewSkin<T>) listView.getSkin();
		VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
		int firstIndex = listView.getItems().indexOf(vf.getFirstVisibleCell().getItem());
		int lastIndex = listView.getItems().indexOf(vf.getLastVisibleCell().getItem());

		int scrollingIndex = listView.getItems().indexOf(scrollingObject);
		if (scrollingIndex < firstIndex || scrollingIndex > lastIndex) {
			listView.scrollTo(scrollingObject);
		}
	}
}
