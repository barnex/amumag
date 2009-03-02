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

import amu.core.Pool;
import amu.debug.Consistency;
import amu.geom.solid.Shape;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Face;
import amu.core.Index;
import amu.debug.Bug;
import amu.mag.Unit;
import amu.mag.adapt.AdaptiveMeshRules;
import amu.mag.adapt.TestAdaptiveMesh;
import amu.mag.fmm.WireModule;
import amu.io.Message;
import java.io.OutputStream;
import java.io.Serializable;
import static java.lang.Math.*;
import static amu.geom.Vector.*;
import static amu.mag.Face.LEFT;
import static amu.mag.Face.RIGHT;

/**
 * The Mesh contains the entire magnetic configuration at a given time.
 */
public final class Mesh implements Serializable {

  //"base" fields
  public final Vector boxSize;                    // Size of bounding box.
  public final int nLevels;                       // number of splitting levels
  public int[] splitDirection;                    // Direction in which each of the levels was split. X,Y or Z
  public transient final AdaptiveMeshRules aMRules;
  //"derived" fields
  public Cell[][][][] levels;                     // Cells of all levels. index: level, x, y, z
  public final Cell[][][] baseLevel;              // Smallest cells.
  public final Cell[][][] coarseLevel;
  public final int coarseLevelIndex;
  //"mutable" fields
  public Cell rootCell;                           // Root for all cells
  public Cell coarseRoot;                         // Root for the coarsest allowed simulation level
  public Cell baseRoot;                           // Root for the smallest level
  public Face rootFace;                           // Root for all faces (linkedlist).
  //temporary fields
  public transient Pool<Vector> vertexPool;
  public transient Pool<Face> facePool;
  //finalization
  private boolean isFinal = false;                // A final mesh can not be modified anymore.
  //2D-3D sim?
  public int dimension;
  public int cells,  baseCells,  coarseCells,  faces;

  public void toText(OutputStream out) {
  }

  public Mesh(Vector boxSize, Vector maxCellSize, AdaptiveMeshRules aMRules) {

    this(boxSize, calcLevels(boxSize, maxCellSize), new PartitionRuleCubeMaxSize(maxCellSize), aMRules);

  }

  public Mesh(Vector size, int numLevels, PartitionType partRules, AdaptiveMeshRules aMRules) {

    Message.title("mesh box");

    this.boxSize = size;
    size.makeImmutable();

    this.nLevels = numLevels;

    vertexPool = new Pool<Vector>();
    facePool = new Pool<Face>();

    new PartitionModule(this, size, numLevels, partRules).createLevels();	// construct the primitive grid of cells.
    int x = levels[levels.length - 1].length;
    int y = levels[levels.length - 1][0].length;
    int z = levels[levels.length - 1][0][0].length;


    this.aMRules = aMRules;
    aMRules.mesh = this;
    //aMRules.updateUniform();  //what was this doing here?

    coarseLevelIndex = aMRules.getCoarseRootLevel();


    rootCell = levels[0][0][0][0];						// set the root cell for convenience
    baseLevel = levels[levels.length - 1];					// set the base level (smallest cells) for convienience
    coarseLevel = levels[coarseLevelIndex];

    Consistency.checkMesh(this);

    Message.indent("box size: \t" + boxSize.x * Unit.LENGTH + "m x" + boxSize.y * Unit.LENGTH + "m x" + boxSize.z * Unit.LENGTH + "m");
    Message.indent("discretization levels: \t" + nLevels);
    Message.indent("smallest cells: \t" + x + "x" + y + "x" + z + "(=" + x * y * z + ")");
    Message.indent("partitioning: \t" + partRules);
    Message.indent("smallest cell size: \t" + baseLevel[0][0][0].size.x * Unit.LENGTH + "m x " + baseLevel[0][0][0].size.y * Unit.LENGTH + "m x " + baseLevel[0][0][0].size.z * Unit.LENGTH + "m");
    Message.indent("adaptivity: \t" + aMRules);
    Message.indent("coarsest cell size: \t" + coarseLevel[0][0][0].size.x * Unit.LENGTH + "m x " + coarseLevel[0][0][0].size.y * Unit.LENGTH + "m x " + coarseLevel[0][0][0].size.z * Unit.LENGTH + "m");
  }

