/*
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */
package de.ifd.mad.SimpleChess.helpers;

import de.ifd.mad.SimpleChess.interfaces.IController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * FXML opening helper class
 *
 * @author MAD
 * @author iFD
 */
public class FxmlOpener {
    /**
     * Window stage
     */
    Stage stage;
    /**
     * Location of the FXML file
     */
    URL fxmlFile;
    /**
     * Height of the draggable top of the window
     */
    int topHeight;

    Image icon;

    String style;
    /**
     * Value used to exchange information between controllers
     */
    String passingValue = "";
    /**
     * Used to load the fxml file
     */
    FXMLLoader loader;

    // window location
    private double xOffset = 0;
    private double yOffset = 0;

    private static final ChessLogger LOGGER = ChessLogger.createLogger(FxmlOpener.class);

    /**
     * Constructor for a fxml file to open
     *
     * @param fxmlFile  the specified fxml file (name) as string
     * @param topHeight the height if the draggable area, if you pass 0 it will
     *                  be set to 29 by standard
     * @param icon      the displayed app icon (insert null for standard)
     * @throws Exception due to file operations
     */
    public FxmlOpener(Path fxmlFile, int topHeight, Image icon, Path style) throws Exception {
        if (topHeight == 0 || topHeight < 0)
            topHeight = 29;

        if (icon == null)
            icon = ImageProvider.get().getAsImage(Constants.KING1_IMG);

        this.topHeight = topHeight;
        this.icon = icon;
        this.style = style.toUri().toURL().toString();
        this.fxmlFile = fxmlFile.toUri().toURL();

        if (FileProvider.get().isLoaded())
            this.loader = new FXMLLoader(this.fxmlFile);
        else
            throw new Exception("FileProvider is not loaded!");
    }

    public void setInitialValue(String value) {
        this.passingValue = value;
    }

    /**
     * Stage building function
     *
     * @param topHeight the height if the draggable area, if you pass 0 it will
     *                  be by standard set to 29
     * @param icon      icon path
     * @param style     stylesheet as String
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
     * Tries to open the already built stage
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
