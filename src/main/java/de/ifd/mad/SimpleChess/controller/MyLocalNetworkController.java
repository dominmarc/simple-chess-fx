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

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
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
public class MyLocalNetworkController {
	@FXML
	AnchorPane mainPane, buttonPane;
	@FXML
	Button[] buttons;
	@FXML
	Button minButton, closeButton, backButton;
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
	/** Main char-set for encoding and decoding */
	static final Charset charset = StandardCharsets.UTF_8;
	/** The port to connect to/ to bind the server to */
	static final int PORT = 8000;
	/** manages the connection while the connection is stable */
	private Thread workerThread;

	/**
	 * As soon as an agreement has to be made this variable gets assigned with a
	 * certain value, until that it remains null.</br>
	 * The variable should also be set back to null afterwards!</br>
	 * 0 - disagreement</br>
	 * 1 - agreement
	 */
	private Optional<Integer> agreement = Optional.ofNullable(null);

	/**
	 * specifies whether this plays server or client </br>
	 * 1 means client, 2 means server
	 */
	int clientState = 0;

	public void initialize() {

		workerThread = null;

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
	 * Function that starts if a player clicks on a certain (btnIndex related)
	 * button (game field)
	 * 
	 * @param btnIndex identifies the button (game field) the player clicked on
	 */
	private void clickOnField(int btnIndex) {
		if (moveLock()) {
			infoUser("Wait for your opponent!").showPopUp();
			return;
		}

		// player wants to select a figure to make a move
		if (selectedButton[0] == 0) {
			int selectedX = giveXY(btnIndex)[0];
			int selectedY = giveXY(btnIndex)[1];

			// player 1 clicks on field with one of his figures
			if (gamefield[selectedX][selectedY] > 0 && gamefield[selectedX][selectedY] < 7
					&& getActivePlayer() == player1) {
				buttons[btnIndex].setStyle(player1SelectedButton);

				// player 2 clicks on field with one of his figures
			} else if (gamefield[selectedX][selectedY] > 6 && getActivePlayer() == player2) {
				buttons[btnIndex].setStyle(player2SelectedButton);

				// player clicks on field with no figure
			} else if (gamefield[selectedX][selectedY] == 0) {
				// nothing should happen
				return;

				// player wants to select field with enemy figure
			} else {
				infoUser("It is your turn!\nMake your move!").showPopUp();
				return;
			}

			// set (save) the selected button
			selectedButton[0] = btnIndex;
			selectedButton[1] = getActivePlayer().getId();

			// player has already selected a figure and now wants to move it
		} else if (selectedButton[0] > 0 && selectedButton[0] < 65) {
			// save old and new position
			int oldX = giveXY(selectedButton[0])[0];
			int newX = giveXY(btnIndex)[0];
			int oldY = giveXY(selectedButton[0])[1];
			int newY = giveXY(btnIndex)[1];

			// check if the move is invalid
			if ((!tryMove(oldX, oldY, newX, newY)) || (checkIfFieldBlocked(newX, newY, getActivePlayer()))) {
				// show information
				infoUser("Can not move player!\nInvalid move!").showPopUp();
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
				infoUser("Wrong move! \nYou are set \"schach\"!").showPopUp();
				stepBack();
				return;
			}

			// check if you set your enemy to "schach"
			if (checkSchach(getInActivePlayer())) {
				infoUser("\"SCHACH!\"" + "\n\nCan you end the game?").showPopUp();

				// check if the game should end
				if (checkMatt()) {
					// checkMatt will yet not cover moves from not_king_figures to block "matt"
					// Workaround for now: ask the player if there is a way to block "matt"
					PopUp decision = new PopUp();
					decision.createDecisionPopUp(
							"Is there a way the enemy could stop you from setting him \"Schach-Matt\"?");

					if (!decision.showPopUp()) {
						sendTelegram(getChannel(), getPrefix(2)
								+ generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], true, true));

						PopUp wait = new PopUp();
						wait.createInfoPopUp("Please wait for " + getOpponent(null).getName() + "'s decision!");

						do {
							if (!wait.isActive())
								wait.showPopUp();
							try {
								Thread.sleep((1 * 1000));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} while (!agreement.isPresent());

						// if opponent agrees
						if (agreement.get() == 1)
							endGameWithWinner(
									"Congratulations, you won the game!\nThank you for playing!\nHave a nice day :)");
						// if opponent disagrees
						if (agreement.get() == 0)
							switchPlayer();

						// reset agreement variable
						agreement = Optional.ofNullable(null);
						return;
					}
				}

				// enemy is not set to "matt", but "schach" --> send telegram and switch player
				sendTelegram(getChannel(), getPrefix(2)
						+ generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], true, false));

				switchPlayer();
				return;
			}

