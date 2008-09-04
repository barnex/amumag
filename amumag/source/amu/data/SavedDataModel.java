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

import amu.geom.LinkedListModule;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.core.Index;
import amu.io.ArrayInputStream;
import amu.io.Message;
import amu.io.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import static amu.geom.Vector.*;

/**
 * Data model for an incrementally saved time and space Dependent field.
 */
public final class SavedDataModel extends DataModel{
    
    private Mesh mesh;
    private File dir;           // null: time-inDependent
    private boolean hasData;    
    
     // -1: not yet read, 0: false, 1: true;
    private int vectorData = -1;
    
    private double[] time;
    protected int current_t = -1; // currently buffered time
    
    /**
     * File can be mesh, a single data file, or a data dir.
     * @param file
     * @throws java.io.IOException
     */
    public SavedDataModel(File file) throws IOException{
        
        Message.debug("open " + file);
       if(file.getName().startsWith("mesh")){
            openMesh(file);
        }
        else if(file.isDirectory()){
            openDir(file);
        }
        else 
            openFile(file);
    }

    public boolean hasData() {
        return hasData;
    }

    public void openMesh(File file) throws IOException {
        Message.debug("openMesh : " + file);
        mesh = readMesh(file);
        time = null;
        current_t = -1;
        dir = null;
        hasData = false;
    }
    
    public void openDir(File file) throws IOException {
        Message.debug("openDir : " + file);
        if(!file.isDirectory())
            throw new IllegalArgumentException(file + " is not a directory");
        mesh = readMesh(locateMeshFile(file));
        time = readTime(new File(file, "time"));
        this.dir = file;
        current_t = -1;
        hasData = true;
    }
     
    public void openFile(File file) throws IOException {
        hasData = true; // has to be here
        Message.debug("openFile : " + file);
        if(file.isDirectory())
            throw new IllegalArgumentException(file + " is a directory");
        mesh = readMesh(locateMeshFile(file.getParentFile()));
        readData(file);
        this.dir = null;
        this.time = null;
        
        current_t = -1;
    }
   
    /**
     * Looks for mesh.gz in the directory and its parent.
     * @param dataDir
     * @return
     * @throws java.io.FileNotFoundException
     */
    private File locateMeshFile(File dataDir) throws FileNotFoundException{
        if(new File(dataDir, "mesh.gz").exists())
            return new File(dataDir, "mesh.gz");
        else if(new File(dataDir.getParentFile(), "mesh.gz").exists())
            return new File(dataDir.getParentFile(), "mesh.gz");
        else
            throw new FileNotFoundException("mesh.gz");
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        //Message.debug("put: " + time + ", " + r);
        if(hasData){
            if(this.time != null){
                if(time == -1)
                    throw new IllegalArgumentException();
                if (time != current_t)
                    readData(time);
                v.set(mesh.getCell(mesh.nLevels - 1, r).dataBuffer);
            }
            else{
                if(time != -1)
                    throw new IllegalArgumentException();
                 v.set(mesh.getCell(mesh.nLevels - 1, r).dataBuffer);
            }
        }
        else{
            if(time != -1)
                throw new IllegalArgumentException("model is time independent");
            v.set(0, 0, 0);
        }
    }
    
    public static Mesh readMesh(File file) throws IOException{
        Mesh mesh;
        try {
            Message.debug("read mesh: " + file.getAbsolutePath());
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
            mesh = (Mesh) in.readObject();
            in.close();
        } catch (Exception ex) {
            
            throw new IOException("Error reading file: " + file.getAbsolutePath(), ex);
            // patch for gcj:
            //ex.printStackTrace();
            //System.exit(-1);
        }
        // init data buffer
        Cell[][][][] levels = mesh.levels;
        for(int l=0; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null)
                            cell.dataBuffer = new Vector();
                    }
        new LinkedListModule(mesh).linkCells();
        return mesh;
    }
    
    public static double[] readTime(File file) throws IOException{
        FileInputStream fin = new FileInputStream(file);
        ArrayInputStream in = new ArrayInputStream(fin);
        double[] time = in.readList();
        in.close();
        return time;
    }
    
    private void readData(int time) throws IOException{
        readData(new File(dir, "time" + time));
        current_t = time;
    }
    
     
    public void readData(File file) throws IOException{
        Message.debug("reading: " + file);
        
        // open data input stream
        ArrayInputStream in = new ArrayInputStream(new FileInputStream(file));
        
        // todo: handle levels
        
        vectorData = in.getBooleanProperty("vectorData")? 1:0;
        
        int maxComponent = isVector()? Z: X;
        
        Cell[][][][] levels = getMesh().levels;
        // clear data
        for(int l=0; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null)
                            cell.dataBuffer.reset();
                    }
       
        // read data, first Z, then Y, then X.
        int l = mesh.nLevels - 1;
        for (int k = 0; k < levels[l][0][0].length; k++) {
            for (int j = 0; j < levels[l][0].length; j++) {
                for (int i = 0; i < levels[l].length; i++) {
                    Cell cell = levels[l][i][j][k];
                    for (int c = 0; c <= maxComponent; c++) {
                        // note: when a cell is null, the stored data
                        // will be zero, but we have to read it anyway!
                        double value = in.readDoubleImpl(); // hmm, we kind of cheat here by not using readVector.
                        if (cell != null) {
                            cell.dataBuffer.setComponent(c, value);
                        }
                    // else: discard the zero data.
                    }
                }
            }
        }
        in.close();
    }
    
    @Override
    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public double[] getTime() {
        return time;
    }

    @Override
    public boolean isVector() {
        if(!hasData)
            return false;
        
        if(vectorData == 0)
            return false;
        else if (vectorData == 1)
                return true;
        else{
            // no data has yet been read that can determine the type
            try{
                readData(0);
            }
            catch(IOException e){
                e.printStackTrace();
                return true; /// struggle on...
            }
            return isVector();
        }
    }

    @Override
    public boolean isTimeDependent() {
        return time != null;
    }

    @Override
    public boolean isSpaceDependent() {
        return true;
    }

    @Override
    public Index getSpaceDomain() {
        return mesh.getBaseSize();
    }

    @Override
    public int getTimeDomain() {
        return time.length;
    }

    @Override
    public String getName() {
        return "savedData"; //todo: get from header
    }

    @Override
    public String getUnit() {
        return "unspecified"; //dodo: get from header
    }
}
