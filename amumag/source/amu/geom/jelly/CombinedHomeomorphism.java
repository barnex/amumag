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

public class CombinedHomeomorphism extends Homeomorphism{
    
    private Homeomorphism a;
    private Homeomorphism b;
    private Vector buffer = new Vector();
    
    public CombinedHomeomorphism(Homeomorphism a, Homeomorphism b){
        this.a = a;
        this.b = b;
    }
    
    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        a.getMove(r, buffer, mesh, cellIndex, vertexIndex);
        b.getMove(r, target, mesh, cellIndex, vertexIndex);
        target.add(buffer);
    }

    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("Unused");
    }

}