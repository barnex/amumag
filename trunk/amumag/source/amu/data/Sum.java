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
import java.io.IOException;

public final class Sum extends DerivedDataModel{

    private final DataModel b;

    public Sum(DataModel a, DataModel b){
        super(a);
        this.b = b;
    }
    
    private final Vector buffer = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        originalModel.put(time, r, v);
        b.put(time, r, buffer);
        v.add(buffer);
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }

    @Override
    public String getName() {
       return "sum" + originalModel.getName() + "+" + b.getName();
    }

    @Override
    public String getUnit() {
        return originalModel.getUnit();
    }

}