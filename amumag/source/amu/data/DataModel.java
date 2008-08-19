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


package amu.data;

import amu.geom.Mesh;
import amu.geom.Vector;
import amu.core.Index;
import amu.io.ArrayOutputStream;
import amu.mag.Unit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static amu.io.ArrayOutputStream.*;

/**
 * Superclass for any function of time and/or space. The function can have double
 * or Vector values. The function is defined on a number of discrete points and
 * values are accessed through these integer arguments. The corresponding spatial
 * or temporal true coordinates can be found with getMesh() or getTime().
 * @author arne
 */
public abstract class DataModel {
    
    //__________________________________________________________________get data
    
    /**
     * Gets data from the model by putting the data in the Vector buffer.
     * @param r Spatial coordinate
     * @param time Temporal coordinate
     * @param v Vector buffer to store data in.
     * @throws java.io.IOException
     */
    public abstract void put(int time, Index r, Vector v) throws IOException;
    
    /**
     * Gets data from the model and returns it as a new Vector. Double data is
     * stored in the x-coordinate of the vector.
     * @param r Spatial coordinate
     * @param time Temporal coordinate
     * @return A Vector representing a vector or a double.
     * @throws java.io.IOException
     */
    public Vector get( int time,Index r) throws IOException {
        Vector v = new Vector();
        put(time, r, v);
        return v;
    }
    
    /**
     * Gets Vector data from the model, throws an exception when the model
     * contains double data.
     * @param r Spatial coordinate
     * @param time Temporal coordinate
     * @return Vector data from the model
     * @throws java.io.IOException
     * @throws java.lang.IllegalArgumentException when the model contains double data.
     * 
     */
    public Vector getVector(int time, Index r) throws IOException{
        if(!isVector())
            throw new IllegalArgumentException("double data");
        else{
            Vector v = new Vector();
            put(time, r, v);
            return v;
        }
    }
    
    
    private final Vector buffer = new Vector();
    /**
     * Gets double data from the model, throws an exception when the model
     * contains vector data.
     * @param r Spatial coordinate
     * @param time Temporal coordinate
     * @return data data from the model
     * @throws java.io.IOException
     * @throws java.lang.IllegalArgumentException when the model contains vector data.
     * 
     */
    public double getDouble(int time, Index r) throws IOException{
        if(isVector())
            throw new IllegalArgumentException("Vector data");
        else{
            put(time, r, buffer);
            return buffer.x;
        }
    }

   /**
     * Gets data from the model, represented as a String.
     * @param r Spatial coordinate
     * @param time Temporal coordinate
     * @return data data from the model, represented as a String.
     * @throws java.io.IOException
     */
    public String getString( int time,Index r) throws IOException{
        if(isVector()){
            Vector v = getVector(time, r);
            return v.x + " " + v.y + " " + v.z;
        }
        else
            return getDouble(time, r) + "";
    }
    
    //_______________________________________________________________domain info
    
    /**
     * 
     * @return whether the function is space-dependend.
     */
    public abstract boolean isSpaceDependend();
    
    /**
     * 
     * @return The domain of the function in the spatial coordinates, expressed
     * as a range of integer arguments.
     */
    public Index getSpaceDomain(){
        if(isSpaceDependend())
            return getMesh().getBaseSize();
        else
            return null;
    }
     
    /**
     * @return The mesh on which the function is defined, if the function is 
     * space-dependend.
     */
    public abstract Mesh getMesh();
 
    /**
     * @return whether the function is time-dependend.
     */
    public abstract boolean isTimeDependend();
    
    
     public abstract int getTimeDomain();
    /**
     * @return The points in time on which the function is defined, if the function is 
     * time-dependend. SI UNITS!
     */
    public abstract double[] getTime();
    
    /**
     * When doing an incremental save (saving a time-independend model, 
     * corresponding to a point in time of a running simulation), this
     * returns the time of the data snapshot (sim units).
     * This method should be overridden, the default implementation throws
     * an IllegalArgumentException.
     * 
     * @return The time of the data snapshot (sim units).
     * @throw java.lang.IllegalArgumentException when the data model has not
     * overridden this method to support incremental saves.
     */
    public double getTimeForIncrementalSave(){
        throw new IllegalArgumentException("Data model does not support incremental save.");
    }

    //________________________________________________________________value info
    
    /**
     * Returns if the function contains vector or double data.
     */
    public abstract boolean isVector();

    /**
     * Used to create a fileName to save the data.
     * @return
     */
    public abstract String getName();
    
    public abstract String getUnit();
    
    //________________________________________________________________________io
    
    /**
     * Saves a time-independend model, knowing that it is a slice of an underlying
     * time-dependend model. Especially usefull for saving LiveDataModels and
     * derivatives.
     * 
     * If the model is space-dependend, this method save the data in a directory
     * with the name of the data model. The corresponding time will be appended
     * to the array 'time' in that directory.
     * 
     * If the model is not space-dependend, this method will append the data
     * to a list with the name of the data model.
     */
    public void incrementalSave(File baseDir) throws IOException{
        if(isTimeDependend())
            throw new IllegalArgumentException("incrementalSave can only be used for time-independend data.");
        
        if(isSpaceDependend()){
            incrementalSaveDir(baseDir);
        }
        else{
            incrementalSaveFile(baseDir);
        }
    }
    
