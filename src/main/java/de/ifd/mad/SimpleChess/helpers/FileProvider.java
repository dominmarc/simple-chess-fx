package de.ifd.mad.SimpleChess.helpers;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for loading all the necessary game files, such as fxml documents or css
 * stylesheets.</br>
 *
 * @author MAD
 */
public class FileProvider {
    /**
     * represents the status of the file provider
     */
    private boolean loaded;

    static final ChessLogger LOGGER = ChessLogger.createLogger(FileProvider.class);

    public static final FileProvider provider = new FileProvider();

    private final Map<String, Path> files;

    /**
     * Constructor</br>
     * Loads all the necessary files.
     */
    protected FileProvider() {
        files = new HashMap<>();
        loaded = false;
    }

    /**
     * Instance getter of {@link FileProvider}.
     */
    public static FileProvider get() {
        return provider;
    }

    /**
     * Adds a file to {@link FileProvider}.<br/>
     * Make sure file is on resource folder!<br/>
     * E.g. /com/company/name/project/yourFile.txt
     *
     * @param name unique identifier for file/ path
     * @param path relative to resource folder
     * @throws URISyntaxException    for invalid URI
     * @throws FileNotFoundException for invalid path
     * @throws InvalidKeyException   for duplicate/ invalid name
     */
    public void addFile(final String name, final String path) throws URISyntaxException, FileNotFoundException, InvalidKeyException {
        if (files.containsKey(name)) {
            throw new InvalidKeyException("Name of the file already exists!");
        }
        URL url = FileProvider.class.getResource(path);
        if (url == null) {
            throw new FileNotFoundException("File with path: [" + path + "] cannot be found!");
        }
        Path file = Paths.get(url.toURI());

        if (Files.exists(file)) {
            files.put(name, file);
        } else {
            throw new FileNotFoundException("Could not find path: [" + file + "]!");
        }
    }

    /**
     * Gets a file based on the {@link FileProvider} name.
     *
     * @param name of the file
     * @return the file or null
     */
    public Path getFile(final String name) throws FileNotFoundException {
        Path file = files.get(name);
        if (file == null) {
            throw new FileNotFoundException("FileProvider has no such file! [" + name + "]");
        }
        return file;
    }

    /**
     * Getter for the name of a file.
     *
     * @param path to the file
     * @return the ({@link FileProvider}) name of the file or null
     */
    public String getName(final Path path) {
        for (var entry : files.entrySet()) {
            if (entry.getValue().equals(path)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return all loaded files
     */
    public List<Path> getFiles() {
        ArrayList<Path> fileList = new ArrayList<>();
        for (var file : files.entrySet()) {
            fileList.add(file.getValue());
        }
        return fileList;
    }

    /**
     * @return names of all loaded files
     */
    public List<String> getNames() {
        return new ArrayList<>(files.keySet());
    }

    /**
     * Indicates weather all files are loaded or not.
     *
     * @return true, if files are loaded and false, if not
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Tries to load all the given files.</br>
     * (Checks for their existence.)
     */
    public void loadFiles() throws FileLoadingException {
        LOGGER.info("Loading game files...");

        for (String name : files.keySet()) {
            final Path file = files.get(name);
            if (!load(file)) {
                loaded = false;
                throw new FileLoadingException(file.getFileName().toString());
            }
        }

        LOGGER.info("Successfully loaded game files!");
        loaded = true;
    }

    /**
     * Method to indicate whether the file is able to load.</br>
     */
    private boolean load(Path path) {
        try {
            return Files.exists(path);
        } catch (SecurityException e) {
            return false;
        }
    }
}
