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
 * Superclass for a DerivedDataModel who removes the dependence on 
 * the position of simulation data.
 */
abstract class SpaceProbe extends DerivedDataModel{
    
    public SpaceProbe(DataModel model){
        super(model);
    }
    
    public abstract void put(int time, Vector v) throws IOException;
    
    public void put( int time,Index r, Vector v) throws IOException{
        if(r != null)
            throw new IllegalArgumentException("model is space independent");
        else
            put(time, v);
    }
    
    @Override
    public boolean isSpaceDependend(){
        return false;
    }
    
    @Override
    public Index getSpaceDomain() {
        return null;
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }
    
     @Override
    public String getUnit() {
        return originalModel.getUnit();
    }
}
