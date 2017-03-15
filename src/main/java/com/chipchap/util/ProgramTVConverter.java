package com.chipchap.util;

import com.chipchap.model.ProgramTVObject;

import javafx.util.StringConverter;

/**
 * 
 * The converter
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 15.03.2017
 *
 */
public class ProgramTVConverter extends StringConverter<ProgramTVObject> {
	@Override
	public String toString(ProgramTVObject object) {
		return object.toString();
	}

	@Override
	public ProgramTVObject fromString(String string) {
		return null;
	}

}
