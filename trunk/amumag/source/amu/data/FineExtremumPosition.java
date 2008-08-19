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
import amu.mag.Unit;
import java.io.IOException;
import static amu.geom.Vector.*;

/**
 * Gives the position of the mimimum/maximum of a data set over space, thus
 * removing the spatial dependence of the data. The extremum is interpolated 
 * between the cells.
 * @see ExtremumPosition
 * @author arne
 */
public class FineExtremumPosition extends ExtremumPosition{
    
    //

    
    public FineExtremumPosition(DataModel m, int type){
        this(m, type, 1.0);
    }
    
    public FineExtremumPosition(DataModel m, int type, double portion){
        super(m, type, portion);
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
        //Index extremumIndex = (type == MAX? maxpos: minpos);
        Index extremumIndex = null;
        if(type == MAX)
            extremumIndex = maxpos;
        else if (type == MIN)
            extremumIndex = minpos;
        
        Mesh mesh = getMesh();
        Cell centerCell = mesh.getCell(extremumIndex);
        
        Vector extremumPosition = new Vector();
        
        
        for(int dir = X; dir <= Z; dir++){
            Index left = extremumIndex.sum(Index.UNIT[dir].multiply(-1));
            Cell leftCell = mesh.getCell(left);
            Index right = extremumIndex.sum(Index.UNIT[dir]);
            Cell rightCell = mesh.getCell(right);
            
            if(leftCell != null && rightCell != null){
                double d1 = leftCell.center.getComponent(dir);
                double f1 = originalModel.getDouble(time, left);
                
                double d2 = rightCell.center.getComponent(dir);
                double f2 = originalModel.getDouble(time, right);
                
                double d0 = centerCell.center.getComponent(dir);
                double f0 = originalModel.getDouble(time, extremumIndex);
                
                double interpolatedExtremumPosition = d0 + interpolatePeakPosition(f0, d1-d0, f1, d2-d0, f2);
                extremumPosition.setComponent(dir, interpolatedExtremumPosition);
            }
            else
                extremumPosition.setComponent(dir, centerCell.center.getComponent(dir));
        }
        extremumPosition.multiply(Unit.LENGTH);
        v.set(extremumPosition);
    }

    @Override
    public String getName() {
        return originalModel.getName() + ".fine" + (type == MIN? "minimum": "maximum") + "position";
    }
    
    @Override
    public boolean isVector(){
        return true;
    }

    
    
    private double interpolatePeakPosition(double f0, double d1, double f1, double d2, double f2) {
        double b = (f2-f1) / (d2-d1);
        double a =  ( (f2-f0)/d2 - (f0-f1)/(-d1) ) / (d2 - d1);
        return -b / (2*a);
    }
}
