package com.chipchap.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.chipchap.dao.FavObjectDAO;
import com.chipchap.dao.FavObjectDAOImpl;
import com.chipchap.model.Exporter;
import com.chipchap.model.FavObject;
import com.chipchap.util.JsonUtil;
import com.chipchap.util.ObjectIdentifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.concurrent.Task;

/**
 * 
 * the writer that writes the current configuration of hk490 channel list to a
 * file
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public class ChannelWriter extends Task<Void> implements com.chipchap.io.Writer {
	private final String ENCODING = "UTF-8";
	private ChannelReader reader;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private File file;
	private double subMax = ObjectIdentifier.PROGRESS_MAX / 10.0;

	public ChannelWriter(ChannelReader reader, File file) {
		this.reader = reader;
		this.file = file;
	}

	/**
	 * write to a file
	 * 
	 * @throws IOException
	 */
	@Override
	public void write() throws IOException {
		int startIndex = 1;
		JsonArray aucFavReNames = new JsonArray();
		for (FavObject favObject : reader.getFavList()) {
			aucFavReNames.add(favObject.getName());
		}

		// StringBuffer strBuffer = new StringBuffer();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));

		// write sat object to file
		writeSatelliteList(writer, (startIndex++) * subMax);

		// write transpoonder to file
		writeTransponderList(writer, (startIndex++) * subMax);

		// write tv channel to file
		writeTVProgramList(writer, (startIndex++) * subMax);

		// write radio channel to file
		writeRadioList(writer, (startIndex++) * subMax);

		// write box object to file
		writeBoxObject(writer, aucFavReNames, (startIndex++) * subMax);

		// write watching object to file
		writeWatchingObject(writer, (startIndex++) * subMax);

		// write favorites to file
		writeFavList(writer, (startIndex++) * subMax);

		// write FavListInfoInBoxObject to file
		writeFavListInfoInBoxObject(writer, aucFavReNames, (startIndex++) * subMax);

		// write DatabaseHeader to file
		writeDatabaseHeader(writer, (startIndex++) * subMax);

		// write GlobalVariableObject to file
		writeGlobalVariableObject(writer, (startIndex++) * subMax);

		writer.close();
	}

	private void writeRadioList(Writer writer, double startIndex) throws IOException {
		writeList(reader.getRadioList(), writer, startIndex, subMax);
	}

	private void writeGlobalVariableObject(Writer writer, double startIndex) throws IOException {
		writer.write(gson.toJson(reader.getGlobalVariableObject()));
		updateProgress(startIndex + subMax, ObjectIdentifier.PROGRESS_MAX);
	}

	private void writeSatelliteList(Writer writer, double startIndex) throws IOException {
		writeList(reader.getSatelliteList(), writer, startIndex, subMax);
	}

	private void writeTransponderList(Writer writer, double startIndex) throws IOException {
		writeList(reader.getTransponderList(), writer, startIndex, subMax);
	}

	private void writeTVProgramList(Writer writer, double startIndex) throws IOException {
		int counter = 0;
		int size = reader.getTvList().size();
		for (Exporter tvChannel : reader.getTvList()) {
			writer.write(gson.toJson(tvChannel.export(ObjectIdentifier.TV_ID + counter)));
			counter++;
			updateProgress(startIndex + (subMax / size) * counter, ObjectIdentifier.PROGRESS_MAX);
		}
	}

	private void writeBoxObject(Writer writer, JsonArray aucFavReNames, double startIndex) throws IOException {
		JsonElement boxObjectValue = JsonUtil.get(reader.getBoxObject(), ObjectIdentifier.BOX_ID, false);
		JsonUtil.put(boxObjectValue.getAsJsonObject(), ObjectIdentifier.UCFAVOURITETYPE,
				new JsonPrimitive(aucFavReNames.size()));
		JsonUtil.put(boxObjectValue.getAsJsonObject(), ObjectIdentifier.AUCFAVRENAME_ID, aucFavReNames);
		JsonObject newBoxObject = new JsonObject();
		newBoxObject.add(ObjectIdentifier.BOX_ID, boxObjectValue);

		writer.write(gson.toJson(newBoxObject));
		updateProgress(startIndex + subMax, ObjectIdentifier.PROGRESS_MAX);
	}

	private void writeWatchingObject(Writer writer, double startIndex) throws IOException {
		if (reader.getWatchingProgObject() == null) {
			JsonObject emptyWatchingObject = new JsonObject();
			emptyWatchingObject.add(ObjectIdentifier.WATCHING_ID, new JsonObject());
			writer.write(gson.toJson(emptyWatchingObject));
		} else {
			JsonArray usFavSelect = new JsonArray();
			for (int i = 0; i < reader.getFavList().size(); i++) {
				usFavSelect.add(0);
			}
			JsonObject watchingProgObjectValue = reader.getWatchingProgObject().get(ObjectIdentifier.WATCHING_ID)
					.getAsJsonObject();
			watchingProgObjectValue.remove(ObjectIdentifier.USFAVSELECT);
			watchingProgObjectValue.add(ObjectIdentifier.USFAVSELECT, usFavSelect);
			writer.write(gson.toJson(reader.getWatchingProgObject()));
		}
		updateProgress(startIndex + subMax, ObjectIdentifier.PROGRESS_MAX);
	}

	private void writeFavList(Writer writer, double startIndex) throws IOException {
		int counter = 0;
		int size = reader.getFavList().size();
		for (FavObject favObject : reader.getFavList()) {
			FavObjectDAO favDAO = new FavObjectDAOImpl(reader.getTvList());
			favDAO.findProgs4Fav(favObject);
			favObject.setUiMark(reader.getFavUIMark());
			writer.write(gson.toJson(favObject.export(ObjectIdentifier.FAV_ID + counter)));
			counter++;
			updateProgress(startIndex + (subMax / size) * counter, ObjectIdentifier.PROGRESS_MAX);
		}

	}

	private void writeFavListInfoInBoxObject(Writer writer, JsonArray aucFavReNames, double startIndex)
			throws IOException {
		JsonElement favListInfoInBoxObjectValue = JsonUtil.get(reader.getFavListInfoInBoxObject(),
				ObjectIdentifier.FAV_LIST_ID, false);
		JsonUtil.put(favListInfoInBoxObjectValue.getAsJsonObject(), ObjectIdentifier.UCFAVOURITETYPE,
				new JsonPrimitive(aucFavReNames.size()));
		JsonUtil.put(favListInfoInBoxObjectValue.getAsJsonObject(), ObjectIdentifier.AUCFAVRENAME_ID, aucFavReNames);

		JsonObject newFavListInfoInBoxObject = new JsonObject();
		newFavListInfoInBoxObject.add(ObjectIdentifier.FAV_LIST_ID, favListInfoInBoxObjectValue);

		writer.write(gson.toJson(newFavListInfoInBoxObject));
		updateProgress(startIndex + subMax, ObjectIdentifier.PROGRESS_MAX);
	}

	private void writeDatabaseHeader(Writer writer, double startIndex) throws IOException {
		JsonElement databaseHeaderObjectValue = JsonUtil.get(reader.getDatabaseHeaderObject(),
				ObjectIdentifier.DB_HEADER_ID, false);
		JsonUtil.put(databaseHeaderObjectValue.getAsJsonObject(), ObjectIdentifier.STVNUMBER,
				new JsonPrimitive(reader.getTvList().size()));
		JsonUtil.put(databaseHeaderObjectValue.getAsJsonObject(), ObjectIdentifier.SMAXFAVOR,
				new JsonPrimitive(reader.getFavList().size()));

		JsonObject newDatabaseHeaderObject = new JsonObject();
		newDatabaseHeaderObject.add(ObjectIdentifier.DB_HEADER_ID, databaseHeaderObjectValue);

		writer.write(gson.toJson(newDatabaseHeaderObject));
		updateProgress(startIndex + subMax, ObjectIdentifier.PROGRESS_MAX);
	}

	private void writeList(List<JsonObject> list, Writer writer, double start, double subMax) throws IOException {
		int counter = 0;
		for (JsonObject object : list) {
			writer.write(gson.toJson(object));
			counter++;
			updateProgress(start + (subMax / list.size()) * counter, ObjectIdentifier.PROGRESS_MAX);
		}
	}

	@Override
	protected Void call() throws Exception {
		write();
		return null;
	}
}
