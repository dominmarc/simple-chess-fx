/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */

package de.ifd.mad.SimpleChess.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
import de.ifd.mad.SimpleChess.helpers.BasicGameFunctionsHelper;
import de.ifd.mad.SimpleChess.main.PopUp;
import de.ifd.mad.SimpleChess.players.Player;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * Local multi-player controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class MyLocalController implements IController {
	@FXML
	AnchorPane mainPane, buttonPane;
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

	// constant text
	private static final String BUTTON_START_TEXT = "START GAME";

	@Override
	public void initVariable(String value) {
		// nothing (this is in local network controller used for port share)
	}

	/**
	 * Called right before the application opens up
	 */
	public void initialize() {
		askForPlayers();

		setStatusLabelBackgrounds();

		// set start button text
		startButton.setText(BUTTON_START_TEXT);

		// initialize button storage
		buttons = new Button[82];

		// build all buttons (9x9 gamefield)
		int x = 0;
		int y = 0;
		int btnIndex = 1;
		for (int i = 1; i < 9; i++) {
			for (int t = 1; t < 9; t++) {
				buildButtons(x, y, i, t, btnIndex);
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

	/**
	 * loop through all figures from the enemy and check if they set the
	 * activePlayers king "schach"
	 * 
	 * @return
	 */
	private boolean checkOwnSchach() {
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] > 0 && gamefield[x][y] <= 6 && getOppositePlayer(getActivePlayer()) == player1
						|| (gamefield[x][y] >= 7 && getOppositePlayer(getActivePlayer()) == player2)) {
					if (checkSchach(BasicGameFunctionsHelper.giveIndex(x, y), getActivePlayer())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Function that starts if a player clicks on a certain (btnIndex related)
	 * button (game field)
	 * 
	 * @param btnIndex identifies the button (game field) the player clicked on
	 */
	private void clickOnField(int btnIndex) {

		// player wants to select a figure to make a move
		if (selectedButton[0] == 0) {
			selectFigure(btnIndex);

			// player has already selected a figure and now wants to move it
		} else if (selectedButton[0] > 0 && selectedButton[0] < 65) {
			// save old and new position
			int oldX = giveXY(selectedButton[0])[0];
			int newX = giveXY(btnIndex)[0];
			int oldY = giveXY(selectedButton[0])[1];
			int newY = giveXY(btnIndex)[1];

			// check if the move is invalid
			if ((!tryMove(oldX, oldY, newX, newY, getActivePlayer()))
					|| (checkIfFieldBlocked(newX, newY, getActivePlayer()))) {
				// show information
				PopUp info = new PopUp();
				info.createInfoPopUp("Can not move player!\nInvalid move!");
				info.showPopUp();
				// unselect button
				unselectButton();
				return;
			}

			// move seems to be valid --> make the move
			makeMove(giveXY(selectedButton[0])[0], giveXY(selectedButton[0])[1], giveXY(btnIndex)[0],
					giveXY(btnIndex)[1]);

			// deselect button
			unselectButton();

			// check if you made a move that does not block the enemy from setting you
			// "schach"
			// if you did so --> move the last move back and set the correct active player
			if (checkOwnSchach()) {
				PopUp info = new PopUp();
				info.createInfoPopUp("Wrong move! \nYou are set \"schach\"!");
				info.showPopUp();
				stepBack();
				return;
			}

			// check if you set your enemy to "schach"
			if (checkSchach(btnIndex, getOppositePlayer(getActivePlayer()))) {
				PopUp info = new PopUp();
				info.createInfoPopUp("\"SCHACH!\"" + "\n\nCan you end the game?");
				info.showPopUp();

				// check if the game should end
				if (checkMatt()) {
					gameActive = false;
					PopUp ending = new PopUp();
					ending.createWinningPopUp("Congratulations, " + getActivePlayer().getName()
							+ " won the game!\\nThank you for playing!\\nHave a nice day :)");
					ending.showPopUp();
					return;
				}

				// enemy is not set to "matt"
				switchPlayer();
				return;
			}

			// no one set to "schach", end the move and switch player
			switchPlayer();
		}
	}

	/**
	 * Selects the field of a figure.</br>
	 * -visually (setStyle)</br>
	 * -internally (@selectedButton)</br>
	 * 
	 * @param btnIdx Index of the button the user clicked on to select.
	 */
	private void selectFigure(int btnIdx) {
		int selectedX = giveXY(btnIdx)[0];
		int selectedY = giveXY(btnIdx)[1];

		// player 1 clicks on field with one of his figures
		if (gamefield[selectedX][selectedY] > 0 && gamefield[selectedX][selectedY] < 7
				&& getActivePlayer() == player1) {
			buttons[btnIdx].setStyle(player1SelectedButton);

			// player 2 clicks on field with one of his figures
		} else if (gamefield[selectedX][selectedY] > 6 && getActivePlayer() == player2) {
			buttons[btnIdx].setStyle(player2SelectedButton);

			// player clicks on field with no figure
		} else if (gamefield[selectedX][selectedY] == 0) {
			// nothing should happen
			return;

			// player wants to select field with enemy figure
		} else {
			PopUp info = new PopUp();
			info.createInfoPopUp("Player " + getActivePlayer().getName() + " is active!\nMake your move!");
			info.showPopUp();
			return;
		}

		// set (save) the selected button
		selectedButton[0] = btnIdx;
		selectedButton[1] = getActivePlayer().getId();
	}

	/**
	 * Click event for right mouse button on game field
	 * 
	 * @param buttonIndex identifies the button (game field) the player clicked on
	 */
	private void removeClickOnField(int buttonIndex) {
		if (selectedButton[0] != buttonIndex)
			return;

		unselectButton();
	}

	/**
	 * Unselects the currently selected button (globally specified)
	 */
	private void unselectButton() {
		buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
		selectedButton[0] = 0;
		selectedButton[1] = 0;
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

	/**
	 * Checks if the new position is free to move to (just this field)
	 * 
	 * @param newX   figures new x position
	 * @param newY   figures new y position
	 * @param player object to identify the related player
	 * @return true if the field is blocked, false if it is not blocked
	 */
	private boolean checkIfFieldBlocked(int x, int y, Player player) {
		return BasicGameFunctionsHelper.checkIfFieldBlocked(x, y, gamefield, player);
	}

	/**
	 * Resets the last move
	 */
	private void stepBack() {
		int oldX = lastMove[0];
		int oldY = lastMove[1];
		int newX = lastMove[2];
		int newY = lastMove[3];

		gamefield[oldX][oldY] = gamefield[newX][newY];
		gamefield[newX][newY] = 0;

		setPlayers();

	}

	/**
	 * Moves the selected figure (within the gamefield-array) from old to new
	 * 
	 * @param oldX figures old x position
	 * @param oldY figures old y position
	 * @param newX figures new x position
	 * @param newY figures new y position
	 */
	private void makeMove(int oldX, int oldY, int newX, int newY) {
		// set variables to indicate the last move
		lastMove[0] = oldX;
		lastMove[1] = oldY;
		lastMove[2] = newX;
		lastMove[3] = newY;

		gamefield[newX][newY] = gamefield[oldX][oldY];
		gamefield[oldX][oldY] = 0;

		setPlayers();
	}

	/**
	 * Switches the active player and the visualization of that
	 */
	private void switchPlayer() {
		player1.switchStatus();
		player2.switchStatus();
		setStatusLabelBackgrounds();
	}

	/**
	 * Checks weather the enemy's king is in a problematic game situation and has to
	 * move or not </br>
	 * This is called after a player made his move to check if "you" set your enemy
	 * to "schach"
	 * 
	 * @param btnIdx the button index of the "king attacker"
	 * @param player object to identify whose king should be checked
	 * @return true, if king is in problematic situation and false, if he is not
	 */
	private boolean checkSchach(int btnIdx, Player player) {
		int enemyX = giveXY(btnIdx)[0];
		int enemyY = giveXY(btnIdx)[1];
		int kingX = 0;
		int kingY = 0;

		// searches for the king of the specified player
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if ((gamefield[x][y] == 6 && player == player1) || (gamefield[x][y] == 12 && player == player2)) {
					kingX = x;
					kingY = y;
				}
			}
		}

		// no king found
		if (kingX == 0 || kingY == 0) {
			PopUp info = new PopUp();
			info.createInfoPopUp("Location-Error\nYou may restart the game!");
			info.showPopUp();
			return false;
		}

		// try to make the move to the king
		if (tryMove(enemyX, enemyY, kingX, kingY, getActivePlayer())) {
			problemKing[0] = kingX;
			problemKing[1] = kingY;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the move to the new field is valid (does not check the field,
	 * actually moves nothing)
	 * 
	 * @param oldX figures old x position
	 * @param oldY figures old y position
	 * @param newX figures new x position
	 * @param newY figures new y position
	 * @return true or false, depending on if the move is valid or not (depends not
	 *         on if the position is blocked)
	 */
	private boolean tryMove(int oldX, int oldY, int newX, int newY, Player player) {
		return BasicGameFunctionsHelper.tryMove(oldX, oldY, newX, newY, gamefield, player);
	}

	/**
	 * Tries to collect all the fields a figure needs to pass in order to reach the
	 * goal.</br>
	 * This should never be called without first checking if the figure can actually
	 * reach the attacked position.</br>
	 * 
	 * @param x     the field X, where the attacker is situated
	 * @param y     the field Y, where the attacker is situated
	 * @param kingX the field X, the figure tries to attack
	 * @param kingY the field Y, the figure tries to attack
	 * 
	 * @return Map<x,y> representing each field the figure has to pass
	 * 
	 * @author MAD
	 * @author iFD
	 */
	private Map<Integer, Integer> getFieldsOfMovement(int x, int y, int kingX, int kingY, Player player,
			int[][] gamefield) {
		switch (gamefield[x][y]) {
		case 2:
		case 8:
			return rook.fieldsOfMovement(x, y, kingX, kingY, player, gamefield);
		case 4:
		case 10:
			return bishop.fieldsOfMovement(x, y, kingX, kingY, gamefield);
		case 5:
		case 11:
			return queen.fieldsOfMovement(x, y, kingX, kingY, player, gamefield, rook, bishop);
		default:
			return null;
		}
	}

	/**
	 * Checks all the figures of the given player; if at least one of them is able
	 * to move to the given position
	 * 
	 * @param player the player whos figures should be checked
	 * @param posX   the position x we are looking to move to
	 * @param posY   the position y we are looking to move to
	 * 
	 * @return true if he can, false if he cant
	 * 
	 * @author MAD
	 * @author iFD
	 */
	private boolean canAnyFigureMoveThere(Player player, int posX, int posY) {
		return BasicGameFunctionsHelper.canAnyFigureMoveThere(player, posX, posY, gamefield);
	}

	/**
	 * Checks weather the enemies king is able to get out of a problematic game
	 * situation or not</br>
	 * this is called after a player was set to "schach"</br>
	 * 
	 * @return true, if he is not able and false, if he is
	 */
	private boolean checkMatt() {
		// save data from attacked king
		int kingX = problemKing[0];
		int kingY = problemKing[1];

		// get the insulted player
		Player insultedPlayer;
		if (gamefield[kingX][kingY] == 6)
			insultedPlayer = player1;
		else
			insultedPlayer = player2;

		// save fields around insulted king to check if they are free for him to move to
		int[][] posList = new int[8][2]; // max 8 positions with 2 values (x,y)
		int counter = 0; // counts the number of positions

		// 1)list up all positions the king can move to
		for (int y = kingY - 1; y <= kingY + 1; y++) { // loop around the king
			for (int x = kingX - 1; x <= kingX + 1; x++) {
				// king can just move one field to each side, so just check if that field is
				// blocked
				if (!checkIfFieldBlocked(x, y, insultedPlayer)) {
					posList[counter][0] = x;
					posList[counter][1] = y;
					counter++;
				}
			}
		}

		// 2)go threw all the positions the king can move to and scratch them if they
		// are free to move to
		for (int k = 0; k <= counter - 1; k++) {
			int posX = posList[k][0];
			int posY = posList[k][1];

			// Indicates whether the king is able to make a move (escape "schach") or not
			boolean freeToMove = true;

			for (int y = 1; y < 9; y++) {
				for (int x = 1; x < 9; x++) {
					// just go through player1 or player2 positions
					if (gamefield[x][y] > 0 && gamefield[x][y] < 7 && getActivePlayer() == player1
							|| (gamefield[x][y] > 6 && getActivePlayer() == player2)) {
						if (tryMove(x, y, posX, posY, getActivePlayer())) {
							// potential king position is covered --> screw
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

		// look for all the fields that can be blocked by the insulted players figures
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				// player2 gets attacked by player1
				if (gamefield[x][y] > 0 && gamefield[x][y] < 7 && insultedPlayer == player2) {
					// if attacker can attack the king and is no knight or pawn or king then he can
					// be
					// blocked
					if (tryMove(x, y, kingX, kingY, getActivePlayer()) && (gamefield[x][y] != 3) && gamefield[x][y] != 1
							&& gamefield[x][y] != 6) {
						// get all the fields that would block the move
						Map<Integer, Integer> fields = getFieldsOfMovement(x, y, kingX, kingY, player1, gamefield);
						if (fields == null) {
							// error happened
						} else {
							// we have the fields --> now look if any figure of the insulted player can move
							// there
							for (Integer xx : fields.keySet()) {
								if (Boolean.TRUE.equals(canAnyFigureMoveThere(insultedPlayer, xx, fields.get(xx)))) {
									// not matt, move can be blocked
									return false;
								}
							}
						}

					}
				}
				// player1 gets attacked by player2
				if (gamefield[x][y] > 6 && insultedPlayer == player1) {
					// if attacker can attack the king and is no knight or pawn or king then he can
					// be
					// blocked
					if (tryMove(x, y, kingX, kingY, getActivePlayer()) && (gamefield[x][y] != 9) && gamefield[x][y] != 7
							&& gamefield[x][y] != 12) {
						// get all the fields that would block the move
						Map<Integer, Integer> fields = getFieldsOfMovement(x, y, kingX, kingY, player2, gamefield);
						if (fields == null) {
							// error happened
						} else {
							// we have the fields --> now look if any figure of the insulted player can move
							// there
							for (Integer xx : fields.keySet()) {
								if (Boolean.TRUE.equals(canAnyFigureMoveThere(insultedPlayer, xx, fields.get(xx)))) {
									// not matt, move can be blocked
									return false;
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Starts the game
	 */
	public void startButtonClicked() {
		if (gameActive) {
			startButton.setText(BUTTON_START_TEXT);
			gameActive = false;
			player1.setActive(false);
			player2.setActive(false);
			setStatusLabelBackgrounds();
			infoLabel.setText("PRESS START");
			setPlayers();
		} else {
			startButton.setText("RE" + BUTTON_START_TEXT);
			infoLabel.setText("LET'S PLAY");
			resetGlobalVars();
			gameActive = true;
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
		ending.createWinningPopUp(getOppositePlayer(getActivePlayer()).getName());
		ending.showPopUp();
		startButton.setText(BUTTON_START_TEXT);
		infoLabel.setText("PRESS START");
	}

	/**
	 * Pops up a window that asks for player name input
	 */
	private void askForPlayers() {
		if (gameActive) {
			PopUp info = new PopUp();
			info.createInfoPopUp("Game is already active!\nEnd the game!");
			info.showPopUp();
			return;
		}
		PopUp playerSet = new PopUp();
		playerSet.createInputPopUp("Player1", "Player2");
		List<Optional<String>> players = playerSet.showInputPopUp();
		player1 = new Player(1, players.get(0));
		player2 = new Player(2, players.get(1));

		setPlayerNames();
	}

	/**
	 * Resets all the global values and creates a fresh new gamefield in order to
	 * restart the game
	 */
	private void resetGlobalVars() {
		selectedButton[0] = 0;
		selectedButton[1] = 0;
		problemKing[0] = 0;
		problemKing[1] = 0;
		lastMove[0] = 0;
		lastMove[1] = 0;
		lastMove[2] = 0;
		lastMove[3] = 0;
		gamefield = BasicGameFunctionsHelper.createGamefield();
	}

	/**
	 * Sets the players on the game field (arranges the rights colors to the right
	 * buttons)
	 */
	private void setPlayers() {
		int z = 1;
		for (int i = 1; i < 9; i++) {
			for (int k = 1; k < 9; k++) {
				if (gamefield[k][i] == 0) {
					setBackgrounds(i, k, z);
				}
				buttons[z].setGraphic(null);
				switch (gamefield[k][i]) {
				case 1:
					buttons[z].setGraphic(new ImageView(pawn.getBlack()));
					break;

				case 2:
					buttons[z].setGraphic(new ImageView(rook.getBlack()));
					break;

				case 3:
					buttons[z].setGraphic(new ImageView(knight.getBlack()));
					break;

				case 4:
					buttons[z].setGraphic(new ImageView(bishop.getBlack()));
					break;

				case 5:
					buttons[z].setGraphic(new ImageView(queen.getBlack()));
					break;

				case 6:
					buttons[z].setGraphic(new ImageView(king.getBlack()));
					break;

				case 7:
					buttons[z].setGraphic(new ImageView(pawn.getWhite()));
					break;

				case 8:
					buttons[z].setGraphic(new ImageView(rook.getWhite()));
					break;

				case 9:
					buttons[z].setGraphic(new ImageView(knight.getWhite()));
					break;

				case 10:
					buttons[z].setGraphic(new ImageView(bishop.getWhite()));
					break;

				case 11:
					buttons[z].setGraphic(new ImageView(queen.getWhite()));
					break;

				case 12:
					buttons[z].setGraphic(new ImageView(king.getWhite()));
					break;

				default:
					break;

				}
				buttons[z].setStyle(playerNonSelectedButton);
				z++;
			}
		}
	}

	/**
	 * Sets the backgrounds on the game field (arranges the rights colors to the
	 * right buttons)
	 */
	private void setBackgrounds(int i, int k, int z) {
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

	/**
	 * Returns the opposite player
	 * 
	 * @param player the player you want the opposite from
	 * @return Player-Object
	 */
	public Player getOppositePlayer(Player player) {
		if (player == player1)
			return player2;
		else
			return player1;
	}

//	public void printField() {
//		for (int g = 0; g < 10; g++) {
//			for (int h = 0; h < 10; h++) {
//				System.out.print(gamefield[h][g] + "\t");
//			}
//			System.out.println();
//		}
//	}

	/**
	 * Function to add the buttons themselves and their functionality
	 * 
	 * @param x        Layout of the button
	 * @param y        Layout of the button
	 * @param i        gamefield field y coordinate
	 * @param t        gamefield field x coordinate
	 * @param btnIndex Index of the buttons (1...81)
	 */
	private void buildButtons(int x, int y, int i, int t, int btnIndex) {
		// Instantiate the buttons and add them to the button container
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
			if (t % 2 == 0)
				buttons[btnIndex].setBackground(white);
			else
				buttons[btnIndex].setBackground(black);
		} else {
			if (t % 2 == 0)
				buttons[btnIndex].setBackground(black);
			else
				buttons[btnIndex].setBackground(white);
		}

		// finalize variable in order to use in enclosing scope (mouse event)
		final int btnIdx = btnIndex;

		// set event if user clicks on game field (button)
		buttons[btnIndex].setOnMouseClicked(e -> {
			// check if game is running
			if (!gameActive) {
				PopUp info = new PopUp();
				info.createInfoPopUp("Please start the game!");
				info.showPopUp();
				return;
			}

			if (e.getButton().equals(MouseButton.PRIMARY))
				// click on field function
				clickOnField(btnIdx);
			else if (e.getButton().equals(MouseButton.SECONDARY))
				removeClickOnField(btnIdx);

		});
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
		return BasicGameFunctionsHelper.giveXY(idx);
	}

	/**
	 * Button to minimize the application
	 */
	public void minButtonClicked() {
		Stage tempStage = (Stage) mainPane.getScene().getWindow();
		tempStage.setIconified(true);
	}

	/**
	 * Button to close the application
	 */
	public void closeButtonClicked() {
		Stage temp = (Stage) mainPane.getScene().getWindow();
		temp.close();
		System.exit(0);
	}
}
