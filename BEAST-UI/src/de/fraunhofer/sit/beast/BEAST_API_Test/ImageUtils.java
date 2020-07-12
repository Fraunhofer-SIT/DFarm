package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class ImageUtils {
	public static Rectangle drawImageScaled(Image image, Component canvas, Graphics g) {
	    int w = image.getWidth(null);
	    int h = image.getHeight(null);
	     
	    double aspectRatio = (double) h / w;
	
	    int canvasWidth = canvas.getWidth();
	    int canvasHeight = canvas.getHeight();
	     
	    double canvasAspect = (double) canvasHeight / canvasWidth;
	
	    int x1, y1, x2, y2;
	     
	    if (w < canvasWidth && h < canvasHeight) {
	        // the image is smaller than the canvas
	        x1 = (canvasWidth - w)  / 2;
	        y1 = (canvasHeight - h) / 2;
	        x2 = w + x1;
	        y2 = h + y1;
	         
	    } else {
	        if (canvasAspect > aspectRatio) {
	        	x1 = 0;
	            y1 = canvasHeight;
	            canvasHeight = (int) (canvasWidth * aspectRatio);
	            y1 = (y1 - canvasHeight) / 2;
	        } else {
	            x1 = canvasWidth;
	            canvasWidth = (int) (canvasHeight / aspectRatio);
	            y1 = 0;
	            x1 = (x1 - canvasWidth) / 2;
	        }
	        x2 = canvasWidth + x1;
	        y2 = canvasHeight + y1;
	    }
	
	    g.drawImage(image, x1, y1, x2, y2, 0, 0, w, h, null);
	    Rectangle rectOut = new Rectangle();
	    rectOut.x = x1;
	    rectOut.y = y1;
	    rectOut.width = x2 - x1;
	    rectOut.height = y2 - y1;
	    return rectOut;
	}
}

