/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */

package de.ifd.mad.SimpleChess.main;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class for new simplechess starting window
 * 
 * @author MAD
 * @author iFD
 */
public class SimpleChessMain extends Application {
	static final String STARTFORM = "StartingForm.fxml";
	static final String STARTSTYLE = "StartingFileStyle.css";

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
		FxmlOpener newFXML = new FxmlOpener(getClass().getResource("/de/ifd/mad/SimpleChess/main/" + STARTFORM), 0,
				null, getClass().getResource("/de/ifd/mad/SimpleChess/main/" + STARTSTYLE).toString());

		if (!newFXML.open()) {
			System.out.println("IOException on opening " + STARTFORM + "...");
		}
	}
}
