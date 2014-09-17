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

package amu.mag.fmm;

import amu.mag.*;

/**
 * A Vector with integer components, representing a monomial of x,y,z.
 * E.g. (1, 2, 3) represents x*y^2*z^3. Can also be (ab)used to
 * acces a 3D array.
 */
public final class IntVector{
    
    public final int x, y, z;
    /**
     * The order of the monomial, i.e. the sum of all components.
     */
    public final int order, factorial;
    
    public static final int X=0, Y=1, Z=2;
    
    
    //__________________________________________________________________________
    
    public static final IntVector[] UNIT = new IntVector[]{
	new IntVector(1, 0, 0),
	new IntVector(0, 1, 0),
	new IntVector(0, 0, 1)
    };
    
    public static final IntVector UNITY = new IntVector(0, 0, 0);
    
    //__________________________________________________________________________
    
    public IntVector(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.order = x+y+z;
        this.factorial = fac(order);
    }
    
    public IntVector(){
        this(0, 0, 0);
    }
    
    public IntVector(IntVector v){
        this(v.x, v.y, v.z);
    }
    
    //__________________________________________________________________________
    
   
   
    //__________________________________________________________________________
    
    public static int fac(int number){
        int fac = 1;
        for(int i = 2; i <= number; i++)
            fac *= i;
        return fac;
    }
    
    //__________________________________________________________________________
    
    /**
     * Inefficient sum.
     */
    public IntVector sum(IntVector a){
        return new IntVector(x+a.x, y+a.y, z+a.z);
    }
    
    /**
     * Inefficient subtraction.
     */
    public IntVector subtract(IntVector a){
        return new IntVector(x-a.x, y-a.y, z-a.z);
    }
    
    /**
     * Inefficient multiplication by a constant.
     */
    public IntVector multiply(int a){
        return new IntVector(a*x, a*y, a*z);
    }
    
    //__________________________________________________________________________
    
    @Override
    public boolean equals(Object other){
        if(other instanceof IntVector){
            IntVector v = (IntVector)other;
            return x == v.x && y == v.y && z == v.z;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.x;
        hash = 83 * hash + this.y;
        hash = 83 * hash + this.z;
        return hash;
    }
    
    //__________________________________________________________________________
    
    @Override
    public String toString(){
        if(x==0 && y ==0 && z==0)
            return "1 ";
        else
            return (x==0?"":"x^"+x) + (y==0?"":"y^"+y) + (z==0?"":"z^"+z);
    }
}
