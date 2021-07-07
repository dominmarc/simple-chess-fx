module SimpleChess {
	exports de.ifd.mad.SimpleChess.main;
	exports de.ifd.mad.SimpleChess.controller;
	
	requires transitive javafx.graphics;
	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	
	opens de.ifd.mad.SimpleChess.controller;
	opens de.ifd.mad.SimpleChess.main;
}