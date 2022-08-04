/*
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.players;

import de.ifd.mad.SimpleChess.main.Settings;
import javafx.scene.layout.Background;

/**
 * Player class for simple chess
 *
 * @author MAD
 * @author iFD
 */
public class Player {
    private final String name;
    private final String shortName;
    private boolean active;
    private boolean ready;
    private final int id;
    private final String selection;

    /**
     * Constructor
     *
     * @param id   can be 1 or to depending on the player
     * @param name is optional, in case there is no name --> use player + id
     */
    public Player(int id, final String name) {
        this.id = id;
        if (id != Settings.PLAYER_1_ID && id != Settings.PLAYER_2_ID)
            throw new RuntimeException("Invalid Player-ID: " + id);

        this.name = name;
        this.shortName = name;

        this.selection = (id == Settings.PLAYER_1_ID ? (Settings.PLAYER_1_SELECTION_COLOR) : (Settings.PLAYER_2_SELECTION_COLOR)) + Settings.SELECTION_BORDER_WIDTH;

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
     * @return Background color
     */
    public Background getStatusBackground() {
        return this.active ? Settings.GREEN : Settings.RED;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
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
}
