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
package amu.geom;

import amu.core.ArrayPool;
import amu.core.Equality;
import amu.debug.Bug;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.geom.Vector.UNIT;
import amu.core.Index;
import amu.io.Message;

/**
 * Creates faces for a mesh.
 *
 *
 */
public final class FaceModule {

  private Mesh mesh;
  private int count;
  private ArrayPool<Vector> normalVectorPool;

  public FaceModule(Mesh mesh) {
    this.mesh = mesh;
    normalVectorPool = new ArrayPool<Vector>(true);
  }

  /**
   * Create faces starting from the coarse level and smaller.
   */
  public void createFacesAndNormalVectors() {
    Message.debug("createFacesAndNormalVecrtors()");

    //2009-02-26: adaptive mesh: start from course level
    for (int l = mesh.coarseLevelIndex; l < mesh.nLevels; l++) {
      Cell[][][] level = mesh.levels[l];
      for (Cell[][] levelI : level) {
        for (Cell[] levelIJ : levelI) {
          for (Cell cell : levelIJ) {
            if (cell != null) {
              createFaces(cell);
              createNormalVectors(cell);
              check(cell);
            }
          }
        }
      }
    }
    Message.debug("#faces = " + count);
    Message.debug("normal vector pool: " + normalVectorPool.getStats());
    normalVectorPool = null;

    System.gc();
  }
  // U-coordinate of a path around the face.
  private static int[] pathV = new int[]{0, 1, 1, 0};
  // V-coordinate of a path around the face.
  private static int[] pathW = new int[]{0, 0, 1, 1};
  private Vector sum = new Vector();

  private void check(Cell cell) {
    sum.reset();
    for (int i = 0; i < cell.normal.length; i++) {
      sum.add(cell.normal[i]);
    }
    if (sum.norm() > 1E-10) {
      Message.warning("cell vector area = " + sum);
    }
  }

  /**
   * Create faces for the cell, no recursion.
   */
  private void createFaces(Cell cell) {
    cell.faces = new Face[6];

    Index index = new Index();
    for (int dir = X; dir <= Z; dir++) {
      final int U = dir;							//we split in the "U" direction
      final int V = (dir + 1) % 3;
      final int W = (dir + 2) % 3;

      for (int side = 0; side <= 1; side++) {
        Vector[] vertices = new Vector[4];
        index.setComponent(U, side);
        for (int v = 0; v < vertices.length; v++) {
          index.setComponent(V, pathV[v]);
          index.setComponent(W, pathW[v]);
          vertices[v] = cell.getVertex(index.x, index.y, index.z);
        }
        Face newFace = new Face(vertices);
        newFace.sideness = (byte) (2 * side - 1);		    //will be set to 0 if the face turns out to be inner.
        Face face;
        if (mesh.facePool.contains(newFace)) {
          Face inner = mesh.facePool.intern(newFace);
          // 2007-08-08 bugfix: outer face can be shared between parent and child, but should not have winding 0
          if (inner.sideness != newFace.sideness) {
            inner.sideness = 0;
          }
          cell.faces[2 * dir + side] = inner;
          face = inner;
        } else {
          count++;
          mesh.facePool.add(newFace);
          cell.faces[2 * dir + side] = newFace;
          face = newFace;
        }
      }
    }
  }
  private final Vector a = new Vector();
  Vector b = new Vector();
  Vector o = new Vector();

  private void createNormalVectors(Cell cell) {


    Face[] faces = cell.faces;
    cell.normal = new Vector[faces.length];

    for (int dir = X; dir <= Z; dir++) {
      for (int side = 0; side <= 1; side++) {
        a.reset();
        b.reset();
        o.reset();

        Face face = cell.getFace(dir, side);
        Vector normal = new Vector();

        for (int i = 0; i < 4; i++) {
          a.set(face.vertex[(i + 0) % 4]);
          b.set(face.vertex[(i + 1) % 4]);
          o.set(face.center);

          a.subtract(o);
          b.subtract(o);
          a.cross(b);
          normal.add(a);
        }
        // 2008-08-06: this was not correct for strongly bent faces
        // normal.normalizeSafe();
        // normal.multiply(face.area);     //nice area of 4 sub-vertices

        normal.multiply(0.5);

        // By this construction, left normal vector points inwards, right normal vector outwards,
        // so reflect if necessary
        // sigh..., it's the other way around...
        if (side == 0) {
          normal.multiply(-1);
        }
        cell.normal[2 * dir + side] = normal;

        // already set, should not be norm.
        //face.scalarArea = normal.norm();

        /*if( !Equality.equals(face.area, normal.norm()))
        System.out.println(face.area + " " + normal.norm());*/
        assert !normal.isNaN();
      }
    }
    cell.normal = normalVectorPool.intern(cell.normal);
  }
}
