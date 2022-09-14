/*
 * Copyright (c) 2021 iFD GmbH Chemnitz http://www.ifd-gmbh.com
 */
package de.ifd.mad.SimpleChess.helpers;

import java.util.*;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Class for popUp's
 *
 * @author MAD
 * @author iFD
 */
public class PopUpProvider {
    /**
     * the corresponding stage
     */
    private Stage popUp;

    /**
     * Container to store all visual elements
     */
    private Pane container;

    /**
     * boolean to indicate whether the user decided for yes or no
     */
    private boolean userDecision = false;

    private boolean active;

    private final LinkedList<Object> buffer = new LinkedList<>();

    /* Style for our buttons */
    private static final String buttonStyle = "-fx-background-color: #090a0c,\r\n"
            + "linear-gradient(#38424b 0.0%, #1f2429 20.0%, #191d22 100.0%),\r\n"
            + "linear-gradient(#20262b, #191d22),\r\n"
            + "radial-gradient(center 50.0% 0.0%, radius 100.0%, rgba(114.0, 131.0, 148.0, 0.9),\r\n"
            + "rgba(255.0, 255.0, 255.0, 0.0));\r\n" + "-fx-background-radius: 5.0, 4.0, 3.0, 5.0;\r\n"
            + "-fx-background-insets: 0.0, 1.0, 2.0, 0.0;\r\n" + "-fx-text-fill: white;\r\n"
            + "-fx-effect: dropshadow(three-pass-box, rgba(0.0, 0.0, 0.0, 0.6), 5.0, 0.0, 0.0, 1.0);\r\n"
            + "-fx-font-family: \"Berlin Sans FB\";\r\n" + "-fx-text-fill: linear-gradient(white, #d0d0d0);\r\n"
            + "-fx-font-size: 13.0px;\r\n" + "-fx-padding: 1.0 1.0 1.0 1.0;\r\n"
            + "-fx-text-effect: dropshadow(one-pass-box, rgba(0.0, 0.0, 0.0, 0.9), 1.0, 0.0, 0.0, 1.0);";

    private static final String buttonHoverStyle = "-fx-background-color: #323743,\r\n"
            + "radial-gradient(center 50.0% 0.0%, radius 100.0%, rgba(114.0, 131.0, 148.0, 0.9),rgba(255.0, 255.0, 255.0, 0.0));";

    private static final String backgroundStyle = "-fx-background-color: radial-gradient(center 50.0% 50.0%, radius 100.0%, #242424, #434343, #898989);";

    private static final Font BERLIN = new Font("Berlin Sans FB", 20);

    private static final String labelStyle = "-fx-text-fill: linear-gradient(to top, #ffcc00, #fbff02);";

    /**
     * Constructor
     */
    private PopUpProvider() {
        this.active = false;
    }

    /**
     * Add elements here to make them standard for every pop-up.
     *
     * @param title of the window
     */
    private static PopUpProvider preparePopUp(final String title) {
        PopUpProvider provider = new PopUpProvider();
        provider.createPopUp(title);
        provider.container = getContainer(5, Pos.CENTER);
        return provider;
    }

    /**
     * Construction method for yes no decision popUp
     */
    public static PopUpProvider createDecisionPopUp(String question) {
        PopUpProvider provider = preparePopUp("Decide!");

        Label label = new Label();
        label.setFont(BERLIN);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setStyle(labelStyle);
        label.setText(question);

        Button yes = new Button("YES");
        yes.setPrefSize(50, 30);
        yes.setStyle(buttonStyle);

        // animate button on mouse event
        yes.setOnMouseEntered(event -> yes.setStyle(buttonHoverStyle));
        yes.setOnMouseExited(event -> yes.setStyle(buttonStyle));

        // click event
        yes.setOnMouseClicked(e -> {
            provider.userDecision = true;
            provider.popUp.close();
        });

        Button no = new Button("NO");
        no.setPrefSize(50, 30);
        no.setStyle(buttonStyle);

        // animate button on mouse event
        no.setOnMouseEntered(event -> no.setStyle(buttonHoverStyle));
        no.setOnMouseExited(event -> no.setStyle(buttonStyle));

        // click event
        no.setOnMouseClicked(e -> {
            provider.userDecision = false;
            provider.closePopUp();
        });

        provider.container.getChildren().addAll(label, yes, no);
        provider.popUp.setScene(new Scene(provider.container));
        return provider;
    }

