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

package amu.core;

import amu.mag.*;

/**
 * A Vector with integer components, representing a monomial of x,y,z.
 * E.g. (1, 2, 3) represents x*y^2*z^3. Can also be (ab)used to
 * acces a 3D array.
 */
public final class Index{
    
    public int x, y, z;
    
    public static final int X=0, Y=1, Z=2;
    
    
    //__________________________________________________________________________
    
    public static final Index[] UNIT = new Index[]{
	new Index(1, 0, 0),
	new Index(0, 1, 0),
	new Index(0, 0, 1)
    
    };
    
    public static final Index UNITY = new Index(0, 0, 0);
    
    //__________________________________________________________________________
    
    public Index(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Index(){
        this(0, 0, 0);
    }
    
    public Index(Index v){
        this(v.x, v.y, v.z);
    }

    public void add(int delta, Index index) {
        x += delta*index.x;
        y += delta*index.y;
        z += delta*index.z;
    }

    public void reset() {
        x = 0;
        y = 0; 
        z = 0;
    }
    
    //__________________________________________________________________________
    
    public void set(Index v){
        x = v.x;
        y = v.y;
        z = v.z;
    }
    
    public void set(int x, int y, int z){
	this.x = x; 
	this.y = y;
	this.z = z;
    }
    
    public void setComponent(int component, int value){
        switch(component){
            case X: x = value; break;
            case Y: y = value; break;
            case Z: z = value; break;
            default: throw new IllegalArgumentException();
        }
    }
    
    public int getComponent(int component){
        switch(component){
            case X: return x;
            case Y: return y;
	    case Z: return z;
            default: throw new IllegalArgumentException();
        }
    }
    
    //__________________________________________________________________________
    
    /**
     * Inefficient sum.
     */
    public Index sum(Index a){
        return new Index(x+a.x, y+a.y, z+a.z);
    }
    
    public void add(Index a){
        x += a.x;
        y += a.y;
        z += a.z;
    }
    
    /**
     * Inefficient subtraction.
     */
    public Index minus(Index a){
        return new Index(x-a.x, y-a.y, z-a.z);
    }
    
    /**
     * Inefficient multiplication by a constant.
     */
    public Index multiply(int a){
        return new Index(a*x, a*y, a*z);
    }
    
    //__________________________________________________________________________
    
    @Override
    public boolean equals(Object other){
        if(other instanceof Index){
            Index v = (Index)other;
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
