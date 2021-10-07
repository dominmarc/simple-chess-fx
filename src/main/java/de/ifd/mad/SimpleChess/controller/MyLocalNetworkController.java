/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
import de.ifd.mad.SimpleChess.helpers.BasicGameFunctionsHelper;
import de.ifd.mad.SimpleChess.main.FxmlOpener;
import de.ifd.mad.SimpleChess.main.PopUp;
import de.ifd.mad.SimpleChess.players.Player;
import javafx.application.Platform;
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
 * Local network multi-player controller for new simplechess game
 * 
 * @author MAD
 * @author iFD
 */
public class MyLocalNetworkController implements IController {
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
	private int[][] gamefield;

	/**
	 * 0 = oldX, 1 = oldY, 2 = newX, 3 = newY</br>
	 * old = former position, new = current position
	 */
	private int[] lastMove = { 0, 0, 0, 0 };

	/**
	 * [0]=ButtonIndex (1...64) </br>
	 * [1]=PlayerInfo (1,2) </br>
	 * zeroes mean no selected button
	 */
	private int[] selectedButton = { 0, 0 };

	/**
	 * [0] = X</br>
	 * [1] = Y
	 */
	private int[] problemKing = { 0, 0 };

	// selectedButtonStyles
	String player1SelectedButton = "-fx-border-color: #2AB4FF; -fx-border-width: 4px; ";
	String player2SelectedButton = "-fx-border-color: #FE2B2B; -fx-border-width: 4px; ";
	String playerNonSelectedButton = "-fx-border-color: #000000; -fx-border-width: 0px;";

	// gamefield button backgrounds representing the game fields
	Background white;
	Background black;

	// check if the game is active
	private boolean gameActive = false;

	// initialize figure objects
	Pawn pawn = new Pawn();
	Rook rook = new Rook();
	Knight knight = new Knight();
	Bishop bishop = new Bishop();
	Queen queen = new Queen();
	King king = new King();

	// initialize player objects
	/** played by the server application */
	Player player1;
	/** played by the client application */
	Player player2;

	// local network connection variables
	// the channels to write to
	private SocketChannel clientChannel = null;
	private SocketChannel serverChannel = null;
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector = null;
	private Iterator<SelectionKey> iterator = null;
	/** Indicates if there is an established, active connection or not */
	private boolean connected = false;
	/** Indicated if the game is startable or not */
	private boolean startable = false;
	/** Main char-set for encoding and decoding */
	static final Charset charset = StandardCharsets.UTF_8;
	/** The port to connect to/ to bind the server to */
	private int port = 8000;
	/** manages the connection while the connection is stable */
	private Thread workerThread;

	private static final Logger LOGGER = LoggerFactory.getLogger(MyLocalNetworkController.class);

	/**
	 * specifies whether this plays server or client </br>
	 * 1 means client, 2 means server
	 */
	private int clientState = 0;

//====================================================================================================
//==																								==	
//==Initialization:														                 	        ==
//==																								==	
//====================================================================================================		

	/**
	 * The initial value here is used for:</br>
	 * -indicating wheter this is a client or server</br>
	 * -setting up the port</br>
	 * </br>
	 * Structure:</br>
	 * |mode|port|</br>
	 * |xx|xxxx|</br>
	 * Mode: 01 - Client; 02 - Server
	 */
	@Override
	public void initVariable(String value) {
		String passedPort = "";
		String passedMode = "";
		passedMode = value.substring(0, 2);
		passedPort = value.substring(2);

		// check structure
		if (passedPort.length() != 4 || passedMode.length() != 2) {
			LOGGER.error("Failure on incorrect structure of variables port(={}) or mode(={})!", passedPort, passedMode);
			startable = false;
			return;
		}

		// parse mode
		if (passedMode.contentEquals("01")) {
			clientState = 1;
		} else if (passedMode.contentEquals("02")) {
			clientState = 2;
		} else {
			LOGGER.error("Failure on parsing passed mode = {}", passedMode);
			startable = false;
			return;
		}

		// parse port
		try {
			this.port = Integer.valueOf(passedPort);
		} catch (NumberFormatException e) {
			LOGGER.error("Failure on converting port: {} to integer!", passedPort);
			startable = false;
			return;
		}
		startable = true;
	}

