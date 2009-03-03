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
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Simulation;
import amu.mag.Unit;

public abstract class AmuSolver {

  protected Simulation sim;

  // root time step
  public double dt;
  // previous root time step (used by extrapolating solvers)
  public double prevDt;

  public int totalSteps;
  // counts number of simulation.update();
  public int totalUpdates;

  
  //public double maxTorque;
  //public double maxH;

  public double stepTime;
  public double realtime; //fraction of the real time speed, e.g. 10ps/s

  public AmuSolver() {
  }

  public void init(Simulation sim) {
    if (this.sim != null) {
      throw new IllegalArgumentException("Solver already initiated");
    } else {
      this.sim = sim;
    }
    initDt();
    Message.title("Solver");
    Message.indent("Type:\t " + toString());
  }

  protected abstract void initDt();

  public void doStep(){
    long t = System.nanoTime();

    // dt is already set by the last step, let's check if we need to trim it for this step
     if(sim.totalTime + dt > sim.maxTime) // trim dt so it fits in the desired run time.
      dt = sim.maxTime - sim.totalTime;

    if(dt == 0.0)
      throw new Bug("dt=0.0");
    if(Double.isNaN(dt))
      throw new Bug("dt=NaN");

    stepImpl(); // also updates dt for the next step

    stepTime = 1E-9 * (System.nanoTime() - t);
    realtime = dt * Unit.TIME / stepTime;
  }

  protected abstract void stepImpl();

  protected void torque(Vector m, Vector h, Vector torque) {

    // - m cross H
    double _mxHx = -m.y * h.z + h.y * m.z;
    double _mxHy = m.x * h.z - h.x * m.z;
    double _mxHz = -m.x * h.y + h.x * m.y;

    // - m cross (m cross H)
    double _mxmxHx = m.y * _mxHz - _mxHy * m.z;
    double _mxmxHy = -m.x * _mxHz + _mxHx * m.z;
    double _mxmxHz = m.x * _mxHy - _mxHx * m.y;

    double gilbert = 1.0 / (1.0 + Cell.alphaLLG * Cell.alphaLLG);
    torque.x = (_mxHx + _mxmxHx * Cell.alphaLLG) * gilbert;
    torque.y = (_mxHy + _mxmxHy * Cell.alphaLLG) * gilbert;
    torque.z = (_mxHz + _mxmxHz * Cell.alphaLLG) * gilbert;
  }

//  /**
//   * Returns the largest field in the system.
//   * Currently broken
//   * @return
//   */
//  protected double maxH() {
//    if(true) throw new Bug("broken by adaptive mesh");
//    double maxH2 = 0.0;
//    int i = 0;
//    for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//      if (cell.h.norm2() > maxH2) {
//        maxH2 = cell.h.norm2();
//      }
//      i++;
//    }
//    double maxH = Math.sqrt(maxH2);
//    return maxH;
//  }
}
