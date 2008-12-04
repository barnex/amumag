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
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Simulation;

public abstract class AmuSolver{
    
    
    // root time step
    public double dt;
    // previous root time step
    public double prevDt;
    
    //public double totalTime;
    public int totalSteps;
    public double maxTorque;
    
    protected Simulation sim;
    
    public AmuSolver(){
    
    }
    
    public void init(Simulation sim){
        if(this.sim != null)
            throw new IllegalArgumentException("Solver already initiated");
        else
            this.sim = sim;
        
        Message.title("Solver");
        Message.indent("Type:\t " + toString());
    }
    
    public abstract  void stepImpl();
    
    protected void torque(Vector m, Vector h, Vector torque){
        
        // - m cross H
        double _mxHx = -m.y*h.z + h.y*m.z;
	double _mxHy =  m.x*h.z - h.x*m.z;
	double _mxHz = -m.x*h.y + h.x*m.y;
        
        // - m cross (m cross H)
        double _mxmxHx =  m.y*_mxHz - _mxHy*m.z;
        double _mxmxHy = -m.x*_mxHz + _mxHx*m.z;
        double _mxmxHz =  m.x*_mxHy - _mxHx*m.y; 
        
        double gilbert = 1.0 / (1.0 + Cell.alpha * Cell.alpha);
        torque.x = (_mxHx + _mxmxHx * Cell.alpha) * gilbert;
        torque.y = (_mxHy + _mxmxHy * Cell.alpha) * gilbert;
        torque.z = (_mxHz + _mxmxHz * Cell.alpha) * gilbert; 
    }
    
     /**
     * Returns the largest field in the system.
     * @return
     */
    protected double maxH(){
        double maxH2 = 0.0;
        int i = 0;
        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
            if (cell.h.norm2() > maxH2) {
                maxH2 = cell.h.norm2();
            }
            i++;
        }
        double maxH = Math.sqrt(maxH2);
        return maxH;  
    }
}
