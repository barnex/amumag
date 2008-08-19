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


package amu.mag.config;

import amu.geom.Vector;
import static java.lang.Math.*;

public class Region extends Configuration{
    
    private Vector center;
    private double r;
    private Vector m;

    public Region(Vector center, double r, Vector m){
        this.center = center;
        this.r = r;
        this.m = m;
    }
    
    @Override
    public void putM(double x, double y, double z, Vector target) {
        if(abs(x-center.x) < r && abs(y-center.y) <r)
            target.set(m);
    }

}
