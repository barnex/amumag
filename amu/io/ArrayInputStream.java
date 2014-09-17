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

package amu.io;

import amu.core.DoubleArray;
import amu.geom.Vector;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * This class auto-detects the format (text/binary) of an amumag array data file
 * and creates the suited text/binary inputStream to read the file.
 * 
 * @author arne
 */
public final class ArrayInputStream{
    
    private final MixedInputStream in;
    
    private final Hashtable<String, String> header;
    
    private int[] size;  
    private boolean doublePrecission;
    private boolean vectorData;
    private boolean binaryFormat;
    
    public ArrayInputStream(InputStream in) throws IOException{
        this.in = new MixedInputStream(in);         
        this.header = new Hashtable<String, String>();
        readHeader();
    }

    //__________________________________________________________________________
    
    /**
     * Reads the next double.
     */
    public final double readDoubleImpl() throws IOException{
        if(binaryFormat){
            if(doublePrecission)
                return in.readDouble();
            else
                return (double)(in.readFloat());
        }
        else{
            return Double.parseDouble(in.readWord());
        }
    }
    
       
    private final void readHeader() throws IOException{
        String key = in.readWord();
        while(!key.equals("data")){
            String value = in.readWord();
            header.put(key, value);
            key = in.readWord();
        }
        
        // get size from header
        size = new int[getIntProperty("dimension")];
        for(int i=0; i<size.length; i++)
            size[i] = getIntProperty("size[" + i + "]");
        
        // get data format from header
        doublePrecission = getBooleanProperty("doublePrecission");
        vectorData = getBooleanProperty("vectorData");
        binaryFormat = getBooleanProperty("binaryFormat");
    }
      
    public final String getProperty(String key){
        String value = header.get(key);
        if(value == null)
            throw new IllegalArgumentException("Key not in header: " + key);
        else
            return value;
    }
    
    public final int getIntProperty(String key){
        return Integer.parseInt(getProperty(key));
    }
    
    public final boolean getBooleanProperty(String key){
        return Boolean.parseBoolean(getProperty(key));
    }
    
    public final void close() throws IOException {
        in.close();
    }
    
    public final double readDouble() throws IOException{
        if(vectorData)
            throw new IllegalArgumentException("vector Data");
        else
            return readDoubleImpl();
    }
    
    public final Vector readVector() throws IOException{
        if(!vectorData)
            throw new IllegalArgumentException("double Data");
        else
            return new Vector(readDoubleImpl(), readDoubleImpl(), readDoubleImpl());
    }
    
    /**
     * Reads a 1-dimensional list of doubles.
     */
    public final double[] readList() throws IOException{
        if(getDimension() != 1)
            throw new IOException("File does not contain a list: dimension = " + getDimension());
        
        if (size[0] == -1) {                  // growable list with unknown size
            DoubleArray buffer = new DoubleArray();
            try {
                for (;;) {
                    buffer.add(readDoubleImpl());
                }
            } catch (EOFException e) {
                // used to read till the end of the stream, there is no other
                // way to do this with the java API?
            }
            return buffer.toArray();
        }
        else{                                 // fixed-sized list
            double[] list = new double[size[0]];
            for(int i=0; i<list.length; i++)
                list[i] = readDoubleImpl();
            return list;
        }
    }

    public final double[][] readMatrix() throws IOException{
        if(getDimension() != 2)
            throw new IOException("File does not contain a matrix: dimension = " + getDimension());
            double[][] matrix = new double[size[0]][size[1]];
            for(int j=0; j<matrix[0].length; j++)
                for(int i=0; i<matrix.length; i++)   
                    matrix[i][j] = readDoubleImpl();
            return matrix;
    }

    public final double[][][] readBlock() throws IOException{
        if(getDimension() != 2)
            throw new IOException("File does not contain a block: dimension = " + getDimension());
            double[][][] matrix = new double[size[0]][size[1]][size[2]];
            for(int k=0; k<matrix[0][0].length; k++)
                for(int j=0; j<matrix[0].length; j++)
                    for(int i=0; i<matrix.length; i++)        
                        matrix[i][j][k] = readDoubleImpl();
            return matrix;
    }

       public final Vector[][][] readVectorBlock() throws IOException{
        if(getDimension() != 3)
            throw new IOException("File does not contain a block: dimension = " + getDimension());
            Vector[][][] matrix = new Vector[size[0]][size[1]][size[2]];
            for(int k=0; k<matrix[0][0].length; k++)
                for(int j=0; j<matrix[0].length; j++)
                    for(int i=0; i<matrix.length; i++)
                        matrix[i][j][k] = readVector();
            return matrix;
    }
       
    public final Object read() throws IOException{
        if(getDimension() == 1 && !vectorData)
            return readList();
        else if(getDimension() == 2 && !vectorData)
            return readMatrix();
        else if(getDimension() == 3 && !vectorData)
            return readBlock();
        else
            throw new IllegalArgumentException();
    }
    
    private final int getDimension() {
        return size.length;
    }
}