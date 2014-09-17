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

package amu.data;

//package amu.xdata.model;
//
//import amu.core.Index;
//import amu.geom.Mesh;
//import amu.geom.Vector;
//import java.io.IOException;
//
//public final class NoData extends DataModel(){
//
//    @Override
//    public void put(int time, Index r, Vector v) throws IOException {
//        v.reset();
//    }
//
//    @Override
//    public boolean isSpaceDependend() {
//        return true;
//    }
//    
//    @Override
//    public Mesh getMesh() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public boolean isTimeDependend() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public int getTimeDomain() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public double[] getTime() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public boolean isVector() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public String getName() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public String getUnit() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    
//    
//}