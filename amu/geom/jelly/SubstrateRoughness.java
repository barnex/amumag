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

package amu.geom.jelly;

import amu.core.Index;
import amu.core.Interpolator;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Unit;
import static amu.core.Matrix.*;

public class SubstrateRoughness extends RandomHomeomorphism{

    protected double[][] landscape;
    protected Interpolator xInt, yInt, zInt;
    protected Vector boxSize, cellSize;
    protected int cellsX, cellsY, cellsZ;
    protected static final int SUB = 1;
    protected double length, amplitude;
    protected boolean initiated = false;
    
    public SubstrateRoughness(double amplitude, double length){
        this.length = length;
        this.amplitude = amplitude;
    }
    
    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        if(!initiated)
            init(mesh);
        
        int x = (int) (xInt.transf(r.x)+0.5);
        int y = (int) (yInt.transf(r.y)+0.5);
        
        if(x >= landscape.length)
            x = landscape.length - 1;
        if(x < 0)
            x = 0;
        if(y >= landscape[0].length)
            y = landscape[0].length - 1;
        if(y < 0)
            y = 0;
        
       target.z = landscape[x][y];
    }

    protected void init(Mesh mesh) {
        cellsX = mesh.baseLevel.length;
        cellsY = mesh.baseLevel[0].length;
        cellsZ = mesh.baseLevel[0][0].length;
        
        boxSize = new Vector(mesh.boxSize);
        boxSize.multiply(Unit.LENGTH);
        cellSize = new Vector(boxSize);
        cellSize.divide(new Vector(cellsX, cellsY, 1));
        
        xInt = new Interpolator(-0.5*boxSize.x, 0, 0.5*boxSize.x, cellsX*SUB);
        yInt = new Interpolator(-0.5*boxSize.y, 0, 0.5*boxSize.y, cellsY*SUB);
        zInt = new Interpolator(-0.5*boxSize.z, 0, 0.5*boxSize.z, 1);
        
        landscape = roughness(SUB*cellsX+1, SUB*cellsY+1, amplitude, SUB*length/cellSize.x); //todo: change to x,y
        
        initiated = true;
    }
    
     public double[][] roughness(int xsize, int ysize, double amplitude, double length) {
        length /= 2.0; // to make it more or less the physical correlation length.
        double[][] gauss = gauss(length);
        double[][] matrix = new double[xsize+gauss.length][ysize+gauss.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] matrixI = matrix[i];
            for (int j = 0; j < matrixI.length; j++) {
                matrixI[j] = random();
            }
        }
        matrix = convolve(matrix, gauss);
        matrix = crop(matrix, gauss.length, gauss.length, xsize, ysize);
        matrix = subtractConstant(matrix, average(matrix));
        double min = min(matrix);
        double max = max(matrix);
        double peak2peak = max - min;
        amu.core.Matrix.scale(matrix, amplitude / peak2peak);
        return matrix;
    }

    public static double[][] gauss(double stddev) {
        double var = stddev * stddev;
        double sum = 0.0;
        int w = (int) (4 * stddev) + 1;
        double[][] gaussian = new double[2 * w + 1][2 * w + 1];
        for (int i = 0; i < gaussian.length; i++) {
            double x = -w + i;
            double x2 = x * x;
            for (int j = 0; j < gaussian[i].length; j++) {
                double y = -w + j;
                gaussian[i][j] = Math.exp(-(x2 + y * y) / (2 * var));
                sum += gaussian[i][j];
            }
        }
        return gaussian;
    }
 
}