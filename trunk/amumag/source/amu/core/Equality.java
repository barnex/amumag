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
import static java.lang.Math.*;

/**
 * Module for equaltiy and hashcodes.
 * TODO: carefully check equals() and hashCode() in Cell, Face (!), Vector.
 * -> 2007-dec-06: Face.equals not always correct.
 */
public final class Equality {
    
    public static boolean equals(double a, double b){
        double error = abs(a-b);
        boolean equal = (error < 1E-13);
        return equal;
    }
    
    public static double round(double d){
        if(abs(d) < 1E-13)
            return 0.0;
        else
            return d;
    }
    
    public static boolean equals(double[] a, double[] b){
	if(a.length == b.length){					// same number of vertices
	    int i = 0;
	    while(i < a.length && (float)a[i] == (float)b[i])
		i++;
	    return i == a.length;
	} else									// different number of vertices
	    return false;
    }
    
    /**
     * A hashCode for a double, similar to Double.hashCode, but the double is
     * rounded to a float first: small round-off errors should not cause doubles
     * to be considered different.
     */
    public static int hashCode(double d){
	d = (float)d;
	long v = Double.doubleToLongBits(d);
	return (int)(v^(v>>>32));
    }
    
    /**
     * Deep hash code, using hashcode(double).
     */
    public static int hashCode(double[] array){
	int hash = 0;
	int factor = 1;
	for(int i=0; i<array.length; i++){
	    hash += factor*hashCode(array[i]);
	    factor *= 13;
	}
	return hash;
    }
}
