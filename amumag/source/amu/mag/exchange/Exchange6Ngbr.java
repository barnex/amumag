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

package amu.mag.exchange;

import amu.core.ArrayPool;
import amu.core.DoubleArrayWrapper;
import amu.core.Index;
import amu.core.Pool;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Face;
import amu.io.Message;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y; 
import static amu.geom.Vector.Z;

public final class Exchange6Ngbr {

    public static final int LEFT = 0, CENTER = 1, RIGHT = 2;
    
    public double[] laplacian = new double[7];

    public Cell[] neighCell = new Cell[7];
    public Vector[] neighM = new Vector[7]; //magnetization of neighbors, 6 real neighbors and the cell itself
                                            // cell, neighXL, neighXR, neighYL, neighYR, ...
    
    public final Cell cell;
    
    private static Pool<DoubleArrayWrapper> pool = new Pool<DoubleArrayWrapper>();
    
    // system of 6 equations with 6 unknowns (first and second derivatives in X,Y,Z)
    // and 7 free parameters (magnetization of the cell and its 6 neighbors)
    private static double[][] system = new double[6][6+7];
    
            
    public Exchange6Ngbr(Mesh mesh, int level, Index cellPos){
        this.cell = mesh.getCell(level, cellPos);
        
        Index index = new Index();
        for (int dir = X; dir <= Z; dir++) {

            // set pointers to the neighbours' magnetization
            
            //neighM[0] = cell.m;
            neighCell[0] = cell;  //2009-03-13

            index.set(cellPos);
            index.add(-1, Index.UNIT[dir]);
            Cell neighCellL = mesh.getCell(level, index);
            
            if (neighCellL != null) {
                //neighM[1+2*dir] = neighCellL.m;
                neighCell[1+2*dir] = neighCellL; //2009-03-13
            }
            else{
              // will not be used. (could be removed from array)
                //neighM[1+2*dir] = Vector.UNIT[0]; //crappy value to assure not used //cell.m; //remove me!!
                neighCell[1+2*dir] = null; //2009-03-13
            }

            index.set(cellPos);
            index.add(Index.UNIT[dir]);
            Cell neighCellR = mesh.getCell(level, index);
            if (neighCellR != null) {
                //neighM[1+2*dir + 1] = neighCellR.m;
                neighCell[1+2*dir + 1] = neighCellR; //2009-03-13
            }
            else{
              // will not be used. (could be removed from array)
                //neighM[1+2*dir + 1] = Vector.UNIT[0]; //crappy value to assure not used //cell.m; //remove me!!
                neighCell[1+2*dir + 1] = null; //2009-03-13
            }

            Vector dL;
            if(neighCellL != null)
                dL = neighCellL.center.minus(cell.center);
            else
                dL = null;
            
            Vector dR;
            if(neighCellR != null)
                dR = neighCellR.center.minus(cell.center);
            else
                dR = null;
                              
            if (dL != null) {
                system[2 * dir][0] = dL.x * dL.x;
                system[2 * dir][1] = dL.x;
                system[2 * dir][2] = dL.y * dL.y;
                system[2 * dir][3] = dL.y;
                system[2 * dir][4] = dL.z * dL.z;
                system[2 * dir][5] = dL.z;
                system[2 * dir][6] = -1.0;
                for (int i = 7; i < 13; i++) {
                    system[2 * dir][i] = 0.0;
                }
                system[2 * dir][2 * dir + 7] = 1.0;
            }
            else{
                Face face = cell.getFace(dir, Face.LEFT);
                Vector dFace = face.center.minus(cell.center);  //vector from cell to face
                Vector normal = cell.getNormal(dir, Face.LEFT);      //
                
                makeSafe(normal, dir, Face.LEFT); // temp. fix.
                
                system[2 * dir][0] = 2.0 * dFace.x * normal.x;
                system[2 * dir][1] = normal.x;
                system[2 * dir][2] = 2.0 * dFace.y * normal.y;
                system[2 * dir][3] = normal.y;
                system[2 * dir][4] = 2.0 * dFace.z * normal.z;
                system[2 * dir][5] = normal.z;
                system[2 * dir][6] = 0.0;
                for (int i = 7; i < 13; i++) {
                    system[2 * dir][i] = 0.0;
                }
            }

            if (dR != null) {
                system[2 * dir + 1][0] = dR.x * dR.x;
                system[2 * dir + 1][1] = dR.x;
                system[2 * dir + 1][2] = dR.y * dR.y;
                system[2 * dir + 1][3] = dR.y;
                system[2 * dir + 1][4] = dR.z * dR.z;
                system[2 * dir + 1][5] = dR.z;
                system[2 * dir + 1][6] = -1.0;
                for (int i = 7; i < 13; i++) {
                    system[2 * dir + 1][i] = 0.0;
                }
                system[2 * dir + 1][2 * dir + 1 + 7] = 1.0;
            }
            else{
                Face face = cell.getFace(dir, Face.RIGHT);
                Vector dFace = face.center.minus(cell.center);  //vector from cell to face
                Vector normal = cell.getNormal(dir, Face.RIGHT);      //
                
                makeSafe(normal, dir, Face.RIGHT); //temp. fix.
                
                system[2 * dir + 1][0] = 2.0* dFace.x * normal.x;  //debugged * 2
                system[2 * dir + 1][1] = normal.x;
                system[2 * dir + 1][2] = 2.0 * dFace.y * normal.y;
                system[2 * dir + 1][3] = normal.y;
                system[2 * dir + 1][4] = 2.0 * dFace.z * normal.z;
                system[2 * dir + 1][5] = normal.z;
                system[2 * dir + 1][6] = 0.0;
                for (int i = 7; i < 13; i++) {
                    system[2 * dir + 1][i] = 0.0; // debugged + 1
                }
            }
        }
        
        Systems.solve(system);
        
        for(int r=0; r<3; r++)
            for(int i=0; i<7; i++){
                double term = 2.0*system[r*2][i+6];
                // temp. fix: if a cell is too small, the boundary conditions
                // may fail to be solved. setting the laplacian to 0 does not
                // hurt too much in such a small cell.
                if (!Double.isNaN(term) && !Double.isInfinite(term)) {
                    laplacian[i] += term;
                }
                else{
                    if(!warning1issued){
                        Message.warning("warning: cell with 1st-order exchange used.");
                        warning1issued = true;
                    }
                }
            }
        // share equal laplacians.
        laplacian = intern(laplacian);
        // 2009-03-13: set m pointers only now, to test if the method works.
        updateMPointers();
    }