	public void initialize() {
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
		LOGGER.info("==========Local Network Multiplayer===========");
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
		LOGGER.info("Starting initialization...");
		infoUser("Please press START GAME!");

		workerThread = null;

		// set start button text
		startButton.setText("START GAME");

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
		LOGGER.info("Successfully drew lines!");
		LOGGER.info("Initialization finished!");
		LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
	}

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
			// check if game is running & if opponent is ready
			if ((!gameActive) || (!getOpponent(null).isReady())) {
				infoUser("Please start the game or wait for your opponent!").showPopUp();
				return;
			}
			if (e.getButton().equals(MouseButton.PRIMARY))
				// click on field function
				clickOnField(btnIdx);
			else if (e.getButton().equals(MouseButton.SECONDARY))
				// remove players selection on right click
				removeClickOnField(btnIdx);

		});
	}

//====================================================================================================
//==																								==	
//==Click on field actions:															                ==
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
		if (moveLock()) {
			LOGGER.warn("User [{}] is locked!", getActivePlayer().getName());
			infoUser("Wait for your opponent!").showPopUp();
			return;
		}

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
				infoUser("Can not move player!\nInvalid move!").showPopUp();
				LOGGER.warn("Cannot perform move! Invalid action: field blocked or invalid movement.");
				// unselect button
				unselectButton();
				return;
			}

			// move seems to be valid --> make the move
			makeMove(giveXY(selectedButton[0])[0], giveXY(selectedButton[0])[1], giveXY(btnIndex)[0],
					giveXY(btnIndex)[1]);

			// deselect button
			unselectButton();

			// check if you made a move that does not block the enemy from checking you
			// if you did so --> move the last move back and set the correct active player
			if (isChecked(getActivePlayer())) {
				LOGGER.info("User [{}] is checked! Resetting his last move...", getActivePlayer().getName());
				infoUser("Wrong move! \nYou are checked!").showPopUp();
				stepBack();
				return;
			}

			// check if you checked your enemy
			if (isChecked(getInActivePlayer())) {
				infoUser("CHECK!" + "\n\nCan you end the game?").showPopUp();
				LOGGER.info("User [{}] checked [{}].", getActivePlayer().getName(), getInActivePlayer().getName());

				// check if the game should end
				if (isCheckmate()) {
					LOGGER.warn("User [{}] is checkmate, [{}] won! Ending the game!", getInActivePlayer().getName(),
							getActivePlayer().getName());
					endGameWithWinner("Congratulations, you won the game!\nThank you for playing!\nHave a nice day :)");
					sendTelegram(getChannel(), getPrefix(2)
							+ generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], false, true));
					return;
				}

				// enemy is not set to "checkmate", but "checked" --> send telegram and switch
				// player
				sendTelegram(getChannel(), getPrefix(2)
						+ generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], true, false));

				switchPlayer();
				return;
			}

			// no one checked, send telegram and switch player
			sendTelegram(getChannel(),
					getPrefix(2) + generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], false, false));

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
		int fieldVal = getVal(selectedX, selectedY);

		// player clicks on field with his figure
		if (BasicGameFunctionsHelper.isPlayerField(selectedX, selectedY, gamefield, getActivePlayer())) {
			if (getActivePlayer().getId() == 1)
				buttons[btnIdx].setStyle(player1SelectedButton);
			else
				buttons[btnIdx].setStyle(player2SelectedButton);

			// player clicks on field with no figure
		} else if (fieldVal == 0) {
			// nothing should happen
			LOGGER.warn("No figure on field [{}] with field value={}.", btnIdx, fieldVal);
			return;

			// player wants to select field with enemy figure
		} else {
			LOGGER.warn("Enemy figure on field [{}] with field value={}.", btnIdx, fieldVal);
			infoUser("It is your turn!\nMake your move!").showPopUp();
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
		LOGGER.info("Removed field selection on [{}].", buttonIndex);
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
		LOGGER.info("Click on start button...");
		if (!startable) {
			infoUser("Some error occured, please restart the game!");
			LOGGER.warn("User tried to start, but game is not startable!");
		}

		if (gameActive) {
			LOGGER.info("A game is active!");
			PopUp decision = new PopUp();
			decision.createDecisionPopUp("Are you sure you want to request a restart of the game?");
			if (decision.showPopUp()) {
				LOGGER.info("User wants to restart the game...");
				sendTelegram(getChannel(), getPrefix(3) + "00");
				infoUser("Sending request...").showNonWaitingPopUp();
			}

		} else {
			// network
			if (workerThread != null && connected && workerThread.isAlive()) {
				// still connected --> reset game and start new round without opponent being
				// ready because we have to wait for him
				startGame();
				// send to opponent this client is ready
				sendTelegram(getChannel(), getPrefix(5) + "00");

				// notify the user
				if (!getOpponent(null).isReady())
					infoUser("The game will start as soon as your opponent also restarted his game!").showPopUp();
				else
					infoUser(getOpponent(null).getName() + " is ready!\n The game can start!").showPopUp();
				return;
			}

			// no connection --> start server or client
			if (workerThread == null) {
				LOGGER.info("No active connection!");
				if (clientState == 1)
					tryConnect();
				else
					startServer();

				askForPlayers();
				return;
			}
			// server waits for client and wants to start the game (connected = false, but
			// workerThread already active)
			infoUser("Please wait for a client to connect!\nThe game will start automatically. :)")
					.showNonWaitingPopUp();
		}
	}

	/**
	 * Ends the game, shows the winner
	 */
	public void surrenderButtonClicked() {
		LOGGER.info("Surrender button clicked...");
		if ((!gameActive) || (!connected)) {
			infoUser("Please start the game!").showPopUp();
			LOGGER.warn("Game not active!");
			return;
		}

		Player opponent = getOpponent(null);
		Player me = getOpponent(opponent);

		if (me == null || opponent == null) {
			LOGGER.error("Failed to get players!");
			return;
		}
		LOGGER.info("{} surrendered! Ending the game!", me.getName());

		// send surrender message to opponent
		sendTelegram(getChannel(), getPrefix(4));

		// end the game
		endGameWithWinner("You surrendered!\n" + opponent.getName() + " won!\nThank you for playing! Have a nice day!");
	}

	/**
	 * Exports the current game field to a file
	 */
	public void exportButtonClicked() {
		String response = BasicGameFunctionsHelper.tryExport(gamefield);
		if (!response.isBlank())
			infoUser(response);
	}

