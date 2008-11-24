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
import amu.geom.Mesh;
import amu.geom.Vector;
import java.io.IOException;

public final class Table extends DataModel{

    private final DataModel a, b, c;
    
    public Table(DataModel a, DataModel b, DataModel c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    private final Vector buffer = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        a.put(time, r, buffer);
        v.x = buffer.x;
        b.put(time, r, buffer);
        v.y = buffer.x;
        c.put(time, r, buffer);
        v.z = buffer.x;
    }

    @Override
    public boolean isSpaceDependent() {
        return false;
    }

    @Override
    public Mesh getMesh() {
        return null;
    }

    @Override
    public boolean isTimeDependent() {
        return false;
    }

    @Override
    public int getTimeDomain() {
        return -1;
    }

    @Override
    public double[] getTime() {
       return null;
    }

    @Override
    public boolean isVector() {
       return true;
    }

    @Override
    public String getName() {
        return "table-" + a.getName() + "-" + b.getName() + "-" + c.getName();
    }

    @Override
    public String getUnit() {
        return a.getUnit() + "-" + b.getUnit() + "-" + c.getUnit();
    }
    
    @Override
    public double getTimeForIncrementalSave(){
        return a.getTimeForIncrementalSave();
    }
    
}