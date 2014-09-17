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

import amu.geom.Vector;

public final class Bump extends Homeomorphism{
    
    private final double sigma2;
    private final double height;
    
    public Bump(double height, double sigma){
        this.height = height;
        this.sigma2 = sigma*sigma;
    }
    
    @Override
    public void getMove(Vector r, Vector target) {
        double r2 = r.x * r.x + r.y * r.y;
        target.z = height * Math.exp(-r2/(sigma2));
    }

}