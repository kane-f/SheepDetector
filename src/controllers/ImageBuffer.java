/**
 * Class that handles modifications and calculations done to the image, including the detection
 * of black/white pixels and counting sheep.
 */

package controllers;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.METHOD;

import java.util.ArrayList;

public class ImageBuffer {
	private ImageView cache;
	private boolean[][] position;
	private int[] arraySet;
	//Initialising values to zero to work with the first sheep.
	private int sheepCount = 0, median = 0, uQuartile = 0, lQuartile = 0;
	
	//Constructor that parses the cached image.
	public ImageBuffer(ImageView cache) {
		this.cache = cache;
	}
	
	/* Main function that scans an image of sheep. An enumerator passed as an argument
	 * determines what parts of the function do. */
	public void parseImage(ImageView iv, METHOD method) {
		//Pulling unmodified image from cache, and performing actions if an image is returned.
		Image image = cache.getImage();
		if(image != null) {
			
			//Converts a JafaVX image to a buffered image.
			BufferedImage bi = SwingFXUtils.fromFXImage(image,null);
			
			//Initialises the pixels to work with if the program DETECTs.
			if(method == METHOD.DETECT||method == METHOD.DRAW)
				position = new boolean[bi.getWidth()][bi.getHeight()];
			
			//Checking each pixel for a specified colour.
			for(int y = 0; y < bi.getHeight(); y++)
			for(int x = 0; x < bi.getWidth(); x++) {
				int rgb = bi.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;
				
				//If the program DETECTs/DRAWs, add it to a matrix as "true" if it matches.
				if(method == METHOD.DETECT||method == METHOD.DRAW)
					position[x][y] = checkColor(r,g,b);
				
				/* If the program is converting the image, render detected pixels as white,
				 * and everything else as black */
				else bi.setRGB(x,y,setColor(r,g,b));
			}
			//Counts the sheep from the detected pixels if the program is DETECTing.
			if(method == METHOD.DETECT||method == METHOD.DRAW) sheepCount = countSheep();
			//Additionally, find the sheep and draw borders around them if DRAWing.
			if(method == METHOD.DRAW) drawBorders(bi);
			
			//Converts the image back and places it into the image view argument.
			image = SwingFXUtils.toFXImage(bi,null);
			iv.setImage(image);
		}
	}
		
	//Determining the tolerance for pixels containing sheep to be detected as true.
	private boolean checkColor(int r, int g, int b) {
		return (r>160||g>255||b>160) ? true : false;
	}
	
	//Determining the pixels containing sheep to be coloured white, otherwise black.
	private int setColor(int r, int g, int b) {
		return (r>160||g>255||b>160) ? 0xFFFFFF : 0x000000; 
	}
	
	/* Converts the matrix of booleans into an array that uses numerical values to refer
	 * to a parent index, indicating a set of detected pixels representing a sheep.  */
	private int[] populateArray() {
		arraySet = new int[position.length*position[0].length];
		
		//Loop iterating over the matrix.
		for(int y = 0; y < position[0].length; y++)
		for(int x = 0; x < position.length; x++) {
			
			//If this value is false, set its relevant array position to zero.
			if(!position[x][y]) arraySet[x+(position.length*y)] = 0;
			else {
				
				//If this value is true, sets its position to the index number.
				if(position[x][y]) arraySet[x+(position.length*y)] = x+(position.length*y);
				
				//Function that overrides this value if others nearby point to an index.
				for(int j=0;j<=1;j++)
				for(int i=0;i<=1;i++) {
					
					//Preventing array index out of bounds exceptions.
					if(x-i != -1 && y-j != -1)
					//Sets the index to this value if the adjacent matrix position is true.
					if(position[x-i][y-j])
						arraySet[x+(position.length*y)] = arraySet[(x-i)+(position.length*(y-j))];
				}
				
				//Preventing array index out of bounds exceptions.
				if(y-1 != -1 && x+1 < position.length)
				
				/* An algorithm that detects if any immediately adjacent indices are already
				 * set to a value. If they are, replace all of these values with a found
				 * value.
				 * 
				 * Example: Before and after the algorithm, a set looks like this:
				 * 
				 * 01 00 00 04 00 | 04 00 00 04 00
				 * 01 00 00 04 04 | 04 00 00 04 04
				 * 01 00 00 04 04 | 04 00 00 04 04
				 * 01 01 01 04 04 | 04 04 04 04 04
				 * 01 01 01 01 04 | 04 04 04 04 04 */
				
				//Detects if the matrix value above to the right is true.
				if(position[x+1][y-1]
					&& arraySet[x+(position.length*y)]
					!= arraySet[(x+1)+(position.length*(y-1))]) {
					//Replaces the first value.
					arraySet[x+(position.length*y)] = arraySet[(x+1)+(position.length*(y-1))];
					
					int i = 0, j = 0;
					//Continues to replace values as long as they are true on the matrix.
					//The first loop replaces horizontal values.
					while(position[x-i][y]) {
						arraySet[(x-i)+(position.length*y)]
								= arraySet[x+(position.length*y)];
						i++;
						/* If the loop iterator causes the x value to become negative when 
						 * subtracted, break out to prevent index out of bounds exceptions. */
						if(i > x) break;
						
						//An inner loop replaces vertical values while replacing the others.
						while(position[x-i][y-j]) {
							arraySet[(x-i)+(position.length*(y-j))]
									= arraySet[x+(position.length*y)];
							j++;
							//Same break as the other loop, for the same reasons.
							if (j > y) break;
						}
						//Reset the inner loop for the next time.
						j = 0;
						
					}
				}
			}
		}
		
		/* Another loop that passes over these set values, counts them, and sets the total
		 * at the origin of the set as a negative number. */
		int i;
		for(int z=0;z<arraySet.length;z++) {
			i = z;
			
			//Designates this value to count the elements if the index is equal to the value.
			if(arraySet[i] == i) {
				arraySet[i] = -1;
			}
			
			//Otherwise, find the original index and its value, and add it to the total.
			else {
					i = arraySet[i];
					arraySet[i]--;
			}
		}
		
		arraySet[0] = 0;
		//Returns the amount of sheep found as an integer.
		return arraySet;
	}
	
