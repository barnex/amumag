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

package amu.mag.adapt;

import amu.mag.Cell;

public final class TestAdaptiveMesh extends AdaptiveMeshRules{

    private int coarseRootLevel;

    public TestAdaptiveMesh(){
        this.coarseRootLevel = mesh.nLevels - 1 - 2;
    }
    
    public int getCoarseRootLevel() {
        return coarseRootLevel;
    }

    @Override
    public void updateUniform() {
        for(Cell cell=mesh.rootCell; cell != null; cell = cell.next)
            cell.uniform = false;

        for(Cell[][] levelI: mesh.coarseLevel)
            for(Cell[] levelIJ: levelI)
                for(Cell cell: levelIJ){
                    cell.uniform = isUniform(cell);
                    cell.uniformDebug = cell.uniform? 1.0: 0.0;
                }
    }

    @Override
    public boolean isUniform(Cell cell) {
        if(cell.child1 == null)
            return true;
        else
            return cell.child1.m.dot(cell.child2.m) > 0.9;
    }
    
}