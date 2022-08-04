package de.ifd.mad.SimpleChess.main;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class Settings {

    public static int PLAYER_1_ID = 1;
    public static int PLAYER_2_ID = 2;

    public static String PLAYER_1_DEFAULT = "Player " + PLAYER_1_ID;
    public static String PLAYER_2_DEFAULT = "Player " + PLAYER_2_ID;

    public static String PLAYER_1_SELECTION_COLOR = "-fx-border-color: #2AB4FF;";
    public static String PLAYER_2_SELECTION_COLOR = "-fx-border-color: #FE2B2B;";

    public static String NON_SELECTION_STYLE = "-fx-border-color: #000000; -fx-border-width: 0px;";
    public static String SELECTION_BORDER_WIDTH = "-fx-border-width: 4px;";


    public static final Background BLACK = new Background(new BackgroundFill(Color.BLACK, null, new Insets(0)));
    public static final Background WHITE = new Background(new BackgroundFill(Color.WHITE, null, new Insets(0)));
    public static final Background RED = new Background(new BackgroundFill(Color.RED, null, null));
    public static final Background GREEN = new Background(new BackgroundFill(Color.GREEN, null, null));

    public static int GAME_FIELD_WIDTH = 10;
    public static int GAME_FIELD_HEIGHT = 10;

    public static final String BUTTON_START_TEXT = "START GAME";
    public static final String BUTTON_RESTART_TEXT = "RE" + BUTTON_START_TEXT;
}
