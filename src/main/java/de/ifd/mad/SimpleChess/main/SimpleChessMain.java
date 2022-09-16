/*
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */

package de.ifd.mad.SimpleChess.main;

import de.ifd.mad.SimpleChess.helpers.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import static de.ifd.mad.SimpleChess.helpers.Constants.*;

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
     * @param args arguments
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
            addFile(LOCAL_NETWORK_GAME_FILE, "/de/ifd/mad/SimpleChess/main/LocalNetworkChess.fxml");
            addFile(LOCAL_GAME_FILE, "/de/ifd/mad/SimpleChess/main/LocalChess.fxml");
            addFile(GAME_STYLE, "/de/ifd/mad/SimpleChess/main/LocalStyleFile.css");
            addFile(GAME_MENU_FILE, "/de/ifd/mad/SimpleChess/main/StartingForm.fxml");
            addFile(GAME_MENU_STYLE, "/de/ifd/mad/SimpleChess/main/StartingFileStyle.css");

            addImage(PAWN1_IMG, "/de/ifd/mad/SimpleChess/images/bauer1.png");
            addImage(PAWN2_IMG, "/de/ifd/mad/SimpleChess/images/bauer2.png");
            addImage(QUEEN1_IMG, "/de/ifd/mad/SimpleChess/images/dame1.png");
            addImage(QUEEN2_IMG, "/de/ifd/mad/SimpleChess/images/dame2.png");
            addImage(KING1_IMG, "/de/ifd/mad/SimpleChess/images/king1.png");
            addImage(KING2_IMG, "/de/ifd/mad/SimpleChess/images/king2.png");
            addImage(KNIGHT1_IMG, "/de/ifd/mad/SimpleChess/images/pferd1.png");
            addImage(KNIGHT2_IMG, "/de/ifd/mad/SimpleChess/images/pferd2.png");
            addImage(BISHOP1_IMG, "/de/ifd/mad/SimpleChess/images/springer1.png");
            addImage(BISHOP2_IMG, "/de/ifd/mad/SimpleChess/images/springer2.png");
            addImage(ROOK1_IMG, "/de/ifd/mad/SimpleChess/images/turm1.png");
            addImage(ROOK2_IMG, "/de/ifd/mad/SimpleChess/images/turm2.png");

            FileProvider.get().loadFiles();
            ImageProvider.get().loadImages();
        } catch (Exception e) {
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
            FxmlOpener newFXML = new FxmlOpener(FileProvider.get().getFile(Constants.GAME_MENU_FILE), 0, null,
                    FileProvider.get().getFile(Constants.GAME_MENU_STYLE));

            // open
            if (newFXML.open()) {
                LOGGER.info("Success...");
            } else {
                LOGGER.error("Error on opening file!");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to build FxmlOpener - {}", e.getMessage());
        }
    }

    private static void addFile(final String name, final String path) throws FileNotFoundException, URISyntaxException, InvalidKeyException {
        FileProvider.get().addFile(name, path);
    }

    private static void addImage(final String name, final String path) throws FileNotFoundException, URISyntaxException, InvalidKeyException {
        ImageProvider.get().addFile(name, path);
    }
}
