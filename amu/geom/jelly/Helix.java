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

public class Helix extends Homeomorphism{
    
    protected double stepSize;
    protected int steps;
    protected double hang;
    protected double phase;
    protected double center;
    
   /**
    * Helix-like deformation
    * @param stepSize height of the jump
    * @param phase start phase of the helix
    * @param steps number of steps to be made
    * @param hang width (in radians) of the step, avoids extremely steep edges.
    */
    public Helix(double stepSize, double hang, double phase, double center, int steps){
        this.stepSize = stepSize;
        this.steps = steps;
        this.hang = hang;
        this.phase = phase;
        this.center = center;
    }
    
    public Helix(double stepSize, double hang, double phase, double center){
        this(stepSize, hang, phase, center, 1);
    }
    
    public Helix(double stepSize, double hang, double phase){
        this(stepSize, hang, phase, 1);
    }
    
    public Helix(double stepSize, double hang){
        this(stepSize, hang, 0.0);
    }
    
    public Helix(double stepSize){
        this(stepSize, 0.0);
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
        target.z = angle / (2*PI) * stepSize;
        if(center > 0){
            double scale = 1-exp(-radius2/(center*center));
            target.z*=scale;
        }
    }
    
    @Override
    public String toString(){
         return "Helix{" 
                 + "stepSize=" + stepSize
                 + ", steps=" + steps
                 + ", hang=" + hang
                 + ", phase=" + phase
                 + ", center=" + center
                 + "}";
    }
}