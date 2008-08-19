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

import amu.core.Index;
import amu.geom.Vector;
import amu.mag.Unit;
import java.io.IOException;

/**
 * Time derivative of incrementally obtained data, e.g. during a running simulation.
 * Can be used for live simulation data, which is time-independend 
 * (the available data is always a snapshot of the running simulation).
 * @author arne
 */
public class RunningDerivative extends DerivedDataModel{

    private boolean hasPrevious = false;
    private double prevTime;
    private Vector prevValue = new Vector();
    
    public RunningDerivative(DataModel m){
        super(m);
        if(m.isSpaceDependend())
            throw new IllegalArgumentException("DataModel must be space-independend.");
    }
    
    Vector value = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        
        if(!hasPrevious){
            v.set(0, 0, 0);
            hasPrevious = true;
        }
        else{
            originalModel.put(time, r, value);
            v.set(value);
            v.subtract(prevValue);
            v.divide(getTimeForIncrementalSave() - prevTime);
        }
        prevValue.set(value);
        prevTime = getTimeForIncrementalSave();
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "ddt";
    }

    @Override
    public String getUnit() {
        return originalModel.getUnit() + "*s-1";
    }

}