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

/**
 * This module contains methods for the taylor expansion of the smooth field.
 */
public final class FMM {
    
    public static int order;							// order of the taylor expansion
    public static IntVector[] monomials;					// maps an integer index to an integer vector representing a carthesian monomial
    public static double[] monoOrderx2plus1;					// 2*order+1 of monomial i
    public static int[][] monoSum;						// index of "sum" monomial[i] + monomial[j] (IntVector sum = monomial product)
    public static int[][] monoDiff;						// index of difference monomial[i] - monomial[j] (IntVector subtraction = monomial division)
    public static int[] boundForOrder;						// index+1 of last monomial of order i
    public static double[][] derCoeff;						// field derivative coefficients, list of nonzero coeffs for each monomial index
    public static IntVector[][] derPower;					// corresponding (vector) exponents of \vec{r}
    
    /**
     * Sets the order of the taylor expansion of the smooth field and initializes everything.
     */
    public static void setOrder(int l){
      
	if(l < 1)
	    throw new IllegalArgumentException("order < 1");
	order = l;
        
	initMonomials();
	initMonoOrderx2plus1();
	initMonoSum();
	initMonoDiff();
	initBoundForOrder();
	initDerivativeCoefficients();
      
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static void initMonomials(){
        Message.debug("initMonomials()");
	final int l=order;
	final int nMonomials = (l+1)*(l+2)*(l+3)/6;				//the number of needed monomials
	monomials = new IntVector[nMonomials];
	monomials[0] = new IntVector(0, 0, 0);					//construct the 0th order monomial by hand.
	int size = 1;								//how far "n" is already filled.
	
		/* for each order, let a number of "carrets" iterate over (x,y,z). each carret at position x
		 * indicates one power of x. when a carret has visited z, the next carret (if any) is moved
		 * forward and the original carret is set to position of the next. in this way, each monomial
		 * of order l is constructed exactly once.
		 */
	
	for(int order = 1; order <= l; order++){
	    int[] carret = new int[order];					//list with positions of all the carrets
	    while(!isFinished(carret)){
		monomials[size] = toMonomial(carret);				//add the monomial to the list
		size++;
		increase(carret, 0);						//go to next position of the carrets
	    }
	}
	//debug
	Message.debug("order = " + l + ", monomials = " + nMonomials + ": ");
	for(int i = 0; i < monomials.length; i++)
	    Message.debugnoln(monomials[i] + " ");
	Message.println();
    }
    
    /**
     * Initialize derCoeff[indexOfMonomial][i] with the coefficients of the expansion of the derivative
     * of the smooth field, and derPower[indexOfMonomial][i] with the (vector) exponents of \vec{r} in
     * that expansion. The derCoeffs are not stored in an ordinary matrix, which would be very sparse.
     */
    private static void initDerivativeCoefficients(){
	double[][] f = new double[monomials.length][monomials.length];		// derCoeff[indexN][indexP], sparse
	f[0][0] = 1.0/(4.0*Math.PI);						// init f[0][p] to {1/4pi, 0, 0...}
	
	// unit vectors for recursively calculating f[n+x][p'] from f[n][p]
	IntVector x = new IntVector(1, 0, 0), y = new IntVector(0, 1, 0), z = new IntVector(0, 0, 1);
	
	// calculate f[k*x][p'] from f[0][0], this is not strictly necessary since these are all 0, but this is a test.
	for(int nx = 0; nx < order; nx++){
	    IntVector n = new IntVector(nx, 0, 0);
	    int in = indexOf(n);
	    IntVector nPlusX = n.sum(x);
	    int inPlusX = indexOf(nPlusX) ;
	    if(inPlusX != -1){							//otherwise this contributes to higer order terms that we don't use (?)
		for(int ip = 0; ip < monomials.length; ip++){
		    IntVector p = monomials[ip];
		    
		    IntVector p1 = p.sum(x);					//p+x
		    int ip1 = indexOf(p1);
		    if(ip1 != -1) f[inPlusX][ip1] += f[in][ip]*(2*n.order+1-p.x);
		    
		    IntVector p2 = p.subtract(x).sum(y.multiply(2));		//p-2y
		    int ip2 = indexOf(p2);
		    if(ip2 != -1) f[inPlusX][ip2] -= f[in][ip]*p.x;
		    
		    IntVector p3 = p.subtract(x).sum(z.multiply(2));		//p-2z
		    int ip3 = indexOf(p3);
		    if(ip3 != -1) f[inPlusX][ip3] -= f[in][ip]*p.x;
		}
	    }
	}
	
	// calculate f[k*x+l*y][p'] from f[k*x][p]
	for(int nx=0; nx<=order; nx++){
	    for(int ny=0; ny<order; ny++){
		IntVector n = new IntVector(nx, ny, 0);
		int in = indexOf(n);
		IntVector nPlusY = n.sum(y);
		int inPlusY = indexOf(nPlusY);
		if(inPlusY != -1){
		    for(int ip=0; ip<monomials.length; ip++){
			IntVector p = monomials[ip];
			
			IntVector p1 = p.sum(y);
			int ip1 = indexOf(p1);
			if(ip1 != -1) f[inPlusY][ip1] += f[in][ip]*(2*n.order+1-p.y);
			
			IntVector p2 = p.subtract(y).sum(z.multiply(2));
			int ip2 = indexOf(p2);
			if(ip2 != -1) f[inPlusY][ip2] -= f[in][ip]*p.y;
			
			IntVector p3 = p.subtract(y).sum(x.multiply(2));
			int ip3 = indexOf(p3);
			if(ip3 != -1) f[inPlusY][ip3] -= f[in][ip]*p.y;
		    }
		}
	    }
	}
	
	// calculate f[k*x+l*y+m*z][p'] from f[k*x+l*y][p]
	for(int nx=0; nx<=order; nx++){
	    for(int ny=0; ny<=order; ny++){
		for(int nz=0; nz<order; nz++){
		    IntVector n = new IntVector(nx, ny, nz);
		    int in = indexOf(n);
		    IntVector nPlusZ = n.sum(z);
		    int inPlusZ = indexOf(nPlusZ);
		    if(inPlusZ != -1){
			for(int ip=0; ip<monomials.length; ip++){
			    IntVector p = monomials[ip];
			    
			    IntVector p1 = p.sum(z);
			    int ip1 = indexOf(p1);
			    if(ip1 != -1) f[indexOf(nPlusZ)][ip1] += f[in][ip]*(2*n.order+1-p.z);
			    
			    IntVector p2 = p.subtract(z).sum(x.multiply(2));
			    int ip2 = indexOf(p2);
			    if(ip2 != -1) f[indexOf(nPlusZ)][ip2] -= f[in][ip]*p.z;
			    
			    IntVector p3 = p.subtract(z).sum(y.multiply(2));
			    int ip3 = indexOf(p3);
			    if(ip3 != -1) f[indexOf(nPlusZ)][ip3] -= f[in][ip]*p.z;
			}
		    }
		}
	    }
	}
	
	//convert sparse matrix to lists derCoeff, derPower
	derCoeff = new double[f.length][];
	derPower = new IntVector[f.length][];
	
	for(int in=0; in<f.length; in++){
	    //count nonzero derCoeffs
	    int nCoeff = 0;
	    for(int ip=0; ip<f[in].length; ip++)
		if(f[in][ip] != 0.0)
		    nCoeff++;
	    
	    derCoeff[in] = new double[nCoeff];
	    derPower[in] = new IntVector[nCoeff];
	    int index = 0;
	    for(int ip=0; ip<f[in].length; ip++)
		if(f[in][ip] != 0.0){
		derCoeff[in][index] = f[in][ip];
		derPower[in][index] = monomials[ip];
		index++;
		}
	}
	
	//debug: print nonzero coeffs
		/*{System.out.println("initDerivativeCoefficients");
		for(int i = 0; i < derCoeff.length; i++)
			for(int j = 0; j < derCoeff[i].length; j++)
				System.out.print("F_" + monomials[i] + "[" + derPower[i][j] + "]=" + derCoeff[i][j] + "  ");
		System.out.println();}*/
    }
    
    /**
     * The IntVectors in the monomials list with index < boundForOrder(l) are these with order<=l.
     */
    private static void initBoundForOrder(){
	boundForOrder = new int[order+1];
	int index = 0;
	for(int l = 0; l <= order; l++){
	    while(index < monomials.length && monomials[index].order <= l)
		index++;
	    boundForOrder[l] = index;
	}
	//debug
		/*System.out.println("BoundForOrder: ");
		for(int i = 0; i < boundForOrder.length; i++)
			System.out.print(boundForOrder[i] + " ");
		System.out.println();*/
    }
    
    /**
     * Init monoSum[][], which caches the sum of pairs of IntVectors representing monomials.
     * indexOf(a+b) == monoSum[indexOf(a)][indexOf(b)]. The sum of IntVectors represents the
     * product of the corresponding monomials. If the order of the sum is higer than the maximum
     * order, -1 is used to indicate this.
     */
    private static void initMonoSum(){
	int n = monomials.length;
	monoSum = new int[n][n];
	for(int i = 0; i < n; i++)
	    for(int j = 0; j < n; j++)
		monoSum[i][j] = indexOf(monomials[i].sum(monomials[j]));
	
	//debug
		/*System.out.println("MonoSum:");
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++)
				System.out.print(monoSum[i][j] + " ");
			System.out.println();
		}*/
    }
    
    
    private static void initMonoDiff(){
	int n = monomials.length;
	monoDiff = new int[n][n];
	for(int i = 0; i < n; i++)
	    for(int j = 0; j < n; j++)
		monoDiff[i][j] = indexOf(monomials[i].subtract(monomials[j]));
	
	//debug
		/*System.out.println("MonoDiff:");
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++)
				System.out.print(monoDiff[i][j] + " ");
			System.out.println();
		}*/
    }
    
    private static void initMonoOrderx2plus1(){
	monoOrderx2plus1 = new double[monomials.length];
	for(int i = 0; i < monoOrderx2plus1.length; i++)
	    monoOrderx2plus1[i] = 2.0*monomials[i].order+1.0;
    }
    
    private static void increase(int[] carret, int index){
	carret[index]++;
	if(carret[index] == 3 && index < carret.length-1){			// if a carret has reached the end
	    increase(carret, index+1);						// move the next one forward (if any) and
	    carret[index] = carret[index+1];					// put this carret to the position of the next one
	}
    }
    
    private static boolean isFinished(int[] carret){
	return carret[carret.length-1] == 3;
    }
    
    // creates a monomial from the carrets
    private static IntVector toMonomial(int[] carret){
	int[] vector = new int[3];
	for(int i = 0; i < carret.length; i++)
	    vector[carret[i]]++;
	return new IntVector(vector[0], vector[1], vector[2]);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * D_\vec{p}(r) is the \vec{p}-derivative of the coulomb potential 1/(4pi|r|).
     */
    public static double d(int indexN, Vector r){
	double sum = 0.0;
	for(int j=0; j<derCoeff[indexN].length; j++)
	    sum += derCoeff[indexN][j] * r.pow(derPower[indexN][j]);
	return sum/ Math.pow(r.norm(), monoOrderx2plus1[indexN]);
    }
    
    /**
     * Returns D_\vec{p}(r) in the form of a function of r, for debugging.
     */
    public static String dString(int indexN){
	StringBuffer b = new StringBuffer();
	b.append("1/(r^" + monoOrderx2plus1[indexN] + ") * [");
	for(int j=0; j<derCoeff[indexN].length; j++){
	    b.append(derCoeff[indexN][j] + "*" + derPower[indexN][j] + "+");
	}
	b.append("]");
	return b.toString();
    }
    
    /**
     * Index in the monomials list of the IntVector. Returns -1 if the vector is not in the list.
     */
    public static int indexOf(IntVector v){
	int index = 0;
	while(index < monomials.length && !monomials[index].equals(v))
	    index++;
	if(index==monomials.length)
	    return -1;
	else
	    return index;
    }
}
