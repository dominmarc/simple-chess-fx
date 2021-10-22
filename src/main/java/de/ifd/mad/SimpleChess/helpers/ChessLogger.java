/* 
 * Copyright (c) 2021 iFD  Chemnitz http://www.ifd-.com
 */
package de.ifd.mad.SimpleChess.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import de.ifd.mad.SimpleChess.main.FileLoadingException;

/**
 * Custom logger, due to non-modularized slf4j and logback libraries.
 * 
 * @author MAD
 */
public class ChessLogger {

	/** Logger instance */
	private static final ChessLogger logger = new ChessLogger();

	/** checks validity of logger */
	private static boolean loaded;

	/** name of the class */
	private static String className;

	/** path to log file */
	private static Path file;

	/** file name */
	static final String FILE_NAME = "ChessLog";
	/** file type */
	static final String FILE_TYPE = ".log";
	/** the string to replace with the specified strings */
	static final String REPLACE_STR = "\\{\\}";

	/**
	 * Enumeration of all log levels.
	 * 
	 * @author MAD
	 */
	public enum LEVEL {
		ERROR {
			public String toString() {
				return "ERROR";
			}
		},
		INFO {
			public String toString() {
				return "INFO";
			}
		},
		WARN {
			public String toString() {
				return "WARN";
			}
		}
	}

	/**
	 * Constructor
	 */
	public static ChessLogger getLogger(Class<?> clazz) {
		className = clazz.getSimpleName();
		return logger;
	}

	/**
	 * Loads the log file.</br>
	 * If not found, creates a file with directory.
	 * 
	 * @throws FileLoadingException gets thrown on IOException when accessing a
	 *                              file.
	 */
	public static void load() throws FileLoadingException {

		// make sure file to write to exists
		String temp = System.getProperty("user.home") + "\\Documents\\SimpleChessFiles\\Log\\" + FILE_NAME + FILE_TYPE;
		file = Paths.get(temp);

		if (!Files.exists(file)) {
			try {
				Files.createDirectories(file.getParent());
				Path myTmpFile = Files.createFile(file);
				file = myTmpFile;
			} catch (IOException e) {
				loaded = false;
				throw new FileLoadingException(FILE_NAME);
			}
		}

		loaded = true;
	}

	/**
	 * Log a message at the INFO level.
	 * 
	 * @param s the message string to be logged
	 */
	public void info(String s) {
		log(LEVEL.INFO, className, s);
	}

	public void info(String s, Object... strings) {
		for (int i = 0; i < strings.length; i++)
			s = s.replaceFirst(REPLACE_STR, strings[i].toString());

		log(LEVEL.INFO, className, s);
	}

	/**
	 * Log a message at the WARN level.
	 * 
	 * @param s the message string to be logged
	 */
	public void warn(String s) {
		log(LEVEL.WARN, className, s);
	}

	public void warn(String s, Object... strings) {
		for (int i = 0; i < strings.length; i++)
			s = s.replaceFirst(REPLACE_STR, strings[i].toString());

		log(LEVEL.WARN, className, s);
	}

	/**
	 * Log a message at the ERROR level.
	 * 
	 * @param s the message string to be logged
	 */
	public void error(String s) {
		log(LEVEL.ERROR, className, s);
	}

	public void error(String s, Object... strings) {
		for (int i = 0; i < strings.length; i++)
			s = s.replaceFirst(REPLACE_STR, strings[i].toString());

		log(LEVEL.ERROR, className, s);
	}

	private static void log(LEVEL type, String origin, String msg) {
		if (!loaded)
			return;

		StringBuilder line = new StringBuilder();

		// start with date
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
		line.append(formatter.format(Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).getTime()));

		// add log level
		line.append(centerString(11, " - " + type + ": "));

		// add class name
		line.append(centerString(25, origin));

		// add message and CRLF
		line.append(" - " + msg + "\n");

		System.out.println(line);

		BufferedWriter buffW = null;
		try {
			buffW = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
			buffW.write(line.toString());
			buffW.close();
		} catch (IOException e) {
			if (buffW != null)
				try {
					buffW.close();
				} catch (IOException e1) {
					// nothing
				}
			return;
		}
	}

	/**
	 * Centers a string.
	 * 
	 * @param width of the overall string
	 * @param s     text displayed in the middle of the string
	 * 
	 * @return final formatted string
	 */
	private static String centerString(int width, String s) {
		return String.format("%-" + width + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
	}

}
