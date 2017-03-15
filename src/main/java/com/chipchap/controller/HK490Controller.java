package com.chipchap.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;

import com.chipchap.dao.FavObjectDAO;
import com.chipchap.dao.FavObjectDAOImpl;
import com.chipchap.io.ChannelReader;
import com.chipchap.io.ChannelWriter;
import com.chipchap.model.FavObject;
import com.chipchap.model.ProgramTVObject;
import com.chipchap.task.SortProgramTask;
import com.chipchap.task.SortProgramTask.SortOrder;
import com.chipchap.util.JsonUtil;
import com.chipchap.util.ObjectIdentifier;
import com.chipchap.util.ViewUtil;
import com.chipchap.view.ProgressForm;
import com.google.gson.JsonElement;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * 
 * The controller for HK490View
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 08.03.2017
 *
 */
@SuppressWarnings("rawtypes")
public class HK490Controller implements ChangeListener, EventHandler<KeyEvent>, Initializable {
	private ResourceBundle bundle;
	private String newFavInfo;
	private String existingFavInfo;
	private String newFavTitle;
	private boolean dirtyFlag = false;
	private ChannelReader reader;
	private Stage stage;
	private SortOrder sortOrder4AllProg;
	private SortOrder sortOrder4FavProg;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private ListView<ProgramTVObject> allProgsListView;
	@FXML
	private ListView<ProgramTVObject> progs4FavListView;
	@FXML
	private ComboBox<FavObject> favoriteComboBox;
	@FXML
	private TextField filterText;
	@FXML
	private MenuButton sortMenuBtn;
	@FXML
	private MenuButton sortMenuFavBtn;
	@FXML
	private Label allProgramLabel;
	@FXML
	private Label favProgramLabel;

	public HK490Controller(ResourceBundle bundle) {
		this.bundle = bundle;
		newFavInfo = bundle.getString("HK490Controller.newFavInfo");
		existingFavInfo = bundle.getString("HK490Controller.existingFavInfo");
		newFavTitle = newFavInfo;
		allProgsListView = new ListView<ProgramTVObject>();
		progs4FavListView = new ListView<ProgramTVObject>();
		favoriteComboBox = new ComboBox<FavObject>();
		filterText = new TextField();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		resetAllProgramLabelText();
		resetFavProgramLabelText(null);
	}

