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

package amu.geom.jelly;

import amu.core.Index;
import amu.geom.Mesh;
import amu.geom.Vector;

public final class TransformedHomeomorphism extends Homeomorphism{
    
    private final Homeomorphism original;
    private final Vector buffer = new Vector();
    private final Vector[] matrix;
     
    public TransformedHomeomorphism(Homeomorphism original, Vector[] matrix){
	this.original = original;
	this.matrix = matrix;
    }

    @Override
    public void getMove(Vector v, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex){
       
        double xt = matrix[0].dot(v);
	double yt = matrix[1].dot(v);
	double zt = matrix[2].dot(v);
	buffer.set(xt, yt, zt);
        original.getMove(buffer, target, mesh, cellIndex, vertexIndex);
    }

    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("Unused");
    }
}