module SimpleChess {
	exports de.ifdgmbh.mad.SimpleChess.main;
	exports de.ifdgmbh.mad.SimpleChess.controller;
	
	requires transitive javafx.graphics;
	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	
	opens de.ifdgmbh.mad.SimpleChess.controller;
}