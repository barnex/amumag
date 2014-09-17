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
import static amu.geom.Vector.*;

/**
 * This shape contains all points with a given component (X, Y or Z) >= 0 and
 * thus fills one half of the entire space. Usefull to cut off pieces of other
 * shapes.
 */
public final class SemiSpace extends Shape{
    
    private final int component;
    
    /**
     * Shape that contains all points of space with X >=0.
     */
    public SemiSpace(){
        this(X);
    }
    
    /**
     * Shape that contains all points of space with given component >= 0.
     */
    public SemiSpace(int component){
	this.component = component;
    }
    
    protected boolean inside(Vector r) {
	return r.getComponent(component) >= 0;
    }
}
