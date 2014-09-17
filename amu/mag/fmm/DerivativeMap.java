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
import amu.debug.Bug;
import amu.debug.Timer;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.*;
import amu.io.Message;
import java.util.Hashtable;

/**
 * d[r][n] is the nth (vector) derivative of the coulomb potential 1/r at r (vector).
 * r is the shift of a cell with respect to its parent (?)
 * eg: d[r][(0, 0, 0)] = 1/r, d[r][(1, 0, 0)] = d/dx(1/r)
 * these are calculated only once for each and everey r ever needed.
 * Each r vector needed in the simulation is given an integer index used to
 * fetch d[r][n].
 **/
public final class DerivativeMap {

    public static double[][] d;
    
    public static void hash(Cell root){
	
        Hashtable<Vector, Integer> shift2index = new Hashtable<Vector, Integer>();
        java.util.Vector<Vector>index2shift = new java.util.Vector<Vector>();
        
        int currentIndex = 0;
        int shiftCount = 0;
        for(Cell cell = root; cell != null; cell = cell.next){
            Cell[] partners = cell.getPartners();
            int[] partnerShifts = new int[partners.length];
            for(int p = 0; p < partners.length; p++){
                Cell partner = partners[p];
                Vector shift = new Vector(cell.center);
                shift.subtract(partner.center);
                shiftCount++;
                if(shift2index.get(shift) == null){                             //this shift is not mapped onto an integer yet
                    shift2index.put(shift, new Integer(currentIndex));
                    index2shift.add(shift);
                    currentIndex ++;
                }
                partnerShifts[p] = shift2index.get(shift).intValue();
            }
            cell.setPartnerShifts(partnerShifts);
        }
	
        Message.debug(shiftCount + " shifts -> " + currentIndex + " indices.");
	
        
        d = new double[currentIndex][FMM.monomials.length];
        for(int shiftI=0; shiftI<d.length; shiftI++){
            Vector shift = index2shift.get(shiftI);
            for(int in = 0; in < FMM.monomials.length; in++){
                d[shiftI][in] = FMM.d(in, shift);
            }
        }
        shift2index = null;
	
	System.gc();
    }
}
