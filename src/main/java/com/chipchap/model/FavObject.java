package com.chipchap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.chipchap.util.ObjectIdentifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * The DTO of favorite
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public class FavObject implements Exporter {
	private String name = "";
	private long uiMark;

	private List<JsonObject> stProgNos = new ArrayList<JsonObject>();
	private List<ProgramTVObject> progs4FavList = new ArrayList<ProgramTVObject>();

	public FavObject() {
	}

	public FavObject(JsonObject object) {
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			String key = entry.getKey();
			JsonObject value = entry.getValue().getAsJsonObject();

			for (Entry<String, JsonElement> valueEntry : value.entrySet()) {
				key = valueEntry.getKey();

				if (key.equals(ObjectIdentifier.STPROGNO)) {
					JsonArray jsonArray = valueEntry.getValue().getAsJsonArray();
					boolean finish = false;

					for (JsonElement element : jsonArray) {
						JsonObject stProgNo = element.getAsJsonObject();

						for (Entry<String, JsonElement> stProgNoEntry : stProgNo.entrySet()) {
							String stProgNokey = stProgNoEntry.getKey();

							if (stProgNokey.equals(ObjectIdentifier.UIWORD32)) {
								if (stProgNoEntry.getValue().getAsBigInteger().intValue() > 0) {
									getStProgNos().add(stProgNo);
								} else {
									finish = true;
								}

								break;
							}
						}

						if (finish) {
							break;
						}
					}

					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<JsonObject> getStProgNos() {
		return stProgNos;
	}

	public void setStProgNos(List<JsonObject> stProgNos) {
		this.stProgNos = stProgNos;
	}

	public List<ProgramTVObject> getProgs4FavList() {
		return progs4FavList;
	}

	public void setProgs4FavList(List<ProgramTVObject> progs4FavList) {
		this.progs4FavList = progs4FavList;
	}

	@Override
	public JsonElement export(String newKey) {
		JsonObject newObjectValue = new JsonObject();
		newObjectValue.addProperty(ObjectIdentifier.UIMARK, uiMark);

		JsonArray stProgNos = new JsonArray();
		JsonArray sRapsNo = new JsonArray();

		for (ProgramTVObject prog : progs4FavList) {
			stProgNos.add(prog.getStProgNo());
			sRapsNo.add(0);
		}
		newObjectValue.add(ObjectIdentifier.STPROGNO, stProgNos);

		for (String defProp : ObjectIdentifier.DEFAULT_PROPS) {
			newObjectValue.addProperty(defProp, 0);
		}

		newObjectValue.add(ObjectIdentifier.SRAPSNO, sRapsNo);

		JsonObject newObject = new JsonObject();
		newObject.add(newKey, newObjectValue);

		return newObject;
	}

	public long getUiMark() {
		return uiMark;
	}

	public void setUiMark(long uiMark) {
		this.uiMark = uiMark;
	}
}
