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
import amu.mag.Simulation;


public final class DampingEvolver{
    
    public double alpha;
    private Simulation sim;
    
    public DampingEvolver(Simulation sim, double alpha) {
        this.sim = sim;
        this.alpha = alpha;
    }

    private static final Vector ZERO = new Vector();
    
    public void step() {
        Vector mxH = new Vector();
        Vector dmdt = new Vector();
        Cell[][][] base = sim.mesh.baseLevel;
        for(Cell[][] baseI: base)
            for(Cell[] baseIJ: baseI)
                for(Cell cell: baseIJ)
		    if(cell != null){
                  
			Vector m = cell.m;
                        Vector h = cell.hDemag;
			mxH.set(m);
			mxH.cross(h);
			dmdt.set(m);
			dmdt.cross(mxH);
			m.add(-alpha, dmdt);        // some sign problem left somewhere
			m.normalizeSafe();
                }
    }
}
