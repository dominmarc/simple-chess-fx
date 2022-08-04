/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */

package de.ifd.mad.SimpleChess.main;

import java.net.URISyntaxException;

import de.ifd.mad.SimpleChess.helpers.*;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class for new simplechess starting window
 * 
 * @author MAD
 * @author iFD
 */
public class SimpleChessMain extends Application {

	private static final ChessLogger LOGGER = ChessLogger.createLogger(SimpleChessMain.class);

	/**
	 * Application start point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ChessLogger.load();
		} catch (FileLoadingException e) {
			return;
		}

		for (int i = 1; i < 5; i++)
			LOGGER.info(BasicGameFunctionsHelper.getPrintBar());

		try {
			FileProvider.loadFiles();
			ImageProvider.loadFiles();
		} catch (FileLoadingException | URISyntaxException e) {
			LOGGER.error("{}", e.getMessage());
			return;
		}

		launch(args);
	}

	/**
	 * Loads the in .fxml file defined scene and applies it to the stage that is
	 * about to open
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			FxmlOpener newFXML = new FxmlOpener(FileProvider.getGameMenuURL(), 0, null,
					FileProvider.getGameMenuStyleURL().toString());

			// open
			if (!newFXML.open())
				LOGGER.error("Error on opening file!");
			else
				LOGGER.info("Success...");

		} catch (Exception e) {
			LOGGER.error("Failed to build FxmlOpener - {}", e.getMessage());
		}
	}
}
