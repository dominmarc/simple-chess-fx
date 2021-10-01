/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.figures;

import java.util.HashMap;
import java.util.Map;

import de.ifd.mad.SimpleChess.players.Player;
import javafx.scene.image.Image;

/**
 * Rook (Turm) class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Rook {
	private Image white = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/turm2.png").toString());
	private Image black = new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/turm1.png").toString());

	// Constructor
	public Rook() {

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
		int counter = 0;
		// vertical (y) movement
		if (newX == oldX) {
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
			if (counter <= 0) {
				// way is free for the figure to move
				return true;
			}

			// horizontal (x) movement
		} else if (newY == oldY) {
			if (player.getId() == 1) {
				for (int x = oldX + 1; x < newX; x++) {
					if (gamefield[x][newY] > 0) {
						counter++;
					}
				}
			} else if (player.getId() == 2) {
				for (int x = oldX - 1; x > newX; x--) {
					if (gamefield[x][newY] > 0) {
						counter++;
					}
				}
			}
			if (counter <= 0) {
				return true;
			}
		}

		// neither horizontal nor vertical or move violation
		return false;
	}

	/**
	 * Tries to collect all the fields a figure needs to pass in order to reach the
	 * goal.</br>
	 * This should never be called without first checking if the figure can actually
	 * reach the attacked position.</br>
	 * 
	 * @param oldX      the field X, where the attacker is situated
	 * @param oldY      the field Y, where the attacker is situated
	 * @param newX      the field X, the figure tries to attack
	 * @param newY      the field Y, the figure tries to attack
	 * @param player    player object referring to the player, that would make the
	 *                  move
	 * @param gamefield 2d-int array representing the current gamefield situation
	 * 
	 * @return Map<x,y> representing each field the figure has to pass
	 * 
	 * @author MAD
	 * @author iFD
	 */
	public Map<Integer, Integer> fieldsOfMovement(int oldX, int oldY, int newX, int newY, Player player,
			int[][] gamefield) {
		Map<Integer, Integer> fields = new HashMap<>();

		int counter = 0;
		// vertical (y) movement
		if (newX == oldX) {
			if (newY > oldY) {
				for (int y = oldY + 1; y < newY; y++) {
					if (gamefield[newX][y] > 0)
						counter++;
					else
						fields.put(newX, y);
				}
			} else if (newY < oldY) {
				for (int y = oldY - 1; y > newY; y--) {
					if (gamefield[newX][y] > 0)
						counter++;
					else
						fields.put(newX, y);
				}
			}
			if (counter <= 0) {
				// way is free for the figure to move
				return fields;
			}

			// horizontal (x) movement
		} else if (newY == oldY) {
			if (player.getId() == 1) {
				for (int x = oldX + 1; x < newX; x++) {
					if (gamefield[x][newY] > 0)
						counter++;
					else
						fields.put(x, newY);
				}
			} else if (player.getId() == 2) {
				for (int x = oldX - 1; x > newX; x--) {
					if (gamefield[x][newY] > 0)
						counter++;
					else
						fields.put(x, newY);
				}
			}
			if (counter <= 0) {
				return fields;
			}
		}

		// neither horizontal nor vertical or move violation
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
