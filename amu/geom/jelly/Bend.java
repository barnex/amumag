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

public class Bend extends Homeomorphism{

    private final double height;
    private final double lengthX, lengthY;
    
    public Bend(double height, double lengthX, double lengthY){
        if(lengthX == 0.0)
            lengthX = 1.0/0.0;
        if(lengthY == 0.0)
            lengthY = 1.0/0.0;
        this.height = height;
        this.lengthX = lengthX;
        this.lengthY = lengthY;
    }

    @Override
    public void getMove(Vector r, Vector target) {
        target.z = height*
                (r.x*r.x/(lengthX*lengthX) 
                +r.y*r.y/(lengthY*lengthY));
    }
}