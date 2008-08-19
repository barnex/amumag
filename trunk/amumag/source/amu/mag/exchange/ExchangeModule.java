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
//import amu.debug.Bug;
//import amu.geom.Mesh;
//import amu.geom.Vector;
//import amu.mag.Cell;
//import amu.core.Index;
//import static amu.geom.Vector.*;
//
///**
// * Obsolete implementation.
// * Stateless implementation of exchange field. Can be replaced by an implementation
// * with cache.
// */
//public final class ExchangeModule {
//
//    private final Mesh mesh;
//    /** buffers */
//    private final Index index, delta; 
//
//    public ExchangeModule(Mesh mesh){
//        this.mesh = mesh;
//         index = new Index();
//        delta = new Index();
//    }
//    
//    public void update(){
//        // redundant.
//        index.set(0, 0, 0);
//        delta.set(0, 0, 0);
//        
//        final Cell[][][] base = mesh.baseLevel;
//        for(int x=0; x<base.length; x++){
//            Cell[][] baseX = base[x];
//            for(int y=0; y<baseX.length; y++){
//                Cell[] baseXY = baseX[y];
//                for(int z=0; z<baseXY.length; z++){
//                    
//                    Cell cell = baseXY[z];
//                    if(cell != null){
//                        Vector h = cell.hEx;
//                        for (int i = X; i <= Z; i++) {  // component of h
//                            double hi = 0;
//                            for (int j = X; j <= Z; j++) {  // direction of laplacian
//                                
//                                //index.set(x, y, z);
//                                index.x = x;
//                                index.y = y;
//                                index.z = z;
//                                
//                                //delta.reset();
//                                delta.x = 0;
//                                delta.y = 0;
//                                delta.z = 0;
//                                delta.setComponent(j, -1);
//                                index.add(delta);
//                                Cell neighL = mesh.getCell(mesh.nLevels - 1, index);
//
//                                index.set(x, y, z);
//                                delta.reset();
//                                delta.setComponent(j, 1);
//                                index.add(delta);
//                                Cell neighR = mesh.getCell(mesh.nLevels - 1, index);
//
//                                double mi0 = cell.m.getComponent(i);
//                                
//                                if(neighL == null)
//                                    neighL = cell;
//                                if(neighR == null)
//                                    neighR = cell;
//                   
//                                double miL =  neighL.m.getComponent(i);
//                                double miR =  neighR.m.getComponent(i);
//                                
//                                double deltaL = cell.center.getComponent(j) - neighL.center.getComponent(j);
//                                double deltaR = neighR.center.getComponent(j) - cell.center.getComponent(j);
//
//                                if(deltaL == 0.0)
//                                    deltaL = deltaR;
//                                else if(deltaR == 0)
//                                    deltaR = deltaL;
//                                if(deltaL == 0.0 && deltaR == 0.0) //degenerate case of no neighbors
//                                    deltaL = deltaR = 1.0;
//                                
//                                hi += ((miR - mi0) / deltaR - (mi0 - miL) / deltaL) / (deltaL + deltaR);
//                                if(Double.isNaN(hi))
//                                    throw new Bug();
//                            }
//                            h.setComponent(i, 2.0*hi);  //factor 2.0 is absorbed in derivative??? probl. not!
//                                                        //2008-02-05: added *2, for std probl 2.
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
//}
