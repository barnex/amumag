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

import java.util.Arrays;

/**
 * Used as a key in the arrays hashtable of ArrayWrapper. The equals() is a deep
 * equals that tests for equal() array elements.
 */
public final class ArrayWrapper<T>{

    public final T[] array;
    public final int hashCode;
    
    public ArrayWrapper(T[] array) {
        this.array = array;
        hashCode = Arrays.deepHashCode(array);
    }

    @Override
    public boolean equals(Object other) {
        assert other instanceof ArrayWrapper;
        ArrayWrapper otherWrapper = (ArrayWrapper) other;
        assert array.getClass().equals(otherWrapper.array.getClass());
        return Arrays.equals(array, otherWrapper.array);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}