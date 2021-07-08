/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import de.ifd.mad.SimpleChess.players.Player;
import javafx.scene.image.Image;

/**
 * Queen (Dame) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Queen {
	private Image white = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/dame2.png").toString());
	private Image black = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/dame1.png").toString());

	// Constructor
	public Queen() {

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
	 * @param rook      Bishop object to call its movement method (as queen also
	 *                  moves like that)
	 * @param bishop    Bishop object to call its movement method (as queen also
	 *                  moves like that)
	 * @return
	 */
	public boolean tryMove(int oldX, int oldY, int newX, int newY, Player player, int[][] gamefield, Rook rook,
			Bishop bishop) {

		// rook movement
		if (newX == oldX || newY == oldY) {
			if (rook.tryMove(oldX, oldY, newX, newY, player, gamefield))
				return true;

			// bishop movement
		} else {
			if (bishop.tryMove(oldX, oldY, newX, newY, player, gamefield))
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
