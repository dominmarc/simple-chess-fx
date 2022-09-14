/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.security.SecureRandom;

import de.ifd.mad.SimpleChess.helpers.ChessLogger;
import de.ifd.mad.SimpleChess.helpers.PopUpProvider;
import de.ifd.mad.SimpleChess.interfaces.IController;
import de.ifd.mad.SimpleChess.helpers.FileProvider;
import de.ifd.mad.SimpleChess.helpers.FxmlOpener;
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
	/** start ln multiplayer as server button */
	@FXML
	Button startServerButton;
	/** start local multiplayer */
	@FXML
	Button loMuButton;
	/** start ln multiplayer as client button */
	@FXML
	Button connectButton;
	/** close application button */
	@FXML
	Button closeButton;
	/** minimize application button */
	@FXML
	Button minButton;
	/** open the help instructions button */
	@FXML
	Button helpButton;
	/** dragable top of application */
	@FXML
	Label topBar;
	/** Container for all our visible elements */
	@FXML
	AnchorPane backPane;
	/** stores the available adresses in the local network */
	@FXML
	ListView<String> adressView = new ListView<>();

	/* PopUpProvider-Object */
	final PopUpProvider info = PopUpProvider.createInfoPopUp(INFO_MSG);

	private static final ChessLogger LOGGER = ChessLogger.createLogger(StartingController.class);

	/** Text to be displayed if the user presses the help button */
	static final String INFO_MSG = "Local Multiplayer\n" + "Play against your friends just on that pc!\n\n"
			+ "NetworkMultiplayer\n" + "Connect to your friend with his given port!\n"
			+ "Or simply start your own server and let him connect! :)";

	/* max. port */
	private static final int MAX_PORT = 9999;
	/* min. port */
	private static final int MIN_PORT = 1024;

	@Override
	public void initialize() {
		LOGGER.info("Initializing starting page...");
		fillAdressList();
		LOGGER.info("Initialized starting page!");
	}

	@Override
	public void initVariable(String value) {
		// do nothing
	}

	/**
	 * Help button click event
	 */
	public void helpButtonClicked() {
		LOGGER.info("Showing help information pop up...");
		info.showNonWaitingPopUp();
	}

	/**
	 * Connect button click event
	 */
	public void connectButtonClicked() {
		LOGGER.info("Trying to start local network multiplayer as client...");
		int port = 0;

		// check if user selected an adress
		if (adressView.getSelectionModel().getSelectedItem() != null)
			port = givePort(adressView.getSelectionModel().getSelectedItem());
		else {
			LOGGER.warn("User did not select any server!");
			popUp("Please select a server!");
			return;
		}
		// in case givePort() somehow returns 0
		if (port == 0) {
			popUp("Something went wrong!");
			return;
		}

		// 01 means client
		String message = "01" + String.valueOf(port);

		// start
		open(FileProvider.getNetworkGameURL(), null, FileProvider.getGameStyleURL(), message);
	}

	/**
	 * Local multiplayer button click event
	 */
	public void loMuButtonClicked() {
		LOGGER.info("Trying to start local multiplayer...");
		open(FileProvider.getLocalGameURL(), null, FileProvider.getGameStyleURL(), "");
	}

	/**
	 * Local network multiplayer button click event </br>
	 * Start local network multiplayer with a random port
	 */
	public void startServerButtonClicked() {
		LOGGER.info("Trying to start local network multiplayer as server...");
		// random
		SecureRandom rand = new SecureRandom();

		int randomPort = 0;

		// generate new port until the port is available
		do {
			randomPort = rand.nextInt(MAX_PORT - MIN_PORT + 1) + MIN_PORT;
		} while (Boolean.FALSE.equals(available(randomPort)));

		// 02 means server
		String message = "02" + String.valueOf(randomPort);

		// start
		open(FileProvider.getNetworkGameURL(), null, FileProvider.getGameStyleURL(), message);
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
	 * 
	 * @return the Port as int
	 * 
	 * @author MAD
	 * @author iFD
	 */
	private int givePort(String input) {
		// cut "localhost:"
		input = input.substring("localhost:".length());

		// just return a number in order to check port
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			LOGGER.info("Error on parsing port: " + input, e);
			return 0;
		}
	}

	private void popUp(String msg) {
		PopUpProvider.createInfoPopUp(msg).showPopUp();
	}

	/**
	 * Opens a new window with specified .fxml file
	 * 
	 * @param fxmlFile to open
	 */
	private void open(URL fxmlFile, Image icon, URL style, String initialValue) {
		LOGGER.info("Trying to open fxml: [" + fxmlFile + "] with initialValue: " + initialValue + "...");
		FxmlOpener newFXML;

		// construct opener
		try {
			newFXML = new FxmlOpener(fxmlFile, 0, icon, style.toString());
		} catch (Exception e) {
			// FileProvider not loaded
			LOGGER.error("", e);
			return;
		}

		// pass a value (port)
		newFXML.setInitialValue(initialValue);

		// open
		if (!newFXML.open()) {
			LOGGER.error("Error on opening file!");
		} else {
			LOGGER.info("Success... closing start window...");
			// close current window
			Stage current = (Stage) backPane.getScene().getWindow();
			current.close();
		}
	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	public static boolean available(int port) {
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
			// close datagram socket
			if (ds != null) {
				ds.close();
			}

			// close server socket
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

		if (temp != null) {
			LOGGER.warn("Someone closed the application on exit button.");
			LOGGER.info("==============================================");
			LOGGER.info("=====================END======================");
			LOGGER.info("==============================================");

			temp.close();
			System.exit(0);
		} else
			LOGGER.error("Could not close application on by exit button!");
	}
}
