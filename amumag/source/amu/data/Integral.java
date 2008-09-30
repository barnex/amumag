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
import amu.mag.Cell;
import java.io.IOException;

public final class Integral extends SpaceProbe{

    public Integral(DataModel m){
        super(m);
    }
    
    private final Index index = new Index();
    private final Vector buffer  = new Vector();
    
    @Override
    public void put(int time, Vector v) throws IOException {
        //todo: incorporate volume, also in average.
        Mesh mesh = originalModel.getMesh();
        Cell[][][] base = mesh.baseLevel;
        
        index.reset();
        buffer.reset();
        v.reset();
 
        for (int i = 0; i < base.length; i++) {
            index.x = i;
            for (int j = 0; j < base[i].length; j++) {
                index.y = j;
                for (int k = 0; k < base[i][j].length; k++) {
                    Cell cell = base[i][j][k];
                    if (cell != null) {
                        index.z = k;
                        originalModel.put(time, index, buffer);
                        v.add(buffer);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return originalModel.getName() + Names.OPERATOR + "integral";
    }


}