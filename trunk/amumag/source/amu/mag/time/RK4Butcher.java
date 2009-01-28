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

public final class RK4Butcher extends AmuSolver {

  public double[][] butcher = new double[][]{
    {NaN,     NaN,      NaN, NaN},
    {1.0/2.0, NaN,      NaN, NaN},
    {0.0,     1.0/2.0,  NaN, NaN},
    {0.0,     0.0, 1.0, NaN, NaN}};
  public final double[] h = new double[]{NaN, 1.0/2.0, 1.0/2.0, 1.0};
  public static final double[] weight = new double[]{1.0/6.0, 1.0/3.0, 1.0/3.0, 1.0/6.0};

  private boolean adaptiveStep = true;
  private double maxDphi;
  private RKData[] rk;

  public RK4Butcher(double maxDphi) {
    this.maxDphi = maxDphi;
  }

  @Override
  public void init(Simulation sim) {
    super.init(sim);
    rk = new RKData[sim.mesh.cells];
    for(int i=0; i<rk.length; i++)
      rk[i] = new RKData(weight.length); //=order
  }

  @Override
  public void stepImpl() {

    // (0) not used in this solver, but by some differentiating data models.
    prevDt = dt;


    // (1) determine the intrinsic time scale of the system in its present state
    // and use it for the time step.
    // adaptive time step = max precession angle per step.
    // todo: multiply by the appropriate factor for large damping or no precession.
    if (adaptiveStep) {
      dt = maxDphi / maxH();
      double gilbert = 1.0 / (1.0 + Cell.alphaLLG * Cell.alphaLLG);
      dt /= (Cell.alphaLLG * gilbert * 2.0);
    } else {
      dt = maxDphi;
    }


    if (Double.isInfinite(dt)) {
      Message.warning("dt=Infinity");
      dt = maxDphi / 10;
    }

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
    //fields have been asynchronously updated, some have been invalidated to
    //detect bugs, add up the pieces again to get the correct h.
    //sim.mesh.rootCell.resyncH();
  }
}