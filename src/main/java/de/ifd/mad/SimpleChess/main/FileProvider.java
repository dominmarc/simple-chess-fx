package de.ifd.mad.SimpleChess.main;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author MAD
 */
public final class FileProvider {
	/** Local network multiplayer */
	private static Path localNetworkGame;
	/** Local multiplayer */
	private static Path localGame;
	/** game style */
	private static Path gameStyle;
	/** game menu */
	private static Path gameMenu;
	/** game menu style */
	private static Path gameMenuStyle;

	/** represents the status of the file provider */
	private static boolean loaded;

	static final Logger LOGGER = LoggerFactory.getLogger(FileProvider.class);

	/**
	 * Constructor</br>
	 * Loads all the necessary files.
	 */
	private FileProvider() {
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
		LOGGER.info("Loading game files...");

		// reference all files here:
		localNetworkGame = Paths
				.get(FileProvider.class.getResource("/de/ifd/mad/SimpleChess/main/LocalNetworkChess.fxml").toURI());
		localGame = Paths.get(FileProvider.class.getResource("/de/ifd/mad/SimpleChess/main/LocalChess.fxml").toURI());
		gameStyle = Paths
				.get(FileProvider.class.getResource("/de/ifd/mad/SimpleChess/main/LocalStyleFile.css").toURI());
		gameMenu = Paths.get(FileProvider.class.getResource("/de/ifd/mad/SimpleChess/main/StartingForm.fxml").toURI());
		gameMenuStyle = Paths
				.get(FileProvider.class.getResource("/de/ifd/mad/SimpleChess/main/StartingFileStyle.css").toURI());

		// add all files here:
		ArrayList<Path> files = new ArrayList<>();
		files.add(localNetworkGame);
		files.add(localGame);
		files.add(gameStyle);
		files.add(gameMenu);
		files.add(gameMenuStyle);

		for (Path p : files)
			if (!load(p)) {
				loaded = false;
				throw new FileLoadingException(p.getFileName().toString());
			}

		LOGGER.info("Successfully loaded game files!");
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

	public static Path getGameMenu() {
		return gameMenu;
	}

	public static Path getLocalGame() {
		return localGame;
	}

	public static Path getGameStyle() {
		return gameStyle;
	}

	public static Path getNetworkGame() {
		return localNetworkGame;
	}

	public static Path getGameMenuStyle() {
		return gameMenuStyle;
	}

	public static URL getGameMenuStyleURL() {
		try {
			return gameMenuStyle.toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static URL getNetworkGameURL() {
		try {
			return localNetworkGame.toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static URL getGameMenuURL() {
		try {
			return gameMenu.toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static URL getLocalGameURL() {
		try {
			return localGame.toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public static URL getGameStyleURL() {
		try {
			return gameStyle.toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
			return null;
		}
	}
}
