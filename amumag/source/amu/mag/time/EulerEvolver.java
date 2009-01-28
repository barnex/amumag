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

import amu.io.OutputModule;
import amu.mag.Cell;
import amu.mag.Simulation;


public final class EulerEvolver extends Evolver{
    
    public double alpha;
    public double dt;
    
    public EulerEvolver(Simulation sim, double alpha, double dt){
        super(sim);
        this.dt = dt;
	this.alpha = alpha;
        this.sim = sim;
    }
    
    public void init(){
      Cell.alphaLLG=alpha;
    }

    public void stepImpl(){
        sim.update();
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
          // overwrite current data with new
          // System.out.println(cell.m + " " + cell.dmdt);
          cell.m.add(dt, cell.torque);
          cell.m.normalizeSafe();
       }
    }
}
