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

import amu.debug.Bug;
import amu.debug.InvalidProblemDescription;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Simulation;

/**
 * A first, test implementation of the AmuSolver. Does simple euler integration.
 * 
 * @author arne
 */
public final class AmuSolver0 extends AmuSolver{

    protected Extrapolator0[] field;
    protected Extrapolator0[] m;
    //protected Vector[] torque;
    protected double maxDphi;
    
    public AmuSolver0(double maxDphi){
        this.maxDphi = maxDphi;
    }
    
   private final Vector hnew = new Vector();
   private final Vector mnew = new Vector();
   private final Vector torquenew = new Vector();
        
    @Override
   public void init(Simulation sim){
       super.init(sim);
        field = new Extrapolator0[sim.mesh.cells];
        m = new Extrapolator0[sim.mesh.cells];
        //torque = new Vector[sim.mesh.cells];
        
        for(int i=0; i<field.length; i++){
            field[i] = new Extrapolator0();
            m[i] = new Extrapolator0();
            //torque[i] = new Vector();
        }
        //Message.indent("dt:\t " + dt);
   }
   
   
    public void stepImpl(){

        double maxH2 = 0.0;
        
        int i=0;
        for(Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next){
            // store previous state
            field[i].addPoint(111, cell.h);
            m[i].addPoint(111, cell.m);
            
            // extrapolate to estimated new state
            field[i].extrapolate(111, hnew);      //0th order extrapolation, dt=whatever (=111)
            
            //store maxH
            if(hnew.norm2() > maxH2)
                maxH2 = hnew.norm2();
            i++;
        }
        
        double maxH = Math.sqrt(maxH2);
        
        dt = maxDphi/maxH; // adaptive time step: max precession angle per step.
        
        // step
        i=0;
        for(Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next){
            // extrapolate to estimated new state
            field[i].extrapolate(111, hnew);      //0th order extrapolation, dt=whatever (=1)
            m[i].extrapolate(111, mnew);
            mnew.normalize();// not necessary here
            
            torque(mnew, hnew, torquenew);
            cell.m.add(dt, torquenew);
            cell.m.normalize();
            
            i++;
        }
        
        sim.totalTime += dt;
        totalSteps++;
        sim.update();
        
    }
    
    @Override
    public String toString(){
        return "AmuSolver0 (euler type)";
    }
}