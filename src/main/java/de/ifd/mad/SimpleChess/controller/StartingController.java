/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.SecureRandom;

import de.ifd.mad.SimpleChess.main.FxmlOpener;
import de.ifd.mad.SimpleChess.main.PopUp;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

/**
 * Starting form controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class StartingController implements IController {
	@FXML
	Button startServerButton, loMuButton, connectButton;
	@FXML
	Button closeButton, minButton, helpButton;
	@FXML
	Label topBar, chessLabel;
	@FXML
	AnchorPane backPane;
	@FXML
	ListView<String> adressView = new ListView<>();

	int port;
	PopUp info;
	/** Text to be displayed if the user presses the help button */
	static final String INFO_MSG = "Local Multiplayer\n" + "Play against your friends just on that pc!\n\n"
			+ "NetworkMultiplayer\n" + "Connect to your friend with his given port!\n"
			+ "Or simply start your own server and let him connect! :)";

	static final String LN_CHESS = "LocalNetworkChess.fxml";
	static final String L_CHESS = "LocalChess.fxml";
	static final String LN_CHESS_STYLE = "LocalStyleFile.css";
	static final String L_CHESS_STYLE = "LocalStyleFile.css";

	private static final int MAX_PORT = 9999;
	private static final int MIN_PORT = 1024;

	@Override
	public void initialize() {
		info = new PopUp();
		info.createInfoPopUp(INFO_MSG);
		fillAdressList();
	}

	@Override
	public void initVariable(String value) {
		// do nothing
	}

	/**
	 * Help button click event
	 */
	public void helpButtonClicked() {
		info.showNonWaitingPopUp();
	}

	/**
	 * Connect button click event
	 */
	public void connectButtonClicked() {
		if (adressView.getSelectionModel().getSelectedItem() != null)
			this.port = givePort(adressView.getSelectionModel().getSelectedItem());
		else
			popUp("Please select a server!");

		if (this.port == 0) {
			popUp("Something went wrong!");
			return;
		}

		open(LN_CHESS, null, LN_CHESS_STYLE, String.valueOf(port));
	}

	/**
	 * Local multiplayer button click event
	 */
	public void loMuButtonClicked() {
		open(L_CHESS, null, L_CHESS_STYLE, "");
	}

	/**
	 * Local network multiplayer button click event </br>
	 * Start local network multiplayer with a random port
	 */
	public void startServerButtonClicked() {
		// random
		SecureRandom rand = new SecureRandom();

		int randomPort = 0;

		do {
			randomPort = rand.nextInt(MAX_PORT - MIN_PORT + 1) + MIN_PORT;
		} while (Boolean.FALSE.equals(available(randomPort)));

		open(LN_CHESS, null, LN_CHESS_STYLE, String.valueOf(randomPort));
	}

	// fills the ip/ adress list view with active servers
	private void fillAdressList() {
		for (int i = MIN_PORT; i <= MAX_PORT; i++)
			if (Boolean.FALSE.equals(available(i)))
				adressView.getItems().add(("localhost:" + i));

	}

	/**
	 * Returns the selected port
	 * 
	 * @param input the port to be checked as String
	 * @return the Port as int
	 * @author MAD
	 * @author iFD
	 */
	private int givePort(String input) {
		// cut "localhost:"
		input = input.substring("localhost:".length());

		try {
			return Integer.valueOf(input);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void popUp(String msg) {
		PopUp portInfo = new PopUp();
		portInfo.createInfoPopUp(msg);
		portInfo.showNonWaitingPopUp();
	}

	/**
	 * Opens a new window with specified .fxml file
	 * 
	 * @param fxmlFile to open
	 */
	private void open(String fxmlFile, Image icon, String style, String initialValue) {
		// close current window
		Stage current = (Stage) backPane.getScene().getWindow();
		current.close();

		FxmlOpener newFXML = new FxmlOpener(getClass().getResource("/de/ifd/mad/SimpleChess/main/" + fxmlFile), 0, icon,
				getClass().getResource("/de/ifd/mad/SimpleChess/main/" + style).toString());

		newFXML.setInitialValue(initialValue);

		if (!newFXML.open())
			System.out.println("IOException on opening " + fxmlFile + "...");

	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	public static boolean available(int port) {
		if (port < MIN_PORT || port > MAX_PORT) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			// nothing
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
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