//====================================================================================================
//==																								==	
//==Connection functions:															                ==
//==																								==	
//====================================================================================================		

	/**
	 * Tries to connect to an already started server, establish the connection if
	 * there is one and starts the server thread if there is no available connection
	 * 
	 * @return false if the connect has failed, true if the connect was successful
	 */
	private void tryConnect() {
		LOGGER.info("Trying to connect... to port: {}", this.port);
		workerThread = new Thread(() -> {
			try {
				selector = Selector.open();

				// create client channel
				SocketChannel connectionClient = SocketChannel.open();
				connectionClient.configureBlocking(false);

				// connect to Socket to myHost with non blocking connection
				connectionClient.connect(new InetSocketAddress("localhost", this.port));

				// add key "CONNECT KEY"
				connectionClient.register(selector, SelectionKey.OP_CONNECT);

				do {
					// save all keys in an iterator set
					selector.select();
					iterator = selector.selectedKeys().iterator();
					while (iterator.hasNext()) {
						// work through keys in iterator and remove the last used key
						SelectionKey key = iterator.next();
						iterator.remove();
						clientChannel = (SocketChannel) key.channel();

						// 1.finish Connection --> (may start server instead)
						// 2. watch out for message to come in
						if (key.isConnectable()) {
							if (clientChannel.isConnectionPending()) {
								// connect Client
								LOGGER.info("Finishing connection...");
								if (!finishConnection())
									return;
							}
							// watch out for incoming strings
							clientChannel.register(selector, SelectionKey.OP_READ);

							// jump to next iterator part (next key)
							continue;
						}

						if (key.isReadable()) {
							clientChannel = (SocketChannel) key.channel();
							Platform.runLater(() -> receiveTelegram(clientChannel));
						}

					}
					Thread.sleep(1500);
				} while (connected);
			} catch (IOException e) {
				LOGGER.warn("Exception!: ", e);
			} catch (InterruptedException e1) {
				LOGGER.warn("Interrupted!: ", e1);
				Thread.currentThread().interrupt();
			}

		});
		workerThread.start();
	}

	private boolean finishConnection() {
		try {
			clientChannel.finishConnect();
			connected = true;
			LOGGER.info("Successfully connected!");
			Platform.runLater(() -> infoUser("You successfully connected to a host!").showNonWaitingPopUp());
			return true;
		} catch (Exception e) {
			// start server instead
			LOGGER.error("Could not connect to server! --> ", e);
			Platform.runLater(() -> infoUser("Could not connect to a host!").showNonWaitingPopUp());
			selector = null;
			iterator = null;
			return false;
		}
	}

	/**
	 * Starts a server and waits for an incoming connection
	 */
	private void startServer() {
		LOGGER.info("Trying to start server...");
		workerThread = new Thread(() -> {
			try {
				selector = Selector.open();

				// create server channel
				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.configureBlocking(false);

				// bind server socket to port
				serverSocketChannel.socket().bind(new InetSocketAddress("localhost", this.port));

				// info user
				Platform.runLater(
						() -> infoUser("You are now hosting the game!\nAt port: " + this.port).showNonWaitingPopUp());

				// register the channel, add key "ACCEPT KEY" (isAcceptable will be true)
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				LOGGER.info("Server started with port: {}", this.port);
				do {
					// save all keys in an iterator set
					selector.select();
					iterator = selector.selectedKeys().iterator();
					// work through keys in iterator and remove the last used key
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();
						//
						if (key.isAcceptable()) {
							// accept the Socket Connection
							serverChannel = serverSocketChannel.accept();
							serverChannel.configureBlocking(false);

							connected = true;
							LOGGER.info("Client connected!");

							serverChannel.register(selector, SelectionKey.OP_READ);
							// jump to next iterator part (next key)
							continue;
						}

						// receive a string
						if (key.isReadable()) {
							serverChannel = (SocketChannel) key.channel();
							Platform.runLater(() -> receiveTelegram(serverChannel));
						}
					}
					Thread.sleep(1500);
				} while (connected);
			} catch (IOException e) {
				LOGGER.warn("Exception!: ", e);
			} catch (InterruptedException e1) {
				LOGGER.warn("Interrupted!: ", e1);
				Thread.currentThread().interrupt();
			} finally {
				try {
					serverSocketChannel.close();
					selector.close();
				} catch (IOException e) {
					LOGGER.warn("Error on closing server socket channel/ selector! ", e);
				}
			}
		});
		workerThread.start();
		Platform.runLater(() -> infoLabel.setText("Please wait..."));
	}

	/**
	 * 
	 * @return the clientState related SocketChannel
	 */
	private SocketChannel getChannel() {
		if (clientState == 1)
			return clientChannel;
		if (clientState == 2)
			return serverChannel;
		return null;
	}

