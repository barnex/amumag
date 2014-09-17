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

import amu.debug.Bug;
import amu.debug.Consistency;
import amu.geom.solid.Shape;
import amu.io.Message;
import amu.mag.Cell;
import amu.io.Message;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;

/**
 * Removes cells that are fully outside the boundary definced by Cutout.insideInternal().
 * Cells with only one child left are also removed, creating a "shortcut" in the
 * tree.
 */
public final class CutoutModule {

  // the mesh to cut pieces out
  private Mesh mesh;
  // the shape to cut out
  private Shape boundary;
  // maps every vertex THAT IS OUTSIDE THE BOUNDARY to a list of its nearest neighbors THAT ARE INSIDE THE BOUNDARY.
  private IdentityHashMap<Vector, ArrayList<Vector>> neighborHash;
  // List of all vertices outside the boundary.
  // This is the keyset of neighborHash, and could be replaced with neighbourHash.keySet()...
  private ArrayList<Vector> vertexList;
  // the number of iterations in the bolzano boundary search algorithm
  private final int ITERATIONS = 18;
  // make a smooth boundary or just keep the cubes as they are? (debug).
  private final boolean[] smooth;

  public CutoutModule(Mesh mesh, Shape cut, boolean smooth) {
    this(mesh, cut, new boolean[]{smooth, smooth, smooth});
  }

  public CutoutModule(Mesh mesh, Shape cut, boolean[] smooth) {
    this.mesh = mesh;
    this.boundary = cut;
    this.smooth = smooth;
  }

  public void apply() {

    // tag base cells for removal
    tagCells();

    // recursively update the unlink-tags of bigger cells:
    // if both children are tagged, the parent gets tagged for removal.
    updateUnlinkTag(mesh.rootCell);

    // unlink tagged cells from levels and parent: TODO: problem for multigrid exchange: no neighbor!
    Message.warning("todo: modify unlinkTaggedFromLevelsAndParent");
    unlinkTaggedFromLevelsAndParent();

    // shortcut cells with only one child
    shortcut(mesh.rootCell);

    // unlink shortcut cells from levels and parent.
    unlinkTaggedFromLevels();

    if (smoothingEnabled()) {
      Message.debug("Vertices moved to fit boundary = ");
      int moved = smoothen();
      Message.debug("" + moved);
      while (moved != 0) {
        moved = smoothen();
        Message.debug(" + " + moved);
      }
      Message.println();
    }

    // in the rare case that a vertex lies EXACTLY on the boundary, the
    // moved vertices may coincide with it, reducing the area of some
    // faces to zero
    // tagCollapsedCells();
    Message.warning("todo: remove collapsed cells");

    // check if parent-child links are still correct.
    Consistency.checkMesh(mesh);

    // clean up the mess...
    System.gc();
  }

  private boolean smoothingEnabled() {
    return smooth[X] || smooth[Y] || smooth[Z];
  }

  /**
   * First make neighborlist (of neighbouring vertices insideInternal the boundary)
   * for every outside vertex, then try to move the vertex towards
   * each of its neighbors insideInternal the boundary and retain only the shortest
   * move. The vertex is now insideInternal the boundary.
   * Returns the number of vertices that have been moved.
   */
  private int smoothen() {

    constructVertexNeighbors();

    for (Vector vertex : vertexList) {
      ArrayList<Vector> neighbors = neighborHash.get(vertex);
      // a vector outside the boundary does not have to have neighbors insideInternal,
      // this kind of cell will be moved insideInternal at a later iteration when
      // some of its neighbors have been moved insideInternal.
      if (neighbors.size() != 0) {
        // try to move the vertex in the direction of each of its neighbors that are insideInternal the boundary
        // keep the move with the shortest length.
        Vector bestMoveSoFar = bolzano(vertex, neighbors.get(0));

        for (int i = 1; i < neighbors.size(); i++) {
          Vector move = bolzano(vertex, neighbors.get(i));
          if (move.norm2() < bestMoveSoFar.norm2()) {
            bestMoveSoFar = move;
          }
        }
        // and finally move the vertex towards the most favourable neighbor.
        vertex.add(bestMoveSoFar);
      }
    }
    int count = vertexList.size();
    neighborHash = null;
    vertexList = null;
    System.gc();
    // as long as not all vertices are insideInternal, we keep on iterating. (after 3 times it should be ok).
    return count;
  }

