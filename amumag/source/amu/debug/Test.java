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

package amu.debug;

import amu.geom.Vector;

/**
 * Tests a few core methods.
 * ->obsolete?
 */
public final class Test{

    public static void run(){
        //2007-dec-08: removed vector equality check
	//if(!testVector()) throw new Bug();
	//testPool();
	//testArrayPool();
    }
    
    public static boolean testVector(){
	boolean ok = true;
	Vector a = new Vector();
	a.set(1, 2, 3);
	Vector b = new Vector(1+1E-9, 2-1E-10, 3+1E-10);
	Vector c = new Vector(3, 4, 5);
	
	ok &= a.equals(b);
	ok &= a.hashCode() == b.hashCode();
	ok &= !a.equals(c);
	return ok;
    }
}