    /**
     * Used to write the table of times corresponding to the saved files time0,
     * time1, ...
     */
    private ArrayOutputStream timeOut;
    private int incrementalSaveCount;
    
    public void incrementalSaveDir(File baseDir) throws IOException{
        File dir = new File(baseDir, getName());      
        incrementalSaveDir(baseDir, dir);
    }
       
    public void incrementalSaveDir(File baseDir, File dir) throws IOException{
        if(!dir.exists()){      // may exist from previously run sim.
            dir.mkdirs();
            //timeOut should be null
        }
        if(timeOut == null){    // first time invocation
            timeOut = new ArrayOutputStream(new FileOutputStream(new File(dir, "time")), 
                    GROWABLE_LIST, DOUBLE_PRECISSION, TEXT_FORMAT);
            timeOut.writeProperty("name", "time");
            timeOut.writeProperty("unit", "s");
        }

        // first write the data
        save(dir, "time" + incrementalSaveCount);
        // then write the time: when the write of the data gets interrupted, 
        // the bad data file will most likely not appear in the time list and
        // wont be read.
        timeOut.writeDouble(getTimeForIncrementalSave());
        timeOut.flush();
        
        incrementalSaveCount++;
    }
    
    private ArrayOutputStream tableOut;
    private Vector buffer3;
    
    /**
     * Temporary implementation which writes a table, including times.
     * @param baseDir
     * @throws java.io.IOException
     */
    private void incrementalSaveFile(File baseDir) throws IOException {
        if(tableOut == null){ //first time invocation
            // init array output stream
            File file = new File(baseDir, getName());
            int dim = isVector()? 3: 1;
            tableOut = new ArrayOutputStream(new FileOutputStream(file), 
                    new int[]{-1, dim+1}, FLOAT_PRECISSION, TEXT_FORMAT);
            buffer3 = new Vector();
            
            tableOut.writeProperty("name", getName());
            tableOut.writeProperty("unit", getUnit());
            tableOut.writeProperty("firstColumn", "time(s)");
        }
        
        tableOut.writeDouble(getTimeForIncrementalSave());
        put(-1, null, buffer3);
        if(isVector()){
            tableOut.writeDouble(buffer3.x);
            tableOut.writeDouble(buffer3.y);
            tableOut.writeDouble(buffer3.z);
        }
        else{
            tableOut.writeDouble(buffer3.x);
        }
        tableOut.flush();
    }
    
    
    public void save(File dir, String name) throws IOException{
        // 2008-06-17: BufferedOutputStream doubles performance.
        ArrayOutputStream out = new ArrayOutputStream( new BufferedOutputStream (
                new FileOutputStream(new File(dir, name))), getSize(), FLOAT_PRECISSION, TEXT_FORMAT);
        
        out.writeProperty("name", getName());
        out.writeProperty("unit", getUnit());

        double time = -1.0;
        try{
            time = getTimeForIncrementalSave();
            out.writeProperty("time", time);
        }
        catch(IllegalArgumentException e){
            // do not write time key to header
        }
        
        write(out);        
        out.flush();
        out.close();
    }

    private void write(ArrayOutputStream out) throws IOException{
        
         if(isTimeDependend()){
            int timeDomain = getTimeDomain();
            for(int t=0; t<timeDomain; t++)
                write(t, out);
        }
        else
            write(-1, out);
    }
    
    private void write(int time, ArrayOutputStream out) throws IOException{
     
        if(isSpaceDependend()){
            Index i = new Index();
            Index s = getSpaceDomain();
            for(int x=0; x<s.x; x++)
                for(int y=0; y<s.y; y++)
                    for(int z=0; z<s.z; z++){
                        i.set(x, y, z);
                        write(time, i, out);
                    }
        }
        else
            write(time, null);    
    }
        
    private final Vector buffer2 = new Vector();
    private void write(int time, Index r, ArrayOutputStream out) throws IOException{
        
        put(time, r, buffer2);
        
        if(isVector())
            out.writeVector(buffer2);
        else
            out.writeDouble(buffer2.x);
    }
    
    
    /**
     * 
     * @return Size of the array representation of the data.
     */
    private int[] getSize(){
        
        int length = 0;
        if(isTimeDependend())
            length += 1;
        if(isSpaceDependend())
            length += 3;
        
        int[] size = new int[length]; 
        int i=0;
        if(isTimeDependend()){
            size[i] = getTimeDomain();
            i++;
        }
        if(isSpaceDependend()){
            Index space = getSpaceDomain();
            size[i] = space.x;
            size[i+1] = space.y;
            size[i+2] = space.z;
        }
        
        return size;
    }
    
    //__________________________________________________________________________
    
    @Override
    public String toString(){
        StringBuffer b = new StringBuffer();
        
        if (isTimeDependend() && !isSpaceDependend()) {
            double[] time = getTime();
            for (int t = 0; t < time.length; t++) {
                try {
                    b.append(time[t] + "\t" + getString( t,null) + "\n");
                } catch (IOException ex) {
                    // should be nicer.
                    throw new IllegalArgumentException(ex);
                }
            }
        }
        
        return b.toString();
    }
}
