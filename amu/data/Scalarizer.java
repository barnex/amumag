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

import amu.geom.Vector;
import amu.core.Index;
import java.io.IOException;

/**
 * Superclass for DerivedDataModels who turn vector data into scalar data.
 * @author arne
 */
public abstract class Scalarizer extends DerivedDataModel{

    private Vector dataBuffer = new Vector();
    
    public Scalarizer(DataModel model){
        super(model);
    }
    
    @Override
    public double getDouble( int time,Index r) throws IOException{
        originalModel.put(time, r, dataBuffer);
        return toDouble(dataBuffer);
    }
    
    public abstract double toDouble(Vector v);
 
    @Override
    public void put( int time,Index r, Vector v) throws IOException {
        v.x = getDouble(time, r);
    }
    
    @Override
    public boolean isVector(){
        return false;
    }
    
   public int getTimeDomain(){
        return originalModel.getTimeDomain();
   }
   
    @Override
    public Index getSpaceDomain(){
        return originalModel.getSpaceDomain();
    }
    
    @Override
    public String getUnit() {
        return originalModel.getUnit();
    }
}
