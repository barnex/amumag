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

import static java.lang.Math.*;

public class Matrix {

    public static double[][] scale(double[][] matrix, double r) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] *= r;
            }
        }
        return matrix;
    }

    public static double[][] convolve(double[][] a, double[][] b) {
        int aWidth = a.length, aHeight = a[0].length;
        int bWidth = b.length, bHeight = b[0].length;
        //if(bWidth % 2 != 1 || bHeight != 1)
        //	throw new IllegalArgumentException("Dimension of convolution matrix is not odd (" + bWidth + "x" + bHeight + ")");
        //else{
        double[][] convolution = new double[aWidth + bWidth - 1][aHeight + bHeight - 1];
        for (int ia = 0; ia < a.length; ia++) {
            for (int ja = 0; ja < a[ia].length; ja++) {
                double weight = a[ia][ja];
                for (int ib = 0; ib < b.length; ib++) {
                    for (int jb = 0; jb < b[ib].length; jb++) {
                        convolution[ia + ib][ja + jb] += weight * b[ib][jb];
                    }
                }
            }
        }
        return convolution;
    //}
    }

    public static double[][] crop(double[][] data, int x, int y, int w, int h) {
        double[][] cropped = new double[w][h];
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                cropped[i - x][j - y] = data[i][j];
            }
        }
        return cropped;
    }

    public static double[][] centerCrop(double[][] data, int w, int h) {
        int centerx = data.length / 2;
        int centery = data[0].length / 2;
        return crop(data, centerx - w / 2, centery - h / 2, w, h);
    }

    public static double rms(double[][] image) {
        double rms = 0;
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                rms += image[i][j] * image[i][j];
            }
        }
        return sqrt(rms);
    }

    public static double[][] centerCrop(double[][] data, int size) {
        return centerCrop(data, size, size);
    }

    public static double average(double[][] matrix) {
        double sum = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            double[] matrixI = matrix[i];
            for (int j = 0; j < matrixI.length; j++) {
                sum += matrixI[j];
            }
        }
        return sum / (matrix.length * matrix[0].length);
    }

    public static double[][] subtractConstant(double[][] a, double b) {
        double[][] diff = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            diff[i] = new double[a[i].length];
            for (int j = 0; j < diff[i].length; j++) {
                diff[i][j] = a[i][j] - b;
            }
        }
        return diff;
    }

    public static int[] maxIndex(double[][] array) {
        double max = Double.NEGATIVE_INFINITY;
        int[] maxIndex = new int[2];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                double value = array[i][j];
                if (value > max) {
                    max = value;
                    maxIndex[0] = i;
                    maxIndex[1] = j;
                }
            }
        }
        return maxIndex;
    }

    public static int[] minIndex(double[][] array) {
        double max = Double.POSITIVE_INFINITY;
        int[] maxIndex = new int[2];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                double value = array[i][j];
                if (value < max) {
                    max = value;
                    maxIndex[0] = i;
                    maxIndex[1] = j;
                }
            }
        }
        return maxIndex;
    }
    
    public static double max(double[][] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                double value = array[i][j];
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    public static double min(double[][] array) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                double value = array[i][j];
                if (value < min) {
                    min = value;
                }
            }
        }
        return min;
    }
}
