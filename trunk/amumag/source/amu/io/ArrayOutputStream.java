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

import amu.debug.Bug;
import amu.geom.Vector;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes data in the amumag array data format. The arrays can have any dimension
 * and the size of the first dimension can be set to grow as data is added. Data
 * can be written in double or float precission and in binary or text format. A
 * header is automatically created.
 */
public final class ArrayOutputStream implements Flushable, Closeable{
 
    // Base output Stream
    private final MixedOutputStream out;
    
    // Dimension and size of the array.
    // Length = dimension
    // Values = sizes in each dimension, -1 means growable and can only be
    // used for the first dimension.
    private final int[] size;
    private final int[] currentIndex;
    
    // Indicates the data type: float, double, or text.
    private final boolean doublePrecission;
    public static final boolean FLOAT_PRECISSION = false, DOUBLE_PRECISSION = true;
    public static boolean defaultPrecission = DOUBLE_PRECISSION;
    
    private final boolean binaryFormat;
    public static final boolean TEXT_FORMAT = false, BINARY_FORMAT = true;
    public static boolean defaultFormat = TEXT_FORMAT;
    
    // stores if the data is a scalar or (3-)vector, automatically detected
    // at the first data writeTextData invocation.
    private boolean vectorData;
    private boolean startedData;
    
    // Comment character, used at each line of the header.
    public static final String COMMENT_CHAR = "# ";
    // Text data delimiter.
    public static final String TEXT_DELIMITER = "\t";
    // Delimiter between hashtable entries
    public static final String HASH_DELIMITER = ": ";
    
    // size for a 1-dimensional growable list.
    public static final int[] GROWABLE_LIST = new int[]{-1};
    // size for a constant
    public static final int[] CONSTANT = new int[]{1};
    
    // to detect out of bounds.
    private boolean full;
    
    //__________________________________________________________________________

    /** 
     * @param out The OutputStream to write to. 
     * @param size Contains the size of each dimension of the array. 
     *             The first dimension may have size -1, which indicates that
     *             this size is growable.
     * @param doublePrecission Indicates that data should be stored in double precission.
     */
    public ArrayOutputStream(OutputStream out, int[] size, boolean doublePrecission, boolean binaryFormat) throws IOException{
        
        //check validity of size: only dimension 0 may have undefinded (growing) size.
        for(int i=1; i<size.length; i++)
            if(size[i] < 0)
                throw new IllegalArgumentException("size[" + i + "] = " + size[i] + ", should be >= 0");
        
        this.size = size;
        this.currentIndex = new int[size.length];
        this.doublePrecission = doublePrecission;
        this.binaryFormat = binaryFormat;
        this.out = new MixedOutputStream(out);
        writeHeader();
    }
    
    /**
     * Uses the default value for doublePrecission and binaryFormat.
     */
    public ArrayOutputStream(OutputStream out, int[] size) throws IOException{
        this(out, size, defaultPrecission, defaultFormat);
    }

    public void close() throws IOException {
        out.close();
    }

   

    //__________________________________________________________________________
    
    private void writeDataImpl(double data) throws IOException {
        if (binaryFormat) {
            if (doublePrecission) {
                out.writeDouble(data);
            } else {
                out.writeFloat((float) data);
            }
        } else {
            if (doublePrecission) {
                out.print(data);
            } else {
                out.print((float) data);
            }
            out.print(TEXT_DELIMITER);
        }
    }
    
    public void writeProperty(String key, String value) throws IOException{
        if(startedData)
            throw new IOException("Attempted to write header after data.");
        out.println(COMMENT_CHAR + key + HASH_DELIMITER + value);
    }
    
    public void writeProperty(String key, int value) throws IOException {
        writeProperty(key, ""+value);
    }
    
    public void writeProperty(String key, double value) throws IOException {
        writeProperty(key, "" + value);
    }
     
    public void writeProperty(String key, boolean value) throws IOException{
        writeProperty(key, "" + value);
    }
    
    
    public final void flush() throws IOException{
        out.flush();
    }
    
    //__________________________________________________________________________
    
    /**
     * Write double data to the array.
     */
    public void writeDouble(double data) throws IOException{
        if(!startedData){ //first time invocation
            vectorData = false;
            startDataBlock();
        }
         if(full)
            throw new Bug("Array Full");
        
        writeDataImpl(data);
        indexCount();
    }
    
    /**
     * Write a Vector to the array.
     */
    public void writeVector(Vector data) throws IOException {
        if (!startedData) { //first time invocation
            vectorData = true;
            startDataBlock();
        }
        if(full)
            throw new Bug();

        writeDataImpl(data.x);
        writeDataImpl(data.y);
        writeDataImpl(data.z);
        //
        if(!binaryFormat)
            out.print(TEXT_DELIMITER);
        indexCount();
    }
    
    private final void indexCount() throws IOException{
        int dim = size.length-1;
        
        currentIndex[dim]++;
        while(currentIndex[dim] >= size[dim]){
            
            // newline after every line of data, and after every matrix.
            if(!binaryFormat && dim < (size.length < 3? 1:2) )
                out.println("");
            
            if(size[dim] != -1){
                currentIndex[dim] = 0;
                dim--;
                if(dim < 0){
                    full = true;
                    break;
                }
                else
                    currentIndex[dim]++;
            }
            else
                break; 
        }
       
    }
    
    //__________________________________________________________________________
    
    private void writeHeader() throws IOException{
        writeProperty("amumagVersion", 1);
        
        writeProperty("dimension", size.length);
        for(int i=0; i<size.length; i++)
            writeProperty("size[" + i + "]", size[i]);
        
        writeProperty("doublePrecission", doublePrecission);
        writeProperty("binaryFormat", binaryFormat);
    }
    
    private void startDataBlock() throws IOException {
        writeProperty("vectorData", vectorData);
        out.println(COMMENT_CHAR + "data");
        startedData = true;
    }
    
    //todo: count when to add newlines in text mode, throw out of bounds
    // close(). flush after every data block (step in first dimension).
}