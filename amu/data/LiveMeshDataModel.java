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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Data of a live running simulation. The data is Dependent on space.
 * @author arne
 */
public final class LiveMeshDataModel extends DataModel{

    private final Simulation sim;
    private final String name;
    private Method method;
    private Field field;
    private boolean isVector;
    
    public static final Class[] EMPTY = new Class[]{};
    
    public LiveMeshDataModel(Simulation sim, String name){
        this.sim = sim;
        try {
            // check if "name" is the name of a field
            field = Cell.class.getField(name);
            if (field.getType().equals(Vector.class)) {
                isVector = true;
            } else if (field.getType().equals(double.class)) {
                isVector = false;
            } else {
                throw new IllegalArgumentException("Type of " + name + " is " + field.getType() + ", should be double or Vector.");
            }
            method = null;
        } catch (NoSuchFieldException e) {
            // ... otherwise check if get_"name" is a method
            try {
                method = Cell.class.getMethod("get_" + name, EMPTY);
                if (method.getReturnType().equals(Vector.class)) {
                    isVector = true;
                } else if (method.getReturnType().equals(double.class)) {
                    isVector = false;
                } else {
                    throw new IllegalArgumentException("Type of " + name + " is " + method.getReturnType() + ", should be double or Vector.");
                }
                field = null;
            } catch (NoSuchMethodException e2) {
                // "name is invalid"
                throw new IllegalArgumentException("Output field does not exist: " + name);
            }
        }
        this.name = name;
    }
    
    @Override
   public double getTimeForIncrementalSave(){
        return sim.getTotalTime()*Unit.TIME;
   }

    public void incrementalSave() throws IOException {
        incrementalSave(sim.getBaseDirectory());
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        if (time != -1) {
            throw new IllegalArgumentException();
        }

        Cell cell = sim.mesh.getBaseCell(r);
        if (cell != null) {
            try {
                if (isVector) {
                    if(method == null)
                        v.set((Vector) field.get(cell));
                    else
                        v.set((Vector) method.invoke(cell, (Object[])EMPTY));
                } else {
                    if(method == null)
                        v.x = field.getDouble(cell);
                    else
                        v.x = ((Double)(method.invoke(cell, (Object[])EMPTY))).doubleValue();
                }
            } catch (IllegalArgumentException e) {
                throw new Bug(e);
            } catch (IllegalAccessException e2) {
                throw new Bug(e2);
            }
            catch(InvocationTargetException e3){
                throw new Error(e3);
            }
        } 
        else{
            // if there is no cell, we save (0, 0, 0);
            v.reset();
        }
    }

    @Override
    public boolean isSpaceDependent() {
        return true;
    }

    @Override
    public Mesh getMesh() {
        return sim.mesh;
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
        return name;
    }

    @Override
    public String getUnit() {
        return Unit.getUnit(name);
    }
}
