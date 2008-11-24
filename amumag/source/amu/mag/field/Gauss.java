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

public final class Gauss extends ExternalField{

    protected double amplitude, sigma2, time0;
    
    public Gauss(double amplitude, double sigma, double time0){
        this.amplitude = amplitude;
        this.sigma2 = sigma*sigma;
        this.time0 = time0;
    }
    
    @Override
    protected void put(double time, Vector r, Vector field) {
        double time0 = this.time0;
        double t2 = (time - time0);
        t2 *= t2;
        field.x = amplitude*Math.exp(-t2/sigma2);
    }

}