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
import amu.geom.Vector;
import amu.mag.Cell;
import amu.core.Index;
import java.io.IOException;
import static java.lang.Math.abs;
        
/**
 * Returns the maximum or minimum value of the data set over space, thus removing
 * the spatial dependence of the data.
 * @author arne
 */
public class Extremum extends SpaceProbe{

    /**
     * return minimum, maximum or value with maximum absolute value (keeping its sign).
     */
    public static final int MIN = -1, MAX = 1, MAX_ABS = 0;
    private double portion = 1.0;
    protected int type;
            
    public Extremum(DataModel m, int type){
        this(m, type, 1.0);
    }
    
    public Extremum(DataModel m, int type, double portion){
        super(m);
        this.type = type;
        if(type != MIN && type != MAX && type != MAX_ABS)
            throw new IllegalArgumentException("type = " + type + ", should be 0, 1 or -1");
        this.portion = portion;
    }

    @Override
    public void put(int time, Vector v) throws IOException {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        Index index = new Index();
        Cell[][][] base = originalModel.getMesh().baseLevel;
        for(int i= (int)(0.5*(1-portion)*base.length); i< (int)(0.5*(1+portion)*base.length); i++)
            for(int j=(int)(0.5*(1-portion)*base[0].length); j<(int)(0.5*(1+portion)*base[0].length); j++)
              
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
        else if (type == MAX_ABS){
            if(abs(max) > abs(min))
                v.x = max;
            else
                v.x = min;
        }
        else
            throw new Bug();
    }

    @Override
    public String getName() {
        return originalModel.getName() + "." + (type == MIN? "minimum": "maximum");
    }
}
