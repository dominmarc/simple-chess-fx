/*
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.helpers;

import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Class for loading all the necessary images.</br>
 * Please make use of the class' getters in order to use the images.
 *
 * @author MAD
 */
public class ImageProvider extends FileProvider {
    static final ChessLogger LOGGER = ChessLogger.createLogger(ImageProvider.class);

    public static ImageProvider provider = new ImageProvider();

    /**
     * Constructor</br>
     * Loads all the necessary files.
     */
    private ImageProvider() {
        super();
    }

    /**
     * Instance getter of {@link ImageProvider}.
     */
    public static ImageProvider get() {
        return provider;
    }

    public void loadImages() throws FileLoadingException {
        LOGGER.info("Loading game images...");
        super.loadFiles();

        for (Path file : super.getFiles()) {
            try {
                new Image(file.toUri().toURL().toString());
            } catch (Exception e) {
                throw new FileLoadingException("Image not loadable, can not construct image object!", e);
            }
        }

        LOGGER.info("Successfully loaded game images!");
    }

    public Path getImage(final String name) throws FileNotFoundException {
        return super.getFile(name);
    }

    public Image getAsImage(final String name) {
        try {
            Path file = getImage(name);
            return new Image(file.toUri().toURL().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
