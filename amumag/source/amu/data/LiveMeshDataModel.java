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
import amu.debug.Bug;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Simulation;
import amu.mag.Unit;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Data of a live running simulation. The data is dependend on space.
 * @author arne
 */
public final class LiveMeshDataModel extends DataModel{

    private final Simulation sim;
    private final String fieldName;
    private final Field field;
    private final boolean isVector;
    
    public LiveMeshDataModel(Simulation sim, String fieldName) throws NoSuchFieldException{
        this.sim = sim;
        field = Cell.class.getField(fieldName);
        if(field.getType().equals(Vector.class))
            isVector = true;
        else if(field.getType().equals(double.class))
            isVector = false;
        else
            throw new Bug();
        this.fieldName = fieldName;
    }
    
    @Override
   public double getTimeForIncrementalSave(){
        return sim.totalTime*Unit.TIME;
   }

    public void incrementalSave() throws IOException {
        incrementalSave(sim.getBaseDirectory());
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        if (time != -1) {
            throw new IllegalArgumentException();
        }

        Cell cell = sim.mesh.getCell(r);
        if (cell != null) {
            try {
                if (isVector) {
                    v.set((Vector) field.get(cell));
                } else {
                    v.x = field.getDouble(cell);
                }
            } catch (IllegalArgumentException e) {
                throw new Bug(e);
            } catch (IllegalAccessException e2) {
                throw new Bug(e2);
            }
        } 
        else{
            // if there is no cell, we save (0, 0, 0);
            v.reset();
        }
    }

    @Override
    public boolean isSpaceDependend() {
        return true;
    }

    @Override
    public Mesh getMesh() {
        return sim.mesh;
    }

    @Override
    public boolean isTimeDependend() {
        return false;
    }

    @Override
    public int getTimeDomain() {
        return -1;
    }

    @Override
    public double[] getTime() {
        return null;
    }

    @Override
    public boolean isVector() {
        return isVector;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public String getUnit() {
        return Unit.getUnit(fieldName);
    }
}
