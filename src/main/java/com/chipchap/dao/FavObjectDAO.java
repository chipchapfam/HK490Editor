package com.chipchap.dao;

import java.util.List;

import com.chipchap.model.FavObject;
import com.chipchap.model.ProgramTVObject;

/**
 * 
 * The DAO interface for favorites
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public interface FavObjectDAO {
	/**
	 * find programs for the selected favorite
	 * 
	 * @param favObject
	 *            the selected favorite
	 * @return list of programs
	 */
	public List<ProgramTVObject> findProgs4Fav(FavObject favObject);

	/**
	 * returns true if the list of favorites has an item with same favorite name
	 * 
	 * @param list
	 *            list of favorites
	 * @param favObjectName
	 *            name of favorite
	 * @return
	 */
	public boolean contains(List<FavObject> list, String favObjectName);
}
