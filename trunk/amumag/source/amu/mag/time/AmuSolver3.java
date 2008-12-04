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
 * interval of h. After such a large step, h is updated for the next step. However,
 * with the knowledge of the new h, h can be interpolated in the last step interval,
 * rather than extrapolated. So before doing the next step, m is re-calculated
 * with the better knowledge of h. After this correction, h could be updated again
 * and the procedure could be repeated, but this is not done here.
 * @author arne
 */
public class AmuSolver3 extends AmuSolver{

    // extrapolators are always up-to-date, ready to extrapolate.
    // new field values have to be added directly after any simulation update.
    protected Extrapolator2[] field;
    // new m values have to be added after every substep.
    protected Extrapolator1[] m;
    
    protected Extrapolator1[] mBackup;
    
    protected double maxDphi;
    protected int substeps;
    
    public int probe = 0;  //cell # to probe for debug
    
    public AmuSolver3(double maxDphi, int substeps){
        if(maxDphi <= 0.0)
            throw new IllegalArgumentException();
        if(substeps < 1)
            throw new IllegalArgumentException();
        
        this.maxDphi = maxDphi;
        this.substeps = substeps;
    }
    
    // buffers
    private final Vector hnew = new Vector();
    private final Vector mnew = new Vector();
    private final Vector torquenew = new Vector();
        
    @Override
    public void init(Simulation sim){
        super.init(sim);
        
        field = new Extrapolator2[sim.mesh.cells];
        m = new Extrapolator1[sim.mesh.cells];
        mBackup = new Extrapolator1[sim.mesh.cells];
        
        for(int i=0; i<field.length; i++){
            field[i] = new Extrapolator2();
            m[i] = new Extrapolator1();
            mBackup[i] = new Extrapolator1();
        }
   }
   
    /**
     * Pushes the system state to the extrapolators, so they are ready for first use.
     */
    private void initExtrapolators(){
        int i = 0;
        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
            field[i].addPoint(Double.NaN, cell.h);  // time of first point should never be used, make it NaN to be sure...
            m[i].addPoint(Double.NaN, cell.m);
            i++;
        }
    }
    
    /**
     * Make a big step, substeps are invisible to the simulation output.
     */
    public void stepImpl(){

        // (0) not used in this solver, but by some differentiating data models.
        prevDt = dt;
          
        
        // (1) determine the intrinsic time scale of the system in its present state
        // and use it for the time step.
        // adaptive time step = max precession angle per step.
        // todo: multiply by the appropriate factor for large damping or no precession.
        dt = maxDphi / maxH(); 
         if(totalSteps % 10 == 0)
            dt /= 10;//*/
        
        // (2) If the solver has not been started yet, make the first (smaller) steps with lower order.
        // Make normal steps otherwise.
        if (totalSteps == 0) {
            initExtrapolators();                        // first step: push current m,h to extrapolators             
            dt = dt*dt*dt;
            step();     // first step is of the euler type, make it cubically smaller
                          // to get roughly the same accuracy as the next steps
        } 
        else if (totalSteps == 1) 
        {
            dt = dt * dt; // second step is 2st order, make it quadratically smaller
            step();
        }
        else {
            step();    // finalyy, the other steps are big and high-order.
        }
        
        
        // (3) bookkeeping
        sim.totalTime += dt;
        totalSteps++;
    }
    
    /**
     * Makes one large step with the solvers current dt.
     */ 
    private void step() {
        {
            //step with extrapolated h
            int i = 0;
            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
                // make backup copy of current m history to use for the corrector step
                mBackup[i].set(m[i]);
                // does not backup cell.m yet.
                
                // step m with smaller steps than the global dt  
                subStep(cell, i, true);

                i++;
            }
        }
        
        // (4) update h for the next big step, also add to extrapolator already.
        sim.update();
        {
            int i = 0;
            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
                field[i].addPoint(dt, cell.h); 
                //revert to backup for the corrector step
                m[i].set(mBackup[i]);
                i++;
            }
        }
        
        {   //do the same step with h interpolation rather than extrapolation.
            int i = 0;
            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
                // step m with smaller steps than the global dt, interpolate with the new, better h.  
                subStep(cell, i, false);
                i++;
            }
        }       
    }
    
    private void subStep(Cell cell, int i, boolean extrapolate){
        
        double subDt = dt / substeps;
        
        for (int s = 0; s < substeps; s++) {
 
                    // extrapolate to estimated new magnetization, halfway the sub interval
                    if(extrapolate)
                        field[i].extrapolate( (s+0.5)*subDt, hnew);
                    else
                        field[i].extrapolate( (s+0.5-substeps-1)*subDt, hnew);
                    
                    m[i].extrapolate((0.5)*subDt, mnew);
                    mnew.normalize();

                    if(probe == i)
                        System.err.println(totalSteps + " " + (sim.totalTime + (s)*subDt) + " " + hnew + " " + mnew + " 0" + (s==0? " 2": " 0"));
                    
                    torque(mnew, hnew, torquenew);
                    
                    // put the last value of the extrapolator in cell.m, it will be
                    // ahead-of-date when the step is a corrector step.
                    m[i].extrapolate(0.0, cell.m); //todo: extrapolator.getLastValue();
                    
                    cell.m.add(subDt, torquenew);
                    cell.m.normalize();
 
                    //if(probe == i)
                    //    System.err.println(totalSteps + " " + (totalTime + (s+1)*subDt) + " " + hnew + " " + cell.m + " 1" + (s==0? " 2": " 0"));
            
                    // push m to update extrapolator
                    // !! here and only here, not outside this loop or m at the beginning of each sub-loop will be pushed twice!
                    m[i].addPoint(subDt, cell.m);
                    // use same extrapolation for h, however.
                }
    }
    
    @Override
    public String toString(){
        return "AmuSolver2 (2nd order)";
    }
}
