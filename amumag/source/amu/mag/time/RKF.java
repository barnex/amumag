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
import static java.lang.Math.sqrt;

public abstract class RKF extends RK {

  // not thread-safe!
  protected final Vector mErr = new Vector();
  protected double[] weightForError;
  protected double errorPerStep;
  public double lastError;
  public double badSteps = 0; //is double so that it can be saved via reflection

  public RKF(double errorPerStep) {
    dt = 0.001; //initial dt;
    this.errorPerStep = errorPerStep;
  }

  /**
   * called AFTER a step, to update for the next step
   */
  protected void updateDt() {

    double scale = sqrt(sqrt((errorPerStep) / (2.0 * lastError)));
    if (scale > 0.8) {  // if we do not decrease too much, or increase, then
      scale = 1.0 + (scale - 1.0) * 0.8;  // we can adjust the step smoother.
    }

    dt *= scale;

    if (Double.isInfinite(dt)) {
      Message.warning("dt=infinity");
      dt = 0.01; //adhoc
    }
  }

  @Override
  public void stepImpl() {
    double t0 = sim.totalTime;
    prevDt = dt;

    tryStep();
    int trials = 0;

    while (lastError > 2.0 * errorPerStep) { //twice the tolarable error is really to much, revert the step
      badSteps++;
      updateDt();
      dt /= Math.pow(2.0, trials); //just to be sure...
      revert(t0);
      tryStep();
      trials++;
    }

    updateDt();

    totalSteps++;
  }

  protected void revert(double t0) {
    {
      int c = 0;
      for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
        if(cell.updateLeaf)
          cell.m.set(rk[c].m0);
        c++;
      }
    }
    sim.totalTime = t0;
  }

  private final void stage1(Cell cell, int c) {
    //backup m
    rk[c].m0.set(cell.m);

    Vector[] k = rk[c].k;
    // RK4 step 1
    torque(cell.m, cell.h, k[0]);
  }

  private final void stage2(Cell cell, int c, int i) {
    //reset m
    cell.m.set(rk[c].m0);
    Vector[] k = rk[c].k;

    for (int j = 0; j < i; j++) {
      cell.m.add(dt * butcher[i][j], k[i - 1]);
    }
    cell.m.normalize();
  }

  private final void stage3(Cell cell, int c, int i) {
    torque(cell.m, cell.h, rk[c].k[i]);
  }

  private final void stage4(Cell cell, int c) {
    //reset
    cell.m.set(rk[c].m0);
    Vector[] k = rk[c].k;
    mErr.set(rk[c].m0);

    for (int i = 0; i < weight.length; i++) {
      cell.m.add(dt * weight[i], k[i]);         //desired order approximation
      mErr.add(dt * weightForError[i], k[i]);   //order for error estimate
    }
    cell.m.normalize();
    mErr.normalize();
    mErr.subtract(cell.m);
    if (mErr.maxNorm() > lastError) {
      lastError = mErr.maxNorm();
    }
  }

  protected final void tryStep() {

    sim.mesh.aMRules.update();

    //initial RK4
    double t0 = sim.totalTime;
    {
      int c = 0;
      for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
        if (cell.updateLeaf) {
          stage1(cell, c);
        }
        c++;
      }
    }


    //butcher tableau
    for (int i = 1; i < weight.length; i++) {
      //set time and update

      {
        int c = 0;
        for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
          if (cell.updateLeaf) {
            stage2(cell, c, i);
          }

          c++;
        }
      }

      sim.totalTime = t0 + h[i] * dt;
      sim.update();

      //new k
      {
        int c = 0;
        for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
          if (cell.updateLeaf) {
            stage3(cell, c, i);
          }

          c++;
        }
      }
    }


    //new m + error
    lastError = 0.0;
    {
      int c = 0;
      for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
        if (cell.updateLeaf) {
          stage4(cell, c);
        }

        c++;
      }
    }
    //lastError = Math.sqrt(lastError); //we took the square so far...


    sim.totalTime = t0 + dt;
    sim.update();

  }
}