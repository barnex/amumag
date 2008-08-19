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

package amu.geom;


public interface PartitionType {
    
    /**
     * Returns the direction (X, Y or Z) in which cells of a level should be split into
     * child cells.
     * @param x X-size of the cell
     * @param y Y-size of the cell
     * @param z Z-size of the cell
     * @param level discretization level of the cell
     * @param nLevels total number of discretization levels in the mesh.
     * @return the direction in which a cell should be split
     */
    public abstract int splitDirection(double x, double y, double z, int level, int nLevels);
    
}
