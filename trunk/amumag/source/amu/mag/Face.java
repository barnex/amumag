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

package amu.mag;
import amu.debug.Bug;
import amu.geom.Vector;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.geom.Vector.UNIT;
import static java.lang.Math.*;
import amu.geom.GeomModule;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A charged face in the mesh
 */
public final class Face implements Serializable{
   
    /* indicates on which side of a cell this face lies: +-1 for a face at the
     * mesh boundary (left-right) or 0 for a face inside the mesh. */
    public byte sideness;
    
    public Vector[] vertex;
    public Vector center;
    
    //TODO: replace by on-the-fly getArea() ?
    public double scalarArea;
    public double charge;                                               
  
    public transient Face next;                                                           //all faces are in a linked list
    
    private final int hashCode;
    
    public static final int LEFT = 0, RIGHT = 1;
    
    public Face(Vector[] vertex){
	this.vertex = vertex;
	for(Vector v: vertex)
	    v.makeImmutable();
	hashCode = Arrays.deepHashCode(vertex);
	center = GeomModule.barycentrumPlane(vertex);
	// area of 4 subtriangles.
	for(int i=0; i<4; i++)
	    scalarArea += GeomModule.area(vertex[i], vertex[(i+1)%4], center);
        /*if(area < 1E-14)
            throw new Bug("face area = " + area);*/
	
    }
    
    //__________________________________________________________________________
    
    public double getChargeDensity(){
	return charge / scalarArea;
    }
    
    //__________________________________________________________________________    
    /** Multipole expansion of this face with unit charge. This expansion has to be multiplied by
     * the face's actual charge. For a ZFace: Qn = 4a^(nx+1)b^(ny+1)even(nx)even(ny)/(nx+ny)!(nx+1)(ny+1)
     * with a and b the size of the face.
     */
        /*public void initUnitQ(){
                // We want to use the same formula for both X,Y and Z oriented faces, so we use the trick
                // to set size[dim] to 1.0 and n[dim] to zero.
                if(lock) throw new Error(); else lock=true;{
                        unitQ = new double[SmoothField.monomials.length];
                        for(int in = 0; in < unitQ.length; in++){
                                n.set(SmoothField.monomials[in]);
                                n.setComponent(orientation, 0);
                                s.set(size);
                                s.setComponent(orientation, 1.0);
                                unitQ[in] = 4.0 * Math.pow(s.x, n.x+1) * Math.pow(s.y, n.y+1) * Math.pow(s.z, n.z+1) *
                                                                  even(n.x) * even(n.y) * even(n.z) /
                                                                ( n.factorial() * (n.x+1) * (n.y+1) * (n.z+1) );
                                //exclude zeros?
                        }
                }lock=false;
        }*/
    
    /** Returns 1.0 if the number is even, 0.0 if it's odd.
     */
    /*private double even(int number){
        return (number%2 == 0)? 1.0: 0.0;
    }*/
    //__________________________________________________________________________
    
    /** 
     * Checks for equal vertices.
     */
    @Override
    public boolean equals(Object obj){
	if(!(obj instanceof Face))	    // should be redundant
	    throw new Bug();
	
	if(hashCode != obj.hashCode())
	    return false;
	else{
	    Face other = (Face) obj;
	    //return Arrays.deepEquals(vertex, other.vertex);
            //2007-dec-06: changed equals to be more accurate, is this the same?
            for(int v=0; v<vertex.length; v++){
                if(!vertex[v].equals(other.vertex[v]))
                    return false;
            }
            return true;
	}
    }
    
    //__________________________________________________________________________
    
    @Override
    public int hashCode(){
        return hashCode;
    }
    
    //__________________________________________________________________________
    
    @Override
    public String toString(){
        return "Face" + System.identityHashCode(this);
    }  
}
