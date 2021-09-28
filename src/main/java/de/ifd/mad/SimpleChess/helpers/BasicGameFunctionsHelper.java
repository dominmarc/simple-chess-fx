package de.ifd.mad.SimpleChess.helpers;

import de.ifd.mad.SimpleChess.figures.Bishop;
import de.ifd.mad.SimpleChess.figures.King;
import de.ifd.mad.SimpleChess.figures.Knight;
import de.ifd.mad.SimpleChess.figures.Pawn;
import de.ifd.mad.SimpleChess.figures.Queen;
import de.ifd.mad.SimpleChess.figures.Rook;
import de.ifd.mad.SimpleChess.main.PopUp;
import de.ifd.mad.SimpleChess.players.Player;

public class BasicGameFunctionsHelper {
	// initialize figure objects
	private static final Pawn PAWN = new Pawn();
	private static final Rook ROOK = new Rook();
	private static final Knight KNIGHT = new Knight();
	private static final Bishop BISHOP = new Bishop();
	private static final Queen QUEEN = new Queen();
	private static final King KING = new King();

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
	public static boolean tryMove(int oldX, int oldY, int newX, int newY, int[][] gamefield, Player player) {
		int type = gamefield[oldX][oldY];

		if (type > 6)
			type -= 6;

		switch (type) {
		/////////////////////////////////////////////////////////////////////////////
		case 1:
			// Bauer:
			if (PAWN.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		case 2:
			// Turm
			if (ROOK.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		case 3:
			// Pferd
			if (KNIGHT.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		case 4:
			// Springer
			if (BISHOP.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		case 5:
			// queen = rook oder bishop
			if (QUEEN.tryMove(oldX, oldY, newX, newY, player, gamefield, ROOK, BISHOP))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		case 6:
			// Koenig
			if (KING.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			break;

		/////////////////////////////////////////////////////////////////////////////
		default:
			PopUp info = new PopUp();
			info.createInfoPopUp("Figure-Selection-Error\nYou may restart the game!");
			info.showPopUp();
			return false;

		}
		return false;
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
	 * @param newX   figures new x position
	 * @param newY   figures new y position
	 * @param player object to identify the related player
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

}
