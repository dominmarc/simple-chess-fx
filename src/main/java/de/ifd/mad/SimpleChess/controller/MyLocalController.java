/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */

package de.ifd.mad.SimpleChess.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifd.mad.SimpleChess.helpers.BasicGameFunctionsHelper;
import de.ifd.mad.SimpleChess.interfaces.IController;
import de.ifd.mad.SimpleChess.main.ImageProvider;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Local multi-player controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class MyLocalController implements IController {
	/** Container for all our visible elements */
	@FXML
	AnchorPane mainPane;
	/** Container for the game fields (buttons) */
	@FXML
	AnchorPane buttonPane;
	/** Array to store all the game fields (buttons) */
	@FXML
	Button[] buttons;
	/** minimize application button */
	@FXML
	Button minButton;
	/** close application button */
	@FXML
	Button closeButton;
	/** return back button */
	@FXML
	Button backButton;
	/** information on if player 1 is active */
	@FXML
	Label active1Label;
	/** information on if player 2 is active */
	@FXML
	Label active2Label;
	/** display some game state information */
	@FXML
	Label infoLabel;
	/** dragable top of application */
	@FXML
	Label topBar;
	/** (re-)start the game button */
	@FXML
	Button startButton;
	/** surrender the game button */
	@FXML
	Button surrenderButton;
	/** export the gamefield button */
	@FXML
	Button exportButton;
	/** switch the active player button */
	@FXML
	Button switchButton;
	/** import a gamefield button */
	@FXML
	Button importButton;
	/** display name of player 1 */
	@FXML
	Label player1Text;
	/** display name of player 2 */
	@FXML
	Label player2Text;

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

	// gamefield button backgrounds representing the game fields
	Background white;
	Background black;

	// check if the game is active
	boolean gameActive = false;

	// initialize player objects
	Player player1;
	Player player2;

	private static final Logger LOGGER = LoggerFactory.getLogger(MyLocalController.class);

	// constant text
	private static final String BUTTON_START_TEXT = "START GAME";

//====================================================================================================
//==																								==	
//==Initialization:														                 	        ==
//==																								==	
//====================================================================================================	

	@Override
	public void initVariable(String value) {
		// nothing (this is in local network controller used for port share)
	}

	/**
	 * Called right before the application opens up
	 */
	public void initialize() {
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
		LOGGER.info("==============Local Multiplayer===============");
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
		LOGGER.info("Starting initialization...");
		askForPlayers();
		setStatusLabelBackgrounds();

		// set start button text
		startButton.setText(BUTTON_START_TEXT);

		// initialize button storage
		buttons = new Button[82];

		// build all buttons (9x9 gamefield)
		LOGGER.info("Building the gamefield...");
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
		LOGGER.info("Successfully built the field!");
		LOGGER.info("Drawing lines...");
		// draw game field lines
		for (int l = 0; l <= 360; l += 45) {
			Line line = new Line(0, 0, 0, 360);
			line.setLayoutX(l);
			buttonPane.getChildren().add(line);
			Line line2 = new Line(0, 0, 360, 0);
			buttonPane.getChildren().add(line2);
			line2.setLayoutY(l);
		}
		resetGlobalVars();
		LOGGER.info("Successfully drew lines!");
		LOGGER.info("Initialization finished!");
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
	}

	/**
	 * Function to add the buttons to the game pane and assign their functionality
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
				infoUser("Please start the game!");
				return;
			}

			if (e.getButton().equals(MouseButton.PRIMARY))
				// click on field function
				clickOnField(btnIdx);
			else if (e.getButton().equals(MouseButton.SECONDARY))
				removeClickOnField(btnIdx);

		});
	}

//====================================================================================================
//==																								==	
//=Click on field actions:															                ==
//==																								==	
//====================================================================================================	

	/**
	 * Function that starts if a player clicks on a certain (btnIndex related)
	 * button (game field)
	 * 
	 * @param btnIndex identifies the button (game field) the player clicked on
	 */
	private void clickOnField(int btnIndex) {
		LOGGER.info("User [{}] clicks on Button [{}]", getActivePlayer().getName(), btnIndex);

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

			LOGGER.info("User [{}] wants to move his selected figure from [{}|{}] (val={}) to [{}|{}] (val={})",
					getActivePlayer().getName(), oldX, oldY, getVal(oldX, oldY), newX, newY, getVal(newX, newY));

			// check if the move is invalid
			if ((!tryMove(oldX, oldY, newX, newY, getActivePlayer()))
					|| (checkIfFieldBlocked(newX, newY, getActivePlayer()))) {
				// show information
				infoUser("Can not move player!\nInvalid move!");

				LOGGER.warn("Cannot perform move! Invalid action: field blocked or invalid movement.");

				// unselect button
				unselectButton();
				return;
			}

			// move seems to be valid --> make the move
			makeMove(oldX, oldY, newX, newY);

			// deselect button
			unselectButton();

			// check if you made a move that does not block the enemy from checking you
			// if you did so --> move the last move back and set the correct active player
			if (isCheck(getActivePlayer())) {
				LOGGER.info("User [{}] is checked! Resetting his last move...", getActivePlayer().getName());
				infoUser("Wrong move! \nYou are checked!");
				stepBack();
				return;
			}

			// check if you checked your enemy
			if (isCheck(getOppositePlayer(getActivePlayer()))) {
				infoUser("CHECK!" + "\n\nCan you end the game?");
				LOGGER.info("User [{}] checked [{}].", getActivePlayer().getName(),
						getOppositePlayer(getActivePlayer()).getName());

				// check if the game should end
				if (isCheckmate()) {
					LOGGER.warn("User [{}] is checkmate, [{}] won! Ending the game!",
							getOppositePlayer(getActivePlayer()).getName(), getActivePlayer().getName());
					gameActive = false;
					PopUp ending = new PopUp();
					ending.createWinningPopUp("Congratulations, " + getActivePlayer().getName()
							+ " won the game!\nThank you for playing!\nHave a nice day :)");
					ending.showPopUp();
					return;
				}

				// enemy is not in checkmate
				switchPlayer();
				return;
			}

			// no one checked, end the move and switch player
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
		LOGGER.info("{} wants to select Button [{}]", getActivePlayer().getName(), btnIdx);
		int selectedX = giveXY(btnIdx)[0];
		int selectedY = giveXY(btnIdx)[1];

		// player clicks on field with his figure
		if (BasicGameFunctionsHelper.isPlayerField(selectedX, selectedY, gamefield, getActivePlayer())) {
			buttons[btnIdx].setStyle(getActivePlayer().getSelection());

		} else if (getVal(selectedX, selectedY) == 0) {
			// nothing should happen
			LOGGER.warn("No figure on field [{}] with field value={}.", btnIdx, getVal(selectedX, selectedY));
			return;

			// player wants to select field with enemy figure
		} else {
			LOGGER.warn("Enemy figure on field [{}] with field value={}.", btnIdx, getVal(selectedX, selectedY));
			infoUser("Player " + getActivePlayer().getName() + " is active!\nMake your move!");
			return;
		}

		// set (save) the selected button
		selectedButton[0] = btnIdx;
		selectedButton[1] = getActivePlayer().getId();
		LOGGER.info("Field selected!");
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
		LOGGER.info("Removed field selection on [{}].", selectedButton[0]);
		buttons[selectedButton[0]].setStyle(Player.getNonSelection());
		selectedButton[0] = 0;
		selectedButton[1] = 0;
	}

//====================================================================================================
//==																								==	
//==Click on game relevant button functions:												        ==
//==																								==	
//====================================================================================================	

	/**
	 * Starts the game
	 */
	public void startButtonClicked() {
		if (gameActive) {
			LOGGER.info("{} clicked on reset. Ending the game!", getActivePlayer().getName());
			startButton.setText(BUTTON_START_TEXT);
			gameActive = false;
			player1.setActive(false);
			player2.setActive(false);
			resetGlobalVars();
			setStatusLabelBackgrounds();
			infoLabel.setText("PRESS START");
			setPlayers();
		} else {
			LOGGER.info("Starting the game...");
			startButton.setText("RE" + BUTTON_START_TEXT);
			infoLabel.setText("LET'S PLAY");
			gameActive = true;
			setPlayers();
			setRandomPlayerActive();
			// activity label
			setStatusLabelBackgrounds();
			LOGGER.info("The game has started with players 1=[{}], 2=[{}] - {} starts!", player1.getName(),
					player2.getName(), getActivePlayer().getId());
		}
	}

	/**
	 * Ends the game, shows the winner
	 */
	public void surrenderButtonClicked() {
		LOGGER.info("{} surrendered! Ending the game!", getActivePlayer());
		gameActive = false;
		PopUp ending = new PopUp();
		ending.createWinningPopUp("Congratulations, " + getOppositePlayer(getActivePlayer()).getName()
				+ " won the game!\nThank you for playing!\nHave a nice day :)");
		ending.showPopUp();
		startButton.setText(BUTTON_START_TEXT);
		infoLabel.setText("PRESS START");
	}

	/**
	 * Exports the current game field to a file
	 */
	public void exportButtonClicked() {
		String response = BasicGameFunctionsHelper.tryExport(gamefield);
		if (!response.isBlank())
			infoUser(response);
	}

	/**
	 * Changes the active player
	 */
	public void switchButtonClicked() {
		if (selectedButton[0] != 0)
			infoUser("Please first unselect a field or make your move!");
		else
			switchPlayer();
	}

	/**
	 * Imports an extern gamefield and applies it to the game</br>
	 * This will restart the game!
	 */
	public void importButtonClicked() {
		// ask user for restart
		if (gameActive) {
			PopUp decision = new PopUp();
			decision.createDecisionPopUp("This will restart the current game.\nContinue?");
			if (decision.showPopUp())
				startButtonClicked();
		}

		LOGGER.info("User wants to import a gamefield...");

		// create dialog
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select your gamefield file!");
		Stage mystage = null;
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Gamefield", "*.field"));

		// show dialog, get file to open
		File gameFile = chooser.showOpenDialog(mystage);
		if (gameFile == null) {
			// not able to create file
			infoUser("Error on opening file!");
			LOGGER.error("Error on opening a file!");
			return;
		}

		// read from file
		BufferedReader buffR = null;
		try {
			buffR = Files.newBufferedReader(gameFile.toPath(), StandardCharsets.UTF_8);
			String myline = "";
			int line = 0;
			while ((myline = buffR.readLine()) != null) {
				String[] parts = myline.split(" ");
				for (int i = 0; i < parts.length; i++) {
					try {
						gamefield[i][line] = Integer.valueOf(parts[i]);
					} catch (NumberFormatException e) {
						LOGGER.error("Error on parsing gamefile: {}, tried to convert: {}.", gameFile.getAbsoluteFile(),
								parts[i]);
						infoUser("Error on parsing gamefile: " + gameFile.getName() + ".");
						resetGlobalVars();
						buffR.close();
						return;
					}
				}
				line++;
			}
			if (!BasicGameFunctionsHelper.checkGamefieldValidity(gamefield)) {
				LOGGER.warn("Invalid import of gamefield file! Building standard gamefield...");
				infoUser("Import was not successful.\nTried to import invalid gamefield!");
				resetGlobalVars();
				buffR.close();
				return;
			}
			// visually update the gamefield
			setPlayers();
			LOGGER.info("Successfully parsed gamefile and updated figure locations!");
		} catch (IOException e) {
			LOGGER.warn("Buffered Reader Error! = {}", e.getMessage());
		} finally {
			try {
				if (buffR != null)
					buffR.close();
			} catch (IOException e) {
				LOGGER.warn("Could not close Buffered Reader! = {}", e.getMessage());
			}
		}
	}

//====================================================================================================
//==																								==	
//==Game intervening functions:															            ==
//==																								==	
//====================================================================================================		

	/**
	 * Resets the last move
	 */
	private void stepBack() {
		int oldX = lastMove[0];
		int oldY = lastMove[1];
		int newX = lastMove[2];
		int newY = lastMove[3];

		gamefield[oldX][oldY] = getVal(newX, newY);
		gamefield[newX][newY] = 0;

		setPlayers();
		LOGGER.info("Reset last move, [x:{},y:{}] is back to 0 and [x:{},y:{}] is back to {}", newX, newY, oldX, oldY,
				getVal(oldX, oldY));
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

		gamefield[newX][newY] = getVal(oldX, oldY);
		gamefield[oldX][oldY] = 0;

		setPlayers();
		LOGGER.info("Valid move, [x:{},y:{}] is now val=0 and [x:{},y:{}] is now val={}", oldX, oldY, newX, newY,
				getVal(newX, newY));
	}

	/**
	 * Switches the active player and the visualization of that
	 */
	private void switchPlayer() {
		LOGGER.info("Switching active player from {} to {}", getActivePlayer().getName(),
				getOppositePlayer(getActivePlayer()).getName());
		player1.switchStatus();
		player2.switchStatus();
		setStatusLabelBackgrounds();
	}

	/**
	 * Shows an information pop up
	 * 
	 * @param msg the Message to be shown
	 */
	private void infoUser(String msg) {
		PopUp info = new PopUp();
		info.createInfoPopUp(msg);
		info.showPopUp();
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
	 * Pops up a window that asks for player name input
	 */
	private void askForPlayers() {
		LOGGER.info("Asking for players...");
		if (gameActive) {
			infoUser("Game is already active!\nEnd the game!");
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
		LOGGER.info("Reset global variables, created new gamefield!");
	}

	/**
	 * Sets one of the two players active
	 */
	private void setRandomPlayerActive() {
		SecureRandom rnd = new SecureRandom();
		if (rnd.nextInt(2) == 0) {
			player1.setActive(true);
			player2.setActive(false);
		} else {
			player1.setActive(false);
			player2.setActive(true);
		}

	}

	/**
	 * Sets the players on the game field (arranges the rights colors to the right
	 * buttons)
	 */
	private void setPlayers() {
		int z = 1;
		for (int i = 1; i < 9; i++) {
			for (int k = 1; k < 9; k++) {
				if (getVal(k, i) == 0) {
					setBackgrounds(i, k, z);
				}
				buttons[z].setGraphic(null);
				switch (getVal(k, i)) {
				case 1:
					buttons[z].setGraphic(new ImageView(ImageProvider.getPawnBlack()));
					break;

				case 2:
					buttons[z].setGraphic(new ImageView(ImageProvider.getRookBlack()));
					break;

				case 3:
					buttons[z].setGraphic(new ImageView(ImageProvider.getKnightBlack()));
					break;

				case 4:
					buttons[z].setGraphic(new ImageView(ImageProvider.getBishopBlack()));
					break;

				case 5:
					buttons[z].setGraphic(new ImageView(ImageProvider.getQueenBlack()));
					break;

				case 6:
					buttons[z].setGraphic(new ImageView(ImageProvider.getKingBlack()));
					break;

				case 7:
					buttons[z].setGraphic(new ImageView(ImageProvider.getPawnWhite()));
					break;

				case 8:
					buttons[z].setGraphic(new ImageView(ImageProvider.getRookWhite()));
					break;

				case 9:
					buttons[z].setGraphic(new ImageView(ImageProvider.getKnightWhite()));
					break;

				case 10:
					buttons[z].setGraphic(new ImageView(ImageProvider.getBishopWhite()));
					break;

				case 11:
					buttons[z].setGraphic(new ImageView(ImageProvider.getQueenWhite()));
					break;

				case 12:
					buttons[z].setGraphic(new ImageView(ImageProvider.getKingWhite()));
					break;

				default:
					break;

				}
				buttons[z].setStyle(Player.getNonSelection());
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

//====================================================================================================
//==																								==	
//==Game helper functions:        															        ==
//==																								==	
//====================================================================================================		

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
	 * Checks weather the enemy's king is in a problematic game situation and has to
	 * move or not </br>
	 * This is called after a player made his move to check if "you" checked your
	 * enemy
	 * 
	 * @param player object to identify whose king should be checked
	 * 
	 * @return true, if king is in problematic situation and false, if he is not
	 */
	private boolean isCheck(Player player) {
		int[] kingXY = BasicGameFunctionsHelper.isChecked(player, gamefield, player1, player2);

		if (kingXY[0] == 0 && kingXY[1] == 0)
			return false;
		else {
			problemKing[0] = kingXY[0];
			problemKing[1] = kingXY[1];
			return true;
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
	 * Checks weather the enemies king is able to get out of a problematic game
	 * situation or not</br>
	 * this is called after a player was checked</br>
	 * 
	 * @return true, if there is a checkmate and false, if there is none
	 */
	private boolean isCheckmate() {
		return BasicGameFunctionsHelper.isCheckmate(problemKing[0], problemKing[1], gamefield, player1, player2);
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
	 * Function to return the field value of a game-field
	 * 
	 * @param x position x in gamefield array
	 * @param y position y in gamefield array
	 * 
	 * @return -1 if out of bound, or gamefield[x][y] value
	 */
	private int getVal(int x, int y) {
		if (x < 1 || x > 8 || y < 1 || y > 8)
			return -1;
		else
			return gamefield[x][y];
	}

//====================================================================================================
//==																								==	
//==Exiting game functions:        															        ==
//==																								==	
//====================================================================================================		

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