  /**
   * Vertex is outside and neighbor is insideInternal the boundary. Returns a displacement
   * between vertex and neighbor so that vertex gets insideInternal the boundary, but as close as
   * possible to the surface. The displacement is relative to vertex.
   */
  private Vector bolzano(Vector vertex, Vector neighbor) {
    // the vector between vertex and neighbor
    Vector delta = new Vector(neighbor);
    delta.subtract(vertex);

    // the position of the displaced vertex, starts halfway between vertex and vertex+delta.
    Vector pos = new Vector(vertex);
    pos.add(0.5, delta);

    // scalar position of the vertex along delta: starts halfway, with 1/4 step.
    double current = 0.5;
    double step = 0.25;

    // bolzano boundary search
    for (int i = 0; i < ITERATIONS; i++) {
      if (boundary.insideInternal(pos)) {
        current -= step;
      } else {
        current += step;
      }
      step *= 0.5;
      pos.set(vertex);
      pos.add(current, delta);
    }

    // we want the final position to be insideInternal, no matter what.
    while (!boundary.insideInternal(pos)) {
      pos.add(step, delta);
    }

    pos.subtract(vertex);	//return a relative move, so we can evaluate the length.
    return pos;
  }

  /**
   * Make a neighbor list for each vertex in the mesh base level that is outside
   * the boundary, the list
   * contains clones of the original neighbors, since all the vertices will be
   * moved in a later stage.
   *
   * How to find neighbors of vertices: for a cell this vertex belongs to,
   * 3 other vertices will be neighbors. do this for all cells and we find
   * all vertex neighbors, since each vertex belongs to multiple cells.
   *
   * Meanwhile we add all (original) vertices only once to a list so we
   * can then process these.
   */
  private int constructVertexNeighbors() {
    neighborHash = new IdentityHashMap<Vector, ArrayList<Vector>>();
    vertexList = new ArrayList<Vector>();

    // for all base cells
    for (Cell[][] baseI : mesh.baseLevel) {
      for (Cell[] baseIJ : baseI) {
        for (Cell cell : baseIJ) {
          if (cell != null) {

            //for all cell vertices
            for (int i = 0; i <= 1; i++) {
              for (int j = 0; j <= 1; j++) {
                for (int k = 0; k <= 1; k++) {

                  Vector vertex = cell.getVertex(i, j, k);
                  if (!boundary.insideInternal(vertex)) {
                    // get the neighbor list for this vertex
                    ArrayList<Vector> neighbors = neighborHash.get(vertex);
                    // if the list was not yet constructed: do so and add it to the hashtable
                    if (neighbors == null) {
                      neighbors = new ArrayList<Vector>(6);
                      neighborHash.put(vertex, neighbors);
                      // vertex has not been processed before: add to vertex list for later.
                      vertexList.add(vertex);
                    }

                    // add neighbors to list, if they are not added by other cells yet.

                    if (smooth[X]) {
                      addNeighbor(cell.getVertex((i + 1) % 2, j, k), neighbors);
                    }
                    if (smooth[Y]) {
                      addNeighbor(cell.getVertex(i, (j + 1) % 2, k), neighbors);
                    }
                    if (smooth[Z]) {
                      addNeighbor(cell.getVertex(i, j, (k + 1) % 2), neighbors);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    //System.out.println(vertexList.size() + " vertices will be moved to fit the boundary.");
    return vertexList.size();
  }

  /**
   * Add neighbor to the list if neighbors if it is not already in the list AND
   * the neighbor is insideInternal the boundary.
   */
  private void addNeighbor(Vector vertex, ArrayList<Vector> list) {
    if (!list.contains(vertex) && boundary.insideInternal(vertex)) {
      list.add(new Vector(vertex));   //add a CLONE, vertices will be moved, references should stay.
    }	/*if(list.size() > 6)
    throw new Bug();*/
    assert list.size() <= 6;
  }

  /**
   * If a cell has only one child cell, the cell can be removed from the tree
   * and the child becomes child of its grandparent. This method is recursive
   * and does not remove the cell from levels yet.
   */
  private void shortcut(Cell thiz) {
    // call on children first.
    if (thiz.child1 != null) {
      shortcut(thiz.child1);
    }
    if (thiz.child2 != null) {
      shortcut(thiz.child2);
    }

    // check if cell can be shortcut: i.e., if it has only one child
    // if we try to shortcut root, this will fail but that's kind of OK:
    // one should not throw away one half of the simulation volume but
    // use a smaller boundig box.
    if (thiz.childCount() == 1) {
      Cell myOnlyChild = null;
      if (thiz.child1 != null) {
        myOnlyChild = thiz.child1;
      } else {
        myOnlyChild = thiz.child2;
      }
      thiz.parent.swapChild(thiz, myOnlyChild);
      myOnlyChild.parent = thiz.parent;
      thiz.unlinkTag = true;
    }

  }

  /**
   * Recursively update the unlink-tak. If both children (if any) are tagged,
   * thiz also becomes tagged for removal.
   */
  private void updateUnlinkTag(Cell thiz) {
    if (thiz.child1 != null) {
      updateUnlinkTag(thiz.child1);
      updateUnlinkTag(thiz.child2);
      thiz.unlinkTag = thiz.child1.unlinkTag && thiz.child2.unlinkTag;
    }
  }

  /**
   * Remove tagged cells from mesh.levels, also unlink them from their parent.
   */
  private void unlinkTaggedFromLevelsAndParent() {
    Cell[][][][] levels = mesh.levels;
    for (int l = 0; l < levels.length; l++) {
      for (int i = 0; i < levels[l].length; i++) {
        for (int j = 0; j < levels[l][i].length; j++) {
          for (int k = 0; k < levels[l][i][j].length; k++) {
            Cell cell = levels[l][i][j][k];
            if (cell != null) {
              if (cell.unlinkTag) {
                // unlink me from my parent, i have no children so i dissapear.
                if (cell == mesh.rootCell) {
                  throw new IllegalArgumentException("Can not remove root cell.");
                }
                cell.parent.unlinkChild(cell);
                levels[l][i][j][k] = null;
              }
            }
          }
        }
      }
    }
  }

  /**
   * Remove tagged cells from mesh.levels.
   */
  private void unlinkTaggedFromLevels() {
    Cell[][][][] levels = mesh.levels;
    for (int l = 0; l < levels.length; l++) {
      for (int i = 0; i < levels[l].length; i++) {
        for (int j = 0; j < levels[l][i].length; j++) {
          for (int k = 0; k < levels[l][i][j].length; k++) {
            Cell thiz = levels[l][i][j][k];
            if (thiz != null && thiz.unlinkTag) {
              levels[l][i][j][k] = null;
            }
          }
        }
      }
    }
  }

  /**
   * As soon as at least one vertex is insideInternal, the cell is considered insideInternal
   * the boundary.
   * //2008-05-03: If the center of a cell is insideInternal, the cell is also considered
   * insideInternal the boundary. Removed parameter "Cell root".
   */
  private boolean inside(Cell cell, Shape cut) {
    if (cut.insideInternal(cell.center)) {
      return true;
    } else {
      Vector[] vertex = cell.vertex;
      int i = 0;
      while (i < vertex.length && !cut.insideInternal(vertex[i])) {
        i++;
      }
      return i < vertex.length;
    }
  }

  /**
   * Tag cells to be removed on base level.
   */
  private void tagCells() {
    // just for information: count the cells insideInternal/outside the boundary
    int inside = 0, outside = 0;
    Cell[][][] base = mesh.baseLevel;
    for (int i = 0; i < base.length; i++) {
      for (int j = 0; j < base[i].length; j++) {
        for (int k = 0; k < base[i][j].length; k++) {
          Cell cell = base[i][j][k];
          if (cell != null) {
            if (!inside(cell, boundary)) {
              cell.unlinkTag = true;
              outside++;
            } else {
              inside++;
            }
          }
        }
      }
    }
    Message.debug("smallest cells inside boundary = " + inside +
            ", outside the boundary = " + outside);
  //todo: check if all cells are outside and post a waring.
  }
}
