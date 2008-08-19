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

import amu.geom.Mesh;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import amu.mag.Simulation;
import java.util.ArrayList;
import static java.lang.Math.*;

public abstract class FailsafeKernelModule {
    
    protected Simulation sim;
    private final Face[] faceType = new Face[0];
    //private final Vector[] vectorType = new Vector[0];
    
    public FailsafeKernelModule(Simulation sim){
	this.sim = sim;
    }
    
    public void init(){
	
	Mesh mesh = sim.mesh;
	    
	for(Cell cell = mesh.coarseRoot; cell != null; cell = cell.next){
	    
            ArrayList<Face> nearBuffer = new ArrayList<Face>();
            
            // this collects every face twice, except the boundary faces
	    Cell[] nearCells = cell.getNearCells();
	    for(Cell near: nearCells){
		for(Face face: near.faces){
                    nearBuffer.add(face);
		}
	    }
	    
            Face[] nearFaces = nearBuffer.toArray(faceType);
	    Vector[] kernel = new Vector[nearFaces.length];
            
	    for(int i=0; i<nearFaces.length; i++){
                Face face = nearFaces[i];
                Vector vectorBuffer = new Vector();
                Element e = new Element();
                e.set(cell, face);
                integrate(e, vectorBuffer);
                // if the face is inner: weigh by 1/2
                if(face.sideness == 0)
                    vectorBuffer.multiply(0.5);
                kernel[i] = vectorBuffer;
            }
            
            cell.kernel = new Kernel(nearFaces, kernel);
        }
    }

    protected abstract void integrate(Element elem, Vector vectorBuffer);
 
}
