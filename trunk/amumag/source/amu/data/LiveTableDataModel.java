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
import amu.mag.Simulation;
import amu.mag.Unit;
//import amu.mag.time.AdamsEvolver2;
import amu.mag.time.AmuSolver;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Data of a live running simulation. The data is inDependent on space.
 * @author arne
 */
public final class LiveTableDataModel extends DataModel{

    private Object obj;
    private final Simulation sim;
    private String fieldName;
    private Field field;
    private boolean isVector;
    
    public LiveTableDataModel(Simulation sim, String fieldName) throws NoSuchFieldException {
        this.sim = sim;
        this.fieldName = fieldName;
        try {
            field = AmuSolver.class.getField(fieldName);
            this.obj = sim.solver;
        } catch (NoSuchFieldException ex) {
            field = Simulation.class.getField(fieldName);
            this.obj = sim;
        }
        
        if(field.getType().equals(Vector.class))
            isVector = true;
        else if(field.getType().equals(Double.TYPE))
            isVector = false;
        else throw new IllegalArgumentException("Field " + fieldName + " has type " + field.getType() + ", but only double or Vector types can be saved.");
    }
    
    @Override
   public double getTimeForIncrementalSave(){
        return sim.getTotalTime() * Unit.TIME;
   }

    public void incrementalSave() throws IOException {
        incrementalSave(sim.getBaseDirectory());
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        try {
            if (time != -1) {
                throw new IllegalArgumentException();
            }
            if (r != null) {
                throw new IllegalArgumentException();
            }
            if(isVector)
                v.set((Vector)field.get(obj));
            else
                v.x = field.getDouble(obj);
        } catch (IllegalArgumentException ex) {
            throw new Bug(ex);
        } catch (IllegalAccessException ex) {
            throw new Bug(ex);
        }
    }

    @Override
    public boolean isSpaceDependent() {
        return false;
    }

    @Override
    public Mesh getMesh() {
        return null;
    }

    @Override
    public boolean isTimeDependent() {
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
