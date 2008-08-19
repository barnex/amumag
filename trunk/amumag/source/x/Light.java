/*
 *  This file is part of amumag,
 *  a finite-element micromagnetic simulation program.
 *  Copyright (C) 2006-2008 Arne Vansteenkiste
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details (licence.txt).
 */

package x;

import amu.geom.Vector;
import java.awt.Color;
/**
 *
 */
public final class Light{

    private Color color;
    private double contrast;
    private Vector light;
     
    public Light(Vector light, Color color, double contrast){
	this.light = light;
	this.color = color;
	this.contrast = contrast;
    }
    
    public void colorize(Poly poly) {
	Vector center = new Vector();
	for (Vector v : poly.vertex) {
	    center.add(v);
	}
	center.divide(-4.0);
	center.add(light);
	center.normalizeSafe();
	double inprod = (center.dot(poly.normal));
	
	int red = color.getRed();
	int green = color.getGreen();
	int blue = color.getBlue();
	int alpha = color.getAlpha();
	
	poly.color = (new Color(light(red, inprod), light(green, inprod), light(blue, inprod), alpha));
        poly.lineColor = poly.color;
    }
    
    private int light(int color, double inprod){
         if(inprod > 1)
             inprod = 1;
         else if(inprod < -1)
             inprod = -1;
	 double l = 0.5 * (inprod+1); //tussen 0 en 1;
	 double stay = (1-contrast) * color;
	 double remain = (contrast) * l * color;
	 return (int)(stay + remain);
    }
}
