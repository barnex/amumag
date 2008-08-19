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

package amu.mag.kernel;

import amu.core.Recycler;
import amu.debug.Bug;
import amu.geom.GeomModule;
import amu.geom.Vector;

/**
 *
 */
public final class Triangle{
    
    private final Vector[] vertex;
    private final Vector[] side;
    public final Triangle[] split;
    public final Vector center;
    public double area;
    public final Vector buffer;
    
    public Triangle(){
	vertex = new Vector[3];
	side = new Vector[3];
	for(int i=0; i<3; i++){
	    vertex[i] = new Vector();
	    side[i] = new Vector();
	}
	split = new Triangle[4];
	center = new Vector();
	buffer = new Vector();
    }
    
    public void set(Vector a, Vector b, Vector c){
	reset();
	/*vertex[0].set(a);
	vertex[1].set(b);
	vertex[2].set(c);*/
        
        //inlined.
        vertex[0].x = a.x;
	vertex[0].y = a.y;
	vertex[0].z = a.z;
	vertex[1].x = b.x;
	vertex[1].y = b.y;
	vertex[1].z = b.z;
	vertex[2].x = c.x;
	vertex[2].y = c.y;
	vertex[2].z = c.z;
        
        for (int i = 0; i < vertex.length; i++) {
            center.add(vertex[i]);
        }
        
	center.divide(3.0);
	/*if(center.isNaN())
	    throw new Bug();*/
	area = GeomModule.area(vertex[0], vertex[1], vertex[2]);
     }
    
    public void split(Recycler<Triangle> recycler){
	// initiate centers of the sides
	// center C lies between side a and b.
	for(int i=0; i<3; i++){
	    side[(i+2)%3].set(vertex[i]);
	    side[(i+2)%3].add(vertex[(i+1)%3]);
	    side[(i+2)%3].multiply(0.5);
	}
	// construct 4 new triangles
	for(int i=0; i<4; i++){
	    split[i] = recycler.get();
	    split[i].reset();
	}
	
	for(int i=0; i<3; i++)
	    split[i].set(vertex[i], side[(i+1)%3], side[(i+2)%3]);
	split[3].set(side[0], side[1], side[2]);
    }
    
    private void reset(){
        //2008-02-20: inlined resets
	for(int i=0; i<3; i++){
	    vertex[i].x = 0.0;
            vertex[i].y = 0.0;
            vertex[i].z = 0.0;
            
	    side[i].x = 0.0;
            side[i].y = 0.0;
            side[i].z = 0.0;
	}
	for(int i=0; i<split.length; i++)
	    split[i] = null;
	center.x = 0.0;
        center.y = 0.0;
        center.z = 0.0;
	area = 0.0;
	buffer.x = 0.0;
        buffer.y = 0.0;
        buffer.z = 0.0;
    }
}