	//Function that draws a border around detected sheep.
	private void drawBorders(BufferedImage bi) {
		//Loads an array of values to work with, as a data set.
		arraySet = populateArray();
		for(int y = 0; y < bi.getHeight(); y++)
		for(int x = 0; x < bi.getWidth(); x++) {
			//Filtering out noise
			if(arraySet[x+(bi.getWidth()*y)] < -3) {
				//Sets minimums and maximums at detected origin pixel at start.
				int minX = x, minY = y, maxX = x, maxY = y;
				
				//Sub-loop that iterates over set values.
				for(int i = 0; i < arraySet.length; i++) {
					//Sets temporary x and y values to check.
					int tmpX = x, tmpY = y;
					/* If the value at index i traces back to the original x and y, set the
					 * temporary x and y values to a position converted from i. */
					if(arraySet[i] == x+(bi.getWidth()*y)) {
						tmpX = i%bi.getWidth();
						tmpY = i/bi.getWidth();
					}
					/* If these x/y values are less than or greater than the previously defined
					 * minimums/maximums, set the minimums/maximums to them. */
					if(tmpX < minX) minX = tmpX;
					if(tmpX > maxX) maxX = tmpX;
					if(tmpY < minY) minY = tmpY;
					if(tmpY > maxY) maxY = tmpY;
				}
				
				//The function that draws the borders.
				for(int u = minX; u <= maxX; u++)
				for(int v = minY; v <= maxY; v++) {
					//Left border
					bi.setRGB(minX,v,borderCheck(x+(bi.getWidth()*y)));
					//Right border
					bi.setRGB(maxX,v,borderCheck(x+(bi.getWidth()*y)));
					//Top border
					bi.setRGB(u,minY,borderCheck(x+(bi.getWidth()*y)));
					//Bottom border
					bi.setRGB(u,maxY,borderCheck(x+(bi.getWidth()*y)));
				}
			}
		}
	}
	
	/* Checks the size of this particular set, to indicate if it is a cluster and draw blue
	 * borders, or a single sheep and draw red borders. (does not work with PNGs) */
	private int borderCheck(int pos) {
		return arraySet[pos] <= median*2 ? 0x0000FF : 0xFF0000;
	}
	
	//Counts the sheep in this new array, based on negative total values set in populateArray().
	private int countSheep() {
		arraySet = populateArray();
		int number = 0;
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int z = 0; z < arraySet.length; z++) {
			//If an element counter is found and meets a minimum value, count this as a sheep.
			if(arraySet[z] < -3) {
				values.add(arraySet[z]);
				median = arraySet[z];
			}
			
			if(values.size() >= 2) {
				//Insertion sort method to find median value.
				for(int e=0; e<values.size(); e++) {
					int element = values.get(e), i;
				
					for(i=e; i >= 1 && values.get(i-1) > element; i--)
						values.set(i, values.get(i-1));
				
					values.set(i, element);
				}
			
				//Sets median and interquartile range.
				median = values.get(values.size()/2);
				uQuartile = values.get((values.size()/4)*3);
				lQuartile = values.get(values.size()/4);
			}
		}
		
		//Iterates over totals to indicate whether this counts as a sheep or not
		for(int z=0;z<values.size();z++) {
			//If an element counter is found and meets a minimum value, count this as a sheep.
			if(values.get(z) <= median+getIqRange()*2) {
				number++;
				//If it exceeds this value, count it as two sheep.
				if(values.get(z) < median*2) number++;
			}
			System.out.println(values.get(z));
		}
		System.out.println(median);
		System.out.println(getIqRange());
		//Returns the amount of sheep found as an integer.
		return number;
	}
	
	//Getters for specific values
	public int getSheepCount() {
		return sheepCount;
	}
	
	public int getMedian() {
		return median;
	}
	
	public int getIqRange() {
		return uQuartile - lQuartile;
	}
	
	public int getUpperQuartile() {
		return uQuartile;
	}
	
	public int getLowerQuartile() {
		return lQuartile;
	}
	
	//Scans over array of booleans for true-flagged pixels and accumulates them.
	public int getWhites() {
		if(position == null) return 0;
		int total = 0;
		for(int y = 0; y < position[0].length; y++)
		for(int x = 0; x < position.length; x++) {
			if(position[x][y]) total++;
		}
		return total;
	}
}