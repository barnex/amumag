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
import static amu.geom.Vector.*;

/**
 * Partitions the mesh in cells with an aspect ratio as close as possible
 * to the preferred aspect ratio.
 */
public class PartitionRuleAspect implements PartitionType{

    private Vector aspect;
    
    public PartitionRuleAspect(Vector preferredAspectRatio){
        aspect = new Vector(preferredAspectRatio, true);
    }
    
    public int splitDirection(double x, double y, double z, int level, int nLevels) {
        x /= aspect.x;
        y /= aspect.y;
        z /= aspect.z;
        
        if(x >= y && x >= z)
	    return X;
	else if(y >= x && y >= z)
	    return Y;
	else
	    return Z;
    }

    @Override
    public String toString(){
        return "preferred cell aspect ratio " + aspect.x + " x " + aspect.y + " x " + aspect.z;
    }
}
