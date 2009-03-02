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

public final class TestAdaptiveMesh extends AdaptiveMeshRules {

  private double cos;
  private final int maxLevels;

  //private int coarseRootLevel;
  public TestAdaptiveMesh(double maxAngle, int maxLevels) {
    cos = Math.cos(Math.PI*maxAngle/180);
    this.maxLevels = maxLevels;
  }

  public int getCoarseRootLevel() {
    return mesh.nLevels - 1 - maxLevels;
  }

  @Override
  public void update() {
    Message.debug("Updating mesh rules...");
    
    // start just above smallest cells and go up to coarse level
    
     for (Cell[][] levelI : mesh.coarseLevel) {
      for (Cell[] levelIJ : levelI) {
        for (Cell cell : levelIJ) {
          if (cell != null) {
            updateUniform(cell);
          }
        }
      }
    }

     // set all false first
     for(Cell cell = mesh.rootCell; cell != null; cell = cell.next){
      cell.updateLeaf = false;
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
        //but all near cells should be uniform too:
        for(Cell near: cell.nearCells){
          if(!near.uniform){
            cell.updateLeaf = false;
            break;
          }
        }

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