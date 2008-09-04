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
import java.io.File;
import java.io.IOException;

/**
 * DataModel similar to differential images.
 * @author arne
 */
public final class Differential extends DerivedDataModel{

    private int delta;
    private SavedDataModel ref;
    
    public Differential(File file, int delta) throws IOException{
        super(new SavedDataModel(file));
        ref = new SavedDataModel(file);
        this.delta = delta;
    }
    
    private final Vector buffer = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        ref.put(time, r, buffer);
        originalModel.put(time+delta, r, v);
        v.subtract(buffer);
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain() - delta;
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "diff" + delta;
    }

    @Override
    public String getUnit() {
        return originalModel.getUnit();
    }

}