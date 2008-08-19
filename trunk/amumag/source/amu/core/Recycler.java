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

import amu.debug.Bug;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Generic object recycler. Not thread safe, but tries to detect concurrent acces.
 */
public final class Recycler<T> {
    
    // heap of recycled objects
    private final T[] heap;
    
    // constructor to create new objects when the heap runs empty
    private final Constructor<T> constructor;
    
    // current size of the heap, <= heap.length;
    private int size;
    
    // concurrent acces detector.
    private boolean access;
    
    // for statistics
    private int maxSize;
    private boolean gotEmpty = false;
    private boolean gotFull = false;
    private int throughput = 0;
    
    // statics
    private static final Class[] EMPTY_CLASS = new Class[]{};
    private static final Object[] EMPTY_OBJ = new Object[]{};
    
    /**
     * Create a new Recycler with a heap size given by the array length. We have
     * to give an arry instead of an integer because generic array creation is
     * not possible, sigh...
     */
    @SuppressWarnings("unchecked")
    public Recycler(T[] heap){
	this.heap = heap;
	try{
	    //unchecked
	    constructor = (Constructor<T>) (heap.getClass().getComponentType().getConstructor(EMPTY_CLASS));
	}
	catch(NoSuchMethodException e){
	    throw new Bug(e);
	}
	catch(ClassCastException e2){
	    throw new Bug(e2);
	}
	size = 0;  // initial heap is empty, would be nice but annoying if it started filled.
    }
    
    /**
     * If possible, an Object from the recycling heap is returned. A new Object
     * is created and returned otherwise.
     */
    public T get(){
	// check and set concurrent acces detector.
	assert !access;
	access = true;
	
	throughput++;
	T object;
	
	// heap empty: create new object
	if(size == 0)
	    try{
		gotEmpty = true;
		object = constructor.newInstance(EMPTY_OBJ);
	    }
	    catch(InstantiationException e){
		throw new Bug(e);
	    }
	    catch(IllegalAccessException e2){
		throw new Bug(e2);
	    }
	    catch(InvocationTargetException e3){
		throw new Bug(e3);
	    }
	    catch(IllegalArgumentException e4){
		throw new Bug(e4);
	    }
	else{
	    // get an object form the heap
	    object = heap[size-1];
	    heap[size-1] = null;
	    size--;
	}
	
	// clear acces
	access = false;
	
	return object;
    }
    
    /**
     * Adds the Object to the recycling heap if there is place left. The Object
     * may later be returned by recycle().
     */
    public void recycle(T object){
	// check and set concurrent acces detector.
	assert !access;
	access = true;
	
	// refuse to recycle air.
	assert object != null;
	
	// store object on the recycling heap, if possible
	if(size < heap.length){
	    heap[size] = object;
	    size++;
	    if(size > maxSize)
		maxSize = size;
	}
	else
	    // throw it away otherwise
	    gotFull = true;
	
	// clear acces.
	access = false;
    }
    
    public String getStats(){
	return "throughput: " + throughput + ", maxSize: " + maxSize + ", got full: " + gotFull + ", got empty: " + gotEmpty;
    }
}
