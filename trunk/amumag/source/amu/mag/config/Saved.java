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

package amu.mag.config;

import amu.core.Index;
import amu.geom.Vector;
import amu.io.ArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class Saved extends Configuration{

    private Vector[][][] saved;
    
    public Saved(File file) throws IOException{
        saved = new ArrayInputStream(new FileInputStream(file)).readVectorBlock();
    }
    
    @Override
    public void putM(double x, double y, double z, Vector target, Index index) {
        target.set(saved[index.x][index.y][index.z]);
    }
}