package de.ifd.mad.SimpleChess.helpers;

public class FileLoadingException extends Exception {
	private static final long serialVersionUID = 1L;

	public FileLoadingException(String fileName) {
		super("Failed to load necessary game file: " + fileName + "...");
	}

}
