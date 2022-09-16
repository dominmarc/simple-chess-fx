package de.ifd.mad.SimpleChess.helpers;

import java.io.Serial;

public class FileLoadingException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileLoadingException(final String fileName) {
        super("FileLoadingException: " + fileName);
    }

    public FileLoadingException(final String fileName, final Exception e) {
        super("FileLoadingException: " + fileName + " with " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}
