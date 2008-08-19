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

package amu.geom.solid;

import amu.geom.Vector;

/**
 *
 */
public final class TransformedShape extends Shape{
    
    private final Shape shape;
    private final Vector[] matrix;
    
    public TransformedShape(Shape shape, Vector[] matrix){
	this.shape = shape;
	this.matrix = matrix;
    }
    
    public boolean inside(Vector v) {
	double xt = matrix[0].dot(v);
	double yt = matrix[1].dot(v);
	double zt = matrix[2].dot(v);
	v.set(xt, yt, zt);
	return shape.insideInternal(v);
    }
    
}
