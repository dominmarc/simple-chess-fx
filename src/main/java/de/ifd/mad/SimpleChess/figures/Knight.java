/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import de.ifd.mad.SimpleChess.players.Player;
import javafx.scene.image.Image;

/**
 * Knight (Pferd) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Knight {
	private Image white = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/pferd2.png").toString());
	private Image black = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/pferd1.png").toString());

	// Constructors
	public Knight() {

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
	public boolean tryMove(int oldX, int oldY, int newX, int newY, Player player, int[][] gamefield) {
		if ((oldY + 1 == newY && oldX + 2 == newX) || (oldY + 2 == newY && oldX + 1 == newX)) {
			// down, right, right && down, down, right
			return true;

		} else if ((oldY + 1 == newY && oldX - 2 == newX) || (oldY + 2 == newY && oldX - 1 == newX)) {
			// down, left, left && down, down, left
			return true;

		} else if ((oldY - 1 == newY && oldX + 2 == newX) || (oldY - 2 == newY && oldX + 1 == newX)) {
			// up, right, right && up, up, right
			return true;

		} else if ((oldY - 1 == newY && oldX - 2 == newX) || (oldY - 2 == newY && oldX - 1 == newX)) {
			// up, left, left && up, up, left
			return true;

		}
		return false;
	}

	// Getters and Setters
	public Image getWhite() {
		return white;
	}

	public Image getBlack() {
		return black;
	}
}
