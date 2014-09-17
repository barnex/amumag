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
import static java.lang.Double.NaN;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.core.Index.UNIT;

/**
 * Takes the time derivative of a time-dependend data set.
 * @see RunningDerivative
 * @author arne
 */
public class Gradient extends DerivedDataModel{

    public Gradient(DataModel model){
        super(model);
        if(model.isVector())
            throw new IllegalArgumentException("Gradient needs a scalar argument.");
    }
    
    private final Index neighIndex = new Index();
    private final Vector f0 = new Vector(), fNeigh = new Vector();
    
    public void put(int time, Index r, Vector v) throws IOException {

        v.set(0, 0, 0);

        Cell cell = originalModel.getMesh().getBaseCell(r);
        if (cell != null) {
            Vector center = cell.center;
            originalModel.put(time, r, f0);

            for (int comp = X; comp <= Z; comp++) {
                double partial = 0.0;

                double centerPos = center.getComponent(comp);

                int neighborCount = 0;
                for (int side = -1; side <= +1; side += 2) {
                    neighIndex.set(r);
                    neighIndex.add(side, UNIT[comp]);
                    Cell neighbor = originalModel.getMesh().getBaseCell(neighIndex);
                    originalModel.put(time, neighIndex, fNeigh);
                    if (neighbor != null) {
                        neighborCount++;
                        double neighborPos = neighbor.center.getComponent(comp);
                        double delta = centerPos - neighborPos;
                        partial += (f0.x - fNeigh.x) / delta;
                    }
                }
                if (neighborCount != 0) {
                    partial /= neighborCount;
                }
                v.setComponent(comp, partial);
            }
        }
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "grad";
    }

    @Override
    public String getUnit() {
        return originalModel.getUnit() + "*m-1";
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }
    
    @Override
    public boolean isVector(){
        return true;
    }

}