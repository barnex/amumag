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
import amu.mag.Face;

public final class MaxAngle extends AdaptiveMeshRules {

  /**
   * The cosine of the max allowed angle between spins for them to be considered parallel
   */
  private double cos;

  /**
   * The maximum number of cell levels to group.
   * Maximum 2^maxLevels cells will be grouped.
   */
  private final int maxLevels;
  public static boolean DEBUG_MPOINTERS = false;

  /**
   *
   * @param maxAngle max angle between spins for them to be considered parallel
   * @param maxLevels Maximum 2^maxLevels cells will be grouped
   */
  public MaxAngle(double maxAngle, int maxLevels) {
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
    //Message.debug("Updating mesh rules...");

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

     // reset everything first
     for(Cell cell = mesh.rootCell; cell != null; cell = cell.next){
      cell.updateLeaf = false;
      //cell.m = cell.my_m; // reset m pointer to myself
      //cell.m_ex = cell.my_m;
     }
     for(Face face = mesh.rootFace; face != null; face= face.next){ //could be coarserootFace...
      face.adhocChargeCounter = -1; // means charge not needed
     }

    for (Cell[][] levelI : mesh.coarseLevel) {
      for (Cell[] levelIJ : levelI) {
        for (Cell cell : levelIJ) {
          if (cell != null) {
            updateLeaf(cell);
            // also sets m pointers to parents for sub-leafs
          }
        }
      }
    }

    // workaround
    for(Face face = mesh.rootFace; face != null; face= face.next){ //could be coarserootFace...
      if(face.scalarArea == 0.0)
      face.adhocChargeCounter = -1; // means charge not needed
    }

    // updateLeaf has re-arranged some m pointers
    // they have to be set also in exchange
    if (DEBUG_MPOINTERS) {
      for (Cell cell = mesh.coarseRoot; cell != null; cell = cell.next) {
        cell.exchange.updateMPointers();
      }
    }
  }


  private void updateLeaf(Cell cell){
    if(cell.child1 == null){
      // (0) smallest cell, UPDATELEAF = true
      cell.updateLeaf = true;
    }
    else{
      // (1) bigger cell, UPDATELEAF = perhaps
      if(cell.uniform){ // prequisite 1
        //should be uniform, looks fine so far
        cell.updateLeaf = true;
        //but all near cells should be uniform too, and equal to this cell:
        for(Cell near: cell.nearCells){
          //if(!(near.uniform && near.m.dot(cell.m) > cos)){ //prequisite2
          if(!(near.uniform
                  && near.m.x * cell.m.x  + near.m.y * cell.m.y + near.m.z * cell.m.z > cos)){ //prequisite2
            cell.updateLeaf = false;
            break;
          }
        }
      }
      else{//not uniform: sorry...
        cell.updateLeaf = false;
      }

      // if the big cell is not an updateLeaf, too bad, but perhaps it children may be
      if(!cell.updateLeaf){
        updateLeaf(cell.child1);
        updateLeaf(cell.child2);
      }
    }// not a smallest cell

    //// updateLeaf is now determined

    // When we have a leaf...
    if (cell.updateLeaf) {

      // (1) we mark its own faces and near faces as needed, by setting the charge counter to 0.
      for (int i = 0; i < cell.faces.length; i++) {
        cell.faces[i].adhocChargeCounter = 0; // 0 means charge needed
      }
      for (int i = 0; i < cell.kernel.nearFaces.length; i++) {
        cell.kernel.nearFaces[i].adhocChargeCounter = 0; // 0 means charge needed
      }

      // (2) if it's not a smallest cell:
      if(cell.child1 != null){
        // the Q of the children needs to be zeroed out: in some rare cases,
        // their Q will be needed. E.g., when a cell is close to an adaptive mesh
        // "border" some smaller cells might needs it Q. Since the of a leaf cell's
        // children is very small and only rarely needed, it's OK to set it to zero.
        cell.child1.resetAllQ();
        cell.child2.resetAllQ();
        // now, my chidren should have their m the same as me
        // to do so efficiently, we just point their m to mine.
        // beware: we will need to update the m pointers in exchange as well!
//        if(DEBUG_MPOINTERS){
//        pointMtoParent(cell.child1);
//        pointMtoParent(cell.child2);
//        }
      }
    }// end if update leaf.
    
  }

//  private final void pointMtoParent(Cell cell){
//    // currently only m_ex, change to m when debugged.
//    cell.m_ex = cell.parent.m_ex;
//    if(cell.child1 != null){
//      pointMtoParent(cell.child1);
//      pointMtoParent(cell.child2);
//    }
//  }

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

  //  /**
//   * Go through the same tree as updateH(), i.e. until the updateLeafs.
//   * Mark partners as Q-needed.
//   * @param thiz
//   */
//  private void updateQNeededByPartners(final Cell thiz) {
//
//    //mark my partners as Q-needed: I will need their Q.
//
//    for (Cell c : thiz.smooth.partners) {
//      c.qNeeded = true;
//    }
//
//    // if I'm an updateLeaf, stop here, my children won't use Q's
//    if (!thiz.updateLeaf) {
//      updateQNeededByPartners(thiz.child1);
//      updateQNeededByPartners(thiz.child2);
//    }
//  }

//  private void updateQFromFaces(final Cell thiz){
//    // if I'm an updateLeaf, I can definitely take my Q from the faces:
//    if(thiz.updateLeaf){
//      thiz.qFromFaces = true;
//    }
//
//    // then look at my children
//    if (thiz.child1 != null) {
//      // If I can take Q from my faces, then my children can do so too:
//      if(thiz.qFromFaces){
//        thiz.child1.qFromFaces = true;
//        thiz.child2.qFromFaces = true;
//      }
//      // and their children will be able to do so too, if they have any.
//      updateQFromFaces(thiz.child1);
//      updateQFromFaces(thiz.child2);
//    }
//  }

//  /**
//   * Start from root and go down till updateLeafs. If Q is needed and can not be
//   * taken from the faces, then childQ will be needed.
//   * @param thiz
//   */
//  public void updateQNeededByParent(Cell thiz) {
//    if(thiz.parent != null){
//      if(thiz.parent.qNeeded && !thiz.parent.qFromFaces)
//        thiz.qNeeded = true;
//    }
//    if(thiz.child1 != null){
//      updateQNeededByParent(thiz.child1);
//      updateQNeededByParent(thiz.child2);
//    }
//  }


  @Override
  public boolean isUniform(Cell cell) {
   throw new Bug("rm me");
  }

  @Override
  public String toString(){
    return "Adaptive mesh grouping max " + maxLevels + " levels, m1.m2 > " + cos;
  }
}