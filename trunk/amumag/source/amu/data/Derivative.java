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
import static java.lang.Double.NaN;

/**
 * Takes the time derivative of a time-dependend data set.
 * @see RunningDerivative
 * @author arne
 */
public class Derivative extends DerivedDataModel{

    public Derivative(DataModel model){
        super(model);
    }
    
    private final Vector point1 = new Vector(), point2 = new Vector();
     public void put(int time, Index r, Vector v) throws IOException {
        //0 or 1 time points: can not derive :-(
        if (getTimeDomain() < 2) {
            v.set(NaN, NaN, NaN);
        } else {
            int time1, time2;
            if (time == 0) {
                time1 = 0;
                time2 = 1;
            } else if (time == getTimeDomain() - 1) {
                time1 = getTimeDomain() - 2;
                time2 = getTimeDomain() - 1;
            } else {
                time1 = time - 1;
                time2 = time + 1;
            }

            originalModel.put(time1, r, point1);
            originalModel.put(time2, r, point2);
            v.set(point2);
            v.subtract(point1);
            v.divide(getTime()[time1] - getTime()[time2]);
        }
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