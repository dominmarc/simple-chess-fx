/*
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */

package de.ifd.mad.SimpleChess.controller;

import de.ifd.mad.SimpleChess.helpers.BasicGameFunctionsHelper;
import de.ifd.mad.SimpleChess.helpers.ChessLogger;
import de.ifd.mad.SimpleChess.helpers.ImageProvider;
import de.ifd.mad.SimpleChess.helpers.PopUpProvider;
import de.ifd.mad.SimpleChess.interfaces.IController;
import de.ifd.mad.SimpleChess.main.Settings;
import de.ifd.mad.SimpleChess.players.Player;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Local multiplayer controller for new SimpleChess game
 *
 * @author MAD
 * @author iFD
 */
public class MyLocalController implements IController {
    /**
     * Container for all our visible elements
     */
    @FXML
    private AnchorPane mainPane;
    /**
     * Container for the game fields (buttons)
     */
    @FXML
    private AnchorPane buttonPane;
    /**
     * Array to store all the game fields (buttons)
     */
    @FXML
    private Button[] buttons;
    /**
     * minimize application button
     */
    @FXML
    private Button minButton;
    /**
     * close application button
     */
    @FXML
    private Button closeButton;
    /**
     * back button
     */
    @FXML
    private Button backButton;
    /**
     * information on if player 1 is active
     */
    @FXML
    private Label active1Label;
    /**
     * information on if player 2 is active
     */
    @FXML
    private Label active2Label;
    /**
     * display some game state information
     */
    @FXML
    private Label infoLabel;
    /**
     * dragable top of application
     */
    @FXML
    private Label topBar;
    /**
     * (re-)start the game button
     */
    @FXML
    private Button startButton;
    /**
     * surrender the game button
     */
    @FXML
    private Button surrenderButton;
    /**
     * export the gamefield button
     */
    @FXML
    private Button exportButton;
    /**
     * switch the active player button
     */
    @FXML
    private Button switchButton;
    /**
     * import a gamefield button
     */
    @FXML
    private Button importButton;
    /**
     * display name of player 1
     */
    @FXML
    private Label player1Text;
    /**
     * display name of player 2
     */
    @FXML
    private Label player2Text;

    /**
     * Player1: 1 - pawn, 2 - rook, 3 - knight, 4 - bishop, 5 - queen, 6 - king
     * </br>
     * Player2: 7 - pawn, 8 - rook, 9 - knight, 10 - bishop, 11 - queen, 12 - king
     * </br>
     * -1 - Frame, 0 - no figure
     */
    private int[][] game_field;

    /**
     * 0 = oldX, 1 = oldY, 2 = newX, 3 = newY</br>
     * old = former position, new = current position
     */
    private final int[] lastMove = {0, 0, 0, 0};

    /**
     * [0]=ButtonIndex (1...64) </br>
     * [1]=PlayerInfo (1,2) </br>
     * zeroes mean no selected button
     */
    private final int[] selectedButton = {0, 0};

    /**
     * [0] = X</br>
     * [1] = Y
     */
    private final int[] problemKing = {0, 0};

    // check if the game is active
    private boolean gameActive = false;

    // initialize player objects
    private Player player1;
    private Player player2;

    private static final ChessLogger LOGGER = ChessLogger.createLogger(MyLocalController.class);

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
        startButton.setText(Settings.BUTTON_START_TEXT);

        // initialize button storage
        buttons = new Button[82];

        // build all buttons (9x9 gamefield)
        buildGamefield();

