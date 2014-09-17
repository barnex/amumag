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

/**
 * A Pool that internalizes entire arrays. If the arrays are equal, they are
 * internalized as a whole AND their elements are internalized. If they are
 * not equal, only the elements will be internalized. A flag can disable
 * element internalization.
 */
public final class ArrayPool<T> {

    private final Pool<T> elements;
    private final Pool<ArrayWrapper<T>> arrays;
    private final boolean internalizeElements;
    
    public ArrayPool(boolean internalizeElements){
	arrays = new Pool<ArrayWrapper<T>>();
	this.internalizeElements = internalizeElements;
	if(internalizeElements)
	    elements = new Pool<T>();
	else
	    elements = null;
    }
    
    public T[] intern(T[] array){
	ArrayWrapper<T> w = new ArrayWrapper<T>(array);
	
	if(internalizeElements && !arrays.contains(w))
	    internalizeElements(array);
	return arrays.intern(w).array;
    }
    
    private void internalizeElements(T[] array){
	for(int i=0; i<array.length; i++){
	    array[i] = elements.intern(array[i]);
	}
    }
    
     public String getStats(){
	return "arrays: " + arrays.getStats() + (internalizeElements? ", elements: " + elements.getStats() : "");
    }
}
