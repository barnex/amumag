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

import amu.geom.Mesh;
import amu.geom.Vector;
import amu.core.Index;
import java.io.IOException;

/**
 * Superclass for a dataModel which is derived from an other model.
 * @author arne
 */
public abstract class DerivedDataModel extends DataModel{

    protected DataModel originalModel;
    
    public DerivedDataModel(DataModel originalModel){
        setOriginalModel(originalModel);
    }
    
    @Override
    public abstract void put( int time,Index r, Vector v) throws IOException;
    
    public Mesh getMesh(){
        return originalModel.getMesh();
    }
    
    public double[] getTime(){
        return originalModel.getTime();
    }
    
    @Override
    public double getTimeForIncrementalSave(){
        return originalModel.getTimeForIncrementalSave();
    }
    
    public void setOriginalModel(DataModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public boolean isVector() {
        return originalModel.isVector();
    }

    @Override
    public boolean isTimeDependent() {
        return originalModel.isTimeDependent();
    }

    @Override
    public boolean isSpaceDependent() {
        return originalModel.isSpaceDependent();
    }
    
}
