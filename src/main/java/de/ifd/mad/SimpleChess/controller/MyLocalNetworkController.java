/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
import de.ifd.mad.SimpleChess.main.PopUp;
import de.ifd.mad.SimpleChess.players.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.stage.Stage;

/**
 * Local network multi-player controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class MyLocalNetworkController {
	@FXML
	AnchorPane mypane, buttonPane;
	@FXML
	Button[] buttons;
	@FXML
	Button minButton, closeButton;
	@FXML
	Label active1Label, active2Label, infoLabel, topBar;
	@FXML
	Button startButton, surrenderButton;
	@FXML
	Label player1Text, player2Text;
	
	/**
	 * Player1: 1 - pawn, 2 - rook, 3 - knight, 4 - bishop, 5 - queen, 6 - king
	 * </br>
	 * Player2: 7 - pawn, 8 - rook, 9 - knight, 10 - bishop, 11 - queen, 12 - king
	 * </br>
	 * -1 - Frame, 0 - no figure
	 */
	int[][] gamefield;

	/**
	 * 0 = oldX, 1 = oldY, 2 = newX, 3 = newY</br>
	 * old = former position, new = current position
	 */
	int[] lastMove = { 0, 0, 0, 0 };

	/**
	 * [0]=ButtonIndex (1...64) </br>
	 * [1]=PlayerInfo (1,2) </br>
	 * zeroes mean no selected button
	 */
	int[] selectedButton = { 0, 0 };

	/**
	 * [0] = X</br>
	 * [1] = Y
	 */
	int[] problemKing = { 0, 0 };

	// selectedButtonStyles
	String player1SelectedButton = "-fx-border-color: #2AB4FF; -fx-border-width: 4px; ";
	String player2SelectedButton = "-fx-border-color: #FE2B2B; -fx-border-width: 4px; ";
	String playerNonSelectedButton = "-fx-border-color: #000000; -fx-border-width: 0px;";

	// gamefield button backgrounds representing the game fields
	Background white;
	Background black;

	// check if the game is active
	boolean gameActive = false;

	// initialize figure objects
	Pawn pawn = new Pawn();
	Rook rook = new Rook();
	Knight knight = new Knight();
	Bishop bishop = new Bishop();
	Queen queen = new Queen();
	King king = new King();

	// initialize player objects
	Player player1;
	Player player2;
	
	public void initialize() {
		
	}
	
	/**
	 * Starts the game
	 */
	public void startButtonClicked() {
		if (gameActive) {
			startButton.setText("START GAME");
			gameActive = false;
			player1.setActive(false);
			player2.setActive(false);
			setStatusLabelBackgrounds();
			infoLabel.setText("PRESS START");
			prepareGameField();
			setPlayers();
		} else {
			startButton.setText("RESTART GAME");
			infoLabel.setText("LET'S PLAY");
			resetGlobalVars();
			gameActive = true;
			prepareGameField();
			setPlayers();
			player1.setActive(true);
			player2.setActive(false);
			// activity label
			setStatusLabelBackgrounds();
		}
	}

	/**
	 * Ends the game, shows the winner
	 */
	public void surrenderButtonClicked() {
		gameActive = false;
		PopUp ending = new PopUp();
		ending.createWinningPopUp(getInActivePlayer().getName());
		ending.showPopUp();
		startButton.setText("START GAME");
		infoLabel.setText("PRESS START");
	}
	
	public void printField() {
		for (int g = 0; g < 10; g++) {
			for (int h = 0; h < 10; h++) {
				System.out.print(gamefield[h][g] + "\t");
			}
			System.out.println();
		}
	}

	/**
	 * Function to return the index of a button with given game_field coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int giveIndex(int x, int y) {
		if (x % 8 == 0) {
			return (x * y);
		} else {
			y -= 1;
			return ((y * 8) + x);
		}
	}

	/**
	 * Function to return the coordinates from game_field, according to given button
	 * index </br>
	 * [0]= x value </br>
	 * [1]= y value </br>
	 * 
	 * @param idx
	 * @return int[2]
	 */
	private int[] giveXY(int idx) {
		int[] val = new int[2];
		if (idx % 8 == 0) {
			int y = idx / 8;
			int x = 8;
			val[0] = x;
			val[1] = y;
			return val;
		} else {
			int y = (idx / 8);
			int x = idx - (y * 8);
			y++;
			val[0] = x;
			val[1] = y;
			return val;
		}
	}
	
	/**
	 * Button to minimize the application
	 */
	public void minButtonClicked() {
		Stage tempStage = (Stage) mypane.getScene().getWindow();
		tempStage.setIconified(true);
	}

	/**
	 * Button to close the application
	 */
	public void closeButtonClicked() {
		Stage temp = (Stage) mypane.getScene().getWindow();
		temp.close();
		System.exit(0);
	}
}
