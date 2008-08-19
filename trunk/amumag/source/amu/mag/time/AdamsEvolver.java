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
import static java.lang.Math.*;

/**
 *
 * Partly based on OOMMF code (grid.cc) by M. Donahue and D. Porter.
 */
public class AdamsEvolver extends Evolver{

    public double dt;                   //time step
    private final double alpha;         //damping, will be copied to Cell.alpha on init()
    
    private double maxErrorPerStep; 
    public double stepError;
    public int badSteps;
    //public double eEx, eDemag, eTotal;
            
    public AdamsEvolver(Simulation sim, double alpha, double maxErrorPerStep){
        super(sim);
        this.dt = 0.00001;       //just a guess for a good initial dt, todo: refine this.
        this.alpha = alpha;
        this.sim = sim;
        this.maxErrorPerStep = maxErrorPerStep;
    }
    
    /**
     * 
     */
    public void init() {
       Cell.alpha=alpha; 
       sim.update();
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
           cell.dmdt_previous.set(cell.torque);
       }
    }
    
    public void stepImpl() {
        tryStep();

        double increase = cbrt(maxErrorPerStep / stepError);
        if (stepError < maxErrorPerStep) {    //good step
            //never increase step by more than 10%
            if (increase < 1.1) {
                dt *= increase;
            } else {
                dt *= 1.1;
            }
        } else {   //bad step
            //never decrease by more than 90%
            if (increase > 0.1) {
                dt *= increase;
            } else {
                dt *= 0.1;
            }
        }
    }
   
    
    public void tryStep() {
        
       stepError = 0.0;
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next)
            cell.dmdt_previous.set(cell.torque);
       
       sim.update();
       
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
           
           final Vector m = cell.m;
           final Vector dmdt = cell.torque;
           final Vector m_backup = cell.m_backup;
           final Vector dmdt_backup = cell.dmdt_backup;
           final Vector d = cell.dmdt_previous;
           
           m_backup.set(m);        
           dmdt_backup.set(dmdt);

            // quadratic estimation of m at time +1
            m.x = m_backup.x + dt * (1.5 * dmdt.x - 0.5 * d.x);
            m.y = m_backup.y + dt * (1.5 * dmdt.y - 0.5 * d.y);
            m.z = m_backup.z + dt * (1.5 * dmdt.z - 0.5 * d.z);
            m.normalizeSafe();//*/
        }

        // calc new fields based on estimated new m
        sim.update();

        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
            final Vector m = cell.m;
            final Vector dmdt = cell.torque;
            final Vector m_backup = cell.m_backup;
            final Vector dmdt_backup = cell.dmdt_backup;
            final Vector m_estimate = cell.m_estimate;
            final Vector dmdt_previous = cell.dmdt_previous;

            m_estimate.set(m);   //save the estimated m(t+1)
            
            m.x = m_backup.x + dt * (1.5 * dmdt.x - 0.5 * dmdt_previous.x);
            m.y = m_backup.y + dt * (1.5 * dmdt.y - 0.5 * dmdt_previous.y);
            m.z = m_backup.z + dt * (1.5 * dmdt.z - 0.5 * dmdt_previous.z);
            m.normalizeSafe();

            m_estimate.subtract(m);
            cell.stepError.set(m_estimate);
            if(m_estimate.norm() > stepError)
                stepError = m_estimate.norm();
            
        }
        sim.totalIteration++;
        sim.totalTime += dt;
    }
}
