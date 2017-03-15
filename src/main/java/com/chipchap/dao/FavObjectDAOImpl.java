package com.chipchap.dao;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;

import com.chipchap.model.FavObject;
import com.chipchap.model.ProgramTVObject;
import com.chipchap.util.JsonUtil;
import com.chipchap.util.ObjectIdentifier;
import com.google.gson.JsonObject;

/**
 * 
 * the implementation of FavObjectDAO
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public class FavObjectDAOImpl implements FavObjectDAO {
	private List<ProgramTVObject> progs;

	public FavObjectDAOImpl(List<ProgramTVObject> progs) {
		this.progs = progs;
	}

	@Override
	public List<ProgramTVObject> findProgs4Fav(FavObject favObject) {
		List<ProgramTVObject> foundProgs = favObject.getProgs4FavList();

		if (CollectionUtils.isEmpty(favObject.getProgs4FavList())
				&& CollectionUtils.isNotEmpty(favObject.getStProgNos())) {
			for (ProgramTVObject prog : progs) {
				JsonObject stProgNo = prog.getStProgNo();
				long progID = JsonUtil.get(stProgNo, ObjectIdentifier.UIWORD32, false).getAsBigInteger().longValue();

				for (JsonObject favStProgNo : favObject.getStProgNos()) {
					long channelID = JsonUtil.get(favStProgNo, ObjectIdentifier.UIWORD32, false).getAsBigInteger()
							.longValue();
					if (progID == channelID) {
						foundProgs.add(prog);
						break;
					}
				}
			}

			favObject.setProgs4FavList(foundProgs);
			favObject.setStProgNos(null);
		}

		return foundProgs;
	}

	@Override
	public boolean contains(List<FavObject> list, String favObjectName) {
		return IterableUtils.find(list, new Predicate<FavObject>() {
			@Override
			public boolean evaluate(FavObject o) {
				return o.getName().equalsIgnoreCase(favObjectName);
			}
		}) != null;
	}

}
