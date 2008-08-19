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
import static java.lang.Math.*;

/**
 * Exponentially growing field.
 * @author arne
 */
public final class ExponentialField extends ExternalField{

    private final double bx, by, bz;
    private final double tau;
    
    /**
     * Exponentially growing field with initial amplitude (bx, by, bz) and
     * time constant tau.
     */
    public ExponentialField(double bx, double by, double bz, double tau){
        this.bx = bx;
        this.by = by;
        this.bz = bz;
        
        this.tau = tau;
    }
    
     /**
     * Exponentially growing field with initial amplitude (1, 1, 1) and
     * time constant tau. Usefull to mutiply with another field, such as RfField.
     */
    public ExponentialField(double tau){
        this(1.0, 1.0, 1.0, tau);
    }
    
    @Override
    protected void put(double time, Vector field) {
        field.x = bx * exp((time-time0)/tau);
        field.y = by * exp((time-time0)/tau);
        field.z = bz * exp((time-time0)/tau);
    }
}