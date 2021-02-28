/**
 * Test class that ensures functionality of processing the image and counting sheep.
 * 
 */

package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.METHOD;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controllers.ImageBuffer;

public class ImageBufferTest {
	Image image;
	ImageView iv;
	ImageView cache;
	ImageBuffer ib;
	
	/* Setting up the test by creating the two image views, loading a preset image into both,
	 * and creating the image buffer class. The DETECT enum value is used for this test to
	 * parse the image. */
	@Before
	public void setUp() {
		iv = new ImageView();
		try {
			image = new Image(new FileInputStream("img/108220783.jpg"));
		} catch(IOException ex) {
			Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
		}
		iv.setImage(image);
		cache = new ImageView();
		cache.setImage(image);
		ib = new ImageBuffer(cache);
		ib.parseImage(iv,METHOD.DETECT);
	}
	
	@After
	public void tearDown() {}
	
	//Tests the amount of sheep in the image.
	@Test
	public void testSheepNumber() {
		assertEquals(20,ib.getSheepCount());
	}
	
	//Tests median sheep size.
	@Test
	public void testMedian() {
		assertEquals(-65,ib.getMedian());
	}

	//Tests interquartile range of sheep size.
	@Test
	public void testIqRange() {
		assertEquals(19,ib.getIqRange());
	}
	
	//Tests which pixels were flagged for detection.
	@Test
	public void testDetectedPixels() {
		assertEquals(1398,ib.getWhites());
	}
}
