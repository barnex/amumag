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
package amu.mag.kernel;

import amu.core.ArrayPool;
import amu.core.Pool;
import amu.core.Sort;
import amu.debug.Bug;
import amu.debug.KnownBug;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import amu.mag.fmm.UnitQModule;
import amu.io.Message;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import static java.lang.Math.*;

public abstract class KernelModule {

  // Integrals of functions of several variables, over regions with dimension greater
  // than one, are not easy. ... If you don't know where the strongly peaked regions
  // are, you might as well quit: It is hopeless.
  // Numerical Recipes in C
  protected Mesh mesh;
  protected Vector vectorBuffer;
  private int netElementCount,  trueElementCount;
  private int innerCount,  outerCount;
  private Element elementBuffer;
  private Pool<Vector> vectorPool;
  private ArrayPool<Vector> kernelPool = new ArrayPool<Vector>(false);
  private Hashtable<Element, Vector> kernelCache = new Hashtable<Element, Vector>();
  private IdentityHashMap<Face, Vector> nearFaceHash = new IdentityHashMap<Face, Vector>();
  private static final Face[] faceType = new Face[0];
  private static final Vector[] vectorType = new Vector[0];

  public KernelModule(Mesh mesh) {
    this.mesh = mesh;
    elementBuffer = new Element();
    vectorBuffer = new Vector();
    vectorPool = new Pool<Vector>();
  }

  public void init() {
    Message.title("kernel");

    int count = 0;
    Message.startProgress(mesh.baseCells);  // oh well...
    //2009-02-28: adaptive mesh: coarse root: bigger cells also have a kernel :)
    for (Cell cell = mesh.coarseRoot; cell != null; cell = cell.next) {
      Message.progress(count);
      findNearFaces(cell);
      integrateKernel(cell);
      count++;
    }
    Message.stopProgress();



    Message.debug(trueElementCount + " kernel elements, " + netElementCount + " are different, outer:" + outerCount + " ");
    //System.gc();
    Message.debug("Vector pool: " + vectorPool.getStats());
    Message.debug("Array pool: " + kernelPool.getStats());

  }

  private void integrateKernel(Cell cell) {
    // (2) calculate kernel for all near faces, use previously stored weights
    for (Face face : nearFaceHash.keySet()) {
      // if not cached, calculate kernel element.

      Element elem = elementBuffer;
      elem.set(cell, face);
      assert elem.equals(new Element(elem));

      if (!kernelCache.containsKey(elem)) {
        integrate(elem, vectorBuffer);

        vectorBuffer.x = UnitQModule.round(vectorBuffer.x);
        vectorBuffer.y = UnitQModule.round(vectorBuffer.y);
        vectorBuffer.z = UnitQModule.round(vectorBuffer.z);

        if (vectorPool.contains(vectorBuffer)) {	// todo: VectorPool, with auto recycle key. pff.
          kernelCache.put(new Element(elem), vectorPool.get(vectorBuffer));
          assert vectorPool.get(vectorBuffer) != null;
        } else {
          Vector newVector = new Vector(vectorBuffer, true);      //cache should be immutable
          vectorPool.add(newVector);
          kernelCache.put(new Element(elem), vectorPool.get(newVector));
        }
        netElementCount++;
      }


      Vector kernelElem = kernelCache.get(elem); // redundant, but keep this for defense: problems with equals should pop up here
      trueElementCount++;
      // weigh and set kernel element.
      Vector kernelVector = nearFaceHash.get(face);
      double weight = kernelVector.x;
      kernelVector.set(kernelElem);
      // ??!! this mutes the kernel vector, stored in the cache??
      kernelVector.multiply(weight);
    }

    Face[] nearFaces = nearFaceHash.keySet().toArray(faceType);
    Vector[] kernel = nearFaceHash.values().toArray(vectorType);  // this intern makes like 50% difference: load kernel into a buffer and get it from a pool. recycle the buffer. should we make a recycle pool?, here it is not necessary.

    // sorting is needed to make equality of kernel arrays possible.
    Sort.duoSort(kernel, nearFaces);
    kernel = kernelPool.intern(kernel);
    //System.out.println(kernel.length);
    //System.out.println(Arrays.toString(kernel));
    cell.kernel = new Kernel(nearFaces, kernel);
  }

  private void findNearFaces(Cell cell) {
    // (1) construct nearFace list + kernel with initial weights.
    nearFaceHash.clear();

    Cell[] nearCells = cell.getNearCells();
    for (Cell near : nearCells) {
      for (Face face : near.faces) {
        // face has not yet been processed: add to nearFaces,
        // construct kernel Vector which is initially (ab)used
        // to store the fraction of the face to be taken into account
        // (0.5 or 1.0)
        if (!nearFaceHash.containsKey(face)) {
          // inner faces are included twice
          // (either twice in the kernel, or once in the kernel and once in the smooth field of a neighbor).
          // so we should weigh them by 1/2.
          double weight;
          if (face.sideness == 0) {
            weight = 0.5;
          } else {
            weight = 1.0;
          }
          nearFaceHash.put(face, new Vector(weight, weight, weight));
        } // face has already been processed: increase its weight count.
        else {
          //so this can only be an inner face, let's check to be sure.
          if (face.sideness != 0) {
            throw new KnownBug(BUG_MSG1);
          }
          nearFaceHash.get(face).add(0.5, 0.5, 0.5);
          outerCount++;
        }
      }
    }
  }

  protected abstract void integrate(Element elem, Vector vectorBuffer);
  private static final String BUG_MSG1 = "This bug is due to a round-off error in the geometry enginge.\n Workaround: make the mesh box slightly larger than necessary, you might need to try a few different values.";
}
