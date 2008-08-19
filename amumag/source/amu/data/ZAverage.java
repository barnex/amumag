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
import static amu.core.Index.Z;

/**
 * Replaces the data with the averaged data over the thickness of the sample.
 * This corresponds to making an image with a Scanning Transmission microscope.
 */
public final class ZAverage extends DerivedDataModel{

    private final int zsize;
    
    public ZAverage(DataModel model){
        super(model);
        zsize = model.getMesh().baseLevel[0][0].length;
    }
    
    private final Index index = new Index();
    private final Vector buffer = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        index.set(r);
        v.reset();
        for(int i=0; i<zsize; i++){
            index.setComponent(Z, i);
            originalModel.put(time, index, buffer);
            v.add(buffer);
        }
        v.divide(zsize);
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "z-average";
    }

    @Override
    public String getUnit() {
        return originalModel.getUnit();
    }
}