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

/**
 * A test implementation of the AmuSolver, using quadratic extrapolation of h,
 * and midpoint-integration of m using smaller substeps in the extrapolation
 * interval of h.
 * @author arne
 */
public class AmuSolver2 extends AmuSolver{

    protected Extrapolator2[] field;
    protected Extrapolator1[] m;
    
    protected double maxDphi;
    protected int substeps;
    
    public int probe = -1;  //debug
    
    public AmuSolver2(double maxDphi, int substeps){
        if(maxDphi <= 0.0)
            throw new IllegalArgumentException();
        if(substeps < 1)
            throw new IllegalArgumentException();
        
        this.maxDphi = maxDphi;
        this.substeps = substeps;
    }
    
   private final Vector hnew = new Vector();
   private final Vector mnew = new Vector();
   private final Vector torquenew = new Vector();
        
    @Override
   public void init(Simulation sim){
        super.init(sim);
        field = new Extrapolator2[sim.mesh.cells];
        m = new Extrapolator1[sim.mesh.cells];
       
        for(int i=0; i<field.length; i++){
            field[i] = new Extrapolator2();
            m[i] = new Extrapolator1();
        }
        
        //Message.indent("dt:\t " + dt);
   }
   
    public void stepImpl(){

        // (0) backup the current state, which will soon be the previous one!
        prevDt = dt;
             
        // (1) determine the intrinsic time scale of the system in its present state
        // and use for the time step.
        {
            double maxH2 = 0.0;
            int i = 0;
            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
                if (cell.h.norm2() > maxH2) {
                    maxH2 = cell.h.norm2();
                }
                i++; 
            }
            double maxH = Math.sqrt(maxH2);
            dt = maxDphi / maxH; // adaptive time step: max precession angle per step.
                                 // todo: multiply by the appropriate factor for large damping
                                 // or no precession.
            if(totalSteps == 0)
                dt = dt * dt * dt;   // first step is of the euler type, make it cubically smaller
                                     // to get roughly the same accuracy as the next steps
            else if(totalSteps == 1) // second step is 1st order, make it quadratically smaller
                dt = dt * dt;
        }
        
        // (2) step
        {
            int i = 0;
            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {

                // push previous h to extrapolator
                field[i].addPoint(prevDt, cell.h);
                
                // step m with smaller steps than the global dt  
                subStep(cell, i);
                
                i++;
            }
        }
        
        // (4) update fields for the next big step
        sim.update();
        
        // (3) bookkeeping
        sim.totalTime += dt;
        totalSteps++;
             
    }
    
    private void subStep(Cell cell, int i){
        
        double subDt = dt / substeps;
        
        for (int s = 0; s < substeps; s++) {

                    // push previous m to extrapolator
                    // !! here and only here, not outside this loop or m at the beginning of each sub-loop will be pushed twice!
                    m[i].addPoint(subDt, cell.m);
                    
                    // extrapolate to estimated new magnetization, halfway the sub interval
                    field[i].extrapolate( (s+0.5)*subDt, hnew);
                    m[i].extrapolate(0.5*subDt, mnew);
                    mnew.normalize();

                    if(probe == i)
                        System.err.println(totalSteps + " " + (sim.totalTime + (s+0.5)*subDt) + " " + hnew + " " + mnew + " 0" + (s==0? " 2": " 0"));
                    
                    torque(mnew, hnew, torquenew);
                    cell.m.add(subDt, torquenew);
                    cell.m.normalize();
 
                    if(probe == i)
                        System.err.println(totalSteps + " " + (sim.totalTime + (s+1)*subDt) + " " + hnew + " " + cell.m + " 1" + (s==0? " 2": " 0"));
            
                    // use same extrapolation for h, however.
                }
    }
    
    @Override
    public String toString(){
        return "AmuSolver2 (2nd order)";
    }
}
