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

package amu.mag.exchange;

import amu.io.Message;

/**
 * Solves systems of equations.
 * @author arne
 */
public final class Systems{
    
    /**
     * Solves the system of equations represented by the matrix.
     * @param matrix
     */
    public static void solve(double[][] matrix){
    
        double[][] debugcopy = new double[matrix.length][matrix[0].length];
        for(int i=0; i<debugcopy.length; i++)
            for(int j=0; j<debugcopy[0].length; j++)
                debugcopy[i][j] = matrix[i][j];
        
        final int rows = matrix.length;
        final int cols = matrix[0].length;
        
        for(int r=0; r<rows; r++){
            double pivot = matrix[r][r];
            if(pivot == 0.0)
                Message.warningOnce("0 pivot in system.");
            multiply(matrix[r], 1.0/pivot);
            for(int s=0; s<rows; s++){
                if(r != s)
                    add(matrix[s], -matrix[s][r], matrix[r]);
            }
        }
    }
    
    /**
     * Multiplies the row by r.
     */
    private static void multiply(double[] row, double r){
        for(int i=0; i<row.length; i++)
            row[i] *= r;
    }
    
    /**
     * Adds multiplier*value to target.
     */ 
    private static void add(double[] target, double multiplier, double[] value){
        for(int i=0; i<target.length; i++)
            target[i] += multiplier * value[i];
    }
    
    //debug
    public static void main(String[] args){
        double[][] test = new double[][]{{3, 2, 37, 1, 0},{-2, 1.5, -2, 0, 1}};
        solve(test);
        System.out.println(test);
    }
}