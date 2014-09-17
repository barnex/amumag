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
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.io.Message;
import static java.lang.Math.*;

/**
 *
 */
public final class FaceKernel extends KernelModule{

    private final int iterationDepth;			// depth for integration over triangles
    private final Recycler<Triangle> triangles;
    
    
    public FaceKernel(Mesh mesh, int iterationDepth){
	super(mesh);
	triangles = new Recycler<Triangle>(new Triangle[20]);
	this.iterationDepth = iterationDepth;
    }
    
    @Override
    public void init(){
	super.init();
        Message.indent("accuracy: \t" + iterationDepth + " (" + (int)pow(4, iterationDepth) + " points)");
	Message.debug("FaceKernel triangles: " + triangles.getStats());
      
    }
    
    protected void integrate(Element element, Vector buffer){
	    buffer.set(0, 0, 0);
	    
            // split face into 4 
	    Vector[] vertex = element.faceVertex;
	    double area = element.area;
	    if(area != 0.0){		
		// todo: move this to geommodule or so.
		Vector center = GeomModule.barycentrumPlane(vertex);
		for(int i=0; i<4; i++){
		    Triangle t = triangles.get();
		    t.set(vertex[i], vertex[(i+1)%4], center);
		    integrate(t, area, iterationDepth);
		    buffer.add(t.buffer);
		    triangles.recycle(t);
		}
	    }
	    else 
		buffer.set(0, 0, 0);
    }
    
    private void integrate(Triangle t, double faceArea, int level){
	if(level > 0){
	    t.split(triangles);
	    for(int i=0; i<t.split.length; i++){
                Triangle tsplitI = t.split[i];
		integrate(tsplitI, faceArea, level-1);
		//t.buffer.add(tsplitI.buffer); inlined:
		t.buffer.x += tsplitI.buffer.x;
                t.buffer.y += tsplitI.buffer.y;
                t.buffer.z += tsplitI.buffer.z;
                triangles.recycle(tsplitI);
	    }
	}
	else{
	    integrate(t, faceArea);
	}
    }
    
    private static final double MINUS4PI = -4.0*PI;
    
    private void integrate(Triangle t, double faceArea){
	Vector buffer = t.buffer;
        
	//buffer.set(t.center); inlined:
        buffer.x = t.center.x;
        buffer.y = t.center.y;
        buffer.z = t.center.z;
        
	/*if(buffer.isNaN())
	    throw new Bug();*/
        
	/*buffer.divide(pow(buffer.norm2(), 3.0 / 2.0)); 
        buffer.multiply(t.area/faceArea);
	buffer.divide(-4*PI); inlined: */
        double factor = (t.area)/(pow(buffer.norm2(), 1.5)*MINUS4PI*faceArea);
        buffer.x *= factor;
        buffer.y *= factor;
        buffer.z *= factor;
        
	/*if(buffer.isNaN())
	    throw new Bug();*/
    }
}
