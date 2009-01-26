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

import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Simulation;

public final class RK4 extends AmuSolver{

    private boolean adaptiveStep;
    private double maxDphi;
    private RK4Data[] rk4;

    public RK4(double maxDphi){
        this.maxDphi = maxDphi;
    }

    @Override
    public void init(Simulation sim){
        super.init(sim);
        rk4 = new RK4Data[sim.mesh.cells];

    }

    @Override
    public void stepImpl() {

        // (0) not used in this solver, but by some differentiating data models.
        prevDt = dt;


        // (1) determine the intrinsic time scale of the system in its present state
        // and use it for the time step.
        // adaptive time step = max precession angle per step.
        // todo: multiply by the appropriate factor for large damping or no precession.
        if(adaptiveStep)
            dt = maxDphi / maxH();
        else
            dt = maxDphi;

        if(Double.isInfinite(dt)){
            Message.warning("dt=Infinity");
            dt = maxDphi / 10;
        }

        {int i = 0; for(Cell c = sim.mesh.baseRoot; c != null; c = c.next){
            
        }i++;}
    }

}