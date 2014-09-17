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


public class Gyrofield extends DerivedDataModel{

    public Gyrofield(DataModel model){
        super(model);
    }
    
    protected final Vector M = new Vector();
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        v.set(0, 0, 0);
        originalModel.put(time, r, M);
        
        updateGradients(time, r);
        
        for (int i = X; i <= Z; i++) {
            double g = 0.0;
            for (int j = X; j <= Z; j++) {
                for (int k = X; k <= Z; k++) {
                    for (int m = X; m <= Z; m++) {
                        for (int n = X; n <= Z; n++) {
                            for (int p = X; p <= Z; p++) {
                                int d = d(m, n, p, i, j, k);
                                if (d != 0) {
                                    g += d *
                                         M.getComponent(m) *
                                         gradOfComponent[n].getComponent(j) *
                                         gradOfComponent[p].getComponent(k);
                                }
                            }
                        }
                    }
                }
            }
            v.setComponent(i, -0.5*g);
        }
    }
    
    protected final Index neighIndex = new Index();
    protected final Vector f0 = new Vector(), fNeigh = new Vector();
    
    protected final Vector[] gradOfComponent = new Vector[]{new Vector(), new Vector(), new Vector()};
    
    /**
     * Levi-Civita symbol (totally asymmetric unit tensor).
     */
    protected int e(int i, int j, int k){
        if(i == 0 && j == 1 && k == 2
         ||i == 1 && j == 2 && k == 0
         ||i == 2 && j == 0 && k== 1)
            return 1;
        else if(i == 1 && j == 0 && k == 2
              ||i == 0 && j == 2 && k == 1
              ||i == 2 && j == 1 && k == 0)
            return -1;
        else
            return 0;
    }
    
    /**
     * Generalized Kronecker symbol.
     */
    protected int d(int m, int n, int p, int i, int j, int k){
        return e(i, j, k) * e (m, n, p);
    }
    
    protected void updateGradients(int time, Index r) throws IOException {
        int base = getMesh().nLevels - 1;
        Cell cell = originalModel.getMesh().getCell(base, r);
        if (cell != null) {
            Vector center = cell.center;
            originalModel.put(time, r, f0);

            for (int gradientOfComponent = X; gradientOfComponent <= Z; gradientOfComponent++) {
                for (int comp = X; comp <= Z; comp++) {
                    double partial = 0.0;

                    double centerPos = center.getComponent(comp);

                    int neighborCount = 0;
                    for (int side = -1; side <= +1; side += 2) {
                        neighIndex.set(r);
                        neighIndex.add(side, UNIT[comp]);
                        Cell neighbor = originalModel.getMesh().getCell(base, neighIndex);
                        originalModel.put(time, neighIndex, fNeigh);
                        if (neighbor != null) {
                            neighborCount++;
                            double neighborPos = neighbor.center.getComponent(comp);
                            double delta = centerPos - neighborPos;
                            partial += (f0.getComponent(gradientOfComponent) - fNeigh.getComponent(gradientOfComponent)) / delta;
                        }
                    }
                    if (neighborCount != 0) {
                        partial /= neighborCount;
                    }
                    gradOfComponent[gradientOfComponent].setComponent(comp, partial);
                }
            }
        }
    }

    @Override
    public int getTimeDomain() {
        return originalModel.getTimeDomain();
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "gyrofield";
    }

    @Override
    public String getUnit() {
        return "gyrofield";
    }
}