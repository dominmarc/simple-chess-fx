package de.ifd.mad.SimpleChess.main;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Class to provide the title bar with buttons
 * 
 * @author MAD
 */
public class TitleBarButtons extends HBox {
	Button closeButton;
	Button minimizeButton;

	/**
	 * Constructor
	 */
	public TitleBarButtons() {
		closeButton = new Button("X");
		minimizeButton = new Button("_");
		this.getChildren().addAll(closeButton, minimizeButton);
	}

	public void setCloseAction(Stage stage) {
		closeButton.setOnMouseClicked(e -> {
			stage.close();
		});
	}

	public void setMinimizeAction(Stage stage) {
		minimizeButton.setOnMouseClicked(e -> {
			stage.setIconified(true);
		});
	}

}