			// no one set to "schach", send telegram and switch player
			sendTelegram(getChannel(),
					getPrefix(2) + generateMoveMsg(lastMove[0], lastMove[1], lastMove[2], lastMove[3], false, false));

			switchPlayer();
		}
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
	 * Starts the game
	 */
	public void startButtonClicked() {
		if (gameActive) {
			PopUp decision = new PopUp();
			decision.createDecisionPopUp("Are you sure you want to request a restart of the game?");
			if (decision.showPopUp()) {
				sendTelegram(getChannel(), getPrefix(6) + "00");
				infoUser("Sending request...").showNonWaitingPopUp();
			}

		} else {
			// network
			if (workerThread != null && connected)
				if (workerThread.isAlive()) {
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
				tryConnect();
				askForPlayers();
				return;
			}
			// server waits for client and wants to start the game (connected = false, but
			// workerThread already active)
			infoUser("Please wait for a client to connect!\nThe game will start automatically. :)")
					.showNonWaitingPopUp();
			;
		}
	}

	/**
	 * Ends the game, shows the winner
	 */
	public void surrenderButtonClicked() {
		if ((!gameActive) || (!connected)) {
			infoUser("Please start the game!").showPopUp();
			return;
		}
		// send surrender message to opponent
		sendTelegram(getChannel(), getPrefix(4));

		// end the game
		endGameWithWinner("You surrendered!\n" + getInActivePlayer().getName()
				+ " won!\nThank you for playing! Have a nice day!");
	}

	/**
	 * Tries to connect to an already started server, establish the connection if
	 * there is one and starts the server thread if there is no available connection
	 * 
	 * @return false if the connect has failed, true if the connect was successful
	 */
	private void tryConnect() {
		System.out.println("Trying to connect...");
		workerThread = new Thread(() -> {
			try {
				selector = Selector.open();

				// create client channel
				SocketChannel connectionClient = SocketChannel.open();
				connectionClient.configureBlocking(false);

				// connect to Socket to myHost with non blocking connection
				connectionClient.connect(new InetSocketAddress("localhost", PORT));

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
								System.out.println("Finishing connection...");
								try {
									clientChannel.finishConnect();
									clientState = 1;
									connected = true;
									System.out.println("Client connection made!");
									Platform.runLater(() -> infoUser("You successfully connected to a host!")
											.showNonWaitingPopUp());

								} catch (Exception e) {
									// start server instead
									clientState = 2;
									selector = null;
									iterator = null;
									startServer();
									return;
								}
							}
							// watch out for incoming strings
							clientChannel.register(selector, SelectionKey.OP_READ);

							// jump to next iterator part (next key)
							continue;
						}

						if (key.isReadable()) {
							clientChannel = (SocketChannel) key.channel();
							Platform.runLater(() -> receiveString(clientChannel));
//							connected = true;

							// jump to next iterator part (next key)
							continue;
						}

					}
					Thread.sleep(1500);
				} while (connected);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				System.out.println("!Error: " + e.getMessage());

			}

		});
		workerThread.start();

		return;
	}

	/**
	 * Starts a server and waits for an incoming connection
	 */
	private void startServer() {
		System.out.println("Trying to start server");
		workerThread = new Thread(() -> {
			try {
				selector = Selector.open();

				// create server channel
				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.configureBlocking(false);

				// bind server socket to port
				serverSocketChannel.socket().bind(new InetSocketAddress("localhost", PORT));

				// info user
				Platform.runLater(() -> infoUser("You are now hosting the game!").showNonWaitingPopUp());

				// register the channel, add key "ACCEPT KEY" (isAcceptable will be true)
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

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
							System.out.println("a client connected");

							serverChannel.register(selector, SelectionKey.OP_READ);
							// jump to next iterator part (next key)
							continue;
						}

						// receive a string
						if (key.isReadable()) {
							serverChannel = (SocketChannel) key.channel();
							Platform.runLater(() -> receiveString(serverChannel));
						}
					}
					Thread.sleep(1500);
				} while (connected);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					serverSocketChannel.close();
					selector.close();
				} catch (IOException | NullPointerException e) {
					e.printStackTrace();
					System.out.println("Error on closing server socket channel!");
				}
			}
		});
		workerThread.start();
		System.out.println("Server thread started");
		Platform.runLater(() -> infoLabel.setText("Please wait..."));
	}

	private String generateMoveMsg(int oldX, int oldY, int newX, int newY, boolean schach, boolean matt) {
		String message = "";
		if (oldX < 10)
			message += "0";
		message += oldX + "";
		if (oldY < 10)
			message += "0";
		message += oldY + "";
		if (newX < 10)
			message += "0";
		message += newX + "";
		if (newY < 10)
			message += "0";
		message += newY;

		if (schach)
			message += "01";
		else
			message += "00";
		if (matt)
			message += "01";
		else
			message += "00";
		return message;
	}

	private void receiveString(SocketChannel channel) {
		// Size of the File Buffer
		int bufferSize = 8192;

		// allocate memory for the Bytes to be send
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

		// read from given socketChannel
		try {
			channel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// read -> flip ->write ->clear
		buffer.flip();

		// save buffer in new CharBuffer & decode
		CharBuffer charBuffer = charset.decode(buffer);

		if (!charBuffer.toString().isBlank() || !charBuffer.toString().isEmpty()) {
			System.out.println("received: " + charBuffer.toString());
			encodeMessage(charBuffer.toString());
		}
	}

	private void sendTelegram(SocketChannel channel, String message) {
		System.out.println("Trying to send: " + message);
		CharBuffer charBuffer = CharBuffer.wrap(message);
		// allocate memory for the Bytes to be send
		ByteBuffer buff = charset.encode(charBuffer);

		// read --> flip --> write -->clear()
		buff.compact();
		buff.flip();
		try {
			channel.write(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}

		buff.clear();
		charBuffer.clear();

		try {
			channel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
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
	private void encodeMessage(String message) {
		String method = message.substring(0, 2);
		message = message.substring(2, message.length());
		System.out.println("processing: " + method + " : " + message);
		switch (method) {

		// Client -> Server
		case "01": {
			// Server replies with his player name and starts the game on his application
			player2 = new Player(2, Optional.of(message));
			setPlayerNames();
			Platform.runLater(() -> sendTelegram(getChannel(), getPrefix(1) + player1.getName()));
			// Game starts here for server
			startGame();
			getOpponent(null).setReady(true);
			break;
		}

		// Server -> Client
		case "10": {
			// Client receives servers player name and starts the game on his application
			player1 = new Player(1, Optional.of(message));
			setPlayerNames();
			// Game starts here for client
			startGame();
			getOpponent(null).setReady(true);
			break;
		}

		// Client --> Server
		case "02": {
			// Server receives a move --> has to update its gamefield
			updateGamefieldFromTelMesg(message);
			break;
		}

		// Server --> Client
		case "20": {
			// Client receives a move --> has to update its gamefield
			updateGamefieldFromTelMesg(message);
			break;
		}

		// message: 00 --> disagreement, 01 --> agreement
		case "03": {
			// Server receives decision message
			agreement = Optional.of(Integer.valueOf(message));
			break;
		}
		case "30": {
			// Client receives decision message
			agreement = Optional.of(Integer.valueOf(message));
			break;
		}

		case "04": {
			// Server receives surrender message --> server won
			endGameWithWinner(getOpponent(null).getName()
					+ " surrendered!\nCongratulations, you won the game!\nThank you for playing! Have a nice day!");
			break;
		}
		case "40": {
			// Client receives surrender message --> client won
			endGameWithWinner(getOpponent(null).getName()
					+ " surrendered!\nCongratulations, you won the game!\nThank you for playing! Have a nice day!");
			break;
		}

		// 00 - opponent is ready to play, 01 - opponent left
		case "05": {
			if (Integer.valueOf(message) == 0) {
				// client receives start game (after restart) message
				getOpponent(null).setReady(true);
				infoUser(getOpponent(null).getName() + " is ready!\nYou can now start playing!").showPopUp();

			} else if (Integer.valueOf(message) == 1) {
				// Client receives server left message
				connected = false;
				gameActive = false;
				infoUser("Player " + getOpponent(null).getName() + " left the game!").showNonWaitingPopUp();

			}
			break;
		}
		case "50": {
			if (Integer.valueOf(message) == 0) {
				// server receives start game (after restart) message
				getOpponent(null).setReady(true);
				infoUser(getOpponent(null).getName() + " is ready!\nYou can now start playing!").showPopUp();

			} else if (Integer.valueOf(message) == 1) {
				// server receives server left message
				connected = false;
				gameActive = false;
				infoUser("Player " + getOpponent(null).getName() + " left the game!").showNonWaitingPopUp();

			}
			break;
		}
		case "06": {
			// client receives a restart message from server
			if (Integer.valueOf(message) == 0) {
				// client receives a restart request from server
				PopUp decision = new PopUp();
				decision.createDecisionPopUp(getOpponent(null).getName() + " asks for a restart!\nDo you accept that?");
				if (!decision.showPopUp())
					sendTelegram(getChannel(), getPrefix(6) + "02");
				else {
					sendTelegram(getChannel(), getPrefix(6) + "01");
					restartGame();
					getOpponent(null).setReady(true);
				}

			} else if (Integer.valueOf(message) == 1) {
				// server receives a positive restart response from client
				infoUser(getOpponent(null).getName() + " accepted the restart request!\nRestarting game...")
						.showNonWaitingPopUp();
				restartGame();
				getOpponent(null).setReady(true);

			} else if (Integer.valueOf(message) == 2) {
				// server receives a negative restart response from client
				infoUser(getOpponent(null).getName() + " rejected the restart request!").showNonWaitingPopUp();
			}

			break;
		}
		case "60": {
			// server receives a restart message from client
			if (Integer.valueOf(message) == 0) {
				// server receives a restart request from client
				PopUp decision = new PopUp();
				decision.createDecisionPopUp(getOpponent(null).getName() + " asks for a restart!\nDo you accept that?");
				if (!decision.showPopUp())
					sendTelegram(getChannel(), getPrefix(6) + "02");
				else {
					sendTelegram(getChannel(), getPrefix(6) + "01");
					restartGame();
					getOpponent(null).setReady(true);
				}

			} else if (Integer.valueOf(message) == 1) {
				// server receives a positive restart response from client
				infoUser(getOpponent(null).getName() + " accepted the restart request!\nRestarting game...")
						.showNonWaitingPopUp();
				restartGame();
				getOpponent(null).setReady(true);

			} else if (Integer.valueOf(message) == 2) {
				// server receives a negative restart response from client
				infoUser(getOpponent(null).getName() + " rejected the restart request!").showNonWaitingPopUp();
			}

			break;
		}
		default: {
			System.out.println("Telegram error: Parsing failure on prefix: " + method);
			break;
		}
		}
	}

	/**
	 * Ends the game with a winner
	 * 
	 * @param player the winner
	 */
	private void endGameWithWinner(String message) {
		gameActive = false;
		getOpponent(null).setReady(false);
		PopUp ending = new PopUp();
		ending.createWinningPopUp(message);
		ending.showPopUp();
		startButton.setText("START GAME");
		infoLabel.setText("PRESS START");
	}

	/**
	 * Updates the gamefield internally and visually according to the enemies
	 * move</br>
	 * Structure: oldX oldY newX newY schach matt --> 0X 0X 0Y 0Y 01 00-->
	 * 0X0X0Y0Y0100</br>
	 * Example: move from x:5,y:6 to x:3,y:11 with info schach:yes, matt:no -->
	 * "050603110100"</br>
	 * 
	 * @param telegramMsg provides information about the enemies move
	 */
	private void updateGamefieldFromTelMesg(String telegramMsg) {
		int oldX, oldY, newX, newY;
		int schach, matt;
		oldX = Integer.valueOf(telegramMsg.substring(0, 2));
		oldY = Integer.valueOf(telegramMsg.substring(2, 4));
		newX = Integer.valueOf(telegramMsg.substring(4, 6));
		newY = Integer.valueOf(telegramMsg.substring(6, 8));
		schach = Integer.valueOf(telegramMsg.substring(8, 10));
		matt = Integer.valueOf(telegramMsg.substring(10, telegramMsg.length()));

		gamefield[newX][newY] = gamefield[oldX][oldY];
		gamefield[oldX][oldY] = 0;

		if (schach == 1)
			infoUser("\"SCHACH!\"" + "\n\nOh no, will he end the game?").showPopUp();

		if (matt == 1) {
			// the opponent request "schach-matt"
			printField();
			setPlayers();
			PopUp question = new PopUp();
			question.createDecisionPopUp(
					getInActivePlayer().getName() + " claims you are set \"Schach-Matt\"\nDo you agree?");
			if (question.showPopUp()) {
				// agreement
				sendTelegram(getChannel(), getPrefix(3) + "01");
				endGameWithWinner(
						getActivePlayer().getName() + " won the game!\nThank you for playing!\nHave a nice day :)");
				return;
			}
			sendTelegram(getChannel(), getPrefix(3) + "00");
		}

		printField();
		setPlayers();
		switchPlayer();
	}

	/**
	 * Starts the game
	 */
	private void startGame() {
		System.out.println("clientState: " + clientState);
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

	/**
	 * Restarts the game
	 */
	private void restartGame() {
		gameActive = false;
		player1.setReady(false);
		player2.setReady(false);
		player1.setActive(false);
		player2.setActive(false);
		setStatusLabelBackgrounds();

		startGame();
	}

	/**
	 * Checks weather the enemy's king is in a problematic game situation and has to
	 * move or not, checks if any of the activePlayers' figures sets the opponents
	 * king to "schach" </br>
	 * This is called after a player made his move to check if "you" set your enemy
	 * to "schach"
	 * 
	 * @param player object to identify whose king should be checked (the
	 *               inActivePlayer)
	 * @return true, if king is in problematic situation and false, if he is not
	 */
	private boolean checkSchach(Player player) {
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
			infoUser("Location-Error\nYou may restart the game!").showPopUp();
			return false;
		}

		// try to make a move to the king from all of the opponent's (specified in
		// method) figures
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] > 0 && gamefield[x][y] <= 6 && getOpponent(player) == player1
						|| (gamefield[x][y] >= 7 && getOpponent(player) == player2)) {
					if (tryMove(x, y, kingX, kingY)) {
						problemKing[0] = kingX;
						problemKing[1] = kingY;
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * loop through all figures from the enemy and check if they set the
	 * activePlayers king "schach"
	 * 
	 * @return true if I am set to "schach", false if I am not
	 */
	private boolean checkOwnSchach() {
		if (checkSchach(getActivePlayer())) {
			return true;
		}
		return false;
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

		printField();
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

		printField();
		setPlayers();
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
	private boolean tryMove(int oldX, int oldY, int newX, int newY) {
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
			infoUser("Figure-Selection-Error\nYou may restart the game!").showPopUp();
			return false;
		}
		}
		return false;
	}

	/**
	 * Checks weather the enemies king is able to get out of a problematic game
	 * situation or not</br>
	 * this is called after a player was set to "schach"</br>
	 * TODO: Problem: a player can block "matt" with a different figure instead of
	 * moving the king -->workaround for now: asking the players
	 * 
	 * @return true, if he is not able and false, if he is
	 */
	private boolean checkMatt() {
		// strategy:
		// 1)
		// 2)
		// 2.1)free to move to means: king is not attacked there by any enemy
		// go threw all enemies and try to make a move to the currently pointing
		// position
		// 3)if there is positions left king is not "schach-matt", return false
		// 4)if there is none left king is "schach-matt", return true

		int kingX = problemKing[0];
		int kingY = problemKing[1];
		Player insultedPlayer;

		if (gamefield[kingX][kingY] == 6)
			insultedPlayer = player1;
		else
			insultedPlayer = player2;

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
						if (tryMove(x, y, posX, posY)) {
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

		return true;
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
		if (x > 9 || y > 9 || x < 0 || y < 0)
			return true;

		int fieldValue = gamefield[x][y];

		if (fieldValue > -1 && fieldValue < 7 && player == player2)
			return false;

		if ((fieldValue == 0 || (fieldValue < 13 && fieldValue > 6)) && player == player1)
			return false;

		return true;
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
		agreement = Optional.ofNullable(null);
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
	 * Sets the players on the game field (arranges the rights colors to the right
	 * buttons)
	 */
	private void setPlayers() {
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
	 * Prepares the int[][] array representing the field of the game
	 */
	private void prepareGameField() {
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
	 * Checks if your move is locked because the opponent player is active or not
	 * 
	 * @return true if your move should be locked and false if not
	 */
	private boolean moveLock() {
		if (clientState == 1 && getActivePlayer() == player2 || (clientState == 2 && getActivePlayer() == player1))
			return false;

		return true;
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
	 * Methods: </br>
	 * 1 - exchange player names</br>
	 * 2 - exchange figure moves</br>
	 * 3 - exchange decision (send agreement or disagreement)</br>
	 * 4 - exchange surrender </br>
	 * 5 - exchange start game information (game start and client left) </br>
	 * 6 - exchange restart request/response
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

	/**
	 * Switches the active player and the visualization of that
	 */
	private void switchPlayer() {
		player1.switchStatus();
		player2.switchStatus();
		setStatusLabelBackgrounds();
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
		if (player == null)
			if (clientState == 1)
				return player1;
			else
				return player2;
		return null;
	}

	/**
	 * Unselects the currently selected button (globally specified)
	 */
	private void unselectButton() {
		buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
		selectedButton[0] = 0;
		selectedButton[1] = 0;
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
//	private int giveIndex(int x, int y) {
//		if (x % 8 == 0) {
//			return (x * y);
//		} else {
//			y -= 1;
//			return ((y * 8) + x);
//		}
//	}

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
	 * Button to go back to menu
	 */
	public void backButtonClicked() {
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
			e.printStackTrace();
		}

		serverSocketChannel = null;
		clientChannel = null;
		serverChannel = null;
		selector = null;
		iterator = null;

		// close current window
		Stage current = (Stage) mainPane.getScene().getWindow();
		current.close();

		// load FXML
		FxmlOpener newFXML = new FxmlOpener(getClass().getResource("/de/ifd/mad/SimpleChess/main/StartingForm.fxml"), 0,
				null, getClass().getResource("/de/ifd/mad/SimpleChess/main/StartingFileStyle.css").toString());

		// open FXML
		if (!newFXML.open())
			System.out.println("IOException on opening StartingForm.fxml...");

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

		// send info
		if (connected)
			sendTelegram(getChannel(), getPrefix(5) + "01");

		temp.close();
		System.exit(0);
	}
}
