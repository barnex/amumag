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

public final class RKF45 extends AmuSolver {

//   0 &&&&& \\
//   1/4  & 1/4  &&&& \\
//   3/8  & 3/32  & 9/32 &&& \\
//   12/13  & 1932/2197  & -7200/2197  & 7296/2197 && \\
//   1 & 439/216 & -8 & 3680/513 & -845/4104 & \\
//   1/2 &   -8/27 &   2  &  -3544/2565 &   1859/4104  &  -11/40 \\\hline
//
//    16/135  &0&  6656/12825&  28561/56430&  -9/50 & 2/55 \\
//    25/216  &0&  1408/2565  &2197/4104&  -1/5 & 0 \\

  public static final double[][] butcher = new double[][]{
    {   NaN,         NaN,           NaN,           NaN,           NaN},
    {   1.0/4.0,     NaN,           NaN,           NaN,           NaN},
    {   3.0/32.0,    9.0/32.0,      NaN,           NaN,           NaN},
    {1932.0/2197.0, -7200.0/2197.0, 7296.0/2197.0, NaN,           NaN},
    { 439.0/216.0,  -8.0,           3680.0/513.0, -845.0/4104.0,  NaN},
    {  -8.0/27.0,    2.0,          -3544.0/2565.0, 1859.0/4104.0, -11.0/40.0}};
  
  public static final double[] h = new double[]{NaN, 1.0/4.0, 3.0/8.0, 12.0/13.0, 1.0, 1.0/2.0};

  public static final double[] weight5 = new double[]{16.0/135.0, 0.0, 6656.0/12825.0, 28561.0/56430.0, -9.0/50.0, 2.0/55.0};
  public static final double[] weight4 = new double[]{25.0/216.0, 0.0, 1408.0/2565.0,  2197.0/4104.0,   -1.0/5.0,  0.0};

  private boolean adaptiveStep = true;
  private double maxDphi;
  private RKData[] rk;

  public RKF45(double maxDphi) {
    this.maxDphi = maxDphi;
  }

  @Override
  public void init(Simulation sim) {
    super.init(sim);
    rk = new RKData[sim.mesh.cells];
    for(int i=0; i<rk.length; i++)
      rk[i] = new RKData(weight5.length); //=order
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
    for(int i=1; i<weight5.length; i++){
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

       for(int i=0; i<weight5.length; i++)
        cell.m.add(dt * weight5[i], k[i]);
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