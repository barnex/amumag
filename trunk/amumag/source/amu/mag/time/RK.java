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
  protected final double maxTorque() {
    double maxTorque = 0.0;
    for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
      if (cell.updateLeaf) {
        torque(cell.m, cell.h, buffer);
        if (buffer.norm2() > maxTorque) {
          maxTorque = buffer.norm2();
        }
      }
    }
    if(maxTorque == 0.0)
      throw new Bug("Torque = 0");
    maxTorque = Math.sqrt(maxTorque);
    return maxTorque;
  }

   protected final double maxH() {
    double maxH = 0.0;
    for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
      if (cell.updateLeaf) {

        if (cell.h.norm2() > maxH) {
          maxH = cell.h.norm2();
        }
      }
    }
    if(maxH == 0.0)
      throw new Bug("Field = 0");
    maxH = Math.sqrt(maxH);
    return maxH;
  }


  
  @Override
  public void stepImpl() {

    // todo inline and optimize vector ops

    //initial RK4
    final double t0 = sim.totalTime;
    {
      int c = 0;
      for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
        if (cell.updateLeaf) {

          final RKData rkc = rk[c];
          final Vector m = cell.m;
          //backup m
          //rk[c].m0.set(cell.m); inlined:
          rkc.m0.x = m.x;
          rkc.m0.y = m.y;
          rkc.m0.z = m.z;


          final Vector[] k = rkc.k;
          // RK4 step 1
          torque(m, cell.h, k[0]);

        }
        c++;
      }
    }


    //butcher tableau
    for (int i = 1; i < weight.length; i++) {
      //set time and update
      final double[] butcherI = butcher[i];

      {
        int c = 0;
        for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
          if (cell.updateLeaf) {
            final RKData rkc = rk[c];
            final Vector m = cell.m;

            //reset m
            //cell.m.set(rk[c].m0); inlined:
            m.x = rkc.m0.x;
            m.y = rkc.m0.y;
            m.z = rkc.m0.z;

            final Vector[] k = rk[c].k;

            for (int j = 0; j < i; j++) {
              //cell.m.add(dt * butcher[i][j], k[i-1]); inlined:
              final Vector kI_1 = k[i - 1];
              final double dtXbutcherIJ = dt * butcherI[j];
              m.x += dtXbutcherIJ * kI_1.x;
              m.y += dtXbutcherIJ * kI_1.y;
              m.z += dtXbutcherIJ * kI_1.z;

            }

            m.normalize();
            // push_down;
            // cell.child1.m.set(m);
            // cell.child2.m.set(m);
            // it seems the java function call overhead trashes performance if we
            // propagate m down recursively, so let's hand-code it ... sigh.
            // so we have to limit ourselves to a few levels, should be ok most
            // of the time but we should check this once...
            // alas, this cripples the output, unles we only use 2 adaptive mesh levels.
            final Cell child1 = cell.child1;
            final Cell child2 = cell.child2;
            if (child1 != null) {
              child1.m.x = child2.m.x = m.x;
              child1.m.y = child2.m.y = m.y;
              child1.m.z = child2.m.z = m.z;

              if (child1.child1 != null) {
                child1.child1.m.x = child1.child2.m.x = child2.child1.m.x = child2.child2.m.x;
                child1.child1.m.y = child1.child2.m.y = child2.child1.m.y = child2.child2.m.y;
                child1.child1.m.z = child1.child2.m.z = child2.child1.m.z = child2.child2.m.z;
              }
            }
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
            torque(cell.m, cell.h, rk[c].k[i]);
          }
          c++;
        }
      }
    }


    //new m
    {
      int c = 0;
      for (Cell cell = sim.mesh.coarseRoot; cell != null; cell = cell.next) {
        if (cell.updateLeaf) {
          //reset

          final RKData rkc = rk[c];
          final Vector m = cell.m;

          //cell.m.set(rk[c].m0); inlined:
          m.x = rkc.m0.x;
          m.y = rkc.m0.y;
          m.z = rkc.m0.z;

          Vector[] k = rk[c].k;

          for (int i = 0; i < weight.length; i++) {
            m.add(dt * weight[i], k[i]);
          }

          m.normalize();
          // push_down.

          final Cell child1 = cell.child1;
            final Cell child2 = cell.child2;
            if (child1 != null) {
              child1.m.x = child2.m.x = m.x;
              child1.m.y = child2.m.y = m.y;
              child1.m.z = child2.m.z = m.z;

              if (child1.child1 != null) {
                child1.child1.m.x = child1.child2.m.x = child2.child1.m.x = child2.child2.m.x;
                child1.child1.m.y = child1.child2.m.y = child2.child1.m.y = child2.child2.m.y;
                child1.child1.m.z = child1.child2.m.z = child2.child1.m.z = child2.child2.m.z;
              }
            }

        }
        c++;
      }
    }
    sim.update();

    // (3) bookkeeping
    sim.totalTime = t0 + dt;
    totalSteps++;

  }
}