package com.chipchap.view;

import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 
 * Progressbar dialog view
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 15.03.2017
 *
 */
public class ProgressForm {
	private final Stage dialogStage;
	private final ProgressBar pb = new ProgressBar();
	private Label label = new Label();
	private final int PREF_WIDTH = 250;

	public ProgressForm(ResourceBundle bundle) {
		dialogStage = new Stage();
		dialogStage.initStyle(StageStyle.UTILITY);
		dialogStage.setResizable(false);
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.setTitle(bundle.getString("ProgressForm.dialog.title"));

		label.setPrefWidth(PREF_WIDTH);

		pb.setProgress(-1F);
		pb.setPrefWidth(PREF_WIDTH);

		final VBox hb = new VBox();
		hb.setSpacing(5);
		hb.setAlignment(Pos.CENTER);
		hb.getChildren().addAll(label, pb);

		Scene scene = new Scene(hb);

		dialogStage.setScene(scene);
	}

	public void activateProgressBar(final Task<?> task) {
		pb.progressProperty().bind(task.progressProperty());
		label.textProperty().bind(task.messageProperty());
		dialogStage.show();
	}

	public void deactivateProgressBar() {
		pb.progressProperty().unbind();
		label.textProperty().unbind();
	}

	public Stage getDialogStage() {
		return dialogStage;
	}
}
