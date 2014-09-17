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

import amu.core.Equality;
import amu.debug.Bug;
import amu.mag.*;
import java.io.Serializable;


public final class Multipole implements Serializable{
    
    //TODO: hash
    public int[][] qIndex;                                                     // for each monomial index i_n, the monomial indices i_p for which (1) n-p is a valid monomial and (2) shiftFactor[i_p] != 0.0
    public double[] q;
    
    
    public Multipole() {

    }

    /**
     * Adds weight*multipole to this multipole.
     */
    public void add(double weight, Multipole multipole) {
        for(int i=0; i<q.length; i++)
	    q[i] += weight * multipole.q[i];
    }
    
    /**
     * Initiates this q and the childrens' qIndex.
     * For each monomial index i_n, this contains the monomial indices i_p for which
     * (1) n-p is a valid monomial and
     * (2) child.shiftFactor[i_p] != 0.0
     *
     * TODO: internalize.
     *
     */
    public void init(Cell cell) {
	
	q = new double[FMM.monomials.length];
	
	for(Cell child: cell.getChildren()){
	    int[][] qI = new int[FMM.monomials.length][];		//should be here, one for each child (?) maybe they are equale, QHash could take care of this
	    for(int in = 0; in < FMM.monomials.length; in++){
		IntVector n = FMM.monomials[in];
		int count = 0;
		for(int ip = 0; ip < FMM.monomials.length; ip++){
		    IntVector p = FMM.monomials[ip];
		    if(FMM.monoDiff[in][ip] != -1 && child.smooth.shiftFactor[ip] != 0){
			count++;
		    }
		}
		qI[in] = new int[count];
		int i = 0;
		for(int ip = 0; ip < FMM.monomials.length; ip++){
		    IntVector p = FMM.monomials[ip];
		    if(FMM.monoDiff[in][ip] != -1 && child.smooth.shiftFactor[ip] != 0){
			qI[in][i] = ip;
			i++;
		    }
		}
	    }
	    child.multipole.qIndex = qI; 
	}
    }
    
    public void reset(){
	for(int i = 0; i < q.length; i++)                                       //set q to zero -> system.arraycopy?
	    q[i] = 0.0;
    }
    
    @Override
    public boolean equals(Object o){
	assert o instanceof Multipole;
	Multipole other = (Multipole) o;
	return Equality.equals(q, other.q);
    }

    @Override
    public int hashCode() {
        return Equality.hashCode(q);
    }
    
}
