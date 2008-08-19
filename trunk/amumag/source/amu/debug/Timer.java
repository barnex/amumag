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

/**
 * Prints execution time and memory usage.
 */
public class Timer {
    
    // start time
    private long start;
    private long time;
    
    public void start(){
	start = System.currentTimeMillis();
    }
    
    public void stop(){
	time = System.currentTimeMillis()-start;
    }
    
    @Override
    public String toString(){
        return time + " ms, " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024) + "MB";
    }
}
