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
import static java.lang.Math.*;
import static amu.core.Matrix.*;

public class Stripline extends SubstrateRoughness{

    private double striplinefunction(double d) {
        d=pow(abs(d), 0.25);
        return d;
    }

    public Stripline(double amplitude, double length){
        super(amplitude, length);
    }
    
    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("unused");
    }

    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        if(!initiated)
            init(mesh);
        
        int x = cellIndex.x + vertexIndex.x;
        int y = cellIndex.y + vertexIndex.y;
        int z = cellIndex.z + vertexIndex.z;
        
        int maxZ = mesh.baseLevel[0][0].length;
        if(z == 0 || z == maxZ)
            target.z = landscape[x][y];
    }

     public double[][] roughness(int xsize, int ysize, double amplitude, double length) {
        //length /= 2.0; // to make it more or less the physical correlation length.
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
        double max = max(matrix);
        double min = min(matrix);
        double peak2peak = max - min;
        matrix = amu.core.Matrix.scale(matrix, 2/peak2peak); //peak to peak is now 2     
        
        for(int i=0; i<matrix.length; i++)
            for(int j=0; j<matrix[i].length; j++)
                matrix[i][j] = striplinefunction(matrix[i][j]);  // landscape looks now like grains*/
        
        matrix = subtractConstant(matrix, average(matrix)); 
        max = max(matrix);
        min = min(matrix);
        peak2peak = max - min;
        matrix = amu.core.Matrix.scale(matrix, amplitude/peak2peak); 
     
        return matrix;
    }
}