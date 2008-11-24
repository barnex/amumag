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
import amu.mag.Cell;
import java.io.IOException;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.core.Index.UNIT;


public final class DampingTensor extends Gyrofield{

       public DampingTensor(DataModel model){
        super(model);
    }
 
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        v.set(0, 0, 0);
        originalModel.put(time, r, M);
        updateGradients(time, r);
        
        for (int i = X; i <= Z; i++) {
            double d_ii = 0.0;
                for (int k = X; k <= Z; k++) {
                                   d_ii += 
                                         gradOfComponent[k].getComponent(i) *
                                         gradOfComponent[k].getComponent(i);
            }
            v.setComponent(i, d_ii);
        }
    }
    
   
    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "dampingtensor";
    }

    @Override
    public String getUnit() {
        return "dampingtensor";
    }
}