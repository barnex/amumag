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

public class TopRoughness extends SubstrateRoughness{

    public TopRoughness(double amplitude, double length){
        super(amplitude, length);
    }
    
    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        if(!initiated)
            init(mesh);
        
        int x = (int) xInt.transf(r.x);
        int y = (int) yInt.transf(r.y);
        
        if(x >= landscape.length)
            x = landscape.length - 1;
        if(x < 0)
            x = 0;
        if(y >= landscape[0].length)
            y = landscape[0].length - 1;
        if(y < 0)
            y = 0;
        
       target.z = landscape[x][y] * zInt.transf(r.z);
    }
}