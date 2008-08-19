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

import amu.debug.Bug;
import amu.geom.Vector;
import amu.mag.*;
import java.util.Arrays;

/**
 * Kernel element, represented by a (dummy) cell translated to origin, and a 
 * (dummy) face, equally translated. No scale invariance is used for now.
 */
public final class Element{
    
    public double area;
    public final Vector[] faceVertex;
    private int hashCode;
    
    //__________________________________________________________________________
    
    /**
     * Face and Cell are translated, cell goes to the origin and position is not
     * stored.
     */
    public Element(){
	faceVertex = new Vector[4];
	for(int i=0; i<4; i++)
	    faceVertex[i] = new Vector();
	rehash();
    }
    
    public Element(Element other){
	this();
	for(int i=0; i<4; i++){
	    faceVertex[i].set(other.faceVertex[i]);
	}
	this.area = other.area;
	rehash();
    }
    
    public void set(Cell cell, Face face){
	for(int i=0; i<4; i++){
	    faceVertex[i].set(face.vertex[i]);
	    faceVertex[i].subtract(cell.center);
	}
	area = face.scalarArea;
	rehash();
    }
    
    //__________________________________________________________________________
    
    @Override
    public boolean equals(Object obj) {
        assert obj instanceof Element;
        Element other = (Element) obj;
        //return Equality.equals(faceVertex, other.faceVertex);
        return Arrays.deepEquals(faceVertex, other.faceVertex);
    }
    
    //__________________________________________________________________________
    
    @Override
    public int hashCode(){
        return hashCode;
    }
    
    private void rehash(){
	hashCode = Arrays.deepHashCode(faceVertex);  /// ?
    }
}
