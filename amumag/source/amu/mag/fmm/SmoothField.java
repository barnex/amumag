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

import amu.mag.*;
import java.io.Serializable;


public final class SmoothField implements Serializable{
    
    public int shift;								// position shift from parent, index points to Vector shift in ShiftHash
    public double[] shiftFactor;						// cache: shiftFactor[i_n] = (1/n! * shift^n), for monomial index i_n
    public int[] shiftIndex;							// indices for which the shiftFactor != 0.0
    public double[] field;							// taylor expansion of the smooth field: V(r)=\Sum_i {1/n_i! * smoothField_i * r^n_i}
    public Cell[] partners;
    public int[] partnerShifts;
	
    public SmoothField() {
    }
    
    public void init(){
	field = new double[FMM.monomials.length];
    }
    
    /**
     * Sets this smoothField to its parents shifted smoothField.
     * -> can be inlined in update().
     */
    public void setToShiftedParent(Cell parent){	
	final int l = FMM.order;
	double[] parentField = parent.getSmoothField();
	for(int indexM = 0; indexM < parentField.length; indexM++){
	    IntVector vecM = FMM.monomials[indexM];
	    double sum = 0;
	    int max = FMM.boundForOrder[l-vecM.order]; 
	    for(int i = 0; i < max; i++){
		sum += parentField[FMM.monoSum[indexM][i]] * shiftFactor[i];			//do not include zero's
	    }
	    field[indexM] = sum;
	}	
    }
    
    /**
     * First, the field is set to the field of the parent, adding the necessary shift,
     * then the contributions from this cell's partners are added.
     */
    public void update(Cell parent){
	if(parent != null)
	    setToShiftedParent(parent);
	/*else
	    reset();*/				// 2007-07-18: set to zeemann, currently 0 so not necessary yet.
	
	final int l = FMM.order;
	
	for(int i = 0; i < partners.length; i++){				// add contributions from partner cells to the smooth field
	    Cell partner = partners[i];
	    double[] partnerQ = partner.multipole.q;
	    double[] dHashDPartnerShiftsI = DerivativeMap.d[partnerShifts[i]];		// Still contains a lot of zeros
	    
	    for(int im = 0; im < field.length; im++){
		IntVector m = FMM.monomials[im];
		int mOrder = m.order;
                int max = FMM.boundForOrder[l-mOrder];
		int[] monoSumIm = FMM.monoSum[im];
		double min1powMOrder = (mOrder%2 == 0? 1.0: -1.0);
		
		for(int ip = 0; ip < max; ip++){
		    field[im] += min1powMOrder *
			//DerivativeMap.d[partnerShifts[i]][monoSumIm[ip]] *	// 2007-06-20
			dHashDPartnerShiftsI[monoSumIm[ip]] *
			partnerQ[ip];
		}
	    }
	}
    }
    
    /*public void reset(){
	for(int i=0; i < field.length; i++)
	    field[i] = 0;
    }*/
}
