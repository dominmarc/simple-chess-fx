/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */
package de.ifd.mad.SimpleChess.main;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML opening helper class
 * 
 * @author MAD
 * @author iFD
 */
public class FxmlOpener {

	/** Window stage */
	Stage stage;

	// window location
	private double xOffset = 0;
	private double yOffset = 0;

	/** indicated whether the building process has failed or not */
	private boolean complete = false;

	/**
	 * Constructor for a fxml file to open
	 * 
	 * @param fxmlFile  the specified fxml file (name) as string
	 * @param topHeight the height if the draggable area, if you pass 0 it will
	 *                  standardly set 29
	 * @param icon      the displayed app icon (insert null for standard)
	 */
	public FxmlOpener(URL fxmlFile, int topHeight, Image icon, String style) {
		if (topHeight == 0)
			topHeight = 29;
		if (icon == null)
			icon = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString());
		buildStage(fxmlFile, topHeight, icon, style);
	}

	/**
	 * Stage building function
	 * 
	 * @param fxmlFile  the specified fxml file (name) as string
	 * @param topHeight the height if the draggable area, if you pass 0 it will
	 *                  standardly set 29
	 * @param icon
	 */
	private void buildStage(URL fxmlFile, int topHeight, Image icon, String style) {
		try {
			// setting the scene based on a fxml file
			this.stage = new Stage();
			Parent newRoot = FXMLLoader.load(fxmlFile);
			Scene myScene = new Scene(newRoot);
			// set transparent background
			myScene.setFill(Color.TRANSPARENT);

			// save x and y mouse coordinates of scene
			newRoot.setOnMousePressed(event -> {
				this.xOffset = event.getSceneX();
				this.yOffset = event.getSceneY();
			});

			// move the stage, if the user drags the topBar-Label (height 30) of the scene
			newRoot.setOnMouseDragged(event -> {
				if (this.yOffset < topHeight) {
					this.stage.setX(event.getScreenX() - xOffset);
					this.stage.setY(event.getScreenY() - yOffset);
				}
			});

			// show the stage, apply transparent style, icon and a style-sheet
			this.stage.initStyle(StageStyle.TRANSPARENT);
			this.stage.setScene(myScene);
			this.stage.setResizable(false);

			// add icon if there is one
			this.stage.getIcons().add(icon);

			// add style file
			if (!style.isBlank())
				this.stage.getScene().getStylesheets().add(style);

		} catch (IOException e) {
			e.printStackTrace();
			this.complete = false;
			return;
		}
		this.complete = true;
	}

	/**
	 * Tries to open the built stage
	 * 
	 * @return true or false, depending on stage ready status
	 */
	public boolean open() {
		if (complete) {
			this.stage.show();
			return true;
		} else {
			return false;
		}

	}

}
