package com.chipchap.model;

import java.util.Map.Entry;

import com.chipchap.util.JsonUtil;
import com.chipchap.util.ObjectIdentifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * The DTO of program
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public class ProgramTVObject implements Exporter {
	private JsonObject object;
	private String name;
	private JsonObject stProgNo;

	public ProgramTVObject(JsonObject object) {
		this.object = object;
		int entryCounter = 0;

		for (Entry<String, JsonElement> entry : object.entrySet()) {
			String key = entry.getKey();
			JsonObject value = entry.getValue().getAsJsonObject();

			for (Entry<String, JsonElement> valueEntry : value.entrySet()) {
				key = valueEntry.getKey();
				if (key.equals(ObjectIdentifier.ACSERVICENAME)) {
					name = valueEntry.getValue().getAsString().trim();
					entryCounter++;
				} else if (key.equals(ObjectIdentifier.STPROGNO)) {
					stProgNo = valueEntry.getValue().getAsJsonObject();
					entryCounter++;
				}

				if (entryCounter == 2) {
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public JsonObject getObject() {
		return object;
	}

	public void setObject(JsonObject object) {
		this.object = object;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonObject getStProgNo() {
		return stProgNo;
	}

	public void setStProgNo(JsonObject stProgNo) {
		this.stProgNo = stProgNo;
	}

	@Override
	public JsonElement export(String newKey) {
		JsonObject newObject = new JsonObject();
		JsonObject newValue = JsonUtil.get(object, ObjectIdentifier.TV_ID, true).getAsJsonObject();
		newValue.addProperty(ObjectIdentifier.ACSERVICENAME, name);
		newObject.add(newKey, newValue);

		return newObject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ProgramTVObject) {
			return this.getName().equals(((ProgramTVObject) obj).getName());
		}

		return false;
	}
}
