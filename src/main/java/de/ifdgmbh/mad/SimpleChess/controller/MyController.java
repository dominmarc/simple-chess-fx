package de.ifdgmbh.mad.SimpleChess.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

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

	/**
	 * Player1: 1 - Bauer 2 - Turm 3 - Pferd 4 - Laeufer 5 - Dame 6 - Koenig </br>
	 * Player2: 7 - Bauer 8 - Turm 9 - Pferd 10 - Laeufer 11 - Dame 12 - Koenig
	 */
	int[][] gamefield;

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

	Background red = new Background(new BackgroundFill(Color.RED, null, null));
	Background green = new Background(new BackgroundFill(Color.GREEN, null, null));
	Background white;
	Background black;
	Background player1;
	Background player2;

	boolean player1_active = true;
	boolean player2_active = false;
	boolean game_active = false;

	Alert myAlert;

	public void initialize() {
		// activity label
		active1Label.setBackground(green);
		active2Label.setBackground(red);
		
		//mypane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #242424, #434343, #898989);");
		closeButton.setStyle("");
		minButton.setStyle("");
		startButton.setText("START GAME");
//		startButton.setStyle("-fx-background-color: #090a0c,\r\n"
//				+ "		linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n"
//				+ "		linear-gradient(#20262b, #191d22),\r\n"
//				+ "		radial-gradient(center 50% 0%, radius 100%, rgba(114, 131, 148, 0.9),\r\n"
//				+ "		rgba(255, 255, 255, 0));\r\n"
//				+ "	-fx-background-radius: 5, 4, 3, 5;\r\n"
//				+ "	-fx-background-insets: 0, 1, 2, 0;\r\n"
//				+ "	-fx-text-fill: white;\r\n"
//				+ "	-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.6), 5, 0.0, 0, 1);\r\n"
//				+ "	-fx-font-family: \"Berlin Sans FB\";\r\n"
//				+ "	-fx-text-fill: linear-gradient(white, #d0d0d0);\r\n"
//				+ "	-fx-font-size: 12px;\r\n"
//				+ "	-fx-padding: 10 20 10 20;\r\n"
//				+ "	-fx-text-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.9), 1, 0.0, 0,\r\n"
//				+ "		1);");
		
		buttons = new Button[82];
		int x = 0;
		int y = 0;
		int z = 1;
		for (int i = 1; i < 9; i++) {
			for (int t = 1; t < 9; t++) {
				buttons[z] = new Button();
				buttonPane.getChildren().add(buttons[z]);
				buttons[z].setLayoutX(x);
				buttons[z].setLayoutY(y);
				buttons[z].setPrefWidth(45);
				buttons[z].setPrefHeight(45);
				buttons[z].setPadding(new Insets(0));
				white = new Background(new BackgroundFill(Color.rgb(224, 201, 160), null, null));
				black = new Background(new BackgroundFill(Color.rgb(164, 120, 91), null, null));
				if (i % 2 == 0) {
					if (t % 2 == 0) {
						buttons[z].setBackground(white);
					} else {
						buttons[z].setBackground(black);
					}
				} else {
					if (t % 2 == 0) {
						buttons[z].setBackground(black);
					} else {
						buttons[z].setBackground(white);
					}
				}
//				buttons[z].setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
				final int tempZ = z;
				buttons[z].setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (!game_active) {
							myAlert = new Alert(Alert.AlertType.WARNING, "Please start the game!");
							myAlert.show();
							return;
						}

						if (selectedButton[0] == 0) {
							// player wants to select a figure to make a move
//							if (buttons[tempZ].getBackground().equals(player1) && getActivePlayer() == 1) {
//								buttons[tempZ].setStyle(player1SelectedButton);
//							} else if (buttons[tempZ].getBackground().equals(player2) && getActivePlayer() == 2) {
//								buttons[tempZ].setStyle(player2SelectedButton);
//							} else if (buttons[tempZ].getBackground().equals(white)
//									|| buttons[tempZ].getBackground().equals(black)) {
//								// nothing
//								return;
//							} else {
//								myAlert = new Alert(Alert.AlertType.WARNING,
//										"Player " + getActivePlayer() + " is active!");
//								myAlert.show();
//								return;
//							}

							int tempX = giveXY(tempZ)[0];
							int tempY = giveXY(tempZ)[1];
							if (gamefield[tempX][tempY] > 0 && gamefield[tempX][tempY] < 7 && getActivePlayer() == 1) {
								buttons[tempZ].setStyle(player1SelectedButton);
							} else if (gamefield[tempX][tempY] > 6 && getActivePlayer() == 2) {
								buttons[tempZ].setStyle(player2SelectedButton);
							} else if (gamefield[tempX][tempY] == 0) {
								// nothing
								return;
							} else {
								myAlert = new Alert(Alert.AlertType.WARNING,
										"Player " + getActivePlayer() + " is active!");
								myAlert.show();
								return;
							}

							selectedButton[0] = tempZ;
							selectedButton[1] = getActivePlayer();
						} else if (selectedButton[0] > 0 && selectedButton[0] < 65) {
							// player has already selected a figure and now wants to move it
							if (!makeMove(tempZ)) { // move invalid
								myAlert = new Alert(Alert.AlertType.WARNING, "Cannot move player!");
								myAlert.show();
								// deselect button
								buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
								selectedButton[0] = 0;
								selectedButton[1] = 0;
								return;
							}
							// move valid
							// deselect button
							buttons[selectedButton[0]].setStyle(playerNonSelectedButton);
							selectedButton[0] = 0;
							selectedButton[1] = 0;

							if (checkSchach(tempZ)) {
								myAlert = new Alert(Alert.AlertType.INFORMATION, "SCHACH!");
								myAlert.showAndWait();
								// check if the game has ended
								if (checkMatt()) {
									game_active = false;
									myAlert = new Alert(Alert.AlertType.INFORMATION, "Congratulation!");
									myAlert.setTitle("Game Over!");
									myAlert.setHeaderText("Player " + getActivePlayer() + " won!");
									myAlert.show();
									return;
								}
								// force the player to move the king
								switchPlayer();
								selectedButton[0] = giveIndex(problemKing[0], problemKing[1]);
								selectedButton[1] = getActivePlayer();
								if (getActivePlayer() == 1) {
									buttons[selectedButton[0]].setStyle(player1SelectedButton);
								} else if (getActivePlayer() == 2) {
									buttons[selectedButton[0]].setStyle(player2SelectedButton);
								}
								return;
							}
							// end the move --> switch player
							switchPlayer();
						}
					}
				});
				z++;
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

	/**
	 * Tries to move the selected figure (within the gamefield array)
	 * 
	 * @param var represents the button index
	 * @return true or false depending on weather the player is allowed to make a
	 *         move or not
	 */
	public boolean makeMove(int var) {
		boolean success = false;
		myAlert = new Alert(Alert.AlertType.ERROR, "Something went wrong!");

		int oldX = giveXY(selectedButton[0])[0];
		int oldY = giveXY(selectedButton[0])[1];
		int newX = giveXY(var)[0];
		int newY = giveXY(var)[1];
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
					gamefield[newX][newY] = gamefield[oldX][oldY];
					gamefield[oldX][oldY] = 0;
					success = true;
				}
			} else {
				// Sprung schraeg nach vorn (rechts, links)
				if (getActivePlayer() == 1) {
					if ((newX == oldX - 1 || newX == oldX + 1) && newY == oldY + 1) {
						// valid distance & direction
						if (gamefield[newX][newY] > 6) {
							gamefield[newX][newY] = gamefield[oldX][oldY];
							gamefield[oldX][oldY] = 0;
							success = true;
						}
					}
				} else if (getActivePlayer() == 2) {
					if ((newX == oldX - 1 || newX == oldX + 1) && newY + 1 == oldY) {
						// valid distance & direction
						if (gamefield[newX][newY] < 7 && gamefield[newX][newY] > 0) {
							gamefield[newX][newY] = gamefield[oldX][oldY];
							gamefield[oldX][oldY] = 0;
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
					if (doSetMove(oldX, oldY, newX, newY))
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
					if (doSetMove(oldX, oldY, newX, newY))
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
				if (doSetMove(oldX, oldY, newX, newY))
					success = true;
				break;
			} else if ((oldY + 1 == newY && oldX - 2 == newX) || (oldY + 2 == newY && oldX - 1 == newX)) {
				// down, left, left && down, down, left
				if (doSetMove(oldX, oldY, newX, newY))
					success = true;
				break;
			} else if ((oldY - 1 == newY && oldX + 2 == newX) || (oldY - 2 == newY && oldX + 1 == newX)) {
				// up, right, right && up, up, right
				if (doSetMove(oldX, oldY, newX, newY))
					success = true;
				break;
			} else if ((oldY - 1 == newY && oldX - 2 == newX) || (oldY - 2 == newY && oldX - 1 == newX)) {
				// up, left, left && up, up, left
				if (doSetMove(oldX, oldY, newX, newY))
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
					if (doSetMove(oldX, oldY, newX, newY))
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
					if (doSetMove(oldX, oldY, newX, newY))
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
					if (doSetMove(oldX, oldY, newX, newY))
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
						if (doSetMove(oldX, oldY, newX, newY))
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
			// king can not set himself to "schach" or "schach-matt"

			boolean freeToMove = true;

			for (int y = 1; y < 9; y++) {
				for (int x = 1; x < 9; x++) {
					if (gamefield[x][y] > 0 && gamefield[x][y] < 7 && getActivePlayer() == 2) {
						if (tryMove(x, y, newX, newY)) {
							// potential king position is covered --> screw
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					} else if (gamefield[x][y] > 6 && getActivePlayer() == 1) {
						if (tryMove(x, y, newX, newY)) {
							// potential king position is covered
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					}
				}
			}

			if (freeToMove) {
				if (doSetMove(oldX, oldY, newX, newY))
					success = true;
			}

			break;
		}
		/////////////////////////////////////////////////////////////////////////////
		default: {
			myAlert.show();
			return false;
		}
		}

		printField();
		setPlayers();

		if (success) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Called in makeMove-Function to actually make the move, depending on what
	 * player is active
	 * 
	 * @param oldX
	 * @param oldY
	 * @param newX
	 * @param newY
	 */
	private boolean doSetMove(int oldX, int oldY, int newX, int newY) {
		if (getActivePlayer() == 1) {
			if (gamefield[newX][newY] > 6 || gamefield[newX][newY] == 0) {
				gamefield[newX][newY] = gamefield[oldX][oldY];
				gamefield[oldX][oldY] = 0;
				return true;
			}
		} else {
			if (gamefield[newX][newY] < 7 && gamefield[newX][newY] > -1) {
				gamefield[newX][newY] = gamefield[oldX][oldY];
				gamefield[oldX][oldY] = 0;
				return true;
			}
		}
		return false;
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
	 * Checks weather the enemies king is in a problematic game situation and has to
	 * move or not
	 * 
	 * @return
	 */
	public boolean checkSchach(int var) {
		int enemyX = giveXY(var)[0];
		int enemyY = giveXY(var)[1];
		int kingX = 0;
		int kingY = 0;
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] == 6 && getInActivePlayer() == 1) {
					kingX = x;
					kingY = y;
				} else if (gamefield[x][y] == 12 && getInActivePlayer() == 2) {
					kingX = x;
					kingY = y;
				}
			}
		}
		if (kingX == 0 || kingY == 0) {
			myAlert = new Alert(Alert.AlertType.ERROR, "Problem!");
			myAlert.show();
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
		myAlert = new Alert(Alert.AlertType.ERROR, "Something went wrong!");
		int type = gamefield[oldX][oldY];

		if (type > 6)
			type -= 6;

		switch (type) {
		/////////////////////////////////////////////////////////////////////////////
		case 1: {
			// Bauer:
			// 1 oder 2 Felder nach vorn
			if (oldX == newX) {
				break;
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
			myAlert.show();
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
						System.out.println("trying move from: " + x + "|" + y + " to " + posX + "|" + posY + "!");
						if (tryMove(x, y, posX, posY)) {
							// potential king position is covered --> screw
							freeToMove = false;
						} else {
							// potential king position is free for current enemy figure
						}
					} else if (gamefield[x][y] > 6 && getActivePlayer() == 2) {
						System.out.println("trying move from: " + x + "|" + y + " to " + posX + "|" + posY);
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
			selectedButton[0] = 0;
			selectedButton[1] = 0;
			problemKing[0] = 0;
			problemKing[1] = 0;
			prepareGameField();
			setPlayers();
		} else {
			startButton.setText("RESTART GAME");
			infoLabel.setText("LET'S PLAY");
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
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/bauer1.png").toString())));
					} else if (gamefield[k][i] == 2) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/turm1.png").toString())));
					} else if (gamefield[k][i] == 3) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/pferd1.png").toString())));
					} else if (gamefield[k][i] == 4) {
						buttons[z].setGraphic(new ImageView(new Image(getClass()
								.getResource("/de/ifdgmbh/mad/SimpleChess/images/springer1.png").toString())));
					} else if (gamefield[k][i] == 5) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/dame1.png").toString())));
					} else if (gamefield[k][i] == 6) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/king1.png").toString())));
					}
				} else if (gamefield[k][i] >= 7) {
					// buttons[z].setBackground(player2);
					if (gamefield[k][i] == 7) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/bauer2.png").toString())));
					} else if (gamefield[k][i] == 8) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/turm2.png").toString())));
					} else if (gamefield[k][i] == 9) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/pferd2.png").toString())));
					} else if (gamefield[k][i] == 10) {
						buttons[z].setGraphic(new ImageView(new Image(getClass()
								.getResource("/de/ifdgmbh/mad/SimpleChess/images/springer2.png").toString())));
					} else if (gamefield[k][i] == 11) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/dame2.png").toString())));
					} else if (gamefield[k][i] == 12) {
						buttons[z].setGraphic(new ImageView(new Image(
								getClass().getResource("/de/ifdgmbh/mad/SimpleChess/images/king2.png").toString())));
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
