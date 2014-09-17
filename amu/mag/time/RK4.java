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
import static java.lang.Double.NaN;

/**
 * The classic Runge-Kutta 4th order method with either fixed time step,
 * fixed maximum dm/dt or fixed maximum dPhi/dt.
 * @author arne
 */
public final class RK4 extends RK {
  private double maxDphi = -1;
  private double maxDm = -1;
  private double maxDt = -1;

    public RK4(String flag, double value) {

    if(value <= 0.0)
      throw new IllegalArgumentException("value should be > 0");

    if("dt".equalsIgnoreCase(flag))
      maxDt = value;
    else if("dPhi".equalsIgnoreCase(flag))
      maxDphi = value;
    else if("dm".equalsIgnoreCase(flag))
      maxDm = value;
    else
      throw new IllegalArgumentException("expected dt, dPhi or dm, not: " + flag);

    butcher = new double[][]{
              {NaN, NaN, NaN, NaN},
              {1.0 / 2.0, NaN, NaN, NaN},
              {0.0, 1.0 / 2.0, NaN, NaN},
              {0.0, 0.0, 1.0, NaN, NaN}};
    h = new double[]{NaN, 1.0 / 2.0, 1.0 / 2.0, 1.0};
    weight = new double[]{1.0 / 6.0, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 6.0};
  }

  @Override
  public void stepImpl(){
    updateDt();
    super.stepImpl();
  }
  
  @Override
  protected void updateDt() {

    // (0) not used in this solver, but by some differentiating data models.
    prevDt = dt;

    dt = Double.POSITIVE_INFINITY;

    if(maxDt > 0){
      dt = maxDt;
    }

    if(maxDphi > 0){
      double dt_phi = 2*Math.PI * maxDphi / maxH();
      if(dt_phi < dt)
        dt = dt_phi;
    }

    if(maxDm > 0){
      double dt_m = maxDm / maxTorque();
      if(dt_m < dt)
        dt = dt_m;
    }
    
    // debug
    if (Double.isInfinite(dt)) {
      Message.warning("dt=Infinity");
      dt = 0.001;
    }

    // important: make sure we do not go beyond the requested run time!
    dt = sim.cropDt(dt);
  }

  /**
   * @return the maxDphi
   */
  public double getMaxDphi() {
    return maxDphi;
  }

  /**
   * @param maxDphi the maxDphi to set
   */
  public void setMaxDphi(double maxDphi) {
    this.maxDphi = maxDphi;
  }

  /**
   * @return the maxDm
   */
  public double getMaxDm() {
    return maxDm;
  }

  /**
   * @param maxDm the maxDm to set
   */
  public void setMaxDm(double maxDm) {
    this.maxDm = maxDm;
  }

  /**
   * @return the maxDt
   */
  public double getMaxDt() {
    return maxDt;
  }

  /**
   * @param maxDt the maxDt to set
   */
  public void setMaxDt(double maxDt) {
    this.maxDt = maxDt;
  }

  @Override
  protected void initDt() {
    updateDt();
  }
}