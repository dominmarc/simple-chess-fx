/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import de.ifd.mad.SimpleChess.players.Player;

/**
 * King (Koenig) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class King {
	// Constructor
	private King() {

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
		// zu weit?
		int temp = oldY - newY;
		int temp2 = oldX - newX;
		if (temp2 < 0)
			temp2 *= (-1);
		if (temp < 0)
			temp *= (-1);

		if (temp > 1 || temp2 > 1) {
			return false;
		}

		return true;
	}
}
