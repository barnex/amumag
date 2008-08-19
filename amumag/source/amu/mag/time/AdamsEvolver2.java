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
import amu.io.Message;
import static java.lang.Math.*;
import static java.lang.Double.POSITIVE_INFINITY;

public class AdamsEvolver2 extends Evolver{
    
    public double debug;
    
    //time step
    public double dt = 1E-5;
    public double prevDt = POSITIVE_INFINITY;
    
    //damping, will be copied to Cell.alpha on init()
    private final double alpha;         
    
    //target error settings
    public double 
            targetMaxAbsError = POSITIVE_INFINITY, 
            targetMaxRelError = POSITIVE_INFINITY,
            targetRmsAbsError = POSITIVE_INFINITY, 
            targetRmsRelError = POSITIVE_INFINITY,
            targetMaxDm       = POSITIVE_INFINITY;
            
    //statistics for last step
    public double
            rmsAbsStepError,
            rmsRelStepError, 
            maxAbsStepError, 
            maxRelStepError,
            maxDm;
    
    public double maxTorque;
    
    public AdamsEvolver2(Simulation sim, double alpha){
        super(sim);
        this.alpha = alpha;
        this.sim = sim;
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
        prevDt = dt;
        
        //absolute errors scale with the 3d power of the step,
        //relative errors scale with the 2nd power of the step.  ??
        
        //2008-02-21: it seems that maxAbsStep error scales as dt**2, not dt**3,
        // -> only when time step is already too large.
        
        double increase1 = cbrt(targetRmsAbsError / rmsAbsStepError);
        double increase2 = sqrt(targetRmsRelError / rmsRelStepError);
                
        double increase3 = cbrt(targetMaxAbsError / maxAbsStepError);
        double increase4 = sqrt(targetMaxRelError / maxRelStepError);
        
        double increase = min(min(increase1, increase2), min(increase3, increase4));  

        //2008-02-13: fixed bug: dt could be multiplied two times.
        if(increase > 1.5)
            increase = 1.5;
        else if(increase < 0.5)
            increase = 0.5;
        dt *= increase;
    }
   
    
    public void tryStep() {
       
       maxAbsStepError = 0.0;
       maxRelStepError = 0.0;
       rmsAbsStepError = 0.0;
       rmsRelStepError = 0.0;
       maxTorque = 0.0;
       maxDm = 0.0;
       
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next){
            //cell.dmdt_previous.set(cell.torque);
           cell.dmdt_previous.x = cell.torque.x;
           cell.dmdt_previous.y = cell.torque.y;
           cell.dmdt_previous.z = cell.torque.z;
       }
       
       sim.update();
       // 2008-06-11: bugfix: moved this code here, after sim.update:
       // if the conditions make a big step(field, precession on/off, ...),
       // maxDt should be calculated after the update!
       // limit maximal initial magnetization step (estimate).
       
       //make sure maxTorque is updated using the updated sim fields.
        additionalUpdateMaxTorque();
        debug = maxTorque*dt;
        if(maxTorque * dt > targetMaxDm){
            dt = targetMaxDm / maxTorque;
            Message.debug("Step limited by maxDm=" + targetMaxDm);
        }
       
       
       for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
           
           final Vector m = cell.m;
           final Vector dmdt = cell.torque;
           final Vector m_backup = cell.m_backup;
           //final Vector dmdt_backup = cell.dmdt_backup;
           final Vector dmdt_previous = cell.dmdt_previous;
           
           //m_backup.set(m);        
           //dmdt_backup.set(dmdt);
           m_backup.x = m.x;
           m_backup.y = m.y;
           m_backup.z = m.z;
           
           

            // quadratic estimation of m at time +1
            // !!
            double dmx = dmdt.x * dt + 0.5 * (dmdt.x - dmdt_previous.x)/prevDt * dt*dt;
            double dmy = dmdt.y * dt + 0.5 * (dmdt.y - dmdt_previous.y)/prevDt * dt*dt;
            double dmz = dmdt.z * dt + 0.5 * (dmdt.z - dmdt_previous.z)/prevDt * dt*dt;
            
