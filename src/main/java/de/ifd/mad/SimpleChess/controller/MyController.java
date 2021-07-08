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
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * Controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */

public class MyController {
	@FXML
	AnchorPane mypane, buttonPane;
	@FXML
	Button[] buttons;
	@FXML
	Button minButton, closeButton;
	@FXML
	Label active1Label, active2Label, infoLabel, topBar;
	@FXML
	Button startButton;
	@FXML
	Label player1Text, player2Text;

	/**
	 * Player1: 1 - Bauer 2 - Turm 3 - Pferd 4 - Laeufer 5 - Dame 6 - Koenig </br>
	 * Player2: 7 - Bauer 8 - Turm 9 - Pferd 10 - Laeufer 11 - Dame 12 - Koenig
	 */
	int[][] gamefield;

	/**
	 * 0 = oldX, 1 = oldY, 2 = newX, 3 = newY old = former position new = current
	 * position
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
	boolean game_active = false;

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

	// TODO: 	-stepBack() does not work appropriately
	// 			-player can jump to (on top on) his own figures and the player switches (but
	// 			the move is (correctly) not made)

	public void initialize() {
		PopUp playerSet = new PopUp();
		playerSet.createInputPopUp();
		String[] players = playerSet.showInputPopUp();
		player1 = new Player(1, players[0]);
		player2 = new Player(2, players[1]);

		setPlayerNames();

		setStatusLabelBackgrounds();

		// set start button text
		startButton.setText("START GAME");

		// initialize button storage
		buttons = new Button[82];

		// build all buttons (9x9 gamefield)
		int x = 0;
		int y = 0;
		int btnIndex = 1;
		for (int i = 1; i < 9; i++) {
			for (int t = 1; t < 9; t++) {
				buttons[btnIndex] = new Button();
				buttonPane.getChildren().add(buttons[btnIndex]);
				// configure the buttons
				buttons[btnIndex].setLayoutX(x);
				buttons[btnIndex].setLayoutY(y);
				buttons[btnIndex].setPrefWidth(45);
				buttons[btnIndex].setPrefHeight(45);
				buttons[btnIndex].setPadding(new Insets(0));
				white = new Background(new BackgroundFill(Color.rgb(224, 201, 160), null, null));
				black = new Background(new BackgroundFill(Color.rgb(164, 120, 91), null, null));
				// chess-like color switching on game fields
				if (i % 2 == 0) {
					if (t % 2 == 0) {
						buttons[btnIndex].setBackground(white);
					} else {
						buttons[btnIndex].setBackground(black);
					}
				} else {
					if (t % 2 == 0) {
						buttons[btnIndex].setBackground(black);
					} else {
						buttons[btnIndex].setBackground(white);
					}
				}

				// finalize variable in order to use in enclosing scope (mouse event)
				final int btnIdx = btnIndex;

				// set event if user clicks on game field
				buttons[btnIndex].setOnMouseClicked(e -> {
					// check if game is running
					if (!game_active) {
						PopUp info = new PopUp();
						info.createInfoPopUp("Please start the game!");
						info.showPopUp();
						return;
					}

					// click on field function
					clickOnField(btnIdx);
				});
				btnIndex++;
				x += 45;
			}
			x = 0;
			y += 45;
		}

		// draw game field lines
		for (int l = 0; l <= 360; l += 45) {
			Line line = new Line(0, 0, 0, 360);
			line.setLayoutX(l);
			buttonPane.getChildren().add(line);
			Line line2 = new Line(0, 0, 360, 0);
			buttonPane.getChildren().add(line2);
			line2.setLayoutY(l);
		}
	}

	public boolean checkOwnSchach() {
		// loop through all figures from the enemy and check if they set the
		// activePlayers king schach
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] > 0 && gamefield[x][y] <= 6 && getInActivePlayer() == player1) {
					if (checkSchach(giveIndex(x, y), getActivePlayer().getId())) {
						return true;
					}
				} else if (gamefield[x][y] >= 7 && getInActivePlayer() == player2) {
					if (checkSchach(giveIndex(x, y), getActivePlayer().getId())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Function to
	 * 
	 * @param btnIndex
	 */
	private void clickOnField(int btnIndex) {
		// check if player already selected a figure to make a move or if he wants to
		// select a figure
		if (selectedButton[0] == 0) {
			// player wants to select a figure to make a move
			int tempX = giveXY(btnIndex)[0];
			int tempY = giveXY(btnIndex)[1];
			if (gamefield[tempX][tempY] > 0 && gamefield[tempX][tempY] < 7 && getActivePlayer() == player1) {
				buttons[btnIndex].setStyle(player1SelectedButton);
			} else if (gamefield[tempX][tempY] > 6 && getActivePlayer() == player2) {
				buttons[btnIndex].setStyle(player2SelectedButton);
			} else if (gamefield[tempX][tempY] == 0) {
				// nothing
				return;
			} else {
				PopUp info = new PopUp();
				info.createInfoPopUp("Player " + getActivePlayer().getName() + " is active!\nMake your move!");
				info.showPopUp();

				return;
			}
			selectedButton[0] = btnIndex;
			selectedButton[1] = getActivePlayer().getId();

		} else if (selectedButton[0] > 0 && selectedButton[0] < 65) {
			// player has already selected a figure and now wants to move it
			if (!tryMove(giveXY(selectedButton[0])[0], giveXY(selectedButton[0])[1], giveXY(btnIndex)[0],
					giveXY(btnIndex)[1])) { // move invalid
				PopUp info = new PopUp();
				info.createInfoPopUp("Can not move player!\nInvalid move!");
				info.showPopUp();
				// deselect button
				buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
				selectedButton[0] = 0;
				selectedButton[1] = 0;
				return;
			}
			makeMove(giveXY(selectedButton[0])[0], giveXY(selectedButton[0])[1], giveXY(btnIndex)[0],
					giveXY(btnIndex)[1]);
			// move valid
			// deselect button
			buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
			selectedButton[0] = 0;
			selectedButton[1] = 0;
			// check if you made a move that does not block the enemy from setting you
			// schach
			// if you did so --> move the last move back and set the correct active player
			if (checkOwnSchach()) {
				PopUp info = new PopUp();
				info.createInfoPopUp("Wrong move! \nYou are set schach!");
				info.showPopUp();
				stepBack();
				return;
			}
			if (checkSchach(btnIndex, getInActivePlayer().getId())) {
				PopUp info = new PopUp();
				info.createInfoPopUp("\"SCHACH!\"" + "\n\nCan you end the game?");
				info.showPopUp();
				// check if the game has ended
				if (checkMatt()) {
					// checkMatt will yet not cover moves from not_king_figures to block "matt"
					// Workaround for now: ask the player if there is a way to block "matt"
					PopUp decision = new PopUp();
					decision.createDecisionPopUp("Is there any way to block the enemy from setting you Matt?");
					if (decision.showPopUp()) {
						switchPlayer();
					} else {
						game_active = false;
						PopUp ending = new PopUp();
						ending.createWinningPopUp(getActivePlayer().getName());
						ending.showPopUp();
					}

					return;
				}

				switchPlayer();
				return;
			}
			// end the move --> switch player
			switchPlayer();
		}
	}

