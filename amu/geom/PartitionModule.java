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
import amu.core.Index;
import amu.io.Message;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;

/**
 * Divides the simulation volume into a tree of cells and faces. Adds the cells
 * and faces to their respective linked list, and sets the root nodes for these
 * lists in the mesh;
 * 
 * Detects the dimensionality (2D-3D) of the simulation and stores this in Mesh.
 * 
 */
public class PartitionModule{
    
    private Vector size;
    private int nLevels;
    private PartitionType rules;
    private Mesh mesh;
    
    public PartitionModule(Mesh mesh, Vector size, int nLevels, PartitionType rules){
	this.mesh = mesh;
	if(nLevels < 2)
	    throw new IllegalArgumentException("Number of levels must be > 1");
	this.size = size;
	this.nLevels = nLevels;
	this.rules = rules;
	mesh.splitDirection = new int[nLevels];
    }
    
    /**
     * Creates the geometrical Mesh, with all Cells linked to their parent en children.
     */
    public void createLevels(){
        
	mesh.levels = new Cell[nLevels][][][];				//init root level
	Cell[][][][] levels = mesh.levels;
	levels[0] = new Cell[1][1][1];
	Cell root = new Cell(null, new Vector(0, 0, 0, true), size, mesh.vertexPool, false);    //init root cell
	levels[0][0][0][0] = root;
	
        mesh.dimension = 2;                                             //may be changed to 3 when a split in Z is detected
	Message.startProgress(nLevels-2);
        divide(0);							//start recursive cell division
	System.gc();
        Message.stopProgress();
    }
    
    //__________________________________________________________________________
    
    private void divide(int level){	
            Message.progress(level); 
	    Cell c = mesh.levels[level][0][0][0];
	    int direction = rules.splitDirection(c.getSize().x, c.getSize().y, c.getSize().z, level, nLevels);
	    if(direction == Z)
                mesh.dimension = 3;
            mesh.splitDirection[level] = direction;
	    divide(direction, level, size);
	    
	    if(level+1 < nLevels-1)					// lowest level must not be devided further
		divide(level+1);					//divide next level
               
    }
    
    //__________________________________________________________________________
    
    /**
     * Creates a new level by splitting the parent cells (on the higher level)
     * along the given direction.
     */
    private void divide(int dir, int level, Vector size){
	
	final int U = dir;							//we split in the "U" direction
	final int V = (dir+1) % 3;
	final int W = (dir+2) % 3;
	
	//# of original subdivisions
	final int nX = mesh.levels[level].length;
	final int nY = mesh.levels[level][0].length;
	final int nZ = mesh.levels[level][0][0].length;
	Index n = new Index(nX, nY, nZ);
	
	//size of (all) the child cells
	n.setComponent(dir, 2*n.getComponent(dir));				// new array size on this level
	Vector childsize = new Vector(size);
	childsize.divide(n);
	childsize = new Vector(childsize, true);
	
	mesh.levels[level+1] = new Cell[n.x][n.y][n.z];
	boolean base = (level+1 == nLevels-1);
		
	//loop over old level
	for(int i = 0; i < nX; i++){
	    for(int j = 0; j < nY; j++){
		for(int k = 0; k < nZ; k++){
		    Cell parent = mesh.levels[level][i][j][k];
		    Vector parentPos = parent.center;
		    
		    //loop over the two children "0" and "1"
		    for(int c=0; c<=1; c++){
			int sign = (2*c-1);					// move in + or - direction along U axis (can be +1 or -1);
			double u = parentPos.getComponent(U) + sign * childsize.getComponent(U) / 2;
			Vector pos = new Vector();
			pos.setComponent(U, u);					// new coordinate in the u (split) direction
			pos.setComponent(V, parentPos.getComponent(V));		// same coordinates in v,w directions
			pos.setComponent(W, parentPos.getComponent(W));
			pos = new Vector(pos, true);
			Cell child = new Cell(parent, pos, childsize, mesh.vertexPool, base);
			parent.setChild(c, child);
			
			Index childIndex = new Index(i, j, k);
			childIndex.setComponent(U, 2*childIndex.getComponent(U) + c);	  // index of the child in the array
			mesh.levels[level+1][childIndex.x][childIndex.y][childIndex.z] = child;
		    }
		}
	    }
	}
    }
}
