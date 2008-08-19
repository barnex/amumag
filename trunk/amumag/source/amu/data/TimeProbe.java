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
 * the time of simulation data.
 */
public abstract class TimeProbe extends DerivedDataModel{
    
    public TimeProbe(DataModel model){
        super(model);
    }
    
    public void put( int time,Index r, Vector v) throws IOException{
        if(time != -1)
            throw new IllegalArgumentException("model is time independent");
        else
            put(r, v);
    }
   
    public abstract void put(Index r, Vector v) throws IOException;
  
    @Override
    public boolean isTimeDependend(){
        return false;
    }
    
    @Override
    public Index getSpaceDomain() {
        return originalModel.getSpaceDomain();
    }

    @Override
    public int getTimeDomain() {
        return -1;
    }
   
     @Override
    public String getUnit() {
        return originalModel.getUnit();
    }
}
