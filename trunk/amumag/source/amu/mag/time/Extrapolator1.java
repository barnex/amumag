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
import static java.lang.Double.POSITIVE_INFINITY;

/**
 * Simple 1th order extrapolator for debugging.
 * @author arne
 */
public final class Extrapolator1 extends Extrapolator{

    private double vx0, vy0, vz0;
    private double vx_1, vy_1, vz_1;
    private double dt;
    private boolean hasData = false;
    
    public Extrapolator1(){
        dt = Double.NaN;
    }
    
    /**
     * Copies the state of an other extrapolator.
     * @param other
     */
    public void set(Extrapolator1 other){     
        vx_1 = other.vx_1;
        vy_1 = other.vy_1;
        vz_1 = other.vz_1;
        
        vx0 = other.vx0;
        vy0 = other.vy0;
        vz0 = other.vz0;
        
        dt = other.dt;
        hasData = other.hasData;
    }
    
    /**
     * 
     * @param dt Time between this point and the previous one.
     * @param v Value of the new point.
     */
    public void addPoint(double dt, Vector v){
        vx_1 = vx0;
        vy_1 = vy0;
        vz_1 = vz0;
        
        vx0 = v.x;
        vy0 = v.y;
        vz0 = v.z;
        
        // first time data is added: there is no previous point yet,
        // so do 0th order extrapolation by a trick: set dt to infinity.
        if(hasData)
            this.dt = dt;
        else{
            this.dt = POSITIVE_INFINITY;
            hasData = true;
        }
    }
    
    public void replaceLastPoint(double dt, Vector v){
        if(!hasData){
            throw new IllegalArgumentException("Extrapolator has no data yet");
        }
        vx0 = v.x;
        vy0 = v.y;
        vz0 = v.z;
        this.dt = dt;
    }
    
    public void extrapolate(double dt, Vector target){
        if(!hasData){
            throw new IllegalArgumentException("Extrapolator has no data yet");
        }
        
        target.x = vx0 + (vx0 - vx_1) * (dt/this.dt);
        target.y = vy0 + (vy0 - vy_1) * (dt/this.dt);
        target.z = vz0 + (vz0 - vz_1) * (dt/this.dt);
    }
}