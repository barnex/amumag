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

import amu.geom.solid.Shape;
import amu.geom.Vector;

/**
 *
 */
public final class Intersection extends Shape{
    
    private final Shape shape1, shape2;
    
    public Intersection(Shape shape1, Shape shape2){
	this.shape1 = shape1;
	this.shape2 = shape2;
    }
    
    public boolean inside(Vector position) {
	return shape1.insideInternal(position)
	&& shape2.insideInternal(position);
    }
}
