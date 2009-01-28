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

public final class RK4 extends AmuSolver {

  private boolean adaptiveStep = true;
  private double maxDphi;
  private RK4Data[] rk4;

  public RK4(double maxDphi) {
    this.maxDphi = maxDphi;
  }

  @Override
  public void init(Simulation sim) {
    super.init(sim);
    rk4 = new RK4Data[sim.mesh.cells];
    for(int i=0; i<rk4.length; i++)
      rk4[i] = new RK4Data();
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

    {int i = 0; for (Cell c = sim.mesh.baseRoot; c != null; c = c.next) {
       //backup m
       rk4[i].m0.set(c.m);

       // RK4 step 1
       // k1
       torque(c.m, c.h, rk4[i].k1);

       // RK4 step 2
       // go forward 0.5*h
       c.m.add(0.5*dt, rk4[i].k1);
       c.m.normalize();
    i++;}}

    sim.totalTime += 0.5*dt;
    sim.update();

    {int i = 0; for (Cell c = sim.mesh.baseRoot; c != null; c = c.next) {
       // k2
       torque(c.m, c.h, rk4[i].k2);

       // RK4 step 3
       // go from backup to m+0.5*h*k2
       c.m.set(rk4[i].m0);
       c.m.add(0.5*dt, rk4[i].k2);
       c.m.normalize();
    i++;}}

    // time stands still at +0.5*h here
    sim.update();

    {int i = 0; for (Cell c = sim.mesh.baseRoot; c != null; c = c.next) {
       // k3
       torque(c.m, c.h, rk4[i].k3);
       
       // RK4 step 4
       // go from backup to m+h*k3
       c.m.set(rk4[i].m0);
       c.m.add(dt, rk4[i].k3);
       c.m.normalize();
    i++;}}

    // from +0.5*h to +1*h;
    sim.totalTime += 0.5*dt;
    sim.update();

    {int i = 0; for (Cell c = sim.mesh.baseRoot; c != null; c = c.next) {
        //k4
        torque(c.m, c.h, rk4[i].k4);

        //revert to backup m
        c.m.set(rk4[i].m0);

        //add average slope
        double kx = (1.0/6.0) * (rk4[i].k1.x + 2.0*rk4[i].k2.x + 2.0*rk4[i].k3.x + rk4[i].k4.x);
        double ky = (1.0/6.0) * (rk4[i].k1.y + 2.0*rk4[i].k2.y + 2.0*rk4[i].k3.y + rk4[i].k4.y);
        double kz = (1.0/6.0) * (rk4[i].k1.z + 2.0*rk4[i].k2.z + 2.0*rk4[i].k3.z + rk4[i].k4.z);
        c.m.x += dt*kx;
        c.m.y += dt*ky;
        c.m.z += dt*kz;
        
        c.m.normalize();
    i++;}}

    sim.update();

    // (3) bookkeeping
    //sim.totalTime += dt; //already done!
    totalSteps++;
    //fields have been asynchronously updated, some have been invalidated to
    //detect bugs, add up the pieces again to get the correct h.
    //sim.mesh.rootCell.resyncH();
  }
}