/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import de.ifd.mad.SimpleChess.players.Player;

/**
 * Pawn (Bauer) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Pawn {
	// Constructor
	private Pawn() {

	}

	/**
	 * Returns true or false, depending on if the figure can move to the given
	 * coordinates or not
	 * 
	 * @param oldX      figures old x position
	 * @param oldY      figures old y position
	 * @param newX      figures new x position
	 * @param newY      figures new y position
	 * @param player    the currently active player
	 * @param gamefield int[][] array representing the field of the game
	 * @return
	 */
	public static boolean tryMove(int oldX, int oldY, int newX, int newY, Player player, int[][] gamefield) {
		if (oldX == newX) {
			// zu weit?
			int temp = oldY - newY;
			if (temp < 0)
				temp *= (-1);
			if (temp > 2) {
				return false;
			}
			if (oldY < newY && player.getId() == 2) {
				// player 2 tries to move backwards (down)
				return false;
			} else if (oldY > newY && player.getId() == 1) {
				// player 1 tries to move backwards (up)
				return false;
			}
			if (gamefield[newX][newY] == 0) {
				return true;
			}
		} else {
			// Sprung schraeg nach vorn (rechts, links)
			if (player.getId() == 1) {
				if ((newX == oldX - 1 || newX == oldX + 1) && newY == oldY + 1) {
					// valid distance & direction
					if (gamefield[newX][newY] > 6) {
						return true;
					}
				}
			} else if (player.getId() == 2) {
				if ((newX == oldX - 1 || newX == oldX + 1) && newY + 1 == oldY) {
					// valid distance & direction
					if (gamefield[newX][newY] < 7 && gamefield[newX][newY] > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
