/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import de.ifd.mad.SimpleChess.players.Player;
import javafx.scene.image.Image;

/**
 * King (Koenig) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class King {
	private Image white = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king2.png").toString());
	private Image black = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString());

	// Constructor
	public King() {

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

	// Getters and Setters
	public Image getWhite() {
		return white;
	}

	public Image getBlack() {
		return black;
	}
}
