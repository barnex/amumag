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

import amu.debug.Bug;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.core.Index;
import java.io.IOException;

/**
 * Returns the maximum or minimum value of the data set over space, thus removing
 * the spatial dependence of the data.
 * @author arne
 */
public class Extremum extends SpaceProbe{

    public static final int MIN = -1, MAX = 1;
    
    protected int type;
            
    public Extremum(DataModel m, int type){
        super(m);
        this.type = type;
        if(type != MIN && type != MAX)
            throw new IllegalArgumentException("type = " + type + ", should be 1 or -1");
        
    }

    @Override
    public void put(int time, Vector v) throws IOException {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        Index index = new Index();
        Cell[][][] base = originalModel.getMesh().baseLevel;
        for(int i=0; i<base.length; i++)
            for(int j=0; j<base[i].length; j++)
                for(int k=0; k<base[i][j].length; k++){
                    Cell cell = base[i][j][k];
                    if(cell != null){
                        index.set(i, j, k);
                        double value = originalModel.getDouble(time, index);
                        if(value > max)
                            max = value;
                        if(value < min)
                            min = value;
                    }
                }
        if(type == MAX)
            v.x = max;
        else if(type == MIN)
            v.x = min;
        else
            throw new Bug();
    }

    @Override
    public String getName() {
        return originalModel.getName() + "." + (type == MIN? "minimum": "maximum");
    }
}
