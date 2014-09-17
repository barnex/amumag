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

package amu.geom.jelly;

import amu.core.Index;
import amu.geom.Mesh;
import amu.geom.Vector;

/**
 * Limits an other homeomorphism to the top layer of cells.
 * @author arne
 */
public final class Outerlayer extends Homeomorphism{

    private final Homeomorphism original;
    
    public Outerlayer(Homeomorphism original){
        this.original = original;
    }
    
    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("Unused");
    }

    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex){
        int z = cellIndex.z + vertexIndex.z;
        
        int maxZ = mesh.baseLevel[0][0].length;
        if(z == 0 || z == maxZ)
            original.getMove(r, target, mesh, cellIndex, vertexIndex);
        else
            target.set(0, 0, 0);
    }
}