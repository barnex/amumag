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

import amu.mag.Cell;
import amu.io.Message;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;
import static amu.geom.Vector.*;

/**
 * Simple and safe way to create the vertices of a parent cell based on the children:
 * create a bounding box around both children. Joint cells may not be valid for
 * simulating anymore. TODO: make sure they are valid by combining there faces
 * into faces with valid normal vectors.
 */
public final class CellJoinModule {
    
    private Mesh mesh;
    
    public CellJoinModule(Mesh mesh){
	this.mesh = mesh;
    }
    
    public void joinCells(){
        Message.debug("joinCells()");
	join(mesh.rootCell);
    }
    
    /**
     * Recursively calculate the vertices for all cells based on the vertices of
     * their children. The children already have their vertices defined by 
     * PartitionModule/CutoutModule.
     */
    public void join(Cell thiz){
	// don't process the base level.
	if(thiz.hasChildren()){
	    join(thiz.child1);
	    join(thiz.child2);
	    
	    // these two vectors define the "BOTTOM LEFT BACK" and "TOP RIGHT FRONT" vertices of the bounding box.
	    Vector min = new Vector(POSITIVE_INFINITY, POSITIVE_INFINITY, POSITIVE_INFINITY);
	    Vector max = new Vector(NEGATIVE_INFINITY, NEGATIVE_INFINITY, NEGATIVE_INFINITY);
	    Vector[] box = new Vector[]{min, max};
	    
	    // adjust boundig box to contain all child vertices.
	    for(int c=0; c<=1; c++){
		Cell child = thiz.getChild(c);
		for(int v=0; v<child.vertex.length; v++){
		    Vector vertex = child.vertex[v];
		    for(int i=X; i <= Z; i++){
			if(min.getComponent(i) > vertex.getComponent(i))
			    min.setComponent(i, vertex.getComponent(i));
			if(max.getComponent(i) < vertex.getComponent(i))
			    max.setComponent(i, vertex.getComponent(i));
		    }
		}
	    }
	    
	    // create vertices for the joint cell, using the bounding box.
	    // i,j,k mean LEFT-RIGHT here.
	    thiz.vertex = new Vector[8];
	    for(int i = 0; i<= 1; i++){
		for(int j = 0; j <= 1; j++){
		    for(int k = 0; k <= 1; k++){
			Vector vertex = new Vector();
			vertex.x = box[i].x;
			vertex.y = box[j].y;
			vertex.z = box[k].z;
			vertex = mesh.vertexPool.intern(vertex);
			thiz.setVertex(i, j, k, vertex);
		    }    
		}
	    }
	}
    }
}
