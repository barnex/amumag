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
import amu.mag.Cell;
import amu.mag.Main;
import amu.io.Message;
import static java.lang.Math.*;
import static java.lang.Double.POSITIVE_INFINITY;

public class AmuSolver{
    
    private static double alpha;
    
    private final Cell cell;
    
    //statistics for last step
    public static double
            rmsAbsStepError,
            rmsRelStepError, 
            maxAbsStepError, 
            maxRelStepError,
            maxDm,
            maxTorque;
    
    
    public AmuSolver(Cell cell){
        this.cell = cell;
    }
    
    
    public static void init() {
       Main.sim.update();
    }
    
    public static void step(){
        // fields are assumed up-to-date
        
    }
    
    private static void updateMaxTorquesGlobal(){
    
    }
    
    private void updateMaxTorques(){
    
    }
    
    private static void torque(Vector m, Vector h, Vector torque){
        // - m cross H
        double _mxHx = -m.y*h.z + h.y*m.z;
	double _mxHy =  m.x*h.z - h.x*m.z;
	double _mxHz = -m.x*h.y + h.x*m.y;
        
        // - m cross (m cross H)
        double _mxmxHx =  m.y*_mxHz - _mxHy*m.z;
        double _mxmxHy = -m.x*_mxHz + _mxHx*m.z;
        double _mxmxHz =  m.x*_mxHy - _mxHx*m.y; 
        
        torque.x = _mxHx + _mxmxHx * alpha;
        torque.y = _mxHy + _mxmxHy * alpha;
        torque.z = _mxHz + _mxmxHz * alpha;          
    }
}