        drawLines();
        resetGlobalVars();
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
    private void buildButton(int x, int y, int i, int t, int btnIndex) {
        // Instantiate the buttons and add them to the button container
        buttons[btnIndex] = new Button();
        buttonPane.getChildren().add(buttons[btnIndex]);

        // configure the buttons
        buttons[btnIndex].setLayoutX(x);
        buttons[btnIndex].setLayoutY(y);
        buttons[btnIndex].setPrefWidth(45);
        buttons[btnIndex].setPrefHeight(45);
        buttons[btnIndex].setPadding(new Insets(0));

        // chess-like color switching on game fields
        setBackground(i, t, btnIndex);

        // finalize variable in order to use in enclosing scope (mouse event)
        final int btnIdx = btnIndex;

        // set event if user clicks on game field (button)
        buttons[btnIndex].setOnMouseClicked(e -> {
            // check if game is running
            if (!gameActive) {
                PopUpProvider.createInfoPopUp("Please start the game!").showPopUp();
                return;
            }

            if (e.getButton().equals(MouseButton.PRIMARY))
                // click on field function
                clickOnField(btnIdx);
            else if (e.getButton().equals(MouseButton.SECONDARY))
                removeClickOnField(btnIdx);

        });
    }

    private void buildGamefield() {
        LOGGER.info("Building the gamefield...");
        int x = 0;
        int y = 0;
        int btnIndex = 1;
        for (int i = 1; i < Settings.GAME_FIELD_HEIGHT - 1; i++) {
            for (int t = 1; t < Settings.GAME_FIELD_WIDTH - 1; t++) {
                buildButton(x, y, i, t, btnIndex);
                btnIndex++;
                x += 45;
            }
            x = 0;
            y += 45;
        }
        LOGGER.info("Successfully built the field!");
    }

    private void drawLines() {
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

        LOGGER.info("Successfully drew lines!");
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
        LOGGER.info("User [{}] clicks on Button [{}({}|{})]", getActivePlayer().getName(), btnIndex);

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
                PopUpProvider.createInfoPopUp("Can not move player!\nInvalid move!").showPopUp();

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
                PopUpProvider.createInfoPopUp("Wrong move! \nYou are checked!").showPopUp();
                stepBack();
                return;
            }