    private boolean warning1issued = false;
    private final Vector UNUSED = new Vector(0, 0, 0);
    // the m pointers of cells are sometimes re-arranges by the adaptive mesh:
    // m pointers of sub-leaf cells point to their parents.
    // we thus need to update those pointers here too...
  public void updateMPointers() {
    for(int i=0; i<neighCell.length; i++){
      if(neighCell[i] != null){
        neighM[i] = neighCell[i].m; // was: m_ex, changed back...
      }
      else
        neighM[i] = UNUSED; // could be done only once at init
    }
  }
    
    private double[] intern(double[] laplacian) {
        DoubleArrayWrapper wrapper = new DoubleArrayWrapper(laplacian);
        wrapper = pool.intern(wrapper);
        return wrapper.array;
    }
    
    /**
     * If a normal vector is zero, we replace it by a unit vector pointing more or
     * less in the right direction.
     */
    private void makeSafe(Vector normal, int dir, int side){
        if(normal.norm() < 1E-14){
            normal.set(0, 0, 0);
            normal.setComponent(dir, 2*side-1);
            Message.warningOnce("A face is too small");
        }
    }
        
    public static void purgePool(){
        Message.debug("Exchange laplacian pool: " + pool.getStats());
        pool = null;
        System.gc();
    }
    
    public final void update(){
        final Vector hEx = cell.hEx;
        
        hEx.x = 0.0;
        hEx.y = 0.0;
        hEx.z = 0.0;
        // todo: rm array indices
        for(int i=0; i<laplacian.length; i++){
            hEx.x += laplacian[i] * neighM[i].x;
            hEx.y += laplacian[i] * neighM[i].y;
            hEx.z += laplacian[i] * neighM[i].z;
        }    
    }
}
