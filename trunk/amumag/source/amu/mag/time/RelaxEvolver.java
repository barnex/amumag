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

import amu.mag.Cell;
import amu.mag.Simulation;


public class RelaxEvolver{
    
    private final double alpha;
    private Simulation sim;
    
    public RelaxEvolver(Simulation sim, double alpha) {
        this.sim = sim;
        this.alpha = alpha;
    }

    public void step() {
        Cell[][][] base = sim.mesh.baseLevel;
        for(Cell[][] baseI: base)
            for(Cell[] baseIJ: baseI)
                for(Cell cell: baseIJ)
		    if(cell != null){
			if(cell.h.norm2() > 1.0){
			    cell.h.x = Math.cbrt(cell.h.x);
                            cell.h.y = Math.cbrt(cell.h.y);
                            cell.h.z = Math.cbrt(cell.h.z);
                        }
                       
			cell.m.add(alpha, cell.h);
                }
    }
    
}
