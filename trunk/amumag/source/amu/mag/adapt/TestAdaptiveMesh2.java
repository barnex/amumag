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
package amu.mag.adapt;

import amu.debug.Bug;
import amu.io.Message;
import amu.mag.Cell;

public final class TestAdaptiveMesh2 extends AdaptiveMeshRules {

  /**
   * The cosine of the max allowed angle between spins for them to be considered parallel
   */
  private double cos;

  /**
   * The maximum number of cell levels to group.
   * Maximum 2^maxLevels cells will be grouped.
   */
  private final int maxLevels;

  /**
   *
   * @param maxAngle max angle between spins for them to be considered parallel
   * @param maxLevels Maximum 2^maxLevels cells will be grouped
   */
  public TestAdaptiveMesh2(double maxAngle, int maxLevels) {
    if(maxAngle < 0 || maxAngle > 180)
      throw new IllegalArgumentException("Adaptive mesh maxAngle should be between 0 and 180 degrees");

    if(maxLevels < 0)
      throw new IllegalArgumentException("Adaptive mesh maxLevels should be >= 0");
    
    cos = Math.cos(Math.PI*maxAngle/180);
    this.maxLevels = maxLevels;
  }

  
  public int getCoarseRootLevel() {
    return mesh.nLevels - 1 - maxLevels;
  }

  @Override
  public void update() {
    Message.debug("Updating mesh rules...");

     // update local uniformity_________________________________________________
     // start from the coarse level, will recursively go down
     for (Cell[][] levelI : mesh.coarseLevel) {
      for (Cell[] levelIJ : levelI) {
        for (Cell cell : levelIJ) {
          if (cell != null) {
            updateUniform(cell);
          }
        }
      }
    }

     // update Leaf_____________________________________________________________
     // set all false first
     for(Cell cell = mesh.rootCell; cell != null; cell = cell.next){
      cell.updateLeaf = false;
      //cell.mNeededExch = false;
      cell.m = cell.my_m; // reset m pointer to myself
     }

    for (Cell[][] levelI : mesh.coarseLevel) {
      for (Cell[] levelIJ : levelI) {
        for (Cell cell : levelIJ) {
          if (cell != null) {
            updateLeaf(cell);
          }
        }
      }
    }

    // qNeeded?_________________________________________________________________
    // QNeeded has already been set to false.
    //updateQNeededByPartners(mesh.rootCell);
    //updateQFromFaces(mesh.rootCell);
    //updateQNeededByParent(mesh.rootCell);
   
  }

  /**
   * Go through the same tree as updateH(), i.e. until the updateLeafs.
   * Mark partners as Q-needed.
   * @param thiz
   */
  private void updateQNeededByPartners(final Cell thiz) {

    //mark my partners as Q-needed: I will need their Q.

    for (Cell c : thiz.smooth.partners) {
      c.qNeeded = true;
    }

    // if I'm an updateLeaf, stop here, my children won't use Q's
    if (!thiz.updateLeaf) {
      updateQNeededByPartners(thiz.child1);
      updateQNeededByPartners(thiz.child2);
    }
  }

  private void updateQFromFaces(final Cell thiz){
    // if I'm an updateLeaf, I can definitely take my Q from the faces:
    if(thiz.updateLeaf){
      thiz.qFromFaces = true;
    }

    // then look at my children
    if (thiz.child1 != null) {
      // If I can take Q from my faces, then my children can do so too:
      if(thiz.qFromFaces){
        thiz.child1.qFromFaces = true;
        thiz.child2.qFromFaces = true;
      }
      // and their children will be able to do so too, if they have any.
      updateQFromFaces(thiz.child1);
      updateQFromFaces(thiz.child2);
    }
  }

  /**
   * Start from root and go down till updateLeafs. If Q is needed and can not be
   * taken from the faces, then childQ will be needed.
   * @param thiz
   */
  public void updateQNeededByParent(Cell thiz) {
    if(thiz.parent != null){
      if(thiz.parent.qNeeded && !thiz.parent.qFromFaces)
        thiz.qNeeded = true;
    }
    if(thiz.child1 != null){
      updateQNeededByParent(thiz.child1);
      updateQNeededByParent(thiz.child2);
    }
  }

  private void updateLeaf(Cell cell){
    if(cell.child1 == null){
      // smallest cell, has to be leaf
      cell.updateLeaf = true;
    }
    else{
      // bigger cell
      if(cell.uniform){
        //should be uniform, looks fine so far
        cell.updateLeaf = true;
        //but all near cells should be uniform too, and equal to this cell:
        for(Cell near: cell.nearCells){
          if(!(near.uniform && near.m.dot(cell.m) > cos)){
            cell.updateLeaf = false;
            break;
          }
        }
        // at this point, the cell is an updateleaf
        // the Q of the children needs to be zeroed out: in some rare cases,
        // their Q will be needed. E.g., when a cell is close to an adaptive mesh
        // "border" some smaller cells might needs it Q. Since the of a leaf cell's
        // children is very small and only rarely needed, it's OK to set it to zero.
        cell.child1.resetAllQ();
        cell.child2.resetAllQ();
        // now, my chidren should have their m the same as me
        // to do so efficiently, we just point their m to mine
        //pointMtoParent(cell.child1);
        //pointMtoParent(cell.child2);
      }
      else{
        //not uniform: sorry...
        cell.updateLeaf = false;
      }

      // if the cell is not an updateLeaf, too bad, but perhaps it children may be
      if(!cell.updateLeaf){
        updateLeaf(cell.child1);
        updateLeaf(cell.child2);
      }
    }
  }

  private final void pointMtoParent(Cell cell){
    cell.m = cell.parent.m;
    if(cell.child1 != null){
      pointMtoParent(cell.child1);
      pointMtoParent(cell.child2);
    }
  }

  /**
   * Recursively updates whether a cell and its children are uniform
   * @param cell
   */
  private void updateUniform(Cell cell){
    if(cell.child1 != null){
      // update children first
      updateUniform(cell.child1);
      updateUniform(cell.child2);

      cell.uniform = cell.child1.uniform && cell.child2.uniform
              && cell.child1.m.dot(cell.child2.m) > cos;
    }
    else{//smallest cell
      cell.uniform = true;
    }
  }

  @Override
  public boolean isUniform(Cell cell) {
   throw new Bug("rm me");
  }

  @Override
  public String toString(){
    return "Adaptive mesh grouping max " + maxLevels + " levels, m1.m2 > " + cos;
  }
}