package com.chipchap.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Scanner;

import com.chipchap.model.FavObject;
import com.chipchap.model.ProgramTVObject;
import com.chipchap.util.JsonUtil;
import com.chipchap.util.ObjectIdentifier;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.concurrent.Task;

/**
 * 
 * the reader that reads hk490's channel list
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
public class ChannelReader extends Task<Void> {
	private ResourceBundle bundle;
	private File file;
	private List<JsonObject> satelliteList;
	private List<JsonObject> transponderList;
	private List<ProgramTVObject> tvList;
	private List<JsonObject> radioList;
	private long favUIMark;
	private JsonObject boxObject;
	private JsonObject watchingProgObject;
	private List<FavObject> favList;
	private JsonObject favListInfoInBoxObject;
	private JsonObject databaseHeaderObject;
	private JsonObject globalVariableObject;

	public ChannelReader(String filePath, ResourceBundle bundle) {
		this(new File(filePath), bundle);
	}

	public ChannelReader(File file, ResourceBundle bundle) {
		this.file = file;
		this.bundle = bundle;
	}

	/**
	 * init global properties
	 */
	private void init() {
		satelliteList = new ArrayList<JsonObject>();
		transponderList = new ArrayList<JsonObject>();
		tvList = new ArrayList<ProgramTVObject>();
		radioList = new ArrayList<JsonObject>();
		favUIMark = -1;
		boxObject = null;
		watchingProgObject = null;
		favList = new ArrayList<FavObject>();
		favListInfoInBoxObject = null;
		databaseHeaderObject = null;
		globalVariableObject = null;
	}

	/**
	 * reads hk490 imported file
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException {
		init();
		long count = countLines(file);
		double half = ObjectIdentifier.PROGRESS_MAX / 2.0;

		StringBuilder fileContents = new StringBuilder((int) file.length());
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
		String lineSeparator = System.getProperty("line.separator");
		boolean fristLine = true;

		try {
			updateMessage(bundle.getString("ChannelReader.updateMsg"));
			double counter = 0.0;
			while (scanner.hasNextLine()) {
				String currentLine = scanner.nextLine();

				if (fristLine) {
					fileContents.append("[" + currentLine + lineSeparator);
				} else if (currentLine.contains("}{")) {
					fileContents.append(currentLine.replace("}{", "},{") + lineSeparator);
				} else {
					fileContents.append(currentLine + lineSeparator);
				}
				fristLine = false;
				counter++;
				updateProgress((half / count) * counter, ObjectIdentifier.PROGRESS_MAX);
			}
			fileContents.append("]");

			JsonArray jsonArray = new Gson().fromJson(fileContents.toString(), JsonArray.class);
			counter = 0.0;
			for (JsonElement element : jsonArray) {
				JsonObject object = element.getAsJsonObject();

				for (Entry<String, JsonElement> entry : object.entrySet()) {
					String key = entry.getKey();

					if (key.contains(ObjectIdentifier.SAT_ID)) {
						satelliteList.add(object);
					} else if (key.contains(ObjectIdentifier.TRANS_ID)) {
						transponderList.add(object);
					} else if (key.contains(ObjectIdentifier.TV_ID)) {
						ProgramTVObject newProgram = new ProgramTVObject(object);
						tvList.add(newProgram);
					} else if (key.contains(ObjectIdentifier.RADIO_ID)) {
						radioList.add(object);
					} else if (key.startsWith(ObjectIdentifier.BOX_ID)) {
						boxObject = object;
					} else if (key.contains(ObjectIdentifier.WATCHING_ID)) {
						watchingProgObject = object;
					} else if (key.contains(ObjectIdentifier.FAV_ID)) {
						if (favUIMark == -1) {
							favUIMark = JsonUtil.get(entry.getValue(), ObjectIdentifier.UIMARK, false).getAsLong();
						}
						favList.add(new FavObject(object));
					} else if (key.contains(ObjectIdentifier.FAV_LIST_ID)) {
						favListInfoInBoxObject = object;
					} else if (key.contains(ObjectIdentifier.DB_HEADER_ID)) {
						databaseHeaderObject = object;
					} else if (key.contains(ObjectIdentifier.GLOBAL_ID)) {
						globalVariableObject = object;
					}
				}
				counter++;
				updateProgress(half + (half / jsonArray.size()) * counter, ObjectIdentifier.PROGRESS_MAX);
			}

			postFavList();
		} finally {
			scanner.close();
		}
	}

	/**
	 * post-processing for the list of favorites
	 */
	private void postFavList() {
		for (Entry<String, JsonElement> entry : favListInfoInBoxObject.entrySet()) {
			String key = entry.getKey();

			for (Entry<String, JsonElement> valueEntry : entry.getValue().getAsJsonObject().entrySet()) {
				key = valueEntry.getKey();

				if (key.equals(ObjectIdentifier.AUCFAVRENAME_ID)) {
					JsonArray favArray = valueEntry.getValue().getAsJsonArray();
					for (int i = 0; i < favArray.size(); i++) {
						favList.get(i).setName(favArray.get(i).getAsString().trim());
					}
					break;
				}
			}
		}
	}

	/**
	 * count line number of the file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public int countLines(File file) throws IOException {
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			while (br.readLine() != null)
				count++;
		} finally {
			br.close();
		}
		return count;
	}

	public List<JsonObject> getSatelliteList() {
		return satelliteList;
	}

	public void setSatelliteList(List<JsonObject> satelliteList) {
		this.satelliteList = satelliteList;
	}

	public List<JsonObject> getTransponderList() {
		return transponderList;
	}

	public void setTransponderList(List<JsonObject> transponderList) {
		this.transponderList = transponderList;
	}

	public List<ProgramTVObject> getTvList() {
		return tvList;
	}

	public void setTvList(List<ProgramTVObject> tvList) {
		this.tvList = tvList;
	}

	public List<JsonObject> getRadioList() {
		return radioList;
	}

	public void setRadioList(List<JsonObject> radioList) {
		this.radioList = radioList;
	}

	public JsonObject getBoxObject() {
		return boxObject;
	}

	public void setBoxObject(JsonObject boxObject) {
		this.boxObject = boxObject;
	}

	public JsonObject getWatchingProgObject() {
		return watchingProgObject;
	}

	public void setWatchingProgObject(JsonObject watchingProgObject) {
		this.watchingProgObject = watchingProgObject;
	}

	public List<FavObject> getFavList() {
		return favList;
	}

	public void setFavList(List<FavObject> favList) {
		this.favList = favList;
	}

	public JsonObject getFavListInfoInBoxObject() {
		return favListInfoInBoxObject;
	}

	public void setFavListInfoInBoxObject(JsonObject favListInfoInBoxObject) {
		this.favListInfoInBoxObject = favListInfoInBoxObject;
	}

	public JsonObject getDatabaseHeaderObject() {
		return databaseHeaderObject;
	}

	public void setDatabaseHeaderObject(JsonObject databaseHeaderObject) {
		this.databaseHeaderObject = databaseHeaderObject;
	}

	public JsonObject getGlobalVariableObject() {
		return globalVariableObject;
	}

	public void setGlobalVariableObject(JsonObject globalVariableObject) {
		this.globalVariableObject = globalVariableObject;
	}

	public long getFavUIMark() {
		return favUIMark;
	}

	@Override
	protected Void call() throws Exception {
		read();
		return null;
	}
}
