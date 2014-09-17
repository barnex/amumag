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

import java.util.Hashtable;

/**
 * A Hashtable to remove duplicate (equal) objects from the memory and to use
 * a pointer to a single object instead.
 */
public final class Pool<T> {
    
    public final Hashtable<T, T> hash;
    private int in = 0, out = 0;
    
    public Pool(){
	hash = new Hashtable<T, T>();
    }
    
    /**
     * If an object equal to this object already exists in the pool, this is
     * returned, otherwise it is added to the pool and returned.
     */
    public T intern(T object){
        assert object != null;
        assert object.equals(object);
	/*if(object == null)
	    throw new Bug();*/
	/*if(!object.equals(object))
            throw new Bug();*/
        out++;
	
        
	T intern = hash.get(object);
	if(intern == null){
	    in++;
	    hash.put(object, object);
	    return hash.get(object);	    //redundant, but to be sure...
	}
	else
	    return intern;
    }
    
    /**
     * Add an object to the pool, if the pool already contains the object,
     * an error is thrown.
     */
    public void add(T object){
	/*if(object == null)
	    throw new Bug();
	if(contains(object))
	    throw new Bug();*/
        assert object != null;
        assert !contains(object);
        in++;
        hash.put(object, object);
    }
    
    /**
     * Checks if the pool contains the object.
     */
    public boolean contains(T object){
	/*if(object == null)
	    throw new Bug();*/
        assert object != null;
	return (hash.get(object) != null);
    }
    
    /**
     * Gets the object, equal to (argument) out of the pool, returns null
     * if the pool does not contain the object.
     */
    public T get(T object){
	assert object != null;
        out++;
	return hash.get(object);
    }
    
    public String getStats(){
	return in + " in, " + out + " out, compression = " + (float)((double)in/(double)out);
    }
}