            m.x = m_backup.x + dmx;
            m.y = m_backup.y + dmy;
            m.z = m_backup.z + dmz;
            //m.normalizeSafe();//inlined:
            double invnorm = 1.0/sqrt(m.x*m.x + m.y*m.y + m.z*m.z);
            m.x *= invnorm;
            m.y *= invnorm;
            m.z *= invnorm;
            
            if(sqrt(dmx*dmx + dmy*dmy + dmz*dmz) > maxDm)       //todo: abort here on bad step estimate
                maxDm = sqrt(dmx*dmx + dmy*dmy + dmz*dmz);
            
        }

        // calc new fields based on estimated new m
        sim.update();

        int count = 0;
        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
            count++;
            final Vector m = cell.m;
            final Vector dmdt = cell.torque;
            final Vector m_backup = cell.m_backup;
            final Vector dmdt_backup = cell.dmdt_backup;
            final Vector m_estimate = cell.m_estimate;
            final Vector dmdt_previous = cell.dmdt_previous;

            //m_estimate.set(m);   //save the estimated m(t+1)
            m_estimate.x = m.x;
            m_estimate.y = m.y;
            m_estimate.z = m.z;
            
            double dmx = dmdt.x * dt + 0.5 * (dmdt.x - dmdt_previous.x)/prevDt * dt*dt;
            double dmy = dmdt.y * dt + 0.5 * (dmdt.y - dmdt_previous.y)/prevDt * dt*dt;
            double dmz = dmdt.z * dt + 0.5 * (dmdt.z - dmdt_previous.z)/prevDt * dt*dt;
            
            m.x = m_backup.x + dmx;
            m.y = m_backup.y + dmy;
            m.z = m_backup.z + dmz;
            //m.normalizeSafe();//inlined:
            double invnorm = 1.0/sqrt(m.x*m.x + m.y*m.y + m.z*m.z);
            m.x *= invnorm;
            m.y *= invnorm;
            m.z *= invnorm;

            Vector stepError = m_estimate;
            stepError.subtract(m);
            cell.stepError.set(stepError);  //stepError
            
            double stepErrorNorm = stepError.norm();
            double dmNorm = sqrt(dmx*dmx+dmy*dmy+dmz*dmz);
            double relErrorNorm = stepErrorNorm/dmNorm;
            
            if(stepErrorNorm > maxAbsStepError)
                maxAbsStepError = stepErrorNorm;
            if(relErrorNorm > maxRelStepError)
                maxRelStepError = relErrorNorm;
            rmsAbsStepError += stepErrorNorm * stepErrorNorm;
            rmsRelStepError += relErrorNorm *relErrorNorm;
            if(cell.torque.norm2() > maxTorque) // temporarily this is maxTorque^2
                maxTorque = cell.torque.norm2();
        
        }
        rmsAbsStepError /= count;
        rmsRelStepError /= count;
        maxTorque = sqrt(maxTorque); //now it is maxTorque^1
        sim.totalIteration++;
        sim.totalTime += dt;
    }

    private void additionalUpdateMaxTorque(){
        maxTorque = 0.0;
        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
            if(cell.torque.norm2() > maxTorque) // temporarily this is maxTorque^2
                maxTorque = cell.torque.norm2();
        }
        maxTorque = sqrt(maxTorque); //now it is maxTorque^1
    }
    
    public double get_dt() {
        return dt;
    }

    public double get_maxAbsStepError() {
        return maxAbsStepError;
    }

    public double get_maxRelStepError() {
        return maxRelStepError;
    }

    public double get_rmsAbsStepError() {
        return rmsAbsStepError;
    }

    public double get_rmsRelStepError() {
        return rmsRelStepError;
    }
    
    public double get_maxDm(){
        return maxDm;
    }
}
