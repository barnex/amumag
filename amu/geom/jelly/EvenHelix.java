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
import static java.lang.Math.*;


/**
 * A kind of 2-stpe helix-like structure that does not have a handedness.
 */

public final class EvenHelix extends Helix{

    public EvenHelix(double step, double hang, double phase, double center){
        super(step, hang, phase, center, 1);
    }
    
    @Override
    public void getMove(Vector r, Vector target) {
        double angle = atan2(r.y, r.x) + phase;
        double radius2 = (r.x*r.x + r.y*r.y);
        angle *= steps;
        while(angle > PI)
            angle -= 2*PI;
        while(angle < -PI)
            angle += 2*PI;
        if(angle > PI-hang){
            angle = PI/hang*(PI-angle);
        }
        else if(angle < -PI + hang){
            angle = PI/hang*(-PI-angle);
        }
        
        //make it even
        if(angle < 0)
            angle = -PI + angle;
        
        target.z = angle / (2*PI) * stepSize;
        
        
        
        if(center > 0){
            double scale = 1-exp(-radius2/(center*center));
            target.z*=scale;
        }
    }
    
    @Override
    public String toString(){
        return "Even" + super.toString();
    }
}