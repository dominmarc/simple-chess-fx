module SimpleChess {
	exports de.ifd.mad.SimpleChess.main;
	exports de.ifd.mad.SimpleChess.controller;
	exports de.ifd.mad.SimpleChess.players;
	exports de.ifd.mad.SimpleChess.figures;
	
	requires transitive javafx.graphics;
	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	
	opens de.ifd.mad.SimpleChess.controller;
	opens de.ifd.mad.SimpleChess.main;
}