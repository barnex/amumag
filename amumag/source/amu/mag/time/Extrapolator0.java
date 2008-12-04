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

package amu.mag.time;

import amu.geom.Vector;

/**
 * Trivial, 0th order extrapolator for debugging.
 * @author arne
 */
public final class Extrapolator0 extends Extrapolator{

    private double vx, vy, vz;
    
    public Extrapolator0(){
    }
    
    public void addPoint(double dt, Vector v){
        vx = v.x;
        vy = v.y;
        vz = v.z;
    }
    
    public void replaceLastPoint(double dt, Vector v){
        vx = v.x;
        vy = v.y;
        vz = v.z;
    }
    
    public void extrapolate(double dt, Vector target){
        target.x = vx;
        target.y = vy;
        target.z = vz;
    }
}