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

package amu.geom; 

import amu.debug.Bug;
import amu.mag.Cell;
import amu.mag.Face;
import static java.lang.Math.*;

/**
 * Module with general geometric functions.
 */
public final class GeomModule{

    
    /**
     * Returns the gemetrical center of a cell.
     */
    public static Vector barycentrumVolume(Cell cell){
	Vector a, b, c;
	
	Vector[] vertex = cell.vertex;
	Vector o = new Vector();
	for(int i=0; i<vertex.length; i++)
	    o.add(vertex[i]);
	o.divide(vertex.length);

	Vector center = new Vector();
	double weight = 0.0;
	
	for(Face face: cell.faces){
	    for(int i=0; i<2; i++){
		a = face.vertex[(0+i*2)%4];
		b = face.vertex[(1+i*2)%4];
		c = face.vertex[(2+i*2)%4];
		
		double volume = volume(o, a, b, c);
		center.add(volume, a);
		center.add(volume, b);
		center.add(volume, c);
		center.add(volume, o);
		weight += volume;
	    }
	}
	
	center.divide(4.0 * weight);
	//if(center.isNaN())
	    //throw new Bug();
	//if(o.isNaN())
	    //throw new Bug();
        assert !center.isNaN();
        assert !o.isNaN();
        
	// 2007-08-30: barycenter sometimes returns a point out of the cell, which gives total nonsense for the fields
	// for now, let's use the average of the vertices, which is safe and seems quite accurate.
	return o;		// TODO: should be: return center, but that is not correct if o does not lie inside the cube.
    }
    
    /**
     * Returns the geometrical center of a face.
     * OK for a plane figure, not for a volume: gives NaN. 
     *  TODO: if area == 0.0: gives NaN, not nice: but keep this, faces should not have zero area.
     */
    public static Vector barycentrumPlane(Vector[] vertex){
	if(vertex.length < 3)
	    throw new Bug();
	else if(vertex.length == 3){
	    Vector center = new Vector(vertex[0]);
	    center.add(vertex[1]);
	    center.add(vertex[2]);
	    center.multiply(1.0/3.0);
	    assert !center.isNaN();
	    return center;
	}
	else{
	    double weight = 0.0;
	    Vector center = new Vector();
	    for (int i = 0; i < vertex.length - 2; i++) {
		// the weight of each vertex of a sub triangle: 1/3 of the area of the triangle
		double area = area(vertex[0], vertex[i + 1], vertex[i + 2]);
		center.add(area, vertex[0]);
		center.add(area, vertex[i + 1]);
		center.add(area, vertex[i + 2]);
		weight += area;
	    }
	    center.divide(3.0*weight);
	    if(weight == 0.0){
		// hmmm, so the area is zero, let's check this for sure.
		int numDifferentVertices = 0;
		/*for(int i=1; i<vertex.length; i++)
		    if(!vertex[0].equals(vertex[i]))
			numDifferentVertices++;
		if(numDifferentVertices < 2) //with <2 different vertices, the area is zero.
		    return new Vector(vertex[0]);
		else
		    throw new Bug();*/
		return new Vector(vertex[0]);
	    }	
	    assert !center.isNaN();
	    return center;
	}
    }
    
    /**
     * Returns the area of the triangle abc.
     * @param a
     * @param b
     * @param c
     */
    public static double area(Vector a, Vector b, Vector c){
	double x1 = b.x-a.x;
	double y1 = b.y-a.y;
	double z1 = b.z-a.z;
	
	double x2 = c.x-a.x;
	double y2 = c.y-a.y;
	double z2 = c.z-a.z;
	
	double xcross =  y1*z2 - y2*z1;
	double ycross = -x1*z2 + x2*z1;
	double zcross =  x1*y2 - x2*y1;
	
	double area = sqrt(xcross*xcross + ycross*ycross + zcross*zcross) * 0.5; //2007-08-29: forgot * 0.5
	if(Double.isNaN(area))
	    throw new Bug();
	return area;
    }
    
    /**
     * Returns the area of the triangle described by the vertex list.
     * @param vertex, length should be 3
     * @return area.
     */
    public static double area(Vector[] vertex){
	/*if(vertex.length != 3)
	    throw new Bug();*/
        assert vertex.length == 3;
	return area(vertex[0], vertex[1], vertex[2]);
    }
    
    /**
     * Calculates the volume of the tetrahedron oabc.
     * @param o
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static double volume(Vector o, Vector a, Vector b, Vector c){
	double ax = a.x - o.x;
	double ay = a.y - o.y;
	double az = a.z - o.z;
	
	double bx = b.x - o.x;
	double by = b.y - o.y;
	double bz = b.z - o.z;
	
	double cx = c.x - o.x;
	double cy = c.y - o.y;
	double cz = c.z - o.z;
	
	double det = ax * (by*cz-bz*cy) + ay * (bz*cx-bx-cz) + az * (bx*cy-by*cz);
	assert !Double.isNaN(det);
	return det / 6.0;
    }
}
