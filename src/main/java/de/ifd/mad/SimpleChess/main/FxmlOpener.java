/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */
package de.ifd.mad.SimpleChess.main;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifd.mad.SimpleChess.controller.IController;
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
	/** Location of the FXML file */
	URL fxmlFile;
	/** Height of the draggable top of the window */
	int topHeight;

	Image icon;

	String style;
	/** Value used to exchange information between controllers */
	String passingValue = "";
	/** Used to load the fxml file */
	FXMLLoader loader;

	// window location
	private double xOffset = 0;
	private double yOffset = 0;

	private static final Logger LOGGER = LoggerFactory.getLogger(FxmlOpener.class);

	/**
	 * Constructor for a fxml file to open
	 * 
	 * @param fxmlFile  the specified fxml file (name) as string
	 * @param topHeight the height if the draggable area, if you pass 0 it will
	 *                  standardly set 29
	 * @param icon      the displayed app icon (insert null for standard)
	 */
	public FxmlOpener(URL fxmlFile, int topHeight, Image icon, String style) {
		if (topHeight == 0 || topHeight < 0)
			topHeight = 29;

		if (icon == null)
			icon = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString());

		this.topHeight = topHeight;
		this.icon = icon;
		this.style = style;
		this.loader = new FXMLLoader(fxmlFile);
	}

	public void setInitialValue(String value) {
		this.passingValue = value;
	}

	/**
	 * Stage building function
	 * 
	 * @param fxmlFile  the specified fxml file (name) as string
	 * @param topHeight the height if the draggable area, if you pass 0 it will
	 *                  standardly set 29
	 * @param icon
	 */
	private boolean buildStage(int topHeight, Image icon, String style) {
		try {
			// setting the scene based on a fxml file
			this.stage = new Stage();
			Parent newRoot = this.loader.load();

			// pass a value to the new controller if there is a value set (probably port)
			if (!this.passingValue.isBlank()) {
				IController controller = loader.getController();
				controller.initVariable(passingValue);
			}

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
			LOGGER.error("IOException on building fxml opener on file [{}] with: {}", fxmlFile, e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Tries to open the built stage
	 * 
	 * @return true or false, depending on stage ready status
	 */
	public boolean open() {
		LOGGER.info("Trying to open fxml file...");
		// try to build the stage
		if (buildStage(topHeight, icon, style)) {
			this.stage.show();
			return true;
		} else {
			return false;
		}
	}

}
