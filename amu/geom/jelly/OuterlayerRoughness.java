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

public class OuterlayerRoughness extends ToplayerRoughness{
      
    protected double[][] landscape2;
    

    public OuterlayerRoughness(double amplitude, double length){
        super(amplitude, length);
    }
    
    @Override
    protected void init(Mesh mesh) {
       super.init(mesh);
       landscape2 = roughness(cellsX+1, cellsY+1, amplitude, length/cellSize.x); //todo: change to x,y
    }
    
    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        if(!initiated)
            init(mesh);
        
        int x = cellIndex.x + vertexIndex.x;
        int y = cellIndex.y + vertexIndex.y;
        int z = cellIndex.z + vertexIndex.z;
        
        int maxZ = mesh.baseLevel[0][0].length;
        if(z == 0)
            target.z = landscape[x][y];
        else if(z == maxZ)
            target.z = landscape2[x][y];
    }
}
