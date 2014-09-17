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

import amu.geom.Vector;
import amu.mag.Face;

public final class Sort {
    
    /**
     * Sort list a and perform the same moves on list b.
     * (This method is used 
     * @param a
     * @param b
     */
    public static void duoSort(Vector[] a, Face[] b){
        for(int i=1; i<a.length; i++){
            int j=i;
            while(j>0 && a[j].compareTo(a[j-1]) < 0){
                Vector buffer = a[j];
                a[j] = a[j-1];
                a[j-1] = buffer;
                
                Face buffer2 = b[j];
                b[j] = b[j-1];
                b[j-1] = buffer2;
                
                j--;
            }
        }
    }
}
