/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.players;

import java.util.Optional;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * Player class for simple chess
 * 
 * @author MAD
 * @author iFD
 */
public class Player {

	private Background red = new Background(new BackgroundFill(Color.RED, null, null));
	private Background green = new Background(new BackgroundFill(Color.GREEN, null, null));
	private String name;
	private boolean active;
	private boolean ready;
	private final int id;
	private final String selection;
	static final String nonSelection = "-fx-border-color: #000000; -fx-border-width: 0px;";

	/**
	 * Constructor
	 * 
	 * @param id   can be 1 or to depending on the player
	 * @param name is optional, in case there is no name --> use player + id
	 */
	public Player(int id, final String name) {
		this.id = id;
		if (id <= 0 || id > 2)
			id = 1;

		this.name = name;

		if (id == 1)
			this.selection = "-fx-border-color: #2AB4FF; -fx-border-width: 4px;";
		else
			this.selection = "-fx-border-color: #FE2B2B; -fx-border-width: 4px;";

		this.active = false;
		this.ready = false;
	}

	/**
	 * Switches the player to active or non-active
	 */
	public void switchStatus() {
		this.active = !this.active;
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

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
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

	public String getSelection() {
		return selection;
	}

	public static String getNonSelection() {
		return nonSelection;
	}
}
