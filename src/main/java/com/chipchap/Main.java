package com.chipchap;

import java.util.ResourceBundle;

import com.chipchap.controller.HK490Controller;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * main class of hk490 editor
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 15.03.2017
 *
 */
public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			String basename = this.getClass().getPackage().getName() + "." + "HK490View";
			ResourceBundle bundle = ResourceBundle.getBundle(basename);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("HK490View.fxml"), bundle);
			HK490Controller controller = new HK490Controller(bundle);
			loader.setController(controller);
			VBox page = (VBox) loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.setTitle(bundle.getString("Main.title"));
			primaryStage.setResizable(false);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					controller.checkChangesBeforeClose();
				}
			});
			primaryStage.show();
			controller.setStage(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
