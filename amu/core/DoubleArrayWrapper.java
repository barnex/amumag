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

/**
 * Used as a key in the arrays hashtable of ArrayWrapper. The equals() is a deep
 * equals that tests for equal() array elements.
 */
public final class DoubleArrayWrapper{

    public final double[] array;
    public final int hashCode;
    
    public DoubleArrayWrapper(double[] array) {
        
        if(array == null)
            throw new IllegalArgumentException();
        
        this.array = array;
        
        int hash = 0;
        int factor = 1;
        for(int i=0; i<array.length; i++){
            hash += factor * Equality.hashCode(array[i]);
            factor *= 97;
        }
        this.hashCode = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DoubleArrayWrapper) {
            DoubleArrayWrapper otherWrapper = (DoubleArrayWrapper) other;

            for (int i = 0; i < array.length; i++) {
                if (!Equality.equals(array[i], otherWrapper.array[i])) {
                    return false;
                }
            }
            return true;

        } else {
            throw new Bug();
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}