//====================================================================================================
//==																								==	
//==Communication functions:																        ==
//==																								==	
//====================================================================================================	

	/**
	 * Function used to send a message to the opponent</br>
	 * 
	 * @param channel to write the message to
	 * @param message the message to be written
	 */
	private boolean sendTelegram(final SocketChannel channel, String message) {
		LOGGER.info("Trying to send telegram...");
		if (message == null) {
			LOGGER.error("...Failed: message=null");
			return false;
		}
		if (channel == null) {
			LOGGER.error("...Failed no channel to write to!");
			return false;
		}
		LOGGER.info("Sending: [{}]...", message);
		CharBuffer charBuffer = CharBuffer.wrap(message);
		// allocate memory for the Bytes to be send
		ByteBuffer buff = charset.encode(charBuffer);

		// read --> flip --> write -->clear()
		buff.compact();
		buff.flip();
		try {
			channel.write(buff);
		} catch (IOException e) {
			LOGGER.error("Error (IOException) on writing to channel: {}", e.getMessage());
			return false;
		}

		buff.clear();
		charBuffer.clear();
		LOGGER.info("Telegram successfully sent!");

		try {
			channel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			LOGGER.warn("Failed to register channel for reading...");
		}
		return true;
	}

	/**
	 * Function used to receive a message from opponent</br>
	 * -calls: encodeMessage()</br>
	 * -called after key.isReadable</br>
	 * 
	 * @param channel that receives the message
	 */
	private boolean receiveTelegram(final SocketChannel channel) {
		LOGGER.info("Trying to receive telegram...");
		if (channel == null) {
			LOGGER.warn("...Failed: no channel to receive telegram from!");
			return false;
		}

		// Size of the File Buffer
		int bufferSize = 8192;

		// allocate memory for the Bytes to be send
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

		// read from given socketChannel
		try {
			channel.read(buffer);
		} catch (IOException e) {
			LOGGER.error("Error (IOException) on receiving from channel: {}", e.getMessage());
			return false;
		}

		// read -> flip ->write ->clear
		buffer.flip();

		// save buffer in new CharBuffer & decode
		CharBuffer charBuffer = charset.decode(buffer);

		if (!charBuffer.toString().isBlank() || !charBuffer.toString().isEmpty()) {
			LOGGER.info("Successfully received: [{}]", charBuffer);
			if (encodeMessage(charBuffer.toString()))
				return true;
			else {
				LOGGER.error("Failed to encode message: [{}]", charBuffer);
				return false;
			}
		}
		LOGGER.warn("Nothing to encode... message: [{}]", charBuffer);
		return true;
	}

