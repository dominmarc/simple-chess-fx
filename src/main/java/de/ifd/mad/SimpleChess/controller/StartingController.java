/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import de.ifd.mad.SimpleChess.main.FxmlOpener;
import de.ifd.mad.SimpleChess.main.PopUp;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
	TextField txtPort;

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

	@Override
	public void initialize() {
		info = new PopUp();
		info.createInfoPopUp(INFO_MSG);
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
		this.port = checkPort(txtPort.getText());

		if (this.port == 0) {
			popUp(getPortMsg());
			return;
		}

		if (available(this.port)) {
			popUp("No one is hosting a server there!");
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
	 * Local network multiplayer button click event
	 */
	public void startServerButtonClicked() {
		this.port = checkPort(txtPort.getText());

		if (this.port == 0) {
			popUp(getPortMsg());
			return;
		}

		if (available(this.port))
			open(LN_CHESS, null, LN_CHESS_STYLE, String.valueOf(port));
		else
			popUp("This port is currently not available!");
	}

	/**
	 * Checks if the input matches a port
	 * 
	 * @param input the port to be checked as String
	 * @return the Port as int
	 * @author MAD
	 * @author iFD
	 */
	private int checkPort(String input) {
		if (input.isBlank())
			return 0;

		if (input.matches(""))
			return 0;

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
	 * 
	 * @return the wrong port message
	 */
	private String getPortMsg() {
		return "Your port [" + this.port + "] seems to be wrong!\nPlease use port within the range: 1024 to 9999.";
	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	public static boolean available(int port) {
		if (port < 1024 || port > 9999) {
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