    /**
     * Construction method for input fields.
     *
     * @param inputs List of String-Pair<Title, PromptText> -> use null for Title to avoid Label, use null for PromptText to avoid TextField
     * @return instantiated {@link PopUpProvider}
     */
    public static PopUpProvider createInputPopUp(final LinkedList<Pair<String, String>> inputs) {
        PopUpProvider provider = preparePopUp("Input");
        final Collection<Node> items = getInputItems(inputs);
        provider.container.getChildren().addAll(items);

        Button button = new Button("Proceed");
        button.setPrefSize(80, 40);
        button.setStyle(buttonStyle);

        // animate button on mouse event
        button.setOnMouseEntered(event -> button.setStyle(buttonHoverStyle));
        button.setOnMouseExited(event -> button.setStyle(buttonStyle));

        // click event
        button.setOnMouseClicked(e -> {
            for (Node n : items) {
                if (n instanceof TextField) {
                    TextField field = (TextField) n;
                    String text = field.getText();
                    provider.buffer.add(text == null || text.isEmpty() ? null : text.trim());
                }
            }
            provider.closePopUp();
        });
        provider.container.getChildren().add(button);

        provider.popUp.setScene(new Scene(provider.container));
        return provider;
    }

    private static Pane getContainer(final int spacing, final Pos align) {
        VBox container = new VBox();
        container.setSpacing(spacing);
        container.setAlignment(align);
        container.setStyle(backgroundStyle);
        return container;
    }

    /**
     * Getter for input - dialog - elements.
     *
     * @param inputs List of String-Pair<Title, PromptText> -> use null for Title to avoid Label, use null for PromptText to avoid TextField
     * @return Collection of javaFx elements ({@link Node})
     */
    private static Collection<Node> getInputItems(final LinkedList<Pair<String, String>> inputs) {
        LinkedList<Node> items = new LinkedList<>();
        for (Pair<String, String> inputField : inputs) {
            if (inputField.getKey() != null) {
                Label label = new Label(inputField.getKey());
                label.setStyle(labelStyle);
                label.setFont(BERLIN);
                items.add(label);
            }
            if (inputField.getValue() != null) {
                TextField text = new TextField();
                text.setPromptText(inputField.getValue());
                text.setFont(BERLIN);
                items.add(text);
            }
        }

        return items;
    }

    /**
     * Construction method for info popUp
     */
    public static PopUpProvider createInfoPopUp(String info) {
        PopUpProvider provider = preparePopUp("Attention");

        Label label = new Label();
        label.setFont(BERLIN);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setStyle(labelStyle);
        label.setText(info);

        provider.container.getChildren().add(label);
        provider.popUp.setScene(new Scene(provider.container));
        return provider;
    }

    /**
     * Construction method for info popUp
     */
    public static PopUpProvider createPopUp(final String title, final String info) {
        PopUpProvider provider = preparePopUp(title);

        Label label = new Label();
        label.setFont(BERLIN);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setStyle(labelStyle);
        label.setText(info);

        provider.container.getChildren().add(label);
        provider.popUp.setScene(new Scene(provider.container));
        return provider;
    }

    /**
     * Open the popUp and returns true or false for if the user had to make a yes no
     * decision
     *
     * @return boolean true (yes) or false (no)
     */
    public boolean showPopUp() {
        this.active = true;
        popUp.setOnHidden(event -> {
            this.active = false;
        });
        popUp.showAndWait();
        return userDecision;
    }

    /**
     * Opens the popUp (does not "freeze" the gui)
     */
    public void showNonWaitingPopUp() {
        popUp.show();
    }

    /**
     * Displays an input-pop-up.
     *
     * @param number of expected text-fields
     * @return
     */
    public List<String> showInputPopUp(final int number) {
        popUp.showAndWait();
        List<String> list = new LinkedList<>();
        for (Object obj : buffer) {
            if (obj == null) {
                list.add(null);
            }
            if (obj instanceof String) {
                list.add((String) obj);
            }
        }
        buffer.clear();

        while (list.size() < number) {
            list.add(null);
        }
        return list;
    }

    /**
     * Closes the referenced open popUp
     */
    public void closePopUp() {
        this.active = false;
        popUp.close();
    }

    /**
     * Function to create a fresh popUp with no items.
     */
    private void createPopUp(final String title) {
        popUp = new Stage();
        popUp.initModality(Modality.APPLICATION_MODAL);
        popUp.setMinHeight(250);
        popUp.setMinWidth(300);
        popUp.getIcons().add(ImageProvider.getKingBlack());
        popUp.setTitle(title);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
