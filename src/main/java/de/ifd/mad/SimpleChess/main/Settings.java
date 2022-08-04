package de.ifd.mad.SimpleChess.main;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class Settings {

    public static int PLAYER_1_ID = 1;
    public static int PLAYER_2_ID = 2;

    public static String PLAYER_1_DEFAULT = "Player 1";
    public static String PLAYER_2_DEFAULT = "Player 2";
    public static Color PLAYER_1_COLOR = Color.DARKBLUE;
    public static Color PLAYER_2_COLOR = Color.DARKRED;

    public static String PLAYER_1_SELECTION_COLOR = "-fx-border-color: #2AB4FF;";
    public static String PLAYER_2_SELECTION_COLOR = "-fx-border-color: #FE2B2B;";

    public static String SELECTION_BORDER_WIDTH = "-fx-border-width: 4px;";


    public static final Background BLACK = new Background(new BackgroundFill(Color.BLACK, null, new Insets(0)));
    public static final Background WHITE = new Background(new BackgroundFill(Color.WHITE, null, new Insets(0)));
    public static int START_FIGURES = 12;

    public static int PLAYER_1_QUEEN = PLAYER_1_ID + 2;
    public static int PLAYER_2_QUEEN = PLAYER_2_ID + 2;

    public static int GAME_FIELD_WIDTH = 10;
    public static int GAME_FIELD_HEIGHT = 10;
}
