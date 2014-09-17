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
import static amu.data.Names.*;

/**
 * Removes the dependence on time by probing the data
 * at a given time.
 */
public class AtTime extends TimeProbe {

    private int time;

    public AtTime(DataModel model, int time) {
        super(model);
        this.time = time;
    }
    
    @Override
    public void put(Index r, Vector v) throws IOException {
        originalModel.put(time, r, v);
    }

    public void setTime(int time) {
        this.time = time;
    }
    
    public int getTime_(){
        return time;
    }

    @Override
    public String getName() {
        return originalModel.getName() + OPERATOR + "atTime" + time;
    }
    
    @Override
    public double getTimeForIncrementalSave(){
        return originalModel.getTime()[time];
    }
}
