/**
 * Main class of image viewing program, which handles most JavaFX functions.
 */

package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import controllers.FileStore;
import controllers.ImageBuffer;
import javafx.application.Application;
import javafx.stage.Stage;
import models.METHOD;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;

public class Driver extends Application {
	
	@Override
	public void start(Stage stage) {
		try {
			//Initialising JavaFX components.
			stage.setTitle("Image viewer");
			FileStore fs = new FileStore();
			FileChooser fc = new FileChooser();
			VBox vb = new VBox();
			Scene scene = new Scene(vb,100,100);
			
			ImageView iv = new ImageView();
			iv.setPreserveRatio(true);
	        iv.setSmooth(true);
	        iv.setCache(true);
	        
	        ImageView cache = new ImageView();
			cache.setPreserveRatio(true);
	        cache.setSmooth(true);
	        cache.setCache(true);
	        
	        ImageBuffer ib = new ImageBuffer(cache);
			
	        Label label = new Label();
	        label.setWrapText(true);
	        
	        //Menu functions and listeners
	        MenuBar menuBar = new MenuBar();
			
			Menu menuFile = new Menu("File");
			
			//Launches a file chooser to open an image to display.
			MenuItem open = new MenuItem("Open image...");
			open.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					FileStore.configureFileChooser(fc);
					File file = fc.showOpenDialog(stage);
					if (file != null) {
						iv.setImage(fs.openFile(file));
						cache.setImage(fs.openFile(file));
						stage.setTitle(file.getName());
						stage.setWidth(fs.openFile(file).getWidth()+16);
						stage.setHeight(fs.openFile(file).getHeight()+64+label.getHeight());
					}
				}
			});
			
			//Resets the image.
			MenuItem reset = new MenuItem("Reset image");
			reset.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					iv.setImage(cache.getImage());
				}
			});
			
			//Exits the program.
			MenuItem exit = new MenuItem("Quit");
			exit.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					System.exit(0);
				}
			});
			
			//Image editing functions
			Menu menuEffect = new Menu("Analyse");
			
			//Counts the sheep in the image.
			MenuItem count = new MenuItem("Count sheep in image");
			count.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					ib.parseImage(iv,METHOD.DETECT);
				    label.setText("There are " + ib.getSheepCount()
				    						   + " sheep in this image.");
				}
			});
			
			//Shows which pixels were detected.
			MenuItem bw = new MenuItem("View detected sheep as black/white");
			bw.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					ib.parseImage(iv,METHOD.BWHITE);
					label.setText("");
				}
			});
			
			//Draws rectangles around detected sheep
			MenuItem rect = new MenuItem("View detected sheep as selections");
			rect.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t) {
					ib.parseImage(iv,METHOD.DRAW);
					label.setText("");
				}
			});
	        
			//Key events
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				public void handle(KeyEvent t) {
					Image image = null;
					//Load debug image and count sheep.
					if(t.getCode() == KeyCode.CONTROL) {
						try {
							image = new Image(new FileInputStream("img/debug.jpg"));
						} catch(IOException ex) {
							Logger.getLogger(
									Driver.class.getName()).log(Level.SEVERE, null, ex);
						}
						iv.setImage(image);
						cache.setImage(image);
					}
					//Open image
					if(t.getCode() == KeyCode.O) {
						FileStore.configureFileChooser(fc);
						File file = fc.showOpenDialog(stage);
						if (file != null) {
							iv.setImage(fs.openFile(file));
							cache.setImage(fs.openFile(file));
							stage.setTitle(file.getName());
							stage.setWidth(fs.openFile(file).getWidth()+16);
							stage.setHeight(fs.openFile(file).getHeight()+64+label.getHeight());
						}
					}
					//Quit
					if(t.getCode() == KeyCode.ESCAPE) System.exit(0);
					//Count sheep
					if(t.getCode() == KeyCode.C) {
						ib.parseImage(iv,METHOD.DETECT);
						label.setText("There are " + ib.getSheepCount()
				    						   + " sheep in this image.");
					}
					//Draw borders
					if(t.getCode() == KeyCode.D) {
						ib.parseImage(iv,METHOD.DRAW);
						label.setText("");
					}
					//View black/white
					if(t.getCode() == KeyCode.G) {
						ib.parseImage(iv,METHOD.BWHITE);
						label.setText("");
					}
					//Reset image
					if(t.getCode() == KeyCode.R) {
						iv.setImage(cache.getImage());
					}
				}
			});
			
			//Adding JavaFX elements to stage.
			menuFile.getItems().addAll(open,reset,exit);
			menuEffect.getItems().addAll(count,bw,rect);
			
			menuBar.getMenus().addAll(menuFile,menuEffect);
			
			vb.getChildren().addAll(menuBar,iv,label);
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
