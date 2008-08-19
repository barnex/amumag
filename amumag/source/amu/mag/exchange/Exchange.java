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

//package amu.mag.exchange;
//
//import amu.core.ArrayPool;
//import amu.core.Index;
//import amu.geom.Mesh;
//import amu.geom.Vector;
//import amu.mag.Cell;
//import amu.output.Message;
//import static amu.geom.Vector.X;
//import static amu.geom.Vector.Y;
//import static amu.geom.Vector.Z;
//
//public final class Exchange {
//
//    public static final int LEFT = 0, CENTER = 1, RIGHT = 2;
//    
//    public Vector[] nabla2; //todo: pool me.
//    
//    public Vector[] neighL, neighR;
//    public final Cell cell;
//    
//    public static ArrayPool<Vector> pool = new ArrayPool<Vector>(true);
//    
//    public Exchange(Mesh mesh, Index cellPos){
//        this.cell = mesh.getCell(cellPos);
//        
//        nabla2 = new Vector[3];
//        for(int i=0; i<nabla2.length; i++)
//            nabla2[i] = new Vector();
//        
//        neighL = new Vector[3];
//        neighR = new Vector[3];
//        
//        Index index = new Index();
//        for (int dir = X; dir <= Z; dir++) {
//
//            // set pointers to the neighbours' magnetization
//            Cell neighCellL = null;
//            Cell neighCellR = null;
//            
//            index.set(cellPos);
//            index.add(-1, Index.UNIT[dir]);
//            neighCellL = mesh.getCell(index);
//            if (neighCellL != null) {
//                neighL[dir] = neighCellL.m;
//            } else {                              //No neighbour cell: boundary condition
//                neighL[dir] = cell.m;
//            }
//            
//            index.set(cellPos);
//            index.add(Index.UNIT[dir]);
//            neighCellR = mesh.getCell(index);
//            if (neighCellR != null) {
//                neighR[dir] = neighCellR.m;
//            } else {                                      
//                neighR[dir] = cell.m;
//            }
//
//            double dL;
//            if(neighCellL != null)
//                dL = cell.center.getComponent(dir) - neighCellL.center.getComponent(dir);
//            else
//                dL = 0.0;
//            
//            double dR;
//            if(neighCellR != null)
//                dR = neighCellR.center.getComponent(dir) - cell.center.getComponent(dir);
//            else
//                dR = 0.0;
//            
//            if(neighCellL == null)
//                dL = dR;
//            if(neighCellR == null)
//                dR = dL;
//            
//            if(neighCellL == null && neighCellR == null){
//                // case of no neighbours in this direction
//                // due to the boundary conditions, the laplacian in this direction is zero.
//                for(int i=LEFT; i <= RIGHT; i++)
//                    nabla2[i].setComponent(dir, 0.0);
//            }
//            else{
//                nabla2[LEFT]  .setComponent(dir,  2.0/(dL*(dR+dL)) );
//                nabla2[CENTER].setComponent(dir, -2.0/(dL*dR)      );
//                nabla2[RIGHT] .setComponent(dir,  2.0/(dR*(dR+dL)) );
//            }    
//        }
//        
//        for(int i=0; i<nabla2.length; i++)
//            nabla2[i].makeImmutable();
//        
//        nabla2 = pool.intern(nabla2);
//    }
//
//    public static void purgePool(){
//        Message.println("Exchange nabla2 pool: " + pool.getStats());
//        pool = null;
//        System.gc();
//    }
//    
//    public void update(){
//        Vector hEx = cell.hEx;
//        Vector m0 = cell.m;
//        
//        hEx.x  = nabla2[LEFT].x * neighL[X].x + nabla2[CENTER].x * m0.x + nabla2[RIGHT].x * neighR[X].x;
//        hEx.x += nabla2[LEFT].y * neighL[Y].x + nabla2[CENTER].y * m0.x + nabla2[RIGHT].y * neighR[Y].x;
//        hEx.x += nabla2[LEFT].z * neighL[Z].x + nabla2[CENTER].z * m0.x + nabla2[RIGHT].z * neighR[Z].x;
//        
//        hEx.y  = nabla2[LEFT].x * neighL[X].y + nabla2[CENTER].x * m0.y + nabla2[RIGHT].x * neighR[X].y;
//        hEx.y += nabla2[LEFT].y * neighL[Y].y + nabla2[CENTER].y * m0.y + nabla2[RIGHT].y * neighR[Y].y;
//        hEx.y += nabla2[LEFT].z * neighL[Z].y + nabla2[CENTER].z * m0.y + nabla2[RIGHT].z * neighR[Z].y;
//        
//        hEx.z  = nabla2[LEFT].x * neighL[X].z + nabla2[CENTER].x * m0.z + nabla2[RIGHT].x * neighR[X].z;
//        hEx.z += nabla2[LEFT].y * neighL[Y].z + nabla2[CENTER].y * m0.z + nabla2[RIGHT].y * neighR[Y].z;
//        hEx.z += nabla2[LEFT].z * neighL[Z].z + nabla2[CENTER].z * m0.z + nabla2[RIGHT].z * neighR[Z].z;
//        
//    }
//}
