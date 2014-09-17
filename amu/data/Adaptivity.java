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

package amu.data;

import amu.core.Index;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Simulation;
import java.io.IOException;

public final class Adaptivity extends DataModel{

  private Simulation sim;

  public Adaptivity(Simulation sim){
    this.sim = sim;
  }

  @Override
  public void put(int time, Index r, Vector v) throws IOException {
    Cell cell = sim.mesh.getBaseCell(r);
    int level = 0;
    while(cell.updateLeaf == false){
      level++;
      cell = cell.parent;
    }
    v.x = Math.pow(2, level);
  }

  @Override
  public boolean isSpaceDependent() {
    return true;
  }

  @Override
  public Mesh getMesh() {
    return sim.mesh;
  }

  @Override
  public boolean isTimeDependent() {
    return false;
  }

  @Override
  public int getTimeDomain() {
    return -1;
  }

  @Override
  public double[] getTime() {
    return null;
  }

  @Override
  public boolean isVector() {
    return false;
  }

  @Override
  public String getName() {
    return "adaptivity";
  }

  @Override
  public String getUnit() {
    return "1";
  }

  @Override
  public double getTimeForIncrementalSave(){
        return sim.totalTime;
  }

}