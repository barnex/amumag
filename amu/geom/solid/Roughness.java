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

package amu.geom.solid;

import amu.core.Interpolator;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Main;
import amu.mag.Unit;
import static amu.core.Matrix.*;

public class Roughness extends Shape {

    private double[][] landscape;
    private Interpolator xInt, yInt;
    private Vector boxSize, cellSize;
    private int cellsX, cellsY;
    private static final int SUB = 3;
    private final double length;
    private final double amplitude;
    private boolean initiated = false;
    
    public Roughness(double length, double amplitude) {
        this.length = length;
        this.amplitude = amplitude;
    }

    @Override
    protected boolean inside(Vector r) {
        if(!initiated)
            init();
        int x = (int) xInt.transf(r.x);
        int y = (int) yInt.transf(r.y);
        
        if(x >= landscape.length)
            x = landscape.length - 1;
        if(x < 0)
            x = 0;
        if(y >= landscape[0].length)
            y = landscape[0].length - 1;
        if(y < 0)
            y = 0;
        
        return r.z <= landscape[x][y];
       
    }
    
    private void init(){
        Mesh mesh = Main.sim.mesh;
        cellsX = mesh.baseLevel.length;
        cellsY = mesh.baseLevel[0].length;
        
        boxSize = new Vector(mesh.boxSize);
        boxSize.multiply(Unit.LENGTH);
        cellSize = new Vector(boxSize);
        cellSize.divide(new Vector(cellsX, cellsY, 1));
        
        xInt = new Interpolator(-0.5*boxSize.x, 0, 0.5*boxSize.x, cellsX*SUB);
        yInt = new Interpolator(-0.5*boxSize.y, 0, 0.5*boxSize.y, cellsY*SUB);
        
        landscape = roughness(SUB*cellsX+1, SUB*cellsY+1, amplitude, SUB*length/cellSize.x); //todo: change to x,y
        initiated = true;
    }

    public static double[][] roughness(int xsize, int ysize, double amplitude, double length) {
        length /= 2.0; // to make it more or less the physical correlation length.
        double[][] gauss = gauss(length);
        double[][] matrix = new double[xsize+gauss.length][ysize+gauss.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] matrixI = matrix[i];
            for (int j = 0; j < matrixI.length; j++) {
                matrixI[j] = Math.random();
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
        int w = (int) (3 * stddev) + 1;
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

    // int getRandom(){
    //     return 4; // chosen by fair dice roll,
    //               // guaranteed to be random.
    // }
    // xkcd comics.
 
}
