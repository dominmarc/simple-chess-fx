package de.ifd.mad.SimpleChess.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class StartingController {
	@FXML
	Button LoNeButton, LoMuButton;
	@FXML
	Button closeButton, minButton;
	@FXML
	Label topBar;
	@FXML
	AnchorPane backPane;

	public void initialize() {

	}

	public void LoMuButtonClicked() {

	}

	public void LoNeButtonClicked() {

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
