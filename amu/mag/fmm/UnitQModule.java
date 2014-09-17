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
package amu.mag.fmm;

import amu.core.ArrayPool;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import amu.io.Message;
import java.util.Hashtable;
import static java.lang.Math.*;

/**
 *
 */
public abstract class UnitQModule {

  protected final Mesh mesh;
  private final ArrayPool<Multipole> pool;
  public static double ZERO_LIMIT = 1E-13;

  public UnitQModule(Mesh mesh) {
    this.mesh = mesh;
    pool = new ArrayPool<Multipole>(true);
  }

  // !! 2007-10-16: In order for multipoles to be able to be equal, we need to
  // round very small multipole moments to zero (which they probably also are if they
  // were calculated exactly), otherwise they will not be recognized as being equal,
  // despite the (float) cast.
  public static double round(double r) {
    if (abs(r) < ZERO_LIMIT) {
      return 0.0;
    } else {
      return r;
    }
  }

  public void init() {

    int total = 0, different = 0;

    Hashtable<Cell, Multipole[]> hash = new Hashtable<Cell, Multipole[]>();

    for (Cell cell = mesh.coarseRoot; cell != null; cell = cell.next) {
      total++;
      // todo: weigh outer faces * 2, include in cell equality
      if (hash.containsKey(cell)) {
        cell.unitQ = hash.get(cell);
      } else {
        different++;
        Multipole[] unitQ = calcUnitQ(cell);
        hash.put(cell, unitQ);
        cell.unitQ = unitQ;
      }
    }
    Message.debug("#unitQ = " + total + ", " + different + " are different");
    Message.debug("multipole pool: " + pool.getStats());

    System.gc();
  }

  public Multipole[] calcUnitQ(Cell cell) {
    Vector center = cell.center;                                //2007-08-29: big difference center - fmmCenter !! changed to center
    Face[] faces = cell.faces;
    Multipole[] unitQ = new Multipole[faces.length];
    for (int i = 0; i < faces.length; i++) {
      //for all faces of each cell
      Face face = faces[i];

      Multipole q = new Multipole();
      q.q = new double[FMM.monomials.length];                 //should become one nice constructor
      for (int i_n = 0; i_n < FMM.monomials.length; i_n++) {
        // Qn = 1/n! int( rho(r)*r^n d3r )
        IntVector n = FMM.monomials[i_n];
        // faces belong only for 50% to each cell: *0.5, except for outer faces
        double faceWeight = face.sideness == 0 ? 0.5 : 1.0;
        q.q[i_n] = round(faceWeight * integrate(center, face, n) * 1.0 / n.factorial); // not 100% sure 1/n! should be here
      }
      //System.out.println(Arrays.toString(q.q));
      unitQ[i] = q;
    }
    unitQ = pool.intern(unitQ); // 2008-08-30
    return unitQ;
  }

  protected abstract double integrate(Vector center, Face face, IntVector n);
}
