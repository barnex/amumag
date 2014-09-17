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
package amu.mag;

import amu.debug.Bug;
import amu.geom.Vector;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.geom.Vector.UNIT;
import static java.lang.Math.*;
import amu.geom.GeomModule;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A charged face in the mesh
 */
public final class Face implements Serializable {

  /* indicates on which side of a cell this face lies: +-1 for a face at the
   * mesh boundary (left-right) or 0 for a face inside the mesh. */
  public byte sideness;

  // counts the number of charges added to the face,
  // should not become 2 for inner faces
  // and 1 for outer faces
  public byte adhocChargeCounter;

  public Vector[] vertex;
  public Vector center;
  //TODO: replace by on-the-fly getArea() ?
  public double scalarArea;
  public double charge;
  public transient Face next;                                                           //all faces are in a linked list
  private final int hashCode;
  public static final int LEFT = 0,  RIGHT = 1;

  public Face(Vector[] vertex) {
    this.vertex = vertex;
    for (Vector v : vertex) {
      v.makeImmutable();
    }
    hashCode = Arrays.deepHashCode(vertex);
    center = GeomModule.barycentrumPlane(vertex);
    // area of 4 subtriangles.
    for (int i = 0; i < 4; i++) {
      scalarArea += GeomModule.area(vertex[i], vertex[(i + 1) % 4], center);
    }
  /*if(area < 1E-14)
  throw new Bug("face area = " + area);*/

  }

  //__________________________________________________________________________
  public double getChargeDensity() {
    return charge / scalarArea;
  }
  
  //__________________________________________________________________________
  /**
   * Checks for equal vertices.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Face)) // should be redundant
    {
      throw new Bug();
    }

    if (hashCode != obj.hashCode()) {
      return false;
    } else {
      Face other = (Face) obj;
      //return Arrays.deepEquals(vertex, other.vertex);
      //2007-dec-06: changed equals to be more accurate, is this the same?
      for (int v = 0; v < vertex.length; v++) {
        if (!vertex[v].equals(other.vertex[v])) {
          return false;
        }
      }
      return true;
    }
  }

  //__________________________________________________________________________
  @Override
  public int hashCode() {
    return hashCode;
  }

  //__________________________________________________________________________
  @Override
  public String toString() {
    return "Face" + System.identityHashCode(this);
  }
}
