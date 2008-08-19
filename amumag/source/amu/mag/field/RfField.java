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
 * Alternating field.
 * @author arne
 */
public final class RfField extends ExternalField{

    private final double bx, by, bz;
    private final double phaseX, phaseY, phaseZ;
    private final double freq;
    
    /**
     * Alternating field with amplitude (bx, by, bz) (in Tesla), a fixed
     * frequency and different phases for each component of the field.
     */
    public RfField(double bx, double by, double bz, double freq, 
            double phaseX, double phaseY, double phaseZ){
        this.bx = bx;
        this.by = by;
        this.bz = bz;
        
        this.freq = freq;
        this.phaseX = phaseX;
        this.phaseY = phaseY;
        this.phaseZ = phaseZ;
    }
    
    /**
     * Alternating field with amplitude (bx, by, bz) (in Tesla), a fixed
     * frequency and fixed for all components of the field.
     */
    public RfField(double bx, double by, double bz, double freq, double phase){
        this(bx, by, bz, freq, phase, phase, phase);
    }
    
    /**
     * Alternating field with amplitude (bx, by, bz) (in Tesla), a fixed
     * frequency and zero phase. The field is thus a sinewave.
     */
    public RfField(double bx, double by, double bz, double freq){
        this(bx, by, bz, freq, 0.0);
    }
    
    @Override
    protected void put(double time, Vector field) {
        field.x = bx * sin(2*PI*freq*(time-time0) + phaseX);
        field.y = by * sin(2*PI*freq*(time-time0) + phaseY);
        field.z = bz * sin(2*PI*freq*(time-time0) + phaseZ);
    }
}