package com.chipchap.model;

import com.google.gson.JsonElement;

/**
 * 
 * the interface for exporting to JsonElement
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public interface Exporter {
	public JsonElement export(String newKey);
}