	@FXML
	public void importFile() {
		checkChangesBeforeClose();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(bundle.getString("menu.open.filechooser.title"));

		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			reader = new ChannelReader(file, bundle);
			ProgressForm pForm = new ProgressForm(bundle);
			pForm.activateProgressBar(reader);

			reader.setOnSucceeded(event -> {
				pForm.getDialogStage().close();
				pForm.deactivateProgressBar();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						progs4FavListView.setItems(null);
						stage.setTitle(file.getName());
						allProgsListView.setItems(FXCollections.observableArrayList(reader.getTvList()));
						favoriteComboBox.setItems(FXCollections.observableArrayList(reader.getFavList()));
						initListensers();
						sortMenuBtn.setText(bundle.getString("sortBtn.text"));
						resetAllProgramLabelText();
						dirtyFlag = false;
					}
				});
			});

			pForm.getDialogStage().show();

			Thread thread = new Thread(reader);
			thread.start();
		}
	}

	private void resetAllProgramLabelText() {
		String newLabelText = String.format(bundle.getString("allProgsListView.label"),
				(reader != null ? reader.getTvList().size() : 0) + "");
		allProgramLabel.setText(newLabelText);
	}

	private void resetFavProgramLabelText(FavObject selectFav) {
		String newLabelText = String.format(bundle.getString("progs4FavListView.label"),
				(selectFav != null ? selectFav.getProgs4FavList().size() : 0) + "");
		favProgramLabel.setText(newLabelText);
	}

	private void sort(SortOrder currentSortOrder, SortOrder newOrder, MenuButton sortBtn, String selectedOption,
			ListView<ProgramTVObject> listView, List<ProgramTVObject> list) {
		if (currentSortOrder == newOrder || CollectionUtils.isEmpty(list)) {
			return;
		}

		currentSortOrder = newOrder;

		SortProgramTask task = new SortProgramTask(bundle, list, newOrder);
		ProgressForm pForm = new ProgressForm(bundle);
		pForm.activateProgressBar(task);

		task.setOnSucceeded(event -> {
			pForm.getDialogStage().close();
			pForm.deactivateProgressBar();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					listView.setItems(FXCollections.observableArrayList(list));
					sortBtn.setText(selectedOption);
					dirtyFlag = true;
				}
			});
		});
		pForm.getDialogStage().show();
		Thread thread = new Thread(task);
		thread.start();
	}

	@FXML
	public void sortAllProgA2Z() {
		if (reader != null) {
			sort(sortOrder4AllProg, SortOrder.ASC, sortMenuBtn, bundle.getString("sortBtn.item1.text"),
					allProgsListView, reader.getTvList());
		}
	}

	@FXML
	public void sortAllProgZ2A() {
		if (reader != null) {
			sort(sortOrder4AllProg, SortOrder.DESC, sortMenuBtn, bundle.getString("sortBtn.item2.text"),
					allProgsListView, reader.getTvList());
		}
	}

	@FXML
	public void sortFavProgA2Z() {
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();
		if (selectedFav != null) {
			FavObjectDAO favDAO = new FavObjectDAOImpl(reader.getTvList());
			List<ProgramTVObject> favProgs = favDAO.findProgs4Fav(selectedFav);
			sort(sortOrder4FavProg, SortOrder.ASC, sortMenuFavBtn, bundle.getString("sortBtn.item1.text"),
					progs4FavListView, favProgs);
		}
	}

	@FXML
	public void sortFavProgZ2A() {
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();
		if (selectedFav != null) {
			FavObjectDAO favDAO = new FavObjectDAOImpl(reader.getTvList());
			List<ProgramTVObject> favProgs = favDAO.findProgs4Fav(selectedFav);
			sort(sortOrder4FavProg, SortOrder.DESC, sortMenuFavBtn, bundle.getString("sortBtn.item2.text"),
					progs4FavListView, favProgs);
		}
	}

	@FXML
	public void sortPersistA2Z() {

	}

	@FXML
	public void sortPersistZ2A() {

	}

	@FXML
	public void removeDuplicates() {
		List<ProgramTVObject> uniqueTVPrograms = new ArrayList<ProgramTVObject>();
		for (ProgramTVObject program : reader.getTvList()) {
			if (!uniqueTVPrograms.contains(program)) {
				uniqueTVPrograms.add(program);
			}
		}

		for (FavObject fav : reader.getFavList()) {
			FavObjectDAO favDAO = new FavObjectDAOImpl(reader.getTvList());
			List<ProgramTVObject> favProgs = favDAO.findProgs4Fav(fav);

			Map<Integer, ProgramTVObject> replaceMap = new HashMap<Integer, ProgramTVObject>();
			int indexCounter = 0;
			for (ProgramTVObject favProg : favProgs) {
				if (uniqueTVPrograms.contains(favProg)) {
					int index = uniqueTVPrograms.indexOf(favProg);
					ProgramTVObject uniqueTVProgram = uniqueTVPrograms.get(index);

					if (!uniqueTVProgram.getStProgNo().equals(favProg.getStProgNo())) {
						replaceMap.put(indexCounter, uniqueTVProgram);
					}
				}

				indexCounter++;
			}

			for (Integer key : replaceMap.keySet()) {
				favProgs.set(key, replaceMap.get(key));
			}
		}

		reader.setTvList(uniqueTVPrograms);
		allProgsListView.setItems(FXCollections.observableArrayList(reader.getTvList()));
		resetAllProgramLabelText();
	}

	@FXML
	public void exportFile() {
		if (reader != null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(bundle.getString("menu.save.filechooser.title"));

			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				ChannelWriter writer = new ChannelWriter(reader, file);

				ProgressForm pForm = new ProgressForm(bundle);
				pForm.activateProgressBar(writer);

				writer.setOnSucceeded(event -> {
					pForm.getDialogStage().close();
					pForm.deactivateProgressBar();
					dirtyFlag = false;
				});

				pForm.getDialogStage().show();

				Thread thread = new Thread(writer);
				thread.start();
			}
		}
	}

	@FXML
	public void quit() {
		Platform.exit();
		System.exit(0);
	}

	@FXML
	public void delFavBtnClicked() {
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();
		if (selectedFav != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION, bundle.getString("HK490Controller.deleteFav.comfirm.msg"),
					ButtonType.OK, ButtonType.CANCEL);
			alert.setTitle(bundle.getString("HK490Controller.deleteFav.comfirm.title"));
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {
				if (selectedFav != null) {
					reader.getFavList().remove(selectedFav);
					favoriteComboBox.getItems().remove(selectedFav);
					progs4FavListView.setItems(null);
					dirtyFlag = true;
				}
			}
		} else {
			String error = "HK490Controller.editFav.error.";
			alert(bundle.getString(error + "msg"), bundle.getString(error + "title"));
		}
	}

	@FXML
	public void addFavBtnClicked() {
		if (reader != null) {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle(bundle.getString("HK490Controller.addFav.dialog.title"));
			dialog.setHeaderText(newFavTitle);
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				FavObject existingFavObject = IterableUtils.find(reader.getFavList(), new Predicate<FavObject>() {
					@Override
					public boolean evaluate(FavObject o) {
						return o.getName().equalsIgnoreCase(result.get());
					}
				});

				if (existingFavObject != null) {
					newFavTitle = String.format(existingFavInfo, result.get());
					addFavBtnClicked();
				} else {
					FavObject newFavObject = new FavObject();
					newFavObject.setName(result.get());
					reader.getFavList().add(newFavObject);
					favoriteComboBox.getItems().add(newFavObject);
					favoriteComboBox.getSelectionModel().select(newFavObject);
					newFavTitle = newFavInfo;
					dirtyFlag = true;
				}
			} else {
				newFavTitle = newFavInfo;
			}
		}
	}

	@FXML
	public void editFavBtnClicked() {
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();
		if (selectedFav != null) {
			TextInputDialog dialog = new TextInputDialog(selectedFav.getName());
			dialog.setTitle(bundle.getString("HK490Controller.editFav.dialog.title"));
			dialog.setHeaderText(bundle.getString("HK490Controller.editFav.dialog.headerText"));
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				int index = favoriteComboBox.getItems().indexOf(selectedFav);
				favoriteComboBox.getItems().remove(selectedFav);
				selectedFav.setName(result.get());
				favoriteComboBox.getItems().add(index, selectedFav);
				favoriteComboBox.getSelectionModel().select(selectedFav);
				dirtyFlag = true;
			}
		} else {
			String error = "HK490Controller.editFav.error.";
			alert(bundle.getString(error + "msg"), bundle.getString(error + "title"));
		}
	}

	/**
	 * delete selected programs
	 * 
	 * @param progs
	 *            List of selected programs
	 */
	private void deleteSelectedPrograms(List<ProgramTVObject> progs) {
		Alert alert = new Alert(AlertType.CONFIRMATION, bundle.getString("HK490Controller.delProgs.comfirm.msg"),
				ButtonType.YES, ButtonType.NO);
		alert.setTitle(bundle.getString("HK490Controller.delProgs.comfirm.title"));
		Optional<ButtonType> result = alert.showAndWait();
		long watchingObject = -1;

		JsonElement watchingProgram = JsonUtil.get(reader.getWatchingProgObject().get(ObjectIdentifier.WATCHING_ID),
				ObjectIdentifier.STPROGNO, false);

		if (watchingProgram != null) {
			watchingObject = JsonUtil.get(watchingProgram, ObjectIdentifier.UIWORD32, false).getAsBigInteger()
					.longValue();
		}

		if (result.get() == ButtonType.YES) {
			for (ProgramTVObject prog : progs) {
				dirtyFlag = true;

				if (watchingObject != -1) {
					long progID = JsonUtil.get(prog.getStProgNo(), ObjectIdentifier.UIWORD32, false).getAsBigInteger()
							.longValue();

					if (watchingObject == progID) {
						reader.setWatchingProgObject(null);
					}
				}

				reader.getTvList().remove(prog);
				allProgsListView.getItems().remove(prog);
				for (FavObject fav : reader.getFavList()) {
					fav.getProgs4FavList().remove(prog);
				}
				resetAllProgramLabelText();
			}
		}
	}

	/**
	 * init listensers of view components
	 */
	@SuppressWarnings("unchecked")
	private void initListensers() {
		progs4FavListView.setEditable(true);
		progs4FavListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		progs4FavListView.setCellFactory(lv -> {
			TextFieldListCell<ProgramTVObject> cell = new TextFieldListCell<ProgramTVObject>();
			StringConverter<ProgramTVObject> converter = new StringConverter<ProgramTVObject>() {

				@Override
				public String toString(ProgramTVObject program) {
					return program.getName();
				}

				@Override
				public ProgramTVObject fromString(String string) {
					ProgramTVObject program = cell.getItem();
					program.setName(string);
					dirtyFlag = true;
					return program;
				}
			};

			cell.setConverter(converter);

			return cell;
		});

		allProgsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		allProgsListView.setEditable(true);
		allProgsListView.setOnKeyPressed(this);
		allProgsListView.setCellFactory(lv -> {
			TextFieldListCell<ProgramTVObject> cell = new TextFieldListCell<ProgramTVObject>();
			StringConverter<ProgramTVObject> converter = new StringConverter<ProgramTVObject>() {

				@Override
				public String toString(ProgramTVObject program) {
					return program.getName();
				}

				@Override
				public ProgramTVObject fromString(String string) {
					ProgramTVObject program = cell.getItem();
					program.setName(string);
					dirtyFlag = true;
					return program;
				}

			};

			cell.setConverter(converter);

			return cell;
		});

		filterText.setOnAction((event) -> {
			findNextFilterBtnClicked();
		});

		filterText.textProperty().addListener((observable, oldValue, newValue) -> {
			foundIndex = -1;
		});

		favoriteComboBox.valueProperty().addListener(this);
	}

	/**
	 * move selected programs from one listview to another
	 * 
	 * @param source
	 *            the listview that contains selected programs
	 * @param move2Left
	 *            true: move from right to left | false: move from left to right
	 */
	private void horizontalMove(ListView<ProgramTVObject> source, boolean move2Left) {
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();
		if (selectedFav != null) {
			List<ProgramTVObject> list = source.getSelectionModel().getSelectedItems().stream()
					.collect(Collectors.toList());

			if (list.size() > 0) {
				for (ProgramTVObject prog : list) {
					if (move2Left) {
						selectedFav.getProgs4FavList().remove(prog);
					} else {
						selectedFav.getProgs4FavList().add(prog);
					}
				}
				source.getSelectionModel().clearSelection();
				changeFavComboValue(selectedFav);
				resetFavProgramLabelText(selectedFav);
				dirtyFlag = true;
			}
		} else {
			String error = "HK490Controller.editFav.error.";
			alert(bundle.getString(error + "msg"), bundle.getString(error + "title"));
		}
	}

	@FXML
	public void moveLeftBtnClicked() {
		horizontalMove(progs4FavListView, true);
	}

	@FXML
	public void moveRightBtnClicked() {
		horizontalMove(allProgsListView, false);
	}

	/**
	 * move the selected programs of a listview
	 * 
	 * @param sourceView
	 *            Listview
	 * @param sourceData
	 *            all programs of the listview
	 * @param movingItems
	 *            selected programs
	 * @param moveToPos
	 *            the moving position
	 * @param up
	 *            true: up | false: down
	 */
	private void verticalMove(ListView<ProgramTVObject> sourceView, List<ProgramTVObject> sourceData,
			List<ProgramTVObject> movingItems, int moveToPos, boolean up) {

		if (movingItems.size() > 0) {
			for (ProgramTVObject prog : movingItems) {
				sourceData.remove(prog);
			}

			int newPosition = moveToPos;

			// move to bottom
			if (moveToPos == -1) {
				newPosition = sourceData.size();
			} else if (moveToPos > 0 && up) {
				newPosition = moveToPos - 1;
			} else if (moveToPos > -1 && !up) {
				newPosition = moveToPos + 1;
			} else {
				newPosition = 0;
			}

			for (ProgramTVObject prog : movingItems) {
				sourceData.add(newPosition, prog);
				newPosition++;
			}

			sourceView.setItems(FXCollections.observableArrayList(sourceData));
			sourceView.getSelectionModel().clearSelection();

			for (ProgramTVObject prog : movingItems) {
				sourceView.getSelectionModel().select(prog);
			}

			ViewUtil.scrollTo(sourceView, movingItems.get(0));

			dirtyFlag = true;
		}
	}

	/**
	 * move the selected programs of all-program-listview
	 * 
	 * @param up
	 *            true: up | false: down
	 */
	private void moveAllProgsListView(boolean up) {
		MultipleSelectionModel<ProgramTVObject> selectionModel = allProgsListView.getSelectionModel();
		List<Integer> selectedIndices = selectionModel.getSelectedIndices().stream().collect(Collectors.toList());

		if (selectedIndices.size() > 0 && selectedIndices.size() < reader.getTvList().size()) {
			Collections.sort(selectedIndices);
			List<ProgramTVObject> movingChannels = selectionModel.getSelectedItems().stream()
					.collect(Collectors.toList());
			verticalMove(allProgsListView, reader.getTvList(), movingChannels, selectedIndices.get(0), up);
			sortOrder4AllProg = null;
		}
	}

	@FXML
	public void moveUpLBtnClicked() {
		moveAllProgsListView(true);
	}

	@FXML
	public void moveDownLBtnClicked() {
		moveAllProgsListView(false);
	}

	/**
	 * 
	 * @param up
	 *            true: up | false: down
	 */
	private void multiMoveAllProgsListView(boolean up) {
		List<ProgramTVObject> movingChannels = allProgsListView.getSelectionModel().getSelectedItems().stream()
				.collect(Collectors.toList());
		if (movingChannels.size() > 0 && movingChannels.size() < reader.getTvList().size()) {
			verticalMove(allProgsListView, reader.getTvList(), movingChannels, up ? 0 : -1, up);
			sortOrder4AllProg = null;
		}
	}

	@FXML
	public void multiMoveUpLBtnClicked() {
		multiMoveAllProgsListView(true);
	}

	@FXML
	public void multiMoveDownLBtnClicked() {
		multiMoveAllProgsListView(false);
	}

	@FXML
	public void moveUpRBtnClicked() {
		moveProgs4FavListView(true);
	}

	@FXML
	public void moveDownRBtnClicked() {
		moveProgs4FavListView(false);
	}

	/**
	 * move the selected program of selected favorite
	 * 
	 * @param up
	 *            true: up | false: down
	 */
	private void moveProgs4FavListView(boolean up) {
		MultipleSelectionModel<ProgramTVObject> selectionModel = progs4FavListView.getSelectionModel();
		List<Integer> selectedIndices = selectionModel.getSelectedIndices().stream().collect(Collectors.toList());
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();

		if (selectedIndices.size() > 0 && selectedFav != null) {
			List<ProgramTVObject> allProgs = selectedFav.getProgs4FavList();
			if (selectedIndices.size() < allProgs.size()) {
				Collections.sort(selectedIndices);
				List<ProgramTVObject> movingChannels = selectionModel.getSelectedItems().stream()
						.collect(Collectors.toList());
				verticalMove(progs4FavListView, allProgs, movingChannels, selectedIndices.get(0), up);
				sortOrder4FavProg = null;
			}
		}
	}

	/**
	 * multi moving of selected programs of selected favorite
	 * 
	 * @param up
	 *            true: up | false: down
	 */
	private void multiMoveProgs4FavListView(boolean up) {
		List<ProgramTVObject> movingChannels = progs4FavListView.getSelectionModel().getSelectedItems().stream()
				.collect(Collectors.toList());
		FavObject selectedFav = favoriteComboBox.getSelectionModel().getSelectedItem();

		if (movingChannels.size() > 0 && selectedFav != null) {
			List<ProgramTVObject> allProgs = selectedFav.getProgs4FavList();
			if (movingChannels.size() < allProgs.size()) {
				verticalMove(progs4FavListView, allProgs, movingChannels, up ? 0 : -1, up);
				sortOrder4FavProg = null;
			}
		}
	}

	@FXML
	public void multiMoveUpRBtnClicked() {
		multiMoveProgs4FavListView(true);
	}

	@FXML
	public void multiMoveDownRBtnClicked() {
		multiMoveProgs4FavListView(false);
	}

	private int foundIndex = -1;

	@FXML
	public void findNextFilterBtnClicked() {
		boolean found = false;
		int startCounter = foundIndex > -1 ? (foundIndex + 1) : 0;

		for (int i = startCounter; i < allProgsListView.getItems().size(); i++) {
			String progName = allProgsListView.getItems().get(i).getName().toLowerCase();
			if (progName.contains(filterText.getText().toLowerCase().trim())) {
				foundIndex = i;
				found = true;
				break;
			}
		}

		if (!found) {
			foundIndex = -1;
		}

		if (foundIndex > -1) {
			allProgsListView.getSelectionModel().clearSelection();
			allProgsListView.getSelectionModel().select(foundIndex);
			allProgsListView.getFocusModel().focus(foundIndex);
			allProgsListView.scrollTo(foundIndex);
		}
	}

	@Override
	public void changed(ObservableValue observable, Object oldValue, Object newValue) {
		changeFavComboValue(newValue);
	}

	/**
	 * handle the event of favorite combobox
	 * 
	 * @param newValue
	 */
	private void changeFavComboValue(Object newValue) {
		FavObject selectedFav = (FavObject) newValue;
		if (selectedFav == null) {
			return;
		}

		FavObjectDAO favDAO = new FavObjectDAOImpl(reader.getTvList());
		List<ProgramTVObject> favProgs = favDAO.findProgs4Fav(selectedFav);

		allProgsListView.setItems(FXCollections.observableArrayList(reader.getTvList()));

		progs4FavListView.setItems(FXCollections.observableArrayList(favProgs));
		for (ProgramTVObject prog : favProgs) {
			allProgsListView.getItems().remove(prog);
		}
		resetFavProgramLabelText(selectedFav);
	}

	@Override
	public void handle(KeyEvent event) {
		if (event.getSource().equals(allProgsListView)) {
			List<ProgramTVObject> selectedChannels = allProgsListView.getSelectionModel().getSelectedItems().stream()
					.collect(Collectors.toList());

			if (selectedChannels != null && selectedChannels.size() > 0 && event.getCode().equals(KeyCode.DELETE)) {
				deleteSelectedPrograms(selectedChannels);
			}
		}
	}

	/**
	 * checkt changes before main dialog closes
	 */
	public void checkChangesBeforeClose() {
		if (dirtyFlag) {
			Alert alert = new Alert(AlertType.CONFIRMATION, bundle.getString("HK490Controller.checkChange.comfirm.msg"),
					ButtonType.YES, ButtonType.NO);
			alert.setTitle(bundle.getString("HK490Controller.checkChange.comfirm.title"));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.YES) {
				exportFile();
			}
		}
	}

	/**
	 * show alert dialog
	 * 
	 * @param msg
	 * @param title
	 */
	private void alert(String msg, String title) {
		Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
		alert.setTitle(title);
		alert.showAndWait();
	}
}
