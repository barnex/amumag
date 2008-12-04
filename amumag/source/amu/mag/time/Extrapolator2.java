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
 * 2th order extrapolator.
 * @author arne
 */
public final class Extrapolator2 extends Extrapolator{
  
    private double vx_2, vy_2, vz_2;
    private double vx_1, vy_1, vz_1;
    private double vx0, vy0, vz0;
    
    /**
     * distance between point -2 and point -1 (positive number)
     */
    private double x_1;
    /**
     * distance between point -1 and point 0 (positive number)
     */
    private double x0;
    
    private int dataPoints = 0;
    
    public Extrapolator2(){
       
    }
    
    /**
     * Copies the state of an other extrapolator.
     * @param other
     */
    public void set(Extrapolator2 other){
        vx_2 = other.vx_2;
        vy_2 = other.vy_2;
        vz_2 = other.vz_2;
        
        vx_1 = other.vx_1;
        vy_1 = other.vy_1;
        vz_1 = other.vz_1;
        
        vx0 = other.vx0;
        vy0 = other.vy0;
        vz0 = other.vz0;
        
        x_1 = other.x_1;
        x0 = other.x0;
        dataPoints = other.dataPoints;
    }
    
    /**
     * 
     * @param dt Time between this point and the previous one.
     * @param v Value of the new point.
     */
    public void addPoint(double dt, Vector v){
        vx_2 = vx_1;
        vy_2 = vy_1;
        vz_2 = vz_1;
        
        vx_1 = vx0;
        vy_1 = vy0;
        vz_1 = vz0;
        
        vx0 = v.x;
        vy0 = v.y;
        vz0 = v.z;
     
        x_1 = x0;
        x0 = dt;   
        
        dataPoints++;
    }
    
    public void replaceLastPoint(double dt, Vector v){
        if(dataPoints >= 3){
            vx0 = v.x;
            vy0 = v.y;
            vz0 = v.z;
            x0 = dt;
        } else if (dataPoints == 2) {
            vx_1 = v.x;
            vy_1 = v.y;
            vz_1 = v.z;
            x_1 = dt;
        }
        else if(dataPoints == 1){
            vx_2 = v.x;
            vy_2 = v.y;
            vz_2 = v.z;
        }
        else{
            throw new IllegalArgumentException("Extrapolator has no data yet.");
        }
    }
    
    public void extrapolate(double dt, Vector target){
        
        if(dataPoints >= 3){
            double a = x0*x0;
            double b = -x0;
            double c = (x0+x_1)*(x0+x_1);
            double d = -(x0+x_1);
            double n = a*d-b*c;
            
            double e = vx_1 - vx0;
            double f = vx_2 - vx0;
            
            double A = (e*d - b*f) / n;
            double B = (a*f - e*c) / n;
            double C = vx0;
            target.x = A*dt*dt + B*dt + C;
            
            
            e = vy_1 - vy0;
            f = vy_2 - vy0;
            
            A = (e*d - b*f) / n;
            B = (a*f - e*c) / n;
            C = vy0;
            
            target.y = A*dt*dt + B*dt + C;
            
            e = vz_1 - vz0;
            f = vz_2 - vz0;
            
            A = (e*d - b*f) / n;
            B = (a*f - e*c) / n;
            C = vz0;
            
            target.z = A*dt*dt + B*dt + C;
            
        } else if (dataPoints == 2) {
            target.x = vx0 + (vx0 - vx_1) * (dt / x0);
            target.y = vy0 + (vy0 - vy_1) * (dt / x0);
            target.z = vz0 + (vz0 - vz_1) * (dt / x0);
        }
        else if(dataPoints == 1){
            target.x = vx0;
            target.y = vy0;
            target.z = vz0;
        }
        else{
            throw new IllegalArgumentException("Extrapolator has no data yet.");
        }
    }
}