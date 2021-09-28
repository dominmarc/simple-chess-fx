/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import java.util.HashMap;
import java.util.Map;

import de.ifd.mad.SimpleChess.players.Player;
import javafx.scene.image.Image;

/**
 * Bishop (Springer) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Bishop {
	private Image white = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/springer2.png").toString());
	private Image black = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/springer1.png").toString());

	// Constructor
	public Bishop() {

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
			if (counter <= 0) {
				return true;
			}
		}

		return false;
	}

	public Map<Integer, Integer> fieldsOfMovement(int oldX, int oldY, int newX, int newY, int[][] gamefield) {
		Map<Integer, Integer> fields = new HashMap<>();

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
					else
						fields.put(oldX - k, oldY - k);
				}
			}
			// up, right
			if (newY < oldY && newX > oldX) {
				for (int k = 1; k < temp1; k++) {
					if (gamefield[oldX + k][oldY - k] > 0)
						counter++;
					else
						fields.put(oldX + k, oldY - k);
				}
			}
			// down, left
			if (newY > oldY && newX < oldX) {
				for (int k = 1; k < temp1; k++) {
					if (gamefield[oldX - k][oldY + k] > 0)
						counter++;
					else
						fields.put(oldX - k, oldY + k);
				}
			}
			// down, right
			if (newY > oldY && newX > oldX) {
				for (int k = 1; k < temp1; k++) {
					if (gamefield[oldX + k][oldY + k] > 0)
						counter++;
					else
						fields.put(oldX + k, oldY + k);
				}
			}
			if (counter <= 0) {
				return fields;
			}
		}

		return null;
	}

	// Getters and Setters
	public Image getWhite() {
		return white;
	}

	public Image getBlack() {
		return black;
	}
}
