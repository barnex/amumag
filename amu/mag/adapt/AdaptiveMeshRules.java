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

import amu.geom.Mesh;
import amu.mag.Cell;

public abstract class AdaptiveMeshRules{

    // needs to be set after creation.
    public Mesh mesh;
    
    /**
     * @return The coarsest level which may be used to run the simulation.
     */
   public abstract int getCoarseRootLevel();
   
   /**
    * Updates the "stop" flag of each cell, indicating that m is sufficiently 
    * uniform below this cell. 
    * @param mesh
    */
   public abstract void update();
   
   public abstract boolean isUniform(Cell cell);
   
}