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
import java.io.IOException;

/**
 * Dummy data set which holds a constant.
 * @author arne
 */
public class ConstantModel extends DataModel{

    private Vector value;
    private Mesh mesh;
    
    public ConstantModel(Mesh mesh, Vector value){
        this.value = value;
        this.mesh = mesh;
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        v.set(value);
    }

    @Override
    public boolean isSpaceDependend() {
        return true;
    }

    @Override
    public Mesh getMesh() {
        return mesh;
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
        return true;
    }

    @Override
    public String getName() {
        return "constant" + value;
    }

    @Override
    public String getUnit() {
        return "unspecified.";
    }

}