            // check if you checked your enemy
            if (isCheck(getOppositePlayer(getActivePlayer()))) {
                PopUpProvider.createInfoPopUp("CHECK!" + "\n\nCan you end the game?").showPopUp();
                LOGGER.info("User [{}] checked [{}].", getActivePlayer().getName(),
                        getOppositePlayer(getActivePlayer()).getName());

                // check if the game should end
                if (isCheckmate()) {
                    LOGGER.warn("User [{}] is checkmate, [{}] won! Ending the game!",
                            getOppositePlayer(getActivePlayer()).getName(), getActivePlayer().getName());
                    gameActive = false;
                    PopUpProvider.createPopUp("Game Over!", "Congratulations, " + getActivePlayer().getName()
                            + " won the game!\nThank you for playing!\nHave a nice day :)").showPopUp();
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
        LOGGER.info("{} wants to select field [{}]", getActivePlayer().getName(), btnIdx);
        int selectedX = giveXY(btnIdx)[0];
        int selectedY = giveXY(btnIdx)[1];

        // player clicks on field with his figure
        if (BasicGameFunctionsHelper.isPlayerField(selectedX, selectedY, game_field, getActivePlayer())) {
            buttons[btnIdx].setStyle(getActivePlayer().getSelection());

        } else if (getVal(selectedX, selectedY) == 0) {
            // nothing should happen
            LOGGER.warn("No figure on field [{}] with field value={}.", btnIdx, getVal(selectedX, selectedY));
            return;

            // player wants to select field with enemy figure
        } else {
            LOGGER.warn("Enemy figure on field [{}] with field value={}.", btnIdx, getVal(selectedX, selectedY));
            PopUpProvider.createInfoPopUp("Player " + getActivePlayer().getName() + " is active!\nMake your move!").showPopUp();
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
     * Unselects the currently selected button (globally specified).</br>
     * -visually through button style</br>
     * -internally through global variable</br>
     */
    private void unselectButton() {
        LOGGER.info("Removed field selection on [{}].", selectedButton[0]);
        buttons[selectedButton[0]].setStyle(Settings.NON_SELECTION_STYLE);
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
            startButton.setText(Settings.BUTTON_START_TEXT);
            gameActive = false;
            player1.setActive(false);
            player2.setActive(false);
            resetGlobalVars();
            setStatusLabelBackgrounds();
            infoLabel.setText("PRESS START");
            setPlayers();
        } else {
            LOGGER.info("Starting the game...");
            startButton.setText(Settings.BUTTON_RESTART_TEXT);
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
        if (!gameActive) {
            PopUpProvider.createInfoPopUp("Game is not active!").showPopUp();
            return;
        }

        LOGGER.info("{} surrendered! Ending the game!", getActivePlayer());
        gameActive = false;
        PopUpProvider.createPopUp("Game Over!", "Congratulations, " + getOppositePlayer(getActivePlayer()).getName()
                + " won the game!\nThank you for playing!\nHave a nice day :)").showPopUp();
        startButton.setText(Settings.BUTTON_START_TEXT);
        infoLabel.setText("PRESS START");
    }

    /**
     * Exports the current game field to a file
     */
    public void exportButtonClicked() {
        String response = BasicGameFunctionsHelper.tryExport(game_field);
        if (!response.isBlank())
            PopUpProvider.createInfoPopUp(response).showPopUp();
    }

    /**
     * Changes the active player
     */
    public void switchButtonClicked() {
        if (selectedButton[0] != 0)
            PopUpProvider.createInfoPopUp("Please first unselect a field or make your move!").showPopUp();
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
            PopUpProvider decision = PopUpProvider.createDecisionPopUp("This will restart the current game.\nContinue?");
            if (decision.showPopUp())
                startButtonClicked();
        }

        LOGGER.info("User wants to import a gamefield...");

        // create dialog
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select your gamefield file!");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Gamefield", "*.field"));

        // show dialog, get file to open
        File gameFile = chooser.showOpenDialog(null);
        if (gameFile == null) {
            // not able to create file
            PopUpProvider.createInfoPopUp("Error on opening file!").showPopUp();
            LOGGER.error("Error on opening a file!");
            return;
        }

        // read from file
        try (BufferedReader buffR = Files.newBufferedReader(gameFile.toPath(), StandardCharsets.UTF_8)) {
            String myLine;
            int y = 0;
            while ((myLine = buffR.readLine()) != null) {
                String[] parts = myLine.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    try {
                        game_field[i][y] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Error on parsing gamefile: {}, tried to convert: {}.", gameFile.getName(),
                                parts[i]);
                        PopUpProvider.createInfoPopUp("Error on parsing gamefile: " + gameFile.getName() + ".").showPopUp();
                        resetGlobalVars();
                        return;
                    }
                }
                y++;
            }
            if (!BasicGameFunctionsHelper.checkGamefieldValidity(game_field)) {
                LOGGER.warn("Invalid import of gamefield file! Building standard gamefield...");
                PopUpProvider.createInfoPopUp("Import was not successful.\nTried to import invalid gamefield!").showPopUp();
                resetGlobalVars();
                return;
            }
            // visually update the gamefield
            setPlayers();
            LOGGER.info("Successfully parsed gamefile and updated figure locations!");
        } catch (IOException e) {
            LOGGER.warn("Buffered Reader Error! = {}", e.getMessage());
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

        game_field[oldX][oldY] = getVal(newX, newY);
        game_field[newX][newY] = 0;

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

        game_field[newX][newY] = getVal(oldX, oldY);
        game_field[oldX][oldY] = 0;

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
        player1Text.setText(player1.getShortName());
        player2Text.setText(player2.getShortName());
    }

    /**
     * Pops up a window that asks for player name input
     */
    private void askForPlayers() {
        LOGGER.info("Asking for players...");
        if (gameActive) {
            PopUpProvider.createInfoPopUp("Game is already active!\nEnd the game!").showPopUp();
            return;
        }
        PopUpProvider playerSet = PopUpProvider.createInputPopUp(new LinkedList<>(List.of(
                new Pair<>("Player1:", Settings.PLAYER_1_DEFAULT),
                new Pair<>("Player2:", Settings.PLAYER_2_DEFAULT))));
        List<String> players = playerSet.showInputPopUp(2);
        player1 = new Player(Settings.PLAYER_1_ID, players.get(0) != null ? players.get(0) : (Settings.PLAYER_1_DEFAULT));
        player2 = new Player(Settings.PLAYER_2_ID, players.get(1) != null ? players.get(1) : (Settings.PLAYER_2_DEFAULT));

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
        game_field = BasicGameFunctionsHelper.createGamefield();
        LOGGER.info("Reset global variables, created new gamefield!");
    }

    /**
     * Sets one of the two players active
     */
    private void setRandomPlayerActive() {
        SecureRandom rnd = new SecureRandom();
        int result = rnd.nextInt(2) + 1;

        setPlayerActive(result == 1 ? Settings.PLAYER_1_ID : Settings.PLAYER_2_ID);
    }

    private void setPlayerActive(int id) {
        if (id == Settings.PLAYER_1_ID) {
            player1.setActive(true);
            player2.setActive(false);
        } else if (id == Settings.PLAYER_2_ID) {
            player1.setActive(false);
            player2.setActive(true);
        } else {
            LOGGER.error("Could not set new active Player! ID {} neither fits to Player-1-ID ({}) nor to Player-2-ID ({})!", id, Settings.PLAYER_1_ID, Settings.PLAYER_2_ID);
        }
    }

    /**
     * Sets the players on the game field (arranges the rights colors to the right
     * buttons)
     */
    private void setPlayers() {
        int z = 1;
        for (int i = 1; i < Settings.GAME_FIELD_HEIGHT - 1; i++) {
            for (int k = 1; k < Settings.GAME_FIELD_WIDTH - 1; k++) {
                if (getVal(k, i) == 0)
                    setBackground(i, k, z);

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
                buttons[z].setStyle(Settings.NON_SELECTION_STYLE);
                z++;
            }
        }
    }

    /**
     * Sets the backgrounds on the game field (arranges the rights colors to the
     * right buttons)
     *
     * @param y Position y in gamefield
     * @param x Position x in gamefield
     * @param z Button Index
     */
    private void setBackground(int y, int x, int z) {
        buttons[z].setBackground((y % 2 == 0) ? ((x % 2 == 0) ? Settings.WHITE : Settings.BLACK) : ((x % 2 == 0) ? Settings.BLACK : Settings.WHITE));
    }

