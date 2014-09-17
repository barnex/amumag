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

package amu.geom.solid;

import amu.core.Recycler;
import amu.geom.Vector;
import java.io.Serializable;
import static java.lang.Math.*;

public abstract class Shape implements Serializable{
    
    private static final Recycler<Vector> recycler = new Recycler<Vector>(new Vector[100]);
    
    /**
     * Use this method to check if r is insideInternal the shape, r will not be muted.
     * @param r
     * @return
     */
    public final boolean insideInternal(Vector r){
	Vector fresh = recycler.get();
	fresh.set(r);
	boolean inside = inside(fresh);
	recycler.recycle(fresh);
	return inside;
    }
    
    /**
     * Internal method to check if r is insideInternal, r may be muted and will be
     * recycled later.
     * @param r
     * @return
     */
    protected abstract boolean inside(Vector r);
    
    //______________________________________________________geometric transforms
    
    public final Shape translate(double x, double y, double z){
	return new TranslatedShape(this, x, y, z);
    }
    
    public final Shape translate(double x, double y){
	return new TranslatedShape(this, x, y, 0.0);
    }
    
    public final Shape scale(double r){
        return new ScaledShape(this, r);
    }
    
    public final Shape rotateZ(double angle){
	return new TransformedShape(this, new Vector[]{new Vector(cos(angle), sin(angle), 0),
						       new Vector(-sin(angle), cos(angle), 0),
						       new Vector(0, 0, 1)});
    }

    public final Shape rotateY(double angle){
	return new TransformedShape(this, new Vector[]{new Vector(cos(angle), 0, sin(angle)),
						       new Vector(0, 1, 0),
						       new Vector(-sin(angle), 0, cos(angle))});
    }
    
    public final Shape rotateX(double angle){
	return new TransformedShape(this, new Vector[]{new Vector(1, 0, 0),
						       new Vector(0, cos(angle), sin(angle)),
						       new Vector(0, -sin(angle), cos(angle))});
    }
    
    public final Shape mirrorX(){
        return new TransformedShape(this, new Vector[]{
                                                       new Vector(-1, 0, 0),
						       new Vector(0, 1, 0),
						       new Vector(0, 0, 1)});
    }
    
    public final Shape mirrorY(){
        return new TransformedShape(this, new Vector[]{
                                                       new Vector(-1, 0, 0),
						       new Vector(0, 1, 0),
						       new Vector(0, 0, 1)});
    }
    
    
    public final Shape mirrorZ(){
        return new TransformedShape(this, new Vector[]{
                                                       new Vector(-1, 0, 0),
						       new Vector(0, 1, 0),
						       new Vector(0, 0, 1)});
    }
    
    //________________________________________________________boolean operations
    
    public final Shape intersect(Shape other){
	return new Intersection(this, other);
    }
    
    public final Shape join(Shape other){
	return new Union(this, other);
    }
    
    public final Shape inverse(){
	return new Inverse(this);
    }
    
    public final Shape subtract(Shape other){
	return intersect(other.inverse());
    }
    
    public final Shape xor(Shape other){
	return join(other).subtract(intersect(other));
    }
    
    //___________________________________________________________________utility
    
    public static final double square(double x){
        return x*x;
    }
}
