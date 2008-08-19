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
import static java.lang.Math.abs;

    /**
     * Absolute Value of a data set.
     */
public class Abs extends Operator{

    public Abs(DataModel model){
        super(model);
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        originalModel.put(time, r, v);
        v.x = abs(v.x);
        v.y = abs(v.y);
        v.z = abs(v.z);
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "abs";
    }
}