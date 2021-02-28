/**
 * Class that handles the opening of files from the system into the application.
 */

package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.Driver;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class FileStore {
	//Configuring the file chooser window.
	public static void configureFileChooser(final FileChooser fc) {
		fc.setTitle("Select an image.");
		fc.setInitialDirectory(new File(System.getProperty("user.home")));
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All images", "*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"),
				new FileChooser.ExtensionFilter("PNG", "*.png"));
	}
	
	//Opens a file from the file chooser.
	public Image openFile(File file) {
		try {
			Image image = new Image(new FileInputStream(file));
			return image;
		} catch(IOException ex) {
			Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

}
