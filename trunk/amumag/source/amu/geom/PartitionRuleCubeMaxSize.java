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
import amu.debug.Bug;
import amu.mag.Unit;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;

public final class PartitionRuleCubeMaxSize implements PartitionType{
    
    private final Vector maxSize;
    
    public PartitionRuleCubeMaxSize(Vector maxSize){
        this.maxSize = new Vector(maxSize);
        this.maxSize.makeImmutable();
    }
    
    private final Vector size = new Vector();
    
    public int splitDirection(double x, double y, double z, int level, int nLevels) {
//        if(x > maxSize.x && x >= y && x >= z)
//	    return X;
//	else if(y > maxSize.y && y >= x && y >= z)
//	    return Y;
//	else if(z > maxSize.z)
//	    return Z;
//        else throw new Bug();
        
        // (1) check suited split directions (size > maxSize)
        size.set(x, y, z);
        for(int c=X; c<= Z; c++)
            if(size.getComponent(c) <= maxSize.getComponent(c))
                size.setComponent(c, -1); // means this direction should not be split anymore
        
        // (2) split in the largest suited direction.
        int splitDir = X;
        double largest = size.x;
        for(int c=Y; c<= Z; c++)
            if(size.getComponent(c) > largest){
                splitDir = c;
                largest = size.getComponent(c);
            }
        
        //System.out.println("split: " + splitDir);
        return splitDir;
    }

    @Override
    public String toString(){
        return "approx. cube cells, max size: " + maxSize;
    }
}