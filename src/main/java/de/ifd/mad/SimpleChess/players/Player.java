/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.players;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * Player1 class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Player {

	private Background red = new Background(new BackgroundFill(Color.RED, null, null));
	private Background green = new Background(new BackgroundFill(Color.GREEN, null, null));
	private String name;
	private boolean active;
	private int id;

	// Constructor
	public Player(int id, String name) {
		this.name = name;
		this.id = id;
		if (id <= 0 || id > 2)
			id = 1;
		if (name.isBlank())
			this.name = "Player" + id;
		this.active = false;
	}

	/**
	 * Switches the player to active or non-active
	 */
	public void switchStatus() {
		if (this.active)
			this.active = false;
		else
			this.active = true;
	}

	/**
	 * Function to get the appropriate status label background
	 * 
	 * @return
	 */
	public Background getStatusBackground() {
		if (this.active)
			return green;
		else
			return red;
	}

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getId() {
		return id;
	}
}