//====================================================================================================
//==																								==	
//==Processing telegram functions:															        ==
//==																								==	
//====================================================================================================	

	/**
	 * This function is used to generate a movement message.</br>
	 * This movement message is needed for a movement update telegram.
	 * 
	 * @param oldX      old position x of figure
	 * @param oldY      old position y of figure
	 * @param newX      new position x of figure
	 * @param newY      new position y of figure
	 * @param check     indicates if there is a check or not
	 * @param checkmate indicates if there is a checkmate or not
	 * 
	 * @return Movement message string
	 */
	private String generateMoveMsg(int oldX, int oldY, int newX, int newY, boolean check, boolean checkmate) {
		return BasicGameFunctionsHelper.generateMoveMsg(oldX, oldY, newX, newY, check, checkmate);
	}

	/**
	 * Methods: </br>
	 * 1 - exchange player names</br>
	 * 2 - exchange figure moves</br>
	 * 3 - exchange restart request/response</br>
	 * 4 - exchange surrender </br>
	 * 5 - exchange start game information (game start and client left) </br>
	 * 
	 * @param method the method to be used in telegram
	 * @return correct (clientState related) telegram prefix
	 */
	private String getPrefix(int method) {
		if (clientState == 1)
			return ("0" + method);
		if (clientState == 2)
			return (method + "0");
		return "";
	}

	/**
	 * Acts on received messages</br>
	 * Structure: </br>
	 * [Number] represents the method</br>
	 * [Zero] represents the client in sending direction</br>
	 * Example:</br>
	 * --> 01 --> Method 1 (second Player Name) is send to Server from Client</br>
	 * --> 10 --> Method 1 (second Player Name) is send to Client from Server
	 * 
	 * @param message Telegram message as string containing game relevant
	 *                information
	 */
	private boolean encodeMessage(String message) {
		if (message == null)
			return false;

		LOGGER.info("Starting encoding: [{}]", message);
		String method = message.substring(0, 2);
		message = message.substring(2, message.length());
		LOGGER.info("Processing: [{}] with message: [{}]...", BasicGameFunctionsHelper.getMethodDescription(method),
				message);

		switch (method) {

		// Client -> Server
		case "01":
			// Server replies with his player name and starts the game on his application
			player2 = new Player(2, Optional.of(message));
			LOGGER.info("Reply from Server with name: {}, starting game...", player2.getName());
			setPlayerNames();
			Platform.runLater(() -> sendTelegram(getChannel(), getPrefix(1) + player1.getName()));
			// Game starts here for server
			startGame();
			getOpponent(null).setReady(true);
			return true;

		// Server -> Client
		case "10":
			// Client receives servers player name and starts the game on his application
			player1 = new Player(1, Optional.of(message));
			LOGGER.info("Reply from Server with name: {}, starting game...", player1.getName());
			setPlayerNames();
			// Game starts here for client
			startGame();
			getOpponent(null).setReady(true);
			return true;

		// Client --> Server
		case "02":
			// Server receives a move --> has to update its gamefield
			return updateGamefieldFromTelMesg(message);

		// Server --> Client
		case "20":
			// Client receives a move --> has to update its gamefield
			return updateGamefieldFromTelMesg(message);

		case "03":
			// client receives a restart message from server
			return actOnRestartMessage(method, message, "Server");

		case "30":
			// server receives a restart message from client
			return actOnRestartMessage(method, message, "Client");

		case "04":
			// Server receives surrender message --> server won
			// this is empty because it will call "40", which has the same code
		case "40":
			// Client receives surrender message --> client won
			endGameWithWinner(getOpponent(null).getName()
					+ " surrendered!\nCongratulations, you won the game!\nThank you for playing! Have a nice day!");
			return true;

		case "05":
			return actOnGameMessage(message, "Server");

		case "50":
			return actOnGameMessage(message, "Client");

		default:
			LOGGER.error("Telegram error: Parsing failure on prefix: {}, {}", method,
					BasicGameFunctionsHelper.getMethodDescription(method));
			return false;
		}
	}

	/**
	 * This function gets called whenever the player receives a telegram containing
	 * information on game restart.</br>
	 * This can be information on accepting, declining or requesting a restart.
	 * 
	 * @param prefix  Prefix, identifying which method the telegram belongs to and
	 *                who sent it
	 * @param message Telegram message
	 * @param sender  "Client" or "Server", depending on who sent the telegram
	 * 
	 * @return true or false, depending on success of action
	 */
	private boolean actOnRestartMessage(String prefix, String message, String sender) {
		LOGGER.info("Reacting on restart telegram...");
		if (prefix == null) {
			LOGGER.error("Failure, method-prefix=null!");
			return false;
		}
		if (prefix.isBlank() || prefix.length() > 2 || prefix.contentEquals("00")) {
			LOGGER.error("Failure on method-prefix: [{}]", prefix);
			return false;
		}

		String first = prefix.substring(0, 0);
		String second = prefix.substring(1);

		if (first.contentEquals("0") || second.contentEquals("0")) {
			LOGGER.info("Received a restart message from {}.", sender);
			if (Integer.valueOf(message) == 0) {
				LOGGER.info("Received a restart request ({}) from {}.", message, sender);
				PopUp decision = new PopUp();
				decision.createDecisionPopUp(getOpponent(null).getName() + " asks for a restart!\nDo you accept that?");
				if (!decision.showPopUp()) {
					LOGGER.info("Declined the restart request!");
					sendTelegram(getChannel(), getPrefix(3) + "02");
				} else {
					LOGGER.info("Accepted the restart request!");
					sendTelegram(getChannel(), getPrefix(3) + "01");
					restartGame();
					getOpponent(null).setReady(true);
				}

			} else if (Integer.valueOf(message) == 1) {
				LOGGER.info("Received a positive restart response ({}) from {}.", message, sender);
				infoUser(getOpponent(null).getName() + " accepted the restart request!\nRestarting game...")
						.showNonWaitingPopUp();
				restartGame();
				getOpponent(null).setReady(true);

			} else if (Integer.valueOf(message) == 2) {
				LOGGER.info("Received a negative restart response ({}) from {}.", message, sender);
				infoUser(getOpponent(null).getName() + " rejected the restart request!").showNonWaitingPopUp();
			} else {
				LOGGER.error("Failure on encoding message: [{}] for {}", message, "Restart");
				return false;
			}
			return true;
		} else
			LOGGER.error("Failure on method-prefix: [{}]", prefix);
		return false;
	}

	/**
	 * This function gets called whenever the player receives a message on basic
	 * game information.</br>
	 * This information can be: </br>
	 * -information on opponent left the game</br>
	 * -information on start game after a game is over and a restart was requested
	 * 
	 * @param message Telegram message
	 * @param sender  "Client" or "Server", depending on who sent the telegram
	 * 
	 * @return true or false, depending on success of action
	 */
	private boolean actOnGameMessage(String message, String sender) {
		if (Integer.valueOf(message) == 0) {
			// client receives start game (after restart) message
			LOGGER.info("Received game start message from {}.", sender);
			getOpponent(null).setReady(true);
			infoUser(getOpponent(null).getName() + " is ready!\nYou can now start playing!").showPopUp();
			return true;
		} else if (Integer.valueOf(message) == 1) {
			// Client receives server left message
			LOGGER.info("{} left the game!", sender);
			connected = false;
			gameActive = false;
			infoUser("Player " + getOpponent(null).getName() + " left the game!").showNonWaitingPopUp();
			return true;
		}
		LOGGER.error("Failure on encoding message: {} from {}.", message, sender);
		return false;
	}

	/**
	 * Updates the gamefield internally and visually according to the enemies
	 * move</br>
	 * Structure: oldX oldY newX newY check checkmate --> 0X 0X 0Y 0Y 01 00-->
	 * 0X0X0Y0Y0100</br>
	 * Example: move from x:5, y:6 to x:3, y:11 with info check:yes, checkmate:no
	 * --> "050603110100"</br>
	 * 
	 * @param telegramMsg provides information about the enemies move
	 */
	private boolean updateGamefieldFromTelMesg(String telegramMsg) {
		LOGGER.info("Updating gamefield from telegram...");
		int oldX = 0;
		int oldY = 0;
		int newX = 0;
		int newY = 0;
		int check = 0;
		int checkmate = 0;
		try {
			oldX = Integer.valueOf(telegramMsg.substring(0, 2));
			oldY = Integer.valueOf(telegramMsg.substring(2, 4));
			newX = Integer.valueOf(telegramMsg.substring(4, 6));
			newY = Integer.valueOf(telegramMsg.substring(6, 8));
			check = Integer.valueOf(telegramMsg.substring(8, 10));
			checkmate = Integer.valueOf(telegramMsg.substring(10, telegramMsg.length()));
		} catch (NumberFormatException e) {
			infoUser("Parsing error!\nYour partner made a move that was not able to get to you!\nYou may restart!");
			LOGGER.error("Parsing error on telegram message: [{}] with {}", telegramMsg, e.getMessage());
			return false;
		}

		gamefield[newX][newY] = gamefield[oldX][oldY];
		gamefield[oldX][oldY] = 0;

		LOGGER.info("Valid move, [x:{},y:{}] is now val=0 and [x:{},y:{}] is now val={}", oldX, oldY, newX, newY,
				getVal(newX, newY));

		if (checkmate == 1) {
			LOGGER.info("Checkmate!");
			setPlayers();
			endGameWithWinner(
					getActivePlayer().getName() + " won the game!\nThank you for playing!\nHave a nice day :)");
			return true;
		}

		if (check == 1)
			infoUser("CHECK!" + "\n\nOh no, will he end the game?").showPopUp();

		setPlayers();
		switchPlayer();
		return true;
	}

