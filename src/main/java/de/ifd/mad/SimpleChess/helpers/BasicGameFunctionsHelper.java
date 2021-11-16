/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
import de.ifd.mad.SimpleChess.main.PopUp;
import de.ifd.mad.SimpleChess.players.Player;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Helper Class for chess game by MAD
 * 
 * @author MAD
 * @author iFD
 */
public class BasicGameFunctionsHelper {

	private static final ChessLogger LOGGER = ChessLogger.getLogger(BasicGameFunctionsHelper.class);

	private static final String BAR = "==============================================";

	private BasicGameFunctionsHelper() {
		// we don't need to construct objects of this class
	}

//====================================================================================================
//==																								==	
//==General Helper Functions:																		==
//==																								==	
//====================================================================================================

	/**
	 * Exports the current game field to a file
	 * 
	 * @param gamefield
	 * @return
	 */
	public static String tryExport(int[][] gamefield) {
		LOGGER.info("User wants to export the gamefield...");
		String returnStr = "";
		// create dialog
		Path gameFile = null;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save your file!");
		Stage mystage = null;
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Gamefield", "*.field"));

		// open dialog
		File saveFile = fileChooser.showSaveDialog(mystage);
		// check if dialog returned something
		if (saveFile == null) {
			LOGGER.warn("User selected no file!");
			return "No file selected!";
		}
		// ensure existence
		gameFile = saveFile.toPath();
		if (!Files.exists(gameFile)) {
			try {
				Path tmpFile = Files.createFile(gameFile);
				gameFile = tmpFile;
			} catch (IOException e) {
				LOGGER.error("Error saving file: [{}] = {}: {}", gameFile.getFileName(), e.getClass().getName(),
						e.getMessage());
				return "Error on saving file!";
			}
		}
		// write to created file
		BufferedWriter buffW = null;
		try {
			buffW = Files.newBufferedWriter(gameFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
			buffW.write(buildFieldString(gamefield));
			buffW.close();
			LOGGER.info("Successfully saved file: {}", gameFile);
			return ("Successfully saved file: " + gameFile.getFileName());
		} catch (IOException e) {
			// Buffered writer problem
			returnStr = ("Error on writing to your specified file!\nYou might try a different location!");
			LOGGER.error("Error on writing to file: [{}] = {}: {}", gameFile, e.getClass().getName(), e.getMessage());
		} finally {
			try {
				if (buffW != null)
					buffW.close();
			} catch (IOException e) {
				LOGGER.warn("Could not close Buffered Reader! = {}", e.getMessage());
			}
		}
		return returnStr;
	}

	/**
	 * 
	 * @param gamefield
	 * @return
	 */
	public static String buildFieldString(int[][] gamefield) {
		if (gamefield == null)
			return "null";

		if (gamefield.length == 0)
			return "0";

		StringBuilder field = new StringBuilder();
		for (int g = 0; g < 10; g++) {
			for (int h = 0; h < 10; h++) {
				field.append((gamefield[h][g] + " "));
			}
			field.append("\n");
		}
		return field.toString();
	}

	/**
	 * Checks if the move to the new field is valid (does not check the field,
	 * actually moves nothing)
	 * 
	 * @param oldX      figures old x position
	 * @param oldY      figures old y position
	 * @param newX      figures new x position
	 * @param newY      figures new y position
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * @param player    the player object referring to the player that tries to make
	 *                  the move
	 * @return true or false, depending on if the move is valid or not (depends not
	 *         on if the position is blocked)
	 */
	public static boolean tryMove(int oldX, int oldY, int newX, int newY, int[][] gamefield, Player player) {
		int type = gamefield[oldX][oldY];

		if (type > 6)
			type -= 6;

		switch (type) {
		/////////////////////////////////////////////////////////////////////////////
		case 1:
			// Bauer:
			return (Pawn.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		case 2:
			// Turm
			return (Rook.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		case 3:
			// Pferd
			return (Knight.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		case 4:
			// Springer
			return (Bishop.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		case 5:
			// queen = rook oder bishop
			return (Queen.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		case 6:
			// Koenig
			return (King.tryMove(oldX, oldY, newX, newY, player, gamefield));

		/////////////////////////////////////////////////////////////////////////////
		default:
			LOGGER.error("Figure-Selection-Error in function: {}, with variables: gamefield-value={}", "tryMove",
					gamefield[oldX][oldY]);
			PopUp info = new PopUp();
			info.createInfoPopUp("Figure-Selection-Error\nYou may restart the game!");
			info.showNonWaitingPopUp();
			return false;

		}
	}

	/**
	 * Checks all the figures of the given player; if at least one of them is able
	 * to move to the given position
	 * 
	 * @param player    the player whos figures should be checked
	 * @param posX      the position x we are looking to move to
	 * @param posY      the position y we are looking to move to
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * 
	 * @return true if he can, false if he cant
	 * 
	 * @author MAD
	 * @author iFD
	 */
	public static boolean canAnyFigureMoveThere(Player player, int posX, int posY, int[][] gamefield) {
		// loop through gamefield
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				int fieldVal = gamefield[x][y];

				if (fieldVal > 0 && fieldVal < 7 && (fieldVal != 6) && player.getId() == 1) {
					// player1 moves
					if (Boolean.TRUE.equals(tryMove(x, y, posX, posY, gamefield, player))) {
						return true;
					}
				}
				if (fieldVal > 6 && (fieldVal != 12) && player.getId() == 2) {
					// player2 moves
					if (Boolean.TRUE.equals(tryMove(x, y, posX, posY, gamefield, player))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the new position is free to move to (just this field)
	 * 
	 * @param x         figures new x position
	 * @param y         figures new y position
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * @param player    object to identify the related player
	 * @return true if the field is blocked, false if it is not blocked
	 */
	public static boolean checkIfFieldBlocked(int x, int y, int[][] gamefield, Player player) {
		if (x > 9 || y > 9 || x < 0 || y < 0)
			return true;

		int fieldValue = gamefield[x][y];

		if (fieldValue > -1 && fieldValue < 7 && player.getId() == 2)
			return false;

		if ((fieldValue == 0 || (fieldValue < 13 && fieldValue > 6)) && player.getId() == 1)
			return false;

		return true;
	}

	/**
	 * 
	 * @param player    the player who should be checked for "check"
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * @param player1   instance of player referring to player1
	 * @param player2   instance of player referring to player2
	 * @return int[2] array with 0 representing the insulted kingX and 1
	 *         representing the insulted kingY
	 */
	public static int[] isChecked(Player player, int[][] gamefield, Player player1, Player player2) {
		int kingX = 0;
		int kingY = 0;
		int[] kingXY = { 0, 0 };

		Player opponent = getOpponentOnId(player.getId(), player1, player2);

		// searches for the king of the specified player
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if ((gamefield[x][y] == 6 && player.getId() == 1) || (gamefield[x][y] == 12 && player.getId() == 2)) {
					kingX = x;
					kingY = y;
				}
			}
		}

		// no king found
		if (kingX == 0 || kingY == 0)
			return kingXY;

		// try to make a move to the king from all of the opponent's (specified in
		// method) figures
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (isPlayerField(x, y, gamefield, opponent)) {
					if (tryMove(x, y, kingX, kingY, gamefield, opponent)) {
						kingXY[0] = kingX;
						kingXY[1] = kingY;
						return kingXY;
					}
				}
			}
		}
		return kingXY;
	}

	/**
	 * This function checks for a checkmate.</br>
	 * This happens on the assumption, that there is an insulted king.</br>
	 * </br>
	 * First it will check if there are fields left, that the king could move
	 * to.</br>
	 * Secondly it will check if there are any figures that could block the way to
	 * the king and prevent a check.</br>
	 * Thirdly it will check if any figure of the insulted player can actually
	 * attack the king attacker.</br>
	 * 
	 * @param kingX     the kingX position of the insulted king
	 * @param kingY     the kingY position of the insulted king
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * @param player1   instance of player referring to player1
	 * @param player2   instance of player referring to player2
	 * 
	 * @return true if checkmate, false if not
	 * 
	 * @author MAD
	 * @author iFD
	 */
	public static boolean isCheckmate(int kingX, int kingY, int[][] gamefield, Player player1, Player player2) {
		// get the insulted player (the attacked player)
		Player insultedPlayer;
		if (gamefield[kingX][kingY] == 6)
			insultedPlayer = player1;
		else if (gamefield[kingX][kingY] == 12)
			insultedPlayer = player2;
		else
			return false;

		// get the attacker
		Player opponent = getOpponentOnId(insultedPlayer.getId(), player1, player2);

		// 1)
		if (existFreeFieldsAroundKing(kingX, kingY, insultedPlayer, opponent, gamefield))
			return false;

		// 2)
		if (canTheCheckBeBlocked(kingX, kingY, insultedPlayer, opponent, gamefield))
			return false;

		// 3)
		if (canAttackKingAttacker(kingX, kingY, insultedPlayer, opponent, gamefield))
			return false;

		return true;
	}

	/**
	 * 
	 * @param kingX          the kingX position of the insulted king
	 * @param kingY          the kingY position of the insulted king
	 * @param insultedPlayer the player object referring to the insulted player
	 * @param gamefield      2d-int array representing the current gamefield
	 *                       situation
	 * @return true if there are free fields for the king to move to, false if there
	 *         is none
	 */
	private static boolean existFreeFieldsAroundKing(int kingX, int kingY, Player insultedPlayer, Player opponent,
			int[][] gamefield) {
		// save fields around insulted king to check if they are free for him to move to
		int[][] posList = new int[8][2]; // max 8 positions with 2 values (x,y)
		int counter = 0; // counts the number of positions

		// list up all positions the king can move to
		for (int y = kingY - 1; y <= kingY + 1; y++) { // loop around the king
			for (int x = kingX - 1; x <= kingX + 1; x++) {
				// king can just move one field to each side, so just check if that field is
				// blocked
				if (!checkIfFieldBlocked(x, y, gamefield, insultedPlayer)) {
					posList[counter][0] = x;
					posList[counter][1] = y;
					counter++;
				}
			}
		}

		// go threw all the positions the king can move to and see if they are free to
		// move to
		for (int k = 0; k <= counter - 1; k++) {
			int posX = posList[k][0];
			int posY = posList[k][1];

			// Indicates whether the king is able to make a move (escape "check") or not
			// NOTE that this gets reset per field around the king, so we check for each
			// field around him if he can move there and as soon as there is a field we
			// return true
			boolean freeToMove = true;

			for (int y = 1; y < 9; y++) {
				for (int x = 1; x < 9; x++) {
					// just go through opponent fields and see if his figures block our fields
					if (isPlayerField(x, y, gamefield, opponent))
						if (tryMove(x, y, posX, posY, gamefield, opponent)) {
							freeToMove = false;
							continue;
						}

				}
			}

			if (freeToMove)
				return true;

		}
		return false;
	}

	/**
	 * 
	 * @param kingX
	 * @param kingY
	 * @param insultedPlayer
	 * @param opponent
	 * @param gamefield
	 * @return
	 */
	private static boolean canTheCheckBeBlocked(int kingX, int kingY, Player insultedPlayer, Player opponent,
			int[][] gamefield) {
		// look for all the fields that can be blocked by the insulted players figures
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {

				if (isPlayerField(x, y, gamefield, opponent)) {
					// 1, 3, 6, 7, 9, 12 cannot be blocked(no knight or pawn or king)
					if ((tryMove(x, y, kingX, kingY, gamefield, opponent)) && (gamefield[x][y] != 1)
							&& (gamefield[x][y] != 3) && (gamefield[x][y] != 6) && (gamefield[x][y] != 7)
							&& (gamefield[x][y] != 9) && (gamefield[x][y] != 12)) {
						Map<Integer, Integer> fields = getFieldsOfMovement(x, y, kingX, kingY, opponent, gamefield);
						if (fields == null) {
							// error happened
						} else {
							// we have the fields --> now look if any figure of the insulted player can move
							// there
							for (Integer xx : fields.keySet()) {
								if (Boolean.TRUE
										.equals(canAnyFigureMoveThere(insultedPlayer, xx, fields.get(xx), gamefield))) {
									// move (check) can be blocked
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This function checks if the insulted player can attack the attacker of his
	 * king.
	 * 
	 * @param kingX          the kingX position of the insulted king
	 * @param kingY          the kingY position of the insulted king
	 * @param insultedPlayer the player object referring to the insulted player
	 * @param gamefield      2d-int array representing the current gamefield
	 *                       situation
	 * @return true, if the insulted player can attack the king attacker & false if
	 *         not
	 */
	private static boolean canAttackKingAttacker(int kingX, int kingY, Player insultedPlayer, Player opponent,
			int[][] gamefield) {
		int attackerX = 0;
		int attackerY = 0;

		// 1st we need to get the attacker
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (isPlayerField(x, y, gamefield, opponent)) {
					if (tryMove(x, y, kingX, kingY, gamefield, opponent)) {
						// safe attacker
						attackerX = x;
						attackerY = y;
						// exit loop
						y = 9;
					}
				}
			}
		}

		// no attacker
		if (attackerX == 0 || attackerY == 0)
			return false;

		// after that we check if there is a figure that can attack the attacker
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (isPlayerField(x, y, gamefield, insultedPlayer))
					if (tryMove(x, y, attackerX, attackerY, gamefield, insultedPlayer))
						return true;
			}
		}

		return false;
	}

	/**
	 * Function to indicate weather this is the referred players field or not.
	 * 
	 * @param posX      the position x to be checked
	 * @param posY      the position y to be checked
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * @param player    the corresponding player to be checked
	 * @return true if this is a field of the referred player, false if not
	 */
	public static boolean isPlayerField(int posX, int posY, int[][] gamefield, Player player) {
		int fieldVal = 0;
		if (posX > 8 || posX < 0 || posY > 8 || posY < 0)
			return false;

		if (player == null)
			return false;

		fieldVal = gamefield[posX][posY];

		return ((fieldVal > 0 && fieldVal < 7 && player.getId() == 1) || (fieldVal > 6 && player.getId() == 2));

	}

	/**
	 * Tries to collect all the fields a figure needs to pass in order to reach the
	 * goal.</br>
	 * This should never be called without first checking if the figure can actually
	 * reach the attacked position.</br>
	 * 
	 * @param x         the field X, where the attacker is situated
	 * @param y         the field Y, where the attacker is situated
	 * @param kingX     the field X, the figure tries to attack
	 * @param kingY     the field Y, the figure tries to attack
	 * @param player    player object referring to the player, that would make the
	 *                  move
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * 
	 * @return Map<x,y> representing each field the figure has to pass
	 * 
	 * @author MAD
	 * @author iFD
	 */
	private static Map<Integer, Integer> getFieldsOfMovement(int x, int y, int kingX, int kingY, Player player,
			int[][] gamefield) {
		switch (gamefield[x][y]) {
		case 2:
		case 8:
			return Rook.fieldsOfMovement(x, y, kingX, kingY, player, gamefield);
		case 4:
		case 10:
			return Bishop.fieldsOfMovement(x, y, kingX, kingY, gamefield);
		case 5:
		case 11:
			return Queen.fieldsOfMovement(x, y, kingX, kingY, player, gamefield);
		default:
			return null;
		}
	}

	/**
	 * Prepares the int[][] array representing the field of the game
	 */
	public static int[][] createGamefield() {
		int[][] gamefield = new int[10][10];

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
			}
		}

		// return
		return gamefield;
	}

	public static boolean checkGamefieldValidity(int[][] gamefield) {
		ArrayList<Integer> player1 = new ArrayList<>();
		ArrayList<Integer> player2 = new ArrayList<>();

		// get figures
		for (int y = 1; y < 9; y++) {
			for (int x = 1; x < 9; x++) {
				if (gamefield[x][y] != 0) {
					if (gamefield[x][y] > 6)
						player2.add((gamefield[x][y] - 6));
					else
						player1.add(gamefield[x][y]);
				}
			}
		}

		// all figures or just king, anything else aint valid
		if (player1.size() > 8 || player2.size() > 8 || player1.isEmpty() || player2.isEmpty())
			return false;

		if (verifyQuantity(player1))
			return verifyQuantity(player2);

		return false;
	}

	private static boolean verifyQuantity(ArrayList<Integer> list) {
		int pawns = 0;
		int king = 0;
		int queen = 0;
		int bishops = 0;
		int rooks = 0;
		int knights = 0;

		for (Integer i : list) {
			switch (i) {
			case 1:
				pawns++;
				break;
			case 2:
				rooks++;
				break;
			case 3:
				knights++;
				break;
			case 4:
				bishops++;
				break;
			case 5:
				queen++;
				break;
			case 6:
				king++;
				break;
			default:
				return false;
			}
		}

		return Boolean.FALSE
				.equals((king > 1 || king == 0 || queen > 1 || bishops > 2 || knights > 2 || rooks > 2 || pawns > 8));
	}

	/**
	 * 
	 * @param id
	 * @param player1 instance of player referring to player1
	 * @param player2 instance of player referring to player2
	 * @return corresponding player object
	 */
	private static Player getOpponentOnId(int id, Player player1, Player player2) {
		if (id == 1)
			return player2;
		if (id == 2)
			return player1;
		return null;
	}

	/**
	 * Function to return the index of a button with given game_field coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int giveIndex(int x, int y) {
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
	public static int[] giveXY(int idx) {
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

	public static String getPrintBar() {
		return BAR;
	}

//====================================================================================================
//==																								==	
//==Helper Functions for Local Network:																==
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
	public static String generateMoveMsg(int oldX, int oldY, int newX, int newY, boolean check, boolean checkmate) {
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

		if (check)
			message += "01";
		else
			message += "00";
		if (checkmate)
			message += "01";
		else
			message += "00";
		LOGGER.info("Generated move-message: [{}] ({})", message, "|oldX|oldY|newX|newY|check|checkmate|");
		return message;
	}

	/**
	 * Methods: </br>
	 * 1 - exchange player names</br>
	 * 2 - exchange figure moves</br>
	 * 3 - exchange restart request/response</br>
	 * 4 - exchange surrender </br>
	 * 5 - exchange start game information (game start and client left) </br>
	 * 
	 * @param method 01 or 10
	 * 
	 * @return Description for Method
	 */
	public static String getMethodDescription(String method) {
		if (method == null)
			return "";
		if (method.isBlank() || method.length() > 2)
			return "";

		String first = method.substring(0, 1);
		String second = method.substring(1);
		String returnStr = "";

		if (first.contentEquals("0")) {
			returnStr = "From server:";
			first = second;
		} else if (second.contentEquals("0"))
			returnStr = "From client:";
		else
			return "";

		switch (first) {
		case "1":
			returnStr += " sharing player name, initial game start";
			break;
		case "2":
			returnStr += " incoming move on gamefield";
			break;
		case "3":
			returnStr += " restart request/ response";
			break;
		case "4":
			returnStr += " incoming surrender message";
			break;
		case "5":
			returnStr += " player left/ game start (after restart) information";
			break;
		default:
			return "no information on method prefix";
		}
		return returnStr;
	}

}