  public Mesh(double boxSizeX, double boxSizeY, double boxSizeZ,
          double maxCellSizeX, double maxCellSizeY, double maxCellSizeZ, AdaptiveMeshRules aMRules) {

    this(new Vector(boxSizeX / Unit.LENGTH,
            boxSizeY / Unit.LENGTH,
            boxSizeZ / Unit.LENGTH, true),
            new Vector(maxCellSizeX / Unit.LENGTH,
            maxCellSizeY / Unit.LENGTH,
            maxCellSizeZ / Unit.LENGTH, true),
            aMRules);
  }

  public Index getBaseSize() {
    return new Index(baseLevel.length, baseLevel[0].length, baseLevel[0][0].length);
  }

  public Cell getBaseCell(Index r) {
    return getCell(levels.length - 1, r);
  }

  //__________________________________________________________________________
  public void makeFinal() {
    Message.debug("Mesh::makeFinal()");

    new CellJoinModule(this).joinCells();
    new FaceModule(this).createFacesAndNormalVectors();
    new LinkedListModule(this).linkCellsAndFaces();
    makeCellsFinal();
    Message.debug("vertex pool: " + vertexPool.getStats());
    vertexPool = null;
    Message.debug("face pool: " + facePool.getStats() + "(!)");
    facePool = null;
    System.gc();
    isFinal = true;
    Consistency.checkMesh(this);
    Consistency.checkFacesAll(this);
  }

  private void check(Cell c) {
    /*boolean ok = true;

    ok &= c.center.x < c.getFace(X, LEFT).center.x;

    Message.warningOnce("todo: check cell topology.");*/
  }

  //__________________________________________________________________________
  private void makeCellsFinal() {
    Message.warning("TODO: init super cells.");
    Message.debug("makeCellsFinal()");
    for (Cell c = baseRoot; c != null; c = c.next) { //todo: all cells.
      for (Vector v : c.vertex) {
        v.makeImmutable();			    // should have been done by face, redundant.
      }	    // replace center of initial cell by barycenter (vertices may have been moved).
      // what with non-base cells?

      //2008-08-06: experimenting with centers on the grid.

      c.center = GeomModule.barycentrumVolume(c);

      c.center.makeImmutable();
      check(c);
    }
  }

  public void intersect(Shape shape) {
    intersect(shape, true);
  }

  public void intersect(Shape shape, boolean smooth) {
    if (isFinal) {
      throw new IllegalArgumentException("Mesh has already been finalized.");
    }
    //Message.debug("intersect mesh with " + shape + ", smoothing = " + smooth);
    new CutoutModule(this, shape, smooth).apply();
  }

  //__________________________________________________________________________
  /**
   * Returns the cell corresponding to the given level and index, or null
   * if the index is out of bounds.
   */
  public Cell getCell(int level, Index index) {
    Cell[][][] levelsL = levels[level];
    if (index.x >= 0 && index.x < levelsL.length && index.y >= 0 && index.y < levelsL[index.x].length && index.z >= 0 && index.z < levelsL[index.x][index.y].length) {
      return levels[level][index.x][index.y][index.z];
    } else {
      return null;
    }
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal(boolean Final) {
    this.isFinal = Final;
  }

  //__________________________________________________________________________
  /**
   * Calculate the number of splitting levels needed to accomodate cells of
   * a given maximal size into a given box.
   * @param boxSize
   * @param maxCellSize
   * @return number of levels.
   */
  private static int calcLevels(Vector boxSize, Vector maxCellSize) {
    //number of cells in each direction
    double nx = boxSize.x / maxCellSize.x;
    double ny = boxSize.y / maxCellSize.y;
    double nz = boxSize.z / maxCellSize.z;

    if (nx < 1.0) {
      nx = 1.0;
    }
    if (ny < 1.0) {
      ny = 1.0;
    }
    if (nz < 1.0) {
      nz = 1.0;
    }

    //log2 of minimum number of cells in each direction
    int lx = (int) ceil(log(nx) / log(2));
    int ly = (int) ceil(log(ny) / log(2));
    int lz = (int) ceil(log(nz) / log(2));

    //number of partitioning levels = 1+log2(total number of cells)
    return 1 + lx + ly + lz;
  }
}
