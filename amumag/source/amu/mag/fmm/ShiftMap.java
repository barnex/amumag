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

package amu.mag.fmm;
import amu.geom.Vector;
import amu.mag.*;
import amu.io.Message;
import java.util.Hashtable;

/**
 * Perhaps we could replace all of this by a sparse matrix class + Intern.
 * (premature optimalization...)
 *
 * The shift of a child with respect to its parent has only a small number of possible values.
 * This class makes a map of shift-integer pairs, which allows every different shift, shiftFactor[]
 * and shiftIndex[] to be stored only once (instead of storing equal copies in each Cell).
 * TODO: qindex
 */
public final class ShiftMap {
    
    public void hash(Cell root){
	
        Hashtable<Vector, Integer> shift2index = new Hashtable<Vector, Integer>();
        java.util.Vector<Vector>index2shift = new java.util.Vector<Vector>();
        
        int currentIndex = 0;
        int cellCount = 0;
        for(Cell c = root; c != null; c = c.next){
            cellCount++;
            Vector shift = c.getShift();
            if(shift2index.get(shift) == null){                                 //this shift is not mapped onto an integer yet
                shift2index.put(shift, new Integer(currentIndex));
                index2shift.add(shift);
                currentIndex ++;
            }
        }
        
        Message.debug(cellCount + " shifts -> " + (currentIndex) + " indices.");
        
        double[][] shiftFactor = new double[currentIndex][FMM.monomials.length];
        int[][] shiftIndex = new int[currentIndex][];
        
        for(int s=0; s<shiftFactor.length; s++){
            Vector shift = index2shift.get(s);
            int count = 0;
            for(int i = 0; i < shiftFactor[s].length; i++){
                shiftFactor[s][i] = (1.0/FMM.monomials[i].factorial) * shift.pow(FMM.monomials[i]);
                if(shiftFactor[s][i]!=0)
                    count++;
            }
            shiftIndex[s] = new int[count];
            int j = 0;
            for(int i = 0; i < shiftFactor[s].length; i++){			//todo: do not keep zero's in memory -> no more need for shiftIndices?
                if(shiftFactor[s][i]!=0){
                    shiftIndex[s][j] = i;
                    j++;
                }
            }
        }
        
        for(Cell c=root; c!=null; c=c.next){
            int index = shift2index.get(c.getShift()).intValue();
            c.setShiftFactors(shiftFactor[index], shiftIndex[index]);
        }
        
        index2shift = null;                                                     //clear some memory
        shift2index = null;
	
	System.gc();
    }
}
