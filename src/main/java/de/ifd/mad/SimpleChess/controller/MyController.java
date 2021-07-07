/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */

package de.ifd.mad.SimpleChess.controller;

import de.ifd.mad.SimpleChess.main.PopUp;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
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
	Background red = new Background(new BackgroundFill(Color.RED, null, null));
	Background green = new Background(new BackgroundFill(Color.GREEN, null, null));
	Background white;
	Background black;
	Background player1;
	Background player2;

	// boolean active player indicator
	boolean player1_active = true;
	boolean player2_active = false;
	boolean game_active = false;
	// to save the answer of a previous popUp-Yes-No question
	// boolean decision = false;

	public void initialize() {
		// popUp(false, true, false, "");
		PopUp playerSet = new PopUp();
		playerSet.createInputPopUp();
		String[] players = playerSet.showInputPopUp();
		player1Text.setText(players[0]);
		player2Text.setText(players[1]);

		// activity label
		active1Label.setBackground(green);
		active2Label.setBackground(red);

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
						// popUp(false, false, false, "Please start the game!");
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
		// (light)blue
		player1 = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
		// (light)red
		player2 = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));

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
				if (gamefield[x][y] > 0 && gamefield[x][y] <= 6 && getInActivePlayer() == 1) {
					if (checkSchach(giveIndex(x, y), getActivePlayer())) {
						return true;
					}
				} else if (gamefield[x][y] >= 7 && getInActivePlayer() == 2) {
					if (checkSchach(giveIndex(x, y), getActivePlayer())) {
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
			if (gamefield[tempX][tempY] > 0 && gamefield[tempX][tempY] < 7 && getActivePlayer() == 1) {
				buttons[btnIndex].setStyle(player1SelectedButton);
			} else if (gamefield[tempX][tempY] > 6 && getActivePlayer() == 2) {
				buttons[btnIndex].setStyle(player2SelectedButton);
			} else if (gamefield[tempX][tempY] == 0) {
				// nothing
				return;
			} else {
				PopUp info = new PopUp();
				if (getActivePlayer() == 1) {
					// popUp(false, false, false, "Player " + player1Text.getText() + " is
					// active!\nMake your move!");
					info.createInfoPopUp("Player " + player1Text.getText() + " is active!\nMake your move!");
					info.showPopUp();
				} else {
					// popUp(false, false, false, "Player " + player2Text.getText() + " is
					// active!\nMake your move!");
					info.createInfoPopUp("Player " + player2Text.getText() + " is active!\nMake your move!");
					info.showPopUp();
				}
				return;
			}
			selectedButton[0] = btnIndex;
			selectedButton[1] = getActivePlayer();

		} else if (selectedButton[0] > 0 && selectedButton[0] < 65) {
			// player has already selected a figure and now wants to move it
			if (!tryMove(giveXY(selectedButton[0])[0], giveXY(selectedButton[0])[1], giveXY(btnIndex)[0],
					giveXY(btnIndex)[1])) { // move invalid
				// popUp(false, false, false, "Can not move player!\nInvalid move!");
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
				// popUp(false, false, false, "Wrong move! \nYou are set schach!");
				PopUp info = new PopUp();
				info.createInfoPopUp("Wrong move! \nYou are set schach!");
				info.showPopUp();
				stepBack();
				return;
			}
			if (checkSchach(btnIndex, getInActivePlayer())) {
				// popUp(false, false, false, "\"SCHACH!\"" + "\n\nCan you end the game?");
				PopUp info = new PopUp();
				info.createInfoPopUp("\"SCHACH!\"" + "\n\nCan you end the game?");
				info.showPopUp();
				// check if the game has ended
				if (checkMatt()) {
					// checkMatt will yet not cover moves from not_king_figures to block "matt"
					// Workaround for now: ask the player if there is a way to block "matt"
					// popUp(false, false, true, "Is there any way to block the enemy from setting
					// you Matt?");
					PopUp decision = new PopUp();
					decision.createDecisionPopUp("Is there any way to block the enemy from setting you Matt?");
					if (decision.showPopUp()) {
						switchPlayer();
					} else {
						game_active = false;
						PopUp ending = new PopUp();
						if (getActivePlayer() == 1) {
							// popUp(true, false, false, "" + player1Text.getText());
							ending.createWinningPopUp(player1Text.getText());
						} else {
							// popUp(true, false, false, "" + player2Text.getText());
							ending.createWinningPopUp(player2Text.getText());
						}
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

		if (getActivePlayer() == 1) {
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
	 * Switches the active player visually
	 */
	protected void updateActiveLabel() {
		if (active1Label.getBackground().equals(red)) {
			active1Label.setBackground(green);
			active2Label.setBackground(red);
		} else {
			active1Label.setBackground(red);
			active2Label.setBackground(green);
		}
	}

	public void switchPlayer() {
		if (player1_active) {
			player1_active = false;
			player2_active = true;
		} else {
			player1_active = true;
			player2_active = false;
		}
		updateActiveLabel();
	}

	/**
	 * Pops up a new window with certain game information
	 */
	public void popUp(boolean win, boolean input, boolean yesno, String info) {
		Stage popUp = new Stage();
		popUp.initModality(Modality.APPLICATION_MODAL);
		popUp.setMinHeight(200);
		popUp.setMinWidth(300);
		popUp.getIcons()
				.add(new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString()));
		Label label = new Label();
		label.setFont(new Font("Berlin Sans FB", 20));
		label.setTextAlignment(TextAlignment.CENTER);
		label.setStyle("-fx-text-fill: linear-gradient(to top, #ffcc00, #fbff02);");
		VBox vBox = new VBox();
		if (win) {
			popUp.setTitle("Game Over!");
			label.setText("Player " + info + " won!\nThank you for playing, have a nice day!");
			vBox.getChildren().add(label);
		} else if (input) {
			label.setText("Enter Name:");
			TextField text1 = new TextField();
			text1.setPromptText("Player 1");
			text1.setFont(label.getFont());
			Label label2 = new Label("Enter Name:");
			label2.setStyle(label.getStyle());
			label2.setFont(label.getFont());
			TextField text2 = new TextField();
			text2.setPromptText("Player 2");
			text2.setFont(label.getFont());
			Button button = new Button("Proceed");
			button.setFont(label.getFont());
			button.setStyle(startButton.getStyle());
			button.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					player1Text.setText(text1.getText().trim());
					if (text1.getText().isBlank())
						player1Text.setText(text1.getPromptText().trim());
					player2Text.setText(text2.getText().trim());
					if (text2.getText().isBlank())
						player2Text.setText(text2.getPromptText().trim());
					popUp.close();
				}
			});
			vBox.getChildren().addAll(label, text1, label2, text2, button);
		} else if (yesno) {
			label.setText(info);
			Button yes = new Button("YES");
			yes.setFont(label.getFont());
			yes.setStyle(startButton.getStyle());
			yes.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// decision = true;
					popUp.close();
				}
			});
			Button no = new Button("NO");
			no.setFont(label.getFont());
			no.setStyle(startButton.getStyle());
			no.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// decision = false;
					popUp.close();
				}
			});
			vBox.getChildren().addAll(label, yes, no);
		} else {
			vBox.getChildren().add(label);
			popUp.setTitle("Attention");
			label.setText("" + info);
		}

		vBox.setStyle(
				"-fx-background-color: radial-gradient(center 50.0% 50.0%, radius 100.0%, #242424, #434343, #898989);");
		vBox.setAlignment(Pos.CENTER);
		vBox.setSpacing(5);
		Scene scene = new Scene(vBox);
		popUp.setScene(scene);
		popUp.showAndWait();
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
			// popUp(false, false, false, "Location-Error\nYou may restart the game!");
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
	 * @return true or false, depending on if the move is valid or not
	 */
	public boolean tryMove(int oldX, int oldY, int newX, int newY) {
		boolean success = false;
		int type = gamefield[oldX][oldY];

		if (type > 6)
			type -= 6;

		switch (type) {
		/////////////////////////////////////////////////////////////////////////////
		case 1: {
			// Bauer:
			// 1 oder 2 Felder nach vorn
			if (oldX == newX) {
				// zu weit?
				int temp = oldY - newY;
				if (temp < 0)
					temp *= (-1);
				if (temp > 2) {
					break;
				}
				if (oldY < newY && getActivePlayer() == 2) {
					// player 2 tries to move backwards (down)
					break;
				} else if (oldY > newY && getActivePlayer() == 1) {
					// player 1 tries to move backwards (up)
					break;
				}
				if (gamefield[newX][newY] == 0) {
					success = true;
				}
			} else {
				// Sprung schraeg nach vorn (rechts, links)
				if (getActivePlayer() == 1) {
					if ((newX == oldX - 1 || newX == oldX + 1) && newY == oldY + 1) {
						// valid distance & direction
						if (gamefield[newX][newY] > 6) {
							success = true;
						}
					}
				} else if (getActivePlayer() == 2) {
					if ((newX == oldX - 1 || newX == oldX + 1) && newY + 1 == oldY) {
						// valid distance & direction
						if (gamefield[newX][newY] < 7 && gamefield[newX][newY] > 0) {
							success = true;
						}
					}
				}
			}
			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 2: {
			// Turm
			if (newX == oldX) {
				// moves within y
				int counter = 0;
				if (newY > oldY) {
					for (int y = oldY + 1; y < newY; y++) {
						if (gamefield[newX][y] > 0) {
							counter++;
						}
					}
				} else if (newY < oldY) {
					for (int y = oldY - 1; y > newY; y--) {
						if (gamefield[newX][y] > 0) {
							counter++;
						}
					}
				}
				if (counter > 0) {
					break;
				} else {
					// way is free for the figure to move
					success = true;
				}
			} else if (newY == oldY) {
				// moves within x
				int counter = 0;
				if (getActivePlayer() == 1) {
					for (int x = oldX + 1; x < newX; x++) {
						if (gamefield[x][newY] > 0) {
							counter++;
						}
					}
				} else if (getActivePlayer() == 2) {
					for (int x = oldX - 1; x > newX; x--) {
						if (gamefield[x][newY] > 0) {
							counter++;
						}
					}
				}
				if (counter > 0) {
					break;
				} else {
					// way is free for the figure to move
					success = true;
				}
			} else {
				break;
			}
		}
		/////////////////////////////////////////////////////////////////////////////
		case 3: {
			// Pferd
			if ((oldY + 1 == newY && oldX + 2 == newX) || (oldY + 2 == newY && oldX + 1 == newX)) {
				// down, right, right && down, down, right
				success = true;
				break;
			} else if ((oldY + 1 == newY && oldX - 2 == newX) || (oldY + 2 == newY && oldX - 1 == newX)) {
				// down, left, left && down, down, left
				success = true;
				break;
			} else if ((oldY - 1 == newY && oldX + 2 == newX) || (oldY - 2 == newY && oldX + 1 == newX)) {
				// up, right, right && up, up, right
				success = true;
				break;
			} else if ((oldY - 1 == newY && oldX - 2 == newX) || (oldY - 2 == newY && oldX - 1 == newX)) {
				// up, left, left && up, up, left
				success = true;
				break;
			} else {
				break;
			}
		}
		/////////////////////////////////////////////////////////////////////////////
		case 4: {
			// Springer
			int temp1 = oldX - newX;
			int temp2 = oldY - newY;
			if (temp1 < 0)
				temp1 *= (-1);
			if (temp2 < 0)
				temp2 *= (-1);

			if (temp1 == temp2) {
				// move is diagonally
				// up, left
				int counter = 0;
				if (newY < oldY && newX < oldX) {
					for (int k = 1; k < temp1; k++) {
						if (gamefield[oldX - k][oldY - k] > 0)
							counter++;
					}
				}
				// up, right
				if (newY < oldY && newX > oldX) {
					for (int k = 1; k < temp1; k++) {
						if (gamefield[oldX + k][oldY - k] > 0)
							counter++;
					}
				}
				// down, left
				if (newY > oldY && newX < oldX) {
					for (int k = 1; k < temp1; k++) {
						if (gamefield[oldX - k][oldY + k] > 0)
							counter++;
					}
				}
				// down, right
				if (newY > oldY && newX > oldX) {
					for (int k = 1; k < temp1; k++) {
						if (gamefield[oldX + k][oldY + k] > 0)
							counter++;
					}
				}
				if (counter > 0) {
					break;
				} else {
					// way is free for the figure to move
					success = true;
				}
			} else {
				break;
			}

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 5: {
			// Dame = Turm oder Springer
			// you could create func for Turm and Springer and just call it here, to safe
			// some lines
			// (TURM:)
			if (newX == oldX) {
				// moves within y
				int counter = 0;
				if (newY > oldY) {
					for (int y = oldY + 1; y < newY; y++) {
						if (gamefield[newX][y] > 0) {
							counter++;
						}
					}
				} else if (newY < oldY) {
					for (int y = oldY - 1; y > newY; y--) {
						if (gamefield[newX][y] > 0) {
							counter++;
						}
					}
				}
				if (counter > 0) {
					break;
				} else {
					// way is free for the figure to move
					success = true;
				}
			} else if (newY == oldY) {
				// moves within x
				int counter = 0;
				if (newX > oldX) {
					for (int x = oldX + 1; x < newX; x++) {
						if (gamefield[x][newY] > 0) {
							counter++;
						}
					}
				} else if (oldX > newX) {
					for (int x = oldX - 1; x > newX; x--) {
						if (gamefield[x][newY] > 0) {
							counter++;
						}
					}
				}
				if (counter > 0) {
					break;
				} else {
					// way is free for the figure to move
					success = true;
				}
			} else {
				// (SPRINGER:)
				int temp1 = oldX - newX;
				int temp2 = oldY - newY;
				if (temp1 < 0)
					temp1 *= (-1);
				if (temp2 < 0)
					temp2 *= (-1);

				if (temp1 == temp2) {
					// move is diagonally
					// up, left
					int counter = 0;
					if (newY < oldY && newX < oldX) {
						for (int k = 1; k < temp1; k++) {
							if (gamefield[oldX - k][oldY - k] > 0)
								counter++;
						}
					}
					// up, right
					if (newY < oldY && newX > oldX) {
						for (int k = 1; k < temp1; k++) {
							if (gamefield[oldX + k][oldY - k] > 0)
								counter++;
						}
					}
					// down, left
					if (newY > oldY && newX < oldX) {
						for (int k = 1; k < temp1; k++) {
							if (gamefield[oldX - k][oldY + k] > 0)
								counter++;
						}
					}
					// down, right
					if (newY > oldY && newX > oldX) {
						for (int k = 1; k < temp1; k++) {
							if (gamefield[oldX + k][oldY + k] > 0)
								counter++;
						}
					}
					if (counter > 0) {
						break;
					} else {
						// way is free for the figure to move
						success = true;
					}
				}
			}
			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		case 6: {
			// Koenig
			// zu weit?
			int temp = oldY - newY;
			int temp2 = oldX - newX;
			if (temp2 < 0)
				temp2 *= (-1);
			if (temp < 0)
				temp *= (-1);

			if (temp > 1 || temp2 > 1) {
				break;
			}

			success = true;

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		default: {
			// popUp(false, false, false, "Figure-Selection-Error\nYou may restart the
			// game!");
			PopUp info = new PopUp();
			info.createInfoPopUp("Figure-Selection-Error\nYou may restart the game!");
			info.showPopUp();
			return false;
		}
		}

		if (success) {
			return true;
		} else {
			return false;
		}
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
				if (gamefield[x][y] > -1 && gamefield[x][y] < 7 && getInActivePlayer() == 2) {
					posList[counter][0] = x;
					posList[counter][1] = y;
					counter++;
				} else if ((gamefield[x][y] == 0 || gamefield[x][y] > 6) && getInActivePlayer() == 1) {
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
					if (gamefield[x][y] > 0 && gamefield[x][y] < 7 && getActivePlayer() == 1) {
//						System.out.println("trying move from: " + x + "|" + y + " to " + posX + "|" + posY + "!");
						if (tryMove(x, y, posX, posY)) {
							// potential king position is covered --> screw
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					} else if (gamefield[x][y] > 6 && getActivePlayer() == 2) {
//						System.out.println("trying move from: " + x + "|" + y + " to " + posX + "|" + posY);
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

		// System.out.println("" + Arrays.deepToString(posList));

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
			active1Label.setBackground(red);
			active2Label.setBackground(red);
			infoLabel.setText("PRESS START");
			prepareGameField();
			setPlayers();
		} else {
			startButton.setText("RESTART GAME");
			infoLabel.setText("LET'S PLAY");
			globalVals();
			game_active = true;
			prepareGameField();
			setPlayers();
			player1_active = true;
			// activity label
			active1Label.setBackground(green);
			active2Label.setBackground(red);
		}
	}

	/**
	 * Resets all the global values in order to restart the game
	 */
	public void globalVals() {
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
				if (gamefield[k][i] <= 6 && gamefield[k][i] > 0) {
					// buttons[z].setBackground(player1);
					if (gamefield[k][i] == 1) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/bauer1.png").toString())));
					} else if (gamefield[k][i] == 2) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/turm1.png").toString())));
					} else if (gamefield[k][i] == 3) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/pferd1.png").toString())));
					} else if (gamefield[k][i] == 4) {
						buttons[z].setGraphic(new ImageView(new Image(getClass()
								.getResource("/de/ifd/mad/SimpleChess/images/springer1.png").toString())));
					} else if (gamefield[k][i] == 5) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/dame1.png").toString())));
					} else if (gamefield[k][i] == 6) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString())));
					}
				} else if (gamefield[k][i] >= 7) {
					// buttons[z].setBackground(player2);
					if (gamefield[k][i] == 7) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/bauer2.png").toString())));
					} else if (gamefield[k][i] == 8) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/turm2.png").toString())));
					} else if (gamefield[k][i] == 9) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/pferd2.png").toString())));
					} else if (gamefield[k][i] == 10) {
						buttons[z].setGraphic(new ImageView(new Image(getClass()
								.getResource("/de/ifd/mad/SimpleChess/images/springer2.png").toString())));
					} else if (gamefield[k][i] == 11) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/dame2.png").toString())));
					} else if (gamefield[k][i] == 12) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifd/mad/SimpleChess/images/king2.png").toString())));
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
	 * @return
	 */
	public int getInActivePlayer() {
		if (player1_active)
			return 2;
		if (player2_active)
			return 1;
		return -1;
	}

	/**
	 * Returns the currently active player
	 * 
	 * @return
	 */
	public int getActivePlayer() {
		if (player1_active)
			return 1;
		if (player2_active)
			return 2;
		return -1;
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
