/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import de.ifd.mad.SimpleChess.main.FxmlOpener;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

/**
 * Starting form controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class StartingController {
	@FXML
	Button loNeButton, loMuButton;
	@FXML
	Button closeButton, minButton;
	@FXML
	Label topBar, chessLabel;
	@FXML
	AnchorPane backPane;

	public void initialize() {

	}

	/**
	 * Local multiplayer button click event
	 */
	public void loMuButtonClicked() {
		open("LocalChess.fxml", null, "LocalStyleFile.css");
	}

	/**
	 * Local network multiplayer button click event
	 */
	public void loNeButtonClicked() {
		open("LocalNetworkChess.fxml", null, "LocalStyleFile.css");
	}

	/**
	 * Opens a new window with specified .fxml file
	 * 
	 * @param fxmlFile to open
	 */
	private void open(String fxmlFile, Image icon, String style) {
		// close current window
		Stage current = (Stage) backPane.getScene().getWindow();
		current.close();

		FxmlOpener newFXML = new FxmlOpener(getClass().getResource("/de/ifd/mad/SimpleChess/main/" + fxmlFile), 0, icon,
				getClass().getResource("/de/ifd/mad/SimpleChess/main/" + style).toString());

		if (!newFXML.open()) {
			System.out.println("IOException on opening " + fxmlFile + "...");
			return;
		}

	}

	/**
	 * Button to minimize the application
	 */
	public void minButtonClicked() {
		Stage tempStage = (Stage) backPane.getScene().getWindow();
		tempStage.setIconified(true);
	}

	/**
	 * Button to close the application
	 */
	public void closeButtonClicked() {
		Stage temp = (Stage) backPane.getScene().getWindow();
		temp.close();
		System.exit(0);
	}
}