	/**
	 * Sets the red or green background to show what player is active
	 */
	private void setStatusLabelBackgrounds() {
		// activity label
		active1Label.setBackground(player1.getStatusBackground());
		active2Label.setBackground(player2.getStatusBackground());
	}

	/**
	 * Sets the active player names to the label
	 */
	private void setPlayerNames() {
		player1Text.setText(player1.getName());
		player2Text.setText(player2.getName());
	}

	public void stepBack() {
		int oldX = lastMove[0];
		int oldY = lastMove[1];
		int newX = lastMove[2];
		int newY = lastMove[3];

		gamefield[oldX][oldY] = gamefield[newY][newY];
		gamefield[newX][newY] = 0;

		printField();
		setPlayers();

	}

	/**
	 * Tries to move the selected figure (within the gamefield array)
	 * 
	 * @param var represents the button index
	 * @return true or false depending on weather the player is allowed to make a
	 *         move or not
	 */
	public void makeMove(int oldX, int oldY, int newX, int newY) {
		// boolean success = false;
		lastMove[0] = oldX;
		lastMove[1] = oldY;
		lastMove[2] = newX;
		lastMove[3] = newY;

		if (getActivePlayer() == player1) {
			if (gamefield[newX][newY] > 6 || gamefield[newX][newY] == 0) {
				gamefield[newX][newY] = gamefield[oldX][oldY];
				gamefield[oldX][oldY] = 0;
			}
		} else {
			if (gamefield[newX][newY] < 7 && gamefield[newX][newY] > -1) {
				gamefield[newX][newY] = gamefield[oldX][oldY];
				gamefield[oldX][oldY] = 0;
			}
		}

		printField();
		setPlayers();
	}

	/**
	 * Switches the active player
	 */
	public void switchPlayer() {
		player1.switchStatus();
		player2.switchStatus();
		setStatusLabelBackgrounds();
	}