//====================================================================================================
//==																								==	
//==Initial game functions:															                ==
//==																								==	
//====================================================================================================		

	/**
	 * Starts the game
	 */
	private void startGame() {
		LOGGER.info("Starting game with client state: {}!", clientState);
		startButton.setText("RESTART GAME");
		infoLabel.setText("LET'S PLAY");
		resetGlobalVars();
		gameActive = true;
		setPlayers();

		player1.setActive(true);
		player2.setActive(false);

		// activity label
		setStatusLabelBackgrounds();

	}

	/**
	 * Restarts the game
	 */
	private void restartGame() {
		LOGGER.info("Restarting the game!");
		gameActive = false;
		player1.setReady(false);
		player2.setReady(false);
		player1.setActive(false);
		player2.setActive(false);
		setStatusLabelBackgrounds();

		startGame();
	}

	/**
	 * Ends the game with a winner
	 * 
	 * @param player the winner
	 */
	private void endGameWithWinner(String message) {
		LOGGER.info("Ending the game!");
		gameActive = false;
		getOpponent(null).setReady(false);
		PopUp ending = new PopUp();
		ending.createWinningPopUp(message);
		ending.showPopUp();
		startButton.setText("START GAME");
		infoLabel.setText("PRESS START");
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

		gamefield[oldX][oldY] = gamefield[newX][newY];
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

		gamefield[newX][newY] = gamefield[oldX][oldY];
		gamefield[oldX][oldY] = 0;

		setPlayers();
		LOGGER.info("Valid move, [x:{},y:{}] is now val=0 and [x:{},y:{}] is now val={}", oldX, oldY, newX, newY,
				getVal(newX, newY));
	}

	/**
	 * Pops up a window that asks for player name input
	 */
	private void askForPlayers() {
		if (gameActive) {
			infoUser("Game is already active!\nEnd the game!").showPopUp();
			return;
		}
		PopUp playerSet = new PopUp();
		playerSet.createInputPopUp("Player1", "");
		List<Optional<String>> players = playerSet.showInputPopUp();

		if (clientState == 2)
			player1 = new Player(1, players.get(0));

		if (clientState == 1) {
			player2 = new Player(2, players.get(0));
			sendTelegram(getChannel(), getPrefix(1) + player2.getName());
		}

	}

	/**
	 * Resets all the global values in order to restart the game
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
	 * Sets the active player names to the label
	 */
	private void setPlayerNames() {
		player1Text.setText(player1.getName());
		player2Text.setText(player2.getName());
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
	 * Creates a new infoPopUp
	 * 
	 * @param message you want to display to the user
	 * @return the created PopUp
	 */
	private PopUp infoUser(String message) {
		PopUp info = new PopUp();
		info.createInfoPopUp(message);
		return info;
	}

	/**
	 * Switches the active player and the visualization of that
	 */
	private void switchPlayer() {
		Player opponent = getOpponent(getActivePlayer());
		if (opponent != null)
			LOGGER.info("Switching active player from {} to {}", getActivePlayer().getName(), opponent.getName());
		else
			LOGGER.info("Switching active player {}", getActivePlayer().getName());
		player1.switchStatus();
		player2.switchStatus();
		setStatusLabelBackgrounds();
	}

	/**
	 * Unselects the currently selected button (globally specified)
	 */
	private void unselectButton() {
		LOGGER.info("Removing field selection...");
		buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
		selectedButton[0] = 0;
		selectedButton[1] = 0;
	}

//====================================================================================================
//==																								==	
//==Game helper functions:        															        ==
//==																								==	
//====================================================================================================	

	/**
	 * Checks weather the enemy's king is in a problematic game situation and has to
	 * move or not, checks if any of the activePlayers' figures sets the opponents
	 * king to "check" </br>
	 * This is called after a player made his move to check if "you" set your enemy
	 * to "check"
	 * 
	 * @param player object to identify whose king should be checked (probably the
	 *               inActivePlayer)
	 * @return true, if king is in problematic situation and false, if he is not
	 */
	private boolean isChecked(Player player) {
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
	 * @return true, if he is not able and false, if he is
	 */
	private boolean isCheckmate() {
		return BasicGameFunctionsHelper.isCheckmate(problemKing[0], problemKing[1], gamefield, player1, player2);
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
	 * Checks if your move is locked because the opponent player is active or not
	 * 
	 * @return true if your move should be locked and false if not
	 */
	private boolean moveLock() {
		return (clientState == 2 && getActivePlayer() == player2 || (clientState == 1 && getActivePlayer() == player1));
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

	/**
	 * Returns the opponent player object of given player.</br>
	 * Returns opponent of the current instance if null is inserted in this method!
	 * 
	 * @param player object or null
	 * @return the opponent of the given @param player or of the current instance
	 */
	private Player getOpponent(Player player) {
		if (player == player1)
			return player2;

		if (player == player2)
			return player1;

		if (player == null) {
			if (clientState == 1)
				return player1;
			else
				return player2;
		}

		return null;
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
	 * Button to go back to menu
	 */
	public void backButtonClicked() {
		LOGGER.info("User clicked on button: back...");
		// send info
		if (connected)
			sendTelegram(getChannel(), getPrefix(5) + "01");

		connected = false;

		try {
			if (serverSocketChannel != null)
				serverSocketChannel.close();
			if (selector != null)
				selector.close();
		} catch (IOException e) {
			LOGGER.warn("Error on closing server socket channel/ selector! - ", e);
		}

		serverSocketChannel = null;
		clientChannel = null;
		serverChannel = null;
		selector = null;
		iterator = null;

		// load FXML
		FxmlOpener newFXML = new FxmlOpener(getClass().getResource("/de/ifd/mad/SimpleChess/main/StartingForm.fxml"), 0,
				null, getClass().getResource("/de/ifd/mad/SimpleChess/main/StartingFileStyle.css").toString());

		// open FXML
		if (!newFXML.open()) {
			LOGGER.error("Error on opening file!");
		} else {
			LOGGER.info("Success... closing start window...");
			// close current window
			Stage current = (Stage) mainPane.getScene().getWindow();
			current.close();
		}

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

		if (temp != null) {
			LOGGER.warn("Someone closed the application on exit button.");
			LOGGER.info(BasicGameFunctionsHelper.getPrintBar());
			LOGGER.info("=====================END======================");
			LOGGER.info(BasicGameFunctionsHelper.getPrintBar());

			// send info
			if (connected)
				sendTelegram(getChannel(), getPrefix(5) + "01");

			temp.close();
			System.exit(0);
		} else
			LOGGER.error("Could not close application on by exit button!");
	}
}
