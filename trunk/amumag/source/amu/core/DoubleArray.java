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
 * Equivalent of ArrayList<Double>, but using primitives for performance.
 */
public final class DoubleArray {
    
    private double[] data;
    private int size;
    
    public DoubleArray(){
        this(10);
    }
    
    public DoubleArray(int capacity){
        if(capacity < 1)
            throw new IllegalArgumentException("Vector capacity < 1");
        data = new double[capacity];
        size = 0;
    }
    
    public int size(){
        return size;
    }
    
    public void add(double d){
        size++;
        if(size == data.length){
            double[] buffer = new double[2*data.length];
            for(int i = 0; i < data.length; i++)
                buffer[i] = data[i];
            data = buffer;
        }
        data[size-1] = d;
    }
    
    public double get(int index){
        return data[index];
    }
	
	public void set(int index, double d){
		if(index >= size)
			throw new ArrayIndexOutOfBoundsException();
		else
			data[index] = d;
	}
	
	public void clear(){
		size = 0;
	}	
	
	public double[] toArray(){
		double[] buffer = new double[size()];
		for(int i = 0; i < buffer.length; i++){
			buffer[i] = data[i];
		}
		return buffer;
	}
}
