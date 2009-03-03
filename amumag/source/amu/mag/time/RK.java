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
import static java.lang.Double.NaN;

/**
 * Superclass for the Runge-Kutta family of solvers.
 * @author arne
 */
public abstract class RK extends AmuSolver {

  // butcher tableau
  protected double[][] butcher;
  // "h"-coefficients of butcher tableau
  protected double[] h;
  // weight of k1, k2, ... in final result
  protected double[] weight;

  protected RKData[] rk;

  public RK() {
    
  }

  @Override
  public void init(Simulation sim) {
    super.init(sim);
    rk = new RKData[sim.mesh.cells]; // too much...
    for(int i=0; i<rk.length; i++)
      rk[i] = new RKData(weight.length); //=order

  }


  protected abstract void updateDt();

  private final Vector buffer = new Vector();
  protected double maxTorque() {
    double maxTorque = 0.0;
    for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
      if (cell.updateLeaf) {
        torque(cell.m, cell.h, buffer);
        if (buffer.norm2() > maxTorque) {
          maxTorque = buffer.norm2();
        }
      }
    }
    maxTorque = Math.sqrt(maxTorque);
    return maxTorque;
  }

   protected double maxH() {
    double maxH = 0.0;
    for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
      if (cell.updateLeaf) {

        if (cell.h.norm2() > maxH) {
          maxH = cell.h.norm2();
        }
      }
    }
    maxH = Math.sqrt(maxH);
    return maxH;
  }

  protected void maxDm(){

  }
  
  @Override
  public void stepImpl() {

    updateDt();

    //initial RK4
    double t0 = sim.totalTime;
    {int c = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
       //backup m
       rk[c].m0.set(cell.m);

       Vector[] k = rk[c].k;
       // RK4 step 1
       torque(cell.m, cell.h, k[0]);
    c++;}}

    
    //butcher tableau
    for(int i=1; i<weight.length; i++){
      //set time and update
      
      {int c = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
        //reset m
        cell.m.set(rk[c].m0);
        Vector[] k = rk[c].k;

        for(int j=0; j<i; j++){
          cell.m.add(dt * butcher[i][j], k[i-1]);
        }
       cell.m.normalize();
      c++;}
      }
      sim.totalTime = t0 + h[i]*dt;
      sim.update();

      //new k
      {int c = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
        torque(cell.m, cell.h, rk[c].k[i]);
        c++;}
      }
    }
    
    
    //new m
    {int c = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
       //reset
       cell.m.set(rk[c].m0);
       Vector[] k = rk[c].k;

       for(int i=0; i<weight.length; i++)
        cell.m.add(dt * weight[i], k[i]);
       cell.m.normalize();
      c++;}
      }
    sim.update();

    // (3) bookkeeping
    sim.totalTime = t0 + dt;
    totalSteps++;
   
  }
}