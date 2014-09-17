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

package amu.mag.config;

import amu.core.Index;
import amu.geom.Vector;
import amu.mag.Unit;
import static java.lang.Math.sqrt;
import static amu.geom.solid.Shape.square;

/**
 * Magnetic vortex configuration.
 * @author arne
 */
public class Vortex extends Configuration{

    private final Vector center;
    private final int circulation;
    private final int polarization;
    
    private boolean once = true;
    
    // radius of the vortex core, in exchange lengths.
    public static final double CORE_RADIUS = 1.5;
            
    /**
     * Vortex around the center, circulation 1, polarization 0.
     */
    public Vortex(){
        this(0, 0, 0);
    }
    
    /**
     * Vortex around the center, circulation 1.
     */
    public Vortex(int polarization){
        this(polarization, 1);
    }
    
    /**
     * Vortex around the center.
     */
    public Vortex(int circulation, int polarization){
        this(0, 0, 0, polarization, circulation);
    }
    
    /**
     * Vortex around (x, y, z), circulation 1, polarization 0.
     */
    public Vortex(double x, double y, double z){
        this(x, y, z, 0);
    }
    
    /**
     * Vortex around (x, y, z), circulation 1.
     */
    public Vortex(double x, double y, double z, int polarization){
        this(x, y, z, polarization, 1);
    }
    
    /**
     * Vortex around (x, y, z).
     */
    public Vortex(double x, double y, double z, int polarization, int circulation){
        if(circulation != 1 && circulation != -1)
            throw new IllegalArgumentException("vortex circulation must be 1 or -1");
        // z should be 0 to work also in 3D
        this.center = new Vector(x, y, 0);
        this.polarization = polarization;
        this.circulation = circulation;
    }
    
    @Override
    public void putM(double x, double y, double z, Vector target, Index index) {
        if(once){
            center.divide(Unit.LENGTH);
            once = false;
        }
        // z should be 0 to work also in 3D
        target.set(x, y, 0);
        // we can not divide by Unit.LENGTH earlier because it is not yet set
        // during Problem.init().
	target.subtract(center);
	target.cross(Vector.UNIT[Vector.Z]);
        target.multiply(circulation);
	target.normalize();
	// if the cell position is exactly at the center, we do not want to set it to NaN
        if(target.isNaN())
            target.set(1,0,0);
        // add a vortex core, if specified.
        if(polarization != 0){
            double distance = sqrt(square(x-center.x) + square(y-center.y));
            if(distance <= CORE_RADIUS){
                target.add(0, 0, polarization);
                Configuration.normalizeVerySafe(target);
            }
        }
    }
    
    @Override
    public String toString(){
        return "Vortex, C = " + circulation + ", P = " + polarization;
    }
}
