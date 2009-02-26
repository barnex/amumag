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

import amu.debug.Bug;
import amu.mag.Cell;

public final class FixedMesh extends AdaptiveMeshRules{
    
    public int getCoarseRootLevel() {
        return mesh.nLevels - 1;
    }

    @Override
    public void updateUniform() {
       assert mesh.coarseRoot == mesh.baseRoot;
        
        for(Cell cell=mesh.rootCell; cell != null; cell = cell.next)
            cell.uniform = false;
        for(Cell cell=mesh.coarseRoot; cell != null; cell = cell.next)
            cell.uniform = true;
    }

    @Override
    public boolean isUniform(Cell cell) {
        return false;
    }
    
    @Override
    public String toString(){
        return "fixed mesh";
    }
    
}