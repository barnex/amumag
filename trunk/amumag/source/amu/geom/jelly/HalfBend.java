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

import amu.geom.jelly.Homeomorphism;
import amu.geom.Vector;

public class HalfBend extends Homeomorphism{

    private final double height;
    private final double lengthX;
    
    public HalfBend(double height, double lengthX){
        this.height = height;
        this.lengthX = lengthX;
    }

    @Override
    public void getMove(Vector r, Vector target) {
        if(r.x > 0)
            target.z = height*r.x*r.x/(lengthX*lengthX);
    }
}