//====================================================================================================
//==																								==	
//==Game helper functions:        															        ==
//==																								==	
//====================================================================================================		

    /**
     * Checks if the new position is free to move to (just this field)
     *
     * @param x      figures new x position
     * @param y      figures new y position
     * @param player object to identify the related player
     * @return true if the field is blocked, false if it is not blocked
     */
    private boolean checkIfFieldBlocked(int x, int y, Player player) {
        return BasicGameFunctionsHelper.checkIfFieldBlocked(x, y, game_field, player);
    }

    /**
     * Checks weather the enemy's king is in a problematic game situation and has to
     * move or not </br>
     * This is called after a player made his move to check if "you" checked your
     * enemy
     *
     * @param player object to identify whose king should be checked
     * @return true, if king is in problematic situation and false, if he is not
     */
    private boolean isCheck(Player player) {
        int[] kingXY = BasicGameFunctionsHelper.isChecked(player, game_field, player1, player2);

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
     * on if the position is blocked)
     */
    private boolean tryMove(int oldX, int oldY, int newX, int newY, Player player) {
        return BasicGameFunctionsHelper.tryMove(oldX, oldY, newX, newY, game_field, player);
    }

    /**
     * Checks weather the enemies king is able to get out of a problematic game
     * situation or not</br>
     * this is called after a player was checked</br>
     *
     * @return true, if there is a checkmate and false, if there is none
     */
    private boolean isCheckmate() {
        return BasicGameFunctionsHelper.isCheckmate(problemKing[0], problemKing[1], game_field, player1, player2);
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
        return player == player1 ? player2 : player1;
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
     * @return -1 if out of bound, or gamefield[x][y] value
     */
    private int getVal(int x, int y) {
        return (x < 1 || x > 8 || y < 1 || y > 8) ? -1 : game_field[x][y];
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
