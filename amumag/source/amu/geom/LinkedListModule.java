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
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import amu.io.Message;

/**
 * Creates linked lists containing the cells or faces.
 */
public final class LinkedListModule {
    
    private Mesh mesh;
    
    public LinkedListModule(Mesh mesh){
	this.mesh = mesh;
    }
    
    public void linkCellsAndFaces(){
	linkCells();
	linkFaces();
    }
    
    public void linkFaces(){
	Face current = null;
	int count = 0;                                  //count total number of faces
        
	for(Face face : mesh.facePool.hash.keySet()){
	    if(current == null){
		mesh.rootFace = face;
		current = face;
		count = 1;
	    }
	    else{
		current.next = face;
		current = face;
		count++;
	    }
	}
        mesh.faces = count;
         Message.debug("linkFaces(), #faces = " + count);
    }
    
    public void linkCells(){
	Cell[][][][] levels = mesh.levels;
	
	Cell root = null;
	Cell baseRoot = null;
        Cell coarseRoot = null;
	Cell current = null;
        
	int count = 0, baseCount = 0, coarseCount = 0;           //count total number of cells and number of cells on base level
	
	for(int l=0; l<levels.length; l++)
	    for(int i=0; i<levels[l].length; i++)
		for(int j=0; j<levels[l][i].length; j++)
		    for(int k=0; k<levels[l][i][j].length; k++){
			Cell cell = levels[l][i][j][k];
			// some cells are cut out, do not include these nulls!
			if(cell != null){
			    
			    // extra reference to the first cell of the base level.
			    if(l == mesh.levels.length-1 && baseRoot == null)
				baseRoot = cell;
			    
                            // extra reference to the first cell of the top adaptive level
                            // deserialized mesh: rules==null, ignore.
                            if(mesh.aMRules != null && l == mesh.aMRules.getCoarseRootLevel() && coarseRoot == null)
				coarseRoot = cell;
                            
			    if(current == null){
				current = cell;
				root = current;
			    } else{
				current.next = cell;
				current = cell;
			    }
                            //count cells
                            count++;
                            if(l == mesh.levels.length-1)
                                baseCount++;
                            if(l <= mesh.coarseLevelIndex)
                                coarseCount++;
			}
		    }
        mesh.cells = count;
        mesh.baseCells = baseCount;
        mesh.coarseCells = coarseCount;
        
	Message.debug("linkCells(), #cells = " + count + ", " + baseCount + " smallest cells");
	mesh.rootCell = root;
	mesh.baseRoot = baseRoot;
        mesh.coarseRoot = coarseRoot;
    }
}
