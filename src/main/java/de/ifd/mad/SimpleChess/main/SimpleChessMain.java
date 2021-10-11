/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */

package de.ifd.mad.SimpleChess.main;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifd.mad.SimpleChess.helpers.BasicGameFunctionsHelper;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class for new simplechess starting window
 * 
 * @author MAD
 * @author iFD
 */
public class SimpleChessMain extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleChessMain.class);

	/**
	 * Application start point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 1; i < 5; i++)
			LOGGER.info(BasicGameFunctionsHelper.getPrintBar());

		try {
			FileProvider.loadFiles();
			ImageProvider.loadFiles();
		} catch (FileLoadingException | URISyntaxException e) {
			LOGGER.error("", e);
			return;
		}

		launch(args);
	}

	/**
	 * Loads the in .fxml file defined scene and applies it to the stage that is
	 * about to open
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		FxmlOpener newFXML = new FxmlOpener(FileProvider.getGameMenuURL(), 0, null,
				FileProvider.getGameMenuStyleURL().toString());

		// open
		if (!newFXML.open()) {
			LOGGER.error("Error on opening file!");
		} else {
			LOGGER.info("Success...");
		}
	}
}
