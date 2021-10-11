package de.ifd.mad.SimpleChess.interfaces;

/**
 * Interface for all the fxml controllers.</br>
 * This ensures all the controller can accept a value from the previous
 * controller (@initVariable).
 * 
 * @author MAD
 *
 */
public interface IController {

	public void initVariable(String value);

	public void initialize();
}
