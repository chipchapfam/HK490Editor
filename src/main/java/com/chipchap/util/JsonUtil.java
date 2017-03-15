package com.chipchap.util;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * 
 * Util-Class for JsonObject
 *
 * @author []
 * @version 1.0
 * @since 15.03.2017
 *
 */
public class JsonUtil {
	public static JsonElement get(JsonObject parent, String childKey, boolean rootObject) {
		JsonElement retVal = null;

		for (Entry<String, JsonElement> entry : parent.entrySet()) {
			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (rootObject && key.contains(childKey)) {
				retVal = value;
				break;
			} else if (key.equals(childKey)) {
				retVal = value;
				break;
			}
		}

		return retVal;
	}

	public static JsonElement get(JsonElement parent, String childKey, boolean rootObject) {
		return get(parent.getAsJsonObject(), childKey, rootObject);
	}

	public static JsonElement put(JsonObject valueObject, String newKey, JsonElement newValue) {
		valueObject.remove(newKey);
		valueObject.add(newKey, newValue);
		return valueObject;
	}

	public static JsonElement putProperty(JsonObject valueObject, String newKey, JsonPrimitive newValue) {
		put(valueObject, newKey, newValue);
		return valueObject;
	}
}
