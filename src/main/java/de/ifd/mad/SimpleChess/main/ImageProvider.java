package de.ifd.mad.SimpleChess.main;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;

public class ImageProvider {
	/** player 1, pawn image */
	private static Path pawn1;
	/** player 2, pawn image */
	private static Path pawn2;
	/** player 1, queen image */
	private static Path queen1;
	/** player 2, queen image */
	private static Path queen2;
	/** player 1, king image */
	private static Path king1;
	/** player 2, king image */
	private static Path king2;
	/** player 1, knight image */
	private static Path knight1;
	/** player 2, knight image */
	private static Path knight2;
	/** player 1, bishop image */
	private static Path bishop1;
	/** player 2, bishop image */
	private static Path bishop2;
	/** player 1, rook image */
	private static Path rook1;
	/** player 2, rook image */
	private static Path rook2;

	/** represents the status of the image provider */
	private static boolean loaded;

	static final Logger LOGGER = LoggerFactory.getLogger(ImageProvider.class);

	/**
	 * Constructor</br>
	 * Loads all the necessary files.
	 */
	private ImageProvider() {
	}

	/**
	 * Indicates weather all files are loaded or not.
	 * 
	 * @return true, if files are loaded and false, if not
	 */
	public static boolean isLoaded() {
		return loaded;
	}

	/**
	 * Tries to load all the given files.</br>
	 * (Checks for their existence.)
	 */
	public static void loadFiles() throws FileLoadingException, URISyntaxException {
		LOGGER.info("Loading game image files...");

		// reference all files here:
		pawn1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/bauer1.png").toURI());
		pawn2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/bauer2.png").toURI());

		queen1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/dame1.png").toURI());
		queen2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/dame2.png").toURI());

		king1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/king1.png").toURI());
		king2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/king2.png").toURI());

		knight1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/pferd1.png").toURI());
		knight2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/pferd2.png").toURI());

		bishop1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/springer1.png").toURI());
		bishop2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/springer2.png").toURI());

		rook1 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/turm1.png").toURI());
		rook2 = Paths.get(ImageProvider.class.getResource("/de/ifd/mad/SimpleChess/images/turm2.png").toURI());

		// add all files here:
		ArrayList<Path> files = new ArrayList<>();
		files.add(pawn1);
		files.add(pawn2);
		files.add(queen1);
		files.add(queen2);
		files.add(king1);
		files.add(king2);
		files.add(knight1);
		files.add(knight2);
		files.add(bishop1);
		files.add(bishop2);
		files.add(rook1);
		files.add(rook2);

		for (Path p : files)
			if (!load(p)) {
				loaded = false;
				throw new FileLoadingException(p.getFileName().toString());
			}

		LOGGER.info("Successfully loaded game image files!");
		loaded = true;
	}

	/**
	 * Method to load the files</br>
	 * 
	 * @return
	 */
	private static boolean load(Path path) {
		return Files.exists(path);
	}

	public static Image getRookBlack() {
		try {
			return new Image(rook1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getRookWhite() {
		try {
			return new Image(rook2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getPawnBlack() {
		try {
			return new Image(pawn1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getPawnWhite() {
		try {
			return new Image(pawn2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getQueenBlack() {
		try {
			return new Image(queen1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getQueenWhite() {
		try {
			return new Image(queen2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getKingBlack() {
		try {
			return new Image(king1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getKingWhite() {
		try {
			return new Image(king2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getBishopBlack() {
		try {
			return new Image(bishop1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getBishopWhite() {
		try {
			return new Image(bishop2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getKnightBlack() {
		try {
			return new Image(knight1.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static Image getKnightWhite() {
		try {
			return new Image(knight2.toUri().toURL().toString());
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}
}
