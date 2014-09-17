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

import amu.geom.Vector;
import amu.mag.Cell;
import amu.core.Index;
import amu.mag.Unit;
import java.io.IOException;

/**
 * Gives the position of the mimimum/maximum of a data set over space, thus
 * removing the spatial dependence of the data. The position is truncated to
 * the cell size.
 * @see FineExtremumPosition
 * @author arne
 */
public class ExtremumPosition extends Extremum{
    
    protected double portion;
          
    public ExtremumPosition(DataModel m, int type){
        this(m, type, 1.0);
    }
    
    public ExtremumPosition(DataModel m, int type, double portion){
        super(m, type);
        this.portion = portion;
    }

    @Override
    public void put(int time, Vector v) throws IOException {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        Index index = new Index();
        Index minpos = new Index();
        Index maxpos = new Index();
        Cell[][][] base = originalModel.getMesh().baseLevel;
       
       for(int i= (int)(0.5*(1-portion)*base.length); i< (int)(0.5*(1+portion)*base.length); i++)
            for(int j=(int)(0.5*(1-portion)*base[0].length); j<(int)(0.5*(1+portion)*base[0].length); j++)
                
                for(int k=0; k<base[i][j].length; k++){
                    Cell cell = base[i][j][k];
                    if(cell != null){
                        index.set(i, j, k);
                        double value = originalModel.getDouble(time, index);
                        if(value > max){
                            max = value;
                            maxpos.set(index);
                        }
                        if(value < min){
                            min = value;
                            minpos.set(index);
                        }
                    }
                }
        Index extremumindex = (type == MAX? maxpos: minpos);
        Vector extremumposition = getMesh().getBaseCell(extremumindex).center;
        v.set(extremumposition);
        v.multiply(Unit.LENGTH);
    }

    @Override
    public String getName() {
        return originalModel.getName() + "." + (type == MIN? "minimum": "maximum") + "position";
    }
    
    @Override
    public String getUnit(){
        return "m";
    }
    
    @Override
    public boolean isVector(){
        return true;
    }
}
