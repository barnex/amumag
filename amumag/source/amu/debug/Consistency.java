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

package amu.debug;

import amu.geom.*;
import amu.io.Message;
import amu.mag.*;
import amu.io.Message;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;

/**
 * Checks the consistency of some internal data. This is for debugging only and
 * should not be necessary if the program works perfectly.
 */
public final class Consistency {
    
    /**
     * Checks if parent-child relations are consistent and no cells with only
     * one child exist.
     *
     * Should be called after creating of modifying the cell tree (make cutouts).
     */
    public static void checkMesh(Mesh mesh){
	for(Cell[][][] level: mesh.levels)
	    for(Cell[][] levelI: level)
		for(Cell[] levelIJ: levelI)
		    for(Cell cell: levelIJ)
			if(cell != null){
			    checkParent(cell);
			    checkCutout(cell);
			}
    }
    
    private static void checkParent(Cell thiz){
	if(thiz.child1 != null){
	    if(thiz.child1.parent != thiz)
		throw new Bug();
	    if(thiz.child2.parent != thiz)
		throw new Bug();
	}
    }
    
    public static void checkCutout(Cell thiz){
	if(thiz.childCount() == 1)
	    throw new Bug();
	if(thiz.unlinkTag == true)
	    throw new Bug();
    }
    
    //__________________________________________________________________________
    
    /**
     * Checks for inconsistencies in the partner lists.
     */ 
   public static void checkWiringAll(Cell root){
      
	checkWiring(root);
	if(root.child1 != null){
	    checkWiring(root.child1);
	    checkWiring(root.child2);
	}
	
    }
    
    public static void checkWiring(Cell cell){
	// check if nearCell is also partner
	for(Cell near: cell.getNearCells())
	    for(Cell partner: cell.getPartners())
		if(near == partner)
		    throw new Bug();
	    
	    // check if partners, or parents of partners are also partners of the parent (pffew...)
	    if(cell.parent != null){
		for(Cell partner: cell.getPartners())
		    for(Cell parentPartner: cell.parent.getPartners()){
			if(partner == parentPartner)
			    throw new Bug();
		    }
	    }
    }
    
    //__________________________________________________________________________
    
    public static void checkFacesAll(Mesh mesh){
	for(Cell cell = mesh.rootCell; cell != null; cell = cell.next){
	    checkFaces(cell);
        }
    }
    
    public static void checkFaces(Cell cell){
	/*if(cell.faces == null)
	    throw new Bug();*/
	/*if(cell.faces.length != 6)
	    throw new Bug();
	for(Face f: cell.faces)
	    if(f == null)
		throw new Bug();*/
    }
}
