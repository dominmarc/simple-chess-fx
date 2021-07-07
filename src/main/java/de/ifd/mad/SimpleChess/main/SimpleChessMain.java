/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */

package de.ifd.mad.SimpleChess.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main class for new simplechess starting window
 * 
 * @author MAD
 * @author iFD
 */

public class SimpleChessMain extends Application {
	/** x value of the upper left scene corner */
	private double xOffset = 0;
	/** y value of the upper left scene corner */
	private double yOffset = 0;

	/**
	 * Application start point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Loads the in .fxml file defined scene and applies it to the stage that is
	 * about to open
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// setting the scene based on a fxml file
		Parent root = FXMLLoader.load(getClass().getResource("Chess.fxml"));
		Scene myScene = new Scene(root);
		// set transparent background
		myScene.setFill(Color.TRANSPARENT);

		// save x and y coordinates of scene
		root.setOnMousePressed(e -> {
			xOffset = e.getSceneX();
			yOffset = e.getSceneY();
		});

		// move the stage, if the user drags the topBar-Label (height 27) of the scene
		root.setOnMouseDragged(e -> {
			if (yOffset < 26) {
				primaryStage.setX(e.getScreenX() - xOffset);
				primaryStage.setY(e.getScreenY() - yOffset);
			}
		});

		// show the stage, apply transparent style, icon and a style-sheet
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.setScene(myScene);
		primaryStage.getIcons()
				.add(new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString()));
		primaryStage.getScene().getStylesheets().add(getClass().getResource("StyleFile.css").toString());
		primaryStage.setTitle("SimpleSudoku");
		primaryStage.setResizable(false);
		primaryStage.show();
	}
}