	/**
	 * Checks weather the enemies king is in a problematic game situation and has to
	 * move or not </br>
	 * player should be 1 or 2 according to what players king should be checked
	 * 
	 * @return
	 */
	public boolean checkSchach(int var, int player) {
		int enemyX = giveXY(var)[0];
		int enemyY = giveXY(var)[1];
		int kingX = 0;
		int kingY = 0;

		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] == 6 && player == 1) {
					kingX = x;
					kingY = y;
				} else if (gamefield[x][y] == 12 && player == 2) {
					kingX = x;
					kingY = y;
				}
			}
		}
		if (kingX == 0 || kingY == 0) {
			PopUp info = new PopUp();
			info.createInfoPopUp("Location-Error\nYou may restart the game!");
			info.showPopUp();
			return false;
		}
		if (tryMove(enemyX, enemyY, kingX, kingY)) {
			problemKing[0] = kingX;
			problemKing[1] = kingY;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tries to move the given figure to the given position (actually moves nothing)
	 * 
	 * @param oldX figures old x position
	 * @param oldY figures old y position
	 * @param newX figures new x position
	 * @param newY figures new y position
	 * @return true or false, depending on if the move is valid or not (depends not
	 *         on if the position is blocked)
	 */
	public boolean tryMove(int oldX, int oldY, int newX, int newY) {
		int type = gamefield[oldX][oldY];

		if (type > 6)
			type -= 6;

		switch (type) {
		/////////////////////////////////////////////////////////////////////////////
		case 1: {
			// Bauer:
			if (pawn.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 2: {
			// Turm
			if (rook.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 3: {
			// Pferd
			if (knight.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 4: {
			// Springer
			if (bishop.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 5: {
			// queen = rook oder bishop
			if (queen.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield, rook, bishop))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 6: {
			// Koenig
			if (king.tryMove(oldX, oldY, newX, newY, getActivePlayer(), gamefield))
				return true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		default: {
			PopUp info = new PopUp();
			info.createInfoPopUp("Figure-Selection-Error\nYou may restart the game!");
			info.showPopUp();
			return false;
		}
		}
		return false;
	}

	/**
	 * Checks weather the enemies king is able to get out of a problematic game
	 * situation or not
	 * 
	 * @return
	 */
	public boolean checkMatt() {
		// this is called after a player was set to "schach"
		// strategy:
		// 1)list up all positions the king can move to
		// 2)go threw all the positions and scratch them if they are free to move to
		// 2.1)free to move to means: king is not attacked there by any enemy
		// go threw all enemies and try to make a move to the currently pointing
		// position
		// 3)if there is positions left king is not "schach-matt", return false
		// 4)if there is none left king is "schach-matt", return true
		// TODO: Problem a player can block "matt" with a different figure instead of
		// moving the king -->workaround

		int kingX = problemKing[0];
		int kingY = problemKing[1];
		int[][] posList = new int[8][2]; // max 8 positions with 2 values (x,y)
		int counter = 0; // counts the number of positions

		// 1)
		for (int y = kingY - 1; y <= kingY + 1; y++) { // loop around the king
			for (int x = kingX - 1; x <= kingX + 1; x++) {
				// look for inactive player, because if this is called the player hasn't changed
				// yet
				if (gamefield[x][y] > -1 && gamefield[x][y] < 7 && getInActivePlayer() == player2) {
					posList[counter][0] = x;
					posList[counter][1] = y;
					counter++;
				} else if ((gamefield[x][y] == 0 || gamefield[x][y] > 6) && getInActivePlayer() == player1) {
					posList[counter][0] = x;
					posList[counter][1] = y;
					counter++;
				}
			}
		}

		// 2)
		for (int k = 0; k <= counter - 1; k++) {
			int posX = posList[k][0];
			int posY = posList[k][1];
			boolean freeToMove = true;

			for (int y = 1; y < 9; y++) {
				for (int x = 1; x < 9; x++) {
					if (gamefield[x][y] > 0 && gamefield[x][y] < 7 && getActivePlayer() == player1) {
						if (tryMove(x, y, posX, posY)) {
							// potential king position is covered --> screw
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					} else if (gamefield[x][y] > 6 && getActivePlayer() == player2) {
						if (tryMove(x, y, posX, posY)) {
							// potential king position is covered
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					}
				}
			}

			if (freeToMove)
				return false;

		}

		return true;
	}

	/**
	 * Prepares the int[][] array representing the field of the game
	 */
	public void prepareGameField() {
		gamefield = new int[10][10];

		for (int y = 1; y < 9; y++) {
			int temp = 2;
			for (int x = 1; x < 9; x++) {
				if (y == 1) {
					if (x >= 6) {
						gamefield[x][y] = x - temp;
						temp += 2;
					} else {
						gamefield[x][y] = x + 1;
					}

				} else if (y == 2 || y == 7) {
					if (y == 2)
						gamefield[x][y] = 1;
					if (y == 7)
						gamefield[x][y] = 7;
				} else if (y == 8) {
					if (x >= 6) {
						gamefield[x][y] = x + 6 - temp;
						temp += 2;
					} else {
						gamefield[x][y] = x + 1 + 6;
					}
				} else {
					gamefield[x][y] = 0;
				}
			}
		}

		// print field && initialize the frame with -1 (0, 9)
		for (int g = 0; g < 10; g++) {
			for (int h = 0; h < 10; h++) {
				if (g == 0 || g == 9) {
					gamefield[h][g] = -1;
				}
				if (h == 0 || h == 9) {
					gamefield[h][g] = -1;
				}
				System.out.print(gamefield[h][g] + "\t");
			}
			System.out.println();
		}
	}

	/**
	 * Starts the game
	 */
	public void startButtonClicked() {
		if (game_active) {
			startButton.setText("START GAME");
			game_active = false;
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
			game_active = true;
			prepareGameField();
			setPlayers();
			player1.setActive(true);
			// activity label
			setStatusLabelBackgrounds();
		}
	}

	/**
	 * Resets all the global values in order to restart the game
	 */
	public void resetGlobalVars() {
		selectedButton[0] = 0;
		selectedButton[1] = 0;
		problemKing[0] = 0;
		problemKing[1] = 0;
		lastMove[0] = 0;
		lastMove[1] = 0;
		lastMove[2] = 0;
		lastMove[3] = 0;
	}

	/**
	 * Sets the players on the game field (arranges the rights colors to the right
	 * buttons)
	 */
	public void setPlayers() {
		int z = 1;
		for (int i = 1; i < 9; i++) {
			for (int k = 1; k < 9; k++) {
				if (gamefield[k][i] == 0) {
					if (i % 2 == 0) {
						if (k % 2 == 0) {
							buttons[z].setBackground(white);
						} else {
							buttons[z].setBackground(black);
						}
					} else {
						if (k % 2 == 0) {
							buttons[z].setBackground(black);
						} else {
							buttons[z].setBackground(white);
						}
					}
				}
				buttons[z].setGraphic(null);
				switch (gamefield[k][i]) {
				case 1: {
					buttons[z].setGraphic(new ImageView(pawn.getBlack()));
					break;
				}
				case 2: {
					buttons[z].setGraphic(new ImageView(rook.getBlack()));
					break;
				}
				case 3: {
					buttons[z].setGraphic(new ImageView(knight.getBlack()));
					break;
				}
				case 4: {
					buttons[z].setGraphic(new ImageView(bishop.getBlack()));
					break;
				}
				case 5: {
					buttons[z].setGraphic(new ImageView(queen.getBlack()));
					break;
				}
				case 6: {
					buttons[z].setGraphic(new ImageView(king.getBlack()));
					break;
				}
				case 7: {
					buttons[z].setGraphic(new ImageView(pawn.getWhite()));
					break;
				}
				case 8: {
					buttons[z].setGraphic(new ImageView(rook.getWhite()));
					break;
				}
				case 9: {
					buttons[z].setGraphic(new ImageView(knight.getWhite()));
					break;
				}
				case 10: {
					buttons[z].setGraphic(new ImageView(bishop.getWhite()));
					break;
				}
				case 11: {
					buttons[z].setGraphic(new ImageView(queen.getWhite()));
					break;
				}
				case 12: {
					buttons[z].setGraphic(new ImageView(king.getWhite()));
					break;
				}
				default: {
					break;
				}
				}
				buttons[z].setStyle(playerNonSelectedButton);
				z++;
			}
		}
	}

	/**
	 * Returns the currently inactive player
	 * 
	 * @return currently inactive player object instance
	 */
	public Player getInActivePlayer() {
		if (player1.isActive())
			return player2;
		if (player2.isActive())
			return player1;
		return null;
	}

	/**
	 * Returns the currently active player
	 * 
	 * @return currently inactive player object instance
	 */
	public Player getActivePlayer() {
		if (player1.isActive())
			return player1;
		if (player2.isActive())
			return player2;
		return null;
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
	public int giveIndex(int x, int y) {
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
	public int[] giveXY(int idx) {
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

	public void minButtonClicked() {
		Stage tempStage = (Stage) mypane.getScene().getWindow();
		tempStage.setIconified(true);
	}

	public void closeButtonClicked() {
		Stage temp = (Stage) mypane.getScene().getWindow();
		temp.close();
		System.exit(0);
	}
}
