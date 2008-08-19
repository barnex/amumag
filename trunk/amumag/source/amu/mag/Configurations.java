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

package amu.mag;

import amu.geom.Vector;
import amu.geom.Mesh;
import amu.mag.fmm.IntVector;
import static java.lang.Math.*;
import static amu.geom.Vector.UNIT;
import static amu.geom.Vector.Z;
import java.util.Random;

/**
 * TODO: voor random mag: zie instrumentatie in java/archief.
 */
public final class Configurations {
        
   public static void setUniform(Mesh mesh, Vector m, IntVector start, IntVector stop){
        Cell[][][] base = mesh.baseLevel;
        for(int i=start.x; i<=stop.x; i++)
            for(int j=start.y; j<=stop.y; j++)
                for(int k=start.z; k<=stop.z; k++)
                    base[i][j][k].m.set(m);
    }
    
    public static void setUniform(Mesh mesh, Vector m){
        Cell[][][] base = mesh.baseLevel;
        for(Cell[][] baseI: base)
            for(Cell[] baseIJ: baseI)
                for(Cell cell: baseIJ)
		    if(cell != null)
			cell.m.set(m);
    }
    
    public static void addRandom(Mesh mesh, double amplitude, int seed){
        amplitude *= 2;
        Random rnd = new Random(seed);
        Cell[][][] base = mesh.baseLevel;
        for(Cell[][] baseI: base)
            for(Cell[] baseIJ: baseI)
                for(Cell cell: baseIJ)
		    if(cell != null){
                    double x = amplitude * (rnd.nextDouble() - 0.5);
                    double y = amplitude * (rnd.nextDouble() - 0.5);
                    double z = amplitude * (rnd.nextDouble() - 0.5);
                    cell.m.add(x, y, z);
                    cell.m.normalize();
                }
    }
    
    public static void addRandom(Mesh mesh, double amplitude){
        addRandom(mesh, amplitude, new Random().nextInt());
    }
    
    public static void setRandom(Mesh mesh, int seed){
        Random rnd = new Random(seed);
        Cell[][][] base = mesh.baseLevel;
        for(Cell[][] baseI: base)
            for(Cell[] baseIJ: baseI)
                for(Cell cell: baseIJ)
		    if(cell != null){
                    /*double phi = rnd.nextDouble() * 2 * PI;
                    double theta = acos(rnd.nextDouble());
                    if(rnd.nextBoolean())
                        theta = -theta;
                    double x = cos(phi) * cos(theta);
                    double y = sin(phi) * cos(theta);
                    double z = sin(theta);*/
                    double x = rnd.nextDouble();
                    double y = rnd.nextDouble();
                    double z = rnd.nextDouble();
                    cell.m.set(x, y, z);
                    cell.m.divide(cell.m.norm());
                }
    }
    
    public static void setRandom(Mesh mesh){
        setRandom(mesh, new Random().nextInt());
    }
}
