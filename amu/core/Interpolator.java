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

package amu.core;

/**
 * Linear interpolator.
 */
public final class Interpolator {
    
    public double a, b;

    public Interpolator() {
    }
    
    public Interpolator(double x1, double y1, double x2, double y2){
       set(x1, y1, x2, y2);
    }
    
    public Interpolator(double a, double b){
        this.a = a;
        this.b = b;
    }
    
    public void set(double x1, double y1, double x2, double y2){
	a = (y2-y1)/(x2-x1);
        b = y1-x1*(y2-y1)/(x2-x1);
    }
    
    public final double transf(double x){
        return a*x+b;
    }
    
    public Interpolator inverse(){
        return new Interpolator(1.0/a, -b/a);
    }
}
