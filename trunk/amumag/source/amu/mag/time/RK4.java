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
import static java.lang.Double.NaN;

public final class RK4 extends RK {

  protected double maxDphi;

  public RK4(double maxDphi) {
    this.maxDphi = maxDphi;
    butcher = new double[][]{
              {NaN, NaN, NaN, NaN},
              {1.0 / 2.0, NaN, NaN, NaN},
              {0.0, 1.0 / 2.0, NaN, NaN},
              {0.0, 0.0, 1.0, NaN, NaN}};
    h = new double[]{NaN, 1.0 / 2.0, 1.0 / 2.0, 1.0};
    weight = new double[]{1.0 / 6.0, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 6.0};
  }

  @Override
  protected void updateDt() {
     // (0) not used in this solver, but by some differentiating data models.
    prevDt = dt;


    // (1) determine the intrinsic time scale of the system in its present state
    // and use it for the time step.
    // adaptive time step = max precession angle per step.
    // todo: multiply by the appropriate factor for large damping or no precession.
    //if (adaptiveStep) {
      dt = maxDphi / maxH();
      double gilbert = 1.0 / (1.0 + Cell.alphaLLG * Cell.alphaLLG);
      dt /= (Cell.alphaLLG * gilbert * 2.0);
    //} else {
    //  dt = maxDphi;
    //}


    if (Double.isInfinite(dt)) {
      Message.warning("dt=Infinity");
      dt = maxDphi / 10;
    }
  }
}