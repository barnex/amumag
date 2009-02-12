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

import amu.debug.Consistency;
import amu.geom.*;
import amu.io.Message;
import amu.mag.Cell;
import amu.io.Message;
import static java.lang.Math.*;

/**
 * This module wires the cells of a mesh, i.e., connects cells to their partner
 * cells and near cells based on a proximity criterion.
 * 
 * @see amu.geom.LinkedListModule puts cells and faces in linked lists for 
 * easy acces.
 * @see amu.geom.PartitionModule partitions the mesh into cells and then uses
 * LinkedListModule to add the cells to a linked list.
 * 
 * Note: the methods in this class logically belong to Mesh, but are put here
 * to keep the sourcefiles small.
 * 
 * TODO: opening angle can be changed for partially empty cells, but this would
 * probably only make a small difference in efficiency. The proximity criterion
 * could also include the expected moment of the cells, e.g., cells at the
 * boundary will have larger moments and should be considered as if they were
 * "closer".
 * 
 */
public final class WireModule {

  /**
   * Makes the nearCells partners so a smoothField is used instead of the kernel
   * Only the self-kernel will still be used because smoothField is NaN is this case.
   */
  public final boolean DEBUG_SMOOTH_ONLY = false;
  /**
   * Cells on a level lower than levels-minQLevel can never be partners,
   * but go to the kernel (near cells) right away. UpdateQ() will never
   * be called on these cells. minQLevel=0: normal case.
   */
  //public final int minQLevel = 0;
  private final ProximityModule rules;
  private final Mesh mesh;

  public WireModule(Mesh mesh, ProximityModule m) {
    this.mesh = mesh;
    rules = m;
  }

  public void wire() {

    Message.debug("Wire: " + rules);
    Message.print("Wiring level: ");

    Cell[][][][] levels = mesh.levels;
    Cell root = mesh.rootCell;						//2007-08-02
    root.setPartners(new Cell[]{});
    root.setNearCells(new Cell[]{root});					//root is near to itself

    for (int l = 0; l < levels.length; l++) {
      Message.print(" " + l);
      for (int i = 0; i < levels[l].length; i++) {
        for (int j = 0; j < levels[l][i].length; j++) {
          for (int k = 0; k < levels[l][i][j].length; k++) {
            if (levels[l][i][j][k] != null) {
              wire(levels[l][i][j][k]);
            }
          }
        }
      }
    }


    Message.println();

    if (DEBUG_SMOOTH_ONLY) {
      Message.warning("DEBUG_SMOOTH_ONLY = true");
      removeKernel(levels[levels.length - 1]);
    }

    //Consistency.checkWiringAll(mesh.rootCell);


  }

  /**
   * Wires a Cell, but not its children.
   */
  private void wire(Cell thiz) {

    final java.util.Vector<Cell> partnerBuffer = new java.util.Vector<Cell>();
    final java.util.Vector<Cell> nearBuffer = new java.util.Vector<Cell>();

    if (thiz.parent != null) {
      //fill partnerbuffer with candidates, they may be too near
      Cell[] nearParent = thiz.parent.getNearCells();
      for (int i = 0; i < nearParent.length; i++) {
        partnerBuffer.add(nearParent[i]);
      }

      //cull too near cells from partnerbuffer. split cells that are too large
      //add near cells to nearbuffer.
      int partnerCount = partnerBuffer.size();
      for (int i = 0; i < partnerBuffer.size(); i++) {
        Cell cell = partnerBuffer.get(i);
        if (rules.isNear(thiz, cell)) {					//cell is near. isNear(FIELD, SOURCE), not commutative.
          partnerBuffer.set(i, null);					// remove from partners
          partnerCount--;
          if (cell.getSize().sizeLargerThan(thiz.getSize())) {		//cell is too big
            if (cell.child1 != null) {
              partnerBuffer.add(cell.child1);		// add children to the end of partnerbuffer
              partnerCount++;
            }
            if (cell.child2 != null) {
              partnerBuffer.add(cell.child2);		// they may or may not be split, culled or added to the partners
              partnerCount++;
            }
          } else //cell is not too big, but near
          {
            nearBuffer.add(cell);					// add to near cells
          }
        }
      //cell is not near: do not remove
      }

      //buffer -> array
      Cell[] partners = new Cell[partnerCount];
      int p = 0;
      for (int i = 0; i < partnerBuffer.size(); i++) {
        Cell cell = partnerBuffer.get(i);
        if (cell != null) {
          partners[p] = cell;
          p++;
        }
      }
      thiz.setPartners(partners);

      Cell[] nearCells = new Cell[nearBuffer.size()];
      for (int i = 0; i < nearBuffer.size(); i++) {
        nearCells[i] = nearBuffer.get(i);
      }
      thiz.setNearCells(nearCells);
    }
  }

  /**
   * disalbe kernel for smallest cells, replace by smooth field.
   * (debug only!)
   */
  public void removeKernel(Cell[][][] level) {
    for (Cell[][] levelI : level) {
      for (Cell[] levelIJ : levelI) {
        for (Cell cell : levelIJ) {
          if (cell != null) {
            Cell[] partners = cell.getPartners();
            Cell[] near = cell.getNearCells();
            Cell[] newPartners = new Cell[partners.length + near.length - 1];
            int p = 0;
            for (Cell c : partners) {
              newPartners[p] = c;
              p++;
            }
            for (Cell c : near) {
              if (c != cell) {                                      //do not add itself as partner: NaN field.
                newPartners[p] = c;
                p++;
              }
            }
            cell.setPartners(newPartners);
            cell.setNearCells(new Cell[]{cell});			//self-demag field has to be kernel.
          }
        }
      }
    }
  }
}
