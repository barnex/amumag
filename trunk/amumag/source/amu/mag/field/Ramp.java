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

package amu.mag.field;

import amu.geom.Vector;

public class Ramp extends ExternalField{

    protected double rampTime;
    protected double bx, by, bz;
    
    public Ramp(double rampTime){
        this(rampTime, 1.0);
    }
    
    public Ramp(double rampTime, double b){
        this(rampTime, b, b, b);
    }
    
    public Ramp(double ramp, double bx, double by, double bz){
        this.rampTime = ramp;
        this.bx = bx;
        this.by = by;
        this.bz = bz;
    }
    
    @Override
    protected void put(double time, Vector r, Vector field) {
        if(time > 0 && time <= rampTime){
            double fraction = time / rampTime;
            field.set(fraction * bx, fraction * by, fraction*bz);
        }
        else if(time > rampTime)
            field.set(bx,  by, bz);
    }

}