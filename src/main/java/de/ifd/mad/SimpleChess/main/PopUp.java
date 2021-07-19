/* 
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */
package de.ifd.mad.SimpleChess.main;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Class for popUp's
 * 
 * @author MAD
 * @author iFD
 */
public class PopUp {
	/** the corresponding stage */
	private Stage popUp;

	/** boolean to indicate whether the user decided for yes or no */
	private boolean userDecision = false;

	private String[] players;

	/* Style for our buttons */
	private final String buttonStyle = "-fx-background-color: #090a0c,\r\n"
			+ "		linear-gradient(#38424b 0.0%, #1f2429 20.0%, #191d22 100.0%),\r\n"
			+ "		linear-gradient(#20262b, #191d22),\r\n"
			+ "		radial-gradient(center 50.0% 0.0%, radius 100.0%, rgba(114.0, 131.0, 148.0, 0.9),\r\n"
			+ "		rgba(255.0, 255.0, 255.0, 0.0));\r\n" + "	-fx-background-radius: 5.0, 4.0, 3.0, 5.0;\r\n"
			+ "	-fx-background-insets: 0.0, 1.0, 2.0, 0.0;\r\n" + "	-fx-text-fill: white;\r\n"
			+ "	-fx-effect: dropshadow(three-pass-box, rgba(0.0, 0.0, 0.0, 0.6), 5.0, 0.0, 0.0, 1.0);\r\n"
			+ "	-fx-font-family: \"Berlin Sans FB\";\r\n" + "	-fx-text-fill: linear-gradient(white, #d0d0d0);\r\n"
			+ "	-fx-font-size: 13.0px;\r\n" + "	-fx-padding: 1.0 1.0 1.0 1.0;\r\n"
			+ "	-fx-text-effect: dropshadow(one-pass-box, rgba(0.0, 0.0, 0.0, 0.9), 1.0, 0.0, 0.0,\r\n" + "		1.0);";

	private final String buttonHoverStyle = "-fx-background-color: #323743,\r\n"
			+ "		radial-gradient(center 50.0% 0.0%, radius 100.0%, rgba(114.0, 131.0, 148.0, 0.9),\r\n"
			+ "		rgba(255.0, 255.0, 255.0, 0.0));";

	/**
	 * Constructor
	 */
	public PopUp() {

	}

	/**
	 * Construction method for winning popUp
	 */
	public void createWinningPopUp(String playerName) {
		createPopUp(true, false, false, playerName, null);
	}

	/**
	 * Construction method for yes no decision popUp
	 */
	public void createDecisionPopUp(String question) {
		createPopUp(false, false, true, question, null);
	}

	/**
	 * Construction method for player name input popUp
	 * 
	 * @param player1 promptName for Player1
	 * @param player2 promptName for Player2
	 */
	public void createInputPopUp(String player1, String player2) {
		players = new String[2];
		createPopUp(false, true, false, player1, player2);
	}

	/**
	 * Construction method for info popUp
	 */
	public void createInfoPopUp(String info) {
		createPopUp(false, false, false, info, null);
	}

	/**
	 * Open the popUp and returns true or false for if the user had to make a yes no
	 * decision
	 * 
	 * @return boolean true (yes) or false (no)
	 */
	public boolean showPopUp() {
		popUp.showAndWait();
		return userDecision;
	}

	public String[] showInputPopUp() {
		popUp.showAndWait();
		return players;
	}

	/**
	 * Function to create a popUp
	 * 
	 * @param win   popUp displays the winner
	 * @param input popUp asks for player names
	 * @param yesno popUp asks for decision (yes-no)
	 * @param info  info to be displayed in popUp
	 * @param info2
	 */
	private void createPopUp(boolean win, boolean input, boolean yesno, String info, String info2) {
		popUp = new Stage();
		popUp.initModality(Modality.APPLICATION_MODAL);
		popUp.setMinHeight(250);
		popUp.setMinWidth(300);
		popUp.getIcons().add(new Image(getClass().getResource("/de/ifd/mad/SimpleChess/images/king1.png").toString()));
		Label label = new Label();
		label.setFont(new Font("Berlin Sans FB", 20));
		label.setTextAlignment(TextAlignment.CENTER);
		label.setStyle("-fx-text-fill: linear-gradient(to top, #ffcc00, #fbff02);");
		VBox vBox = new VBox();
		//////////
		// popUp displays the winner
		if (win) {
			popUp.setTitle("Game Over!");
			label.setText("Player " + info + " won!\nThank you for playing, have a nice day!");
			vBox.getChildren().add(label);

			// popUp asks for player names
		} else if (input) {
			label.setText("Enter Name:");
			TextField text1 = new TextField();
			text1.setPromptText(info);
			text1.setFont(label.getFont());

			TextField text2 = new TextField();
			if (!info2.isBlank()) {
				Label label2 = new Label("Enter Name:");
				label2.setStyle(label.getStyle());
				label2.setFont(label.getFont());

				text2.setPromptText(info2);
				text2.setFont(label.getFont());
				vBox.getChildren().addAll(label2, text2);
			}

			Button button = new Button("Proceed");
			button.setPrefSize(80, 40);
			button.setStyle(buttonStyle);

			// animate button on mouse event
			button.setOnMouseEntered(event -> {
				button.setStyle(buttonHoverStyle);
			});
			button.setOnMouseExited(event -> {
				button.setStyle(buttonStyle);
			});

			// click event
			button.setOnMouseClicked(e -> {
				players[0] = text1.getText().trim();
				if (text1.getText().isBlank())
					players[0] = text1.getPromptText().trim();
				if (!info2.isBlank()) {
					players[1] = text2.getText().trim();
					if (text2.getText().isBlank())
						players[1] = text2.getPromptText().trim();
				}

				popUp.close();

			});
			vBox.getChildren().addAll(label, text1, button);

			// popUp asks for decision (yes-no)
		} else if (yesno) {
			label.setText(info);

			Button yes = new Button("YES");
			yes.setPrefSize(50, 30);
			yes.setStyle(buttonStyle);

			// animate button on mouse event
			yes.setOnMouseEntered(event -> {
				yes.setStyle(buttonHoverStyle);
			});
			yes.setOnMouseExited(event -> {
				yes.setStyle(buttonStyle);
			});

			// click event
			yes.setOnMouseClicked(e -> {
				userDecision = true;
				popUp.close();
			});

			Button no = new Button("NO");
			no.setPrefSize(50, 30);
			no.setStyle(buttonStyle);

			// animate button on mouse event
			no.setOnMouseEntered(event -> {
				no.setStyle(buttonHoverStyle);
			});
			no.setOnMouseExited(event -> {
				no.setStyle(buttonStyle);
			});

			// click event
			no.setOnMouseClicked(e -> {
				userDecision = false;
				popUp.close();
			});

			vBox.getChildren().addAll(label, yes, no);

			// popUp just provides information
		} else {
			vBox.getChildren().add(label);
			popUp.setTitle("Attention");
			label.setText("" + info);
		}
		/////////
		vBox.setStyle(
				"-fx-background-color: radial-gradient(center 50.0% 50.0%, radius 100.0%, #242424, #434343, #898989);");
		vBox.setAlignment(Pos.CENTER);
		vBox.setSpacing(5);
		Scene scene = new Scene(vBox);
		popUp.setScene(scene);
	}

}
