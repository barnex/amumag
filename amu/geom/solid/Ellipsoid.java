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

public class Ellipsoid extends Shape{
    
    private final double rx, ry, rz;
    
    public Ellipsoid(double rx, double ry, double rz){
	this.rx = rx;
	this.ry = ry;
	this.rz = rz;
    }
    
    public Ellipsoid(double r){
	this(r, r, r);
    }
    
    public boolean inside(Vector r) {
	return square(r.x/rx) + square(r.y/ry) + square(r.z/rz) <= 1.0;
    }
}
