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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import static amu.geom.Vector.*;

/**
 * Data model for an incrementally saved time and space dependend field.
 */
public final class SavedDataModel extends DataModel{

    private Mesh mesh;
    private double[] time;
    
    // -1: not yet read, 0: false, 1: true;
    private int vectorData = -1;
    
    protected File simDir;
    protected File dataDirectory = null;
    protected int current_t = -1;
    
    public SavedDataModel(File simDir, String dataDir) throws IOException{
        setSimulation(simDir);
        setData(dataDir);
    }
    
    protected void setSimulation(File simDir) throws IOException{
        this.simDir = simDir;
        readMesh(new File(simDir, "mesh.gz"));
    }
    
    protected void setData(String dataDir) throws IOException{
        dataDirectory = new File(simDir, dataDir);
        time = readTime(new File(dataDirectory, "time"));
        current_t = -1;
    }
    
    @Override
    public void put(int time, Index r, Vector v) throws IOException {
        if(dataDirectory == null)
           throw new NullPointerException("dataDirectory");
        else{
            if (time != current_t) {
                readData(time);
            }
            v.set(getMesh().getCell(getMesh().nLevels - 1, r).dataBuffer);
        }
    }

   
    public void readMesh(File file) throws IOException {
        try {
            Message.debug("read mesh: " + file.getAbsolutePath());
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
            mesh = (Mesh) in.readObject();
            in.close();
        } catch (Exception ex) {
            throw new IOException("Error reading file: " + file.getAbsolutePath(), ex);
        }
        // init data buffer
        Cell[][][][] levels = getMesh().levels;
        for(int l=0; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null)
                            cell.dataBuffer = new Vector();
                    }
        new LinkedListModule(getMesh()).linkCells();
    }
    
    public static double[] readTime(File file) throws IOException{
        FileInputStream fin = new FileInputStream(file);
        ArrayInputStream in = new ArrayInputStream(fin);
        double[] time = in.readList();
        in.close();
        return time;
    }
    
    private void readData(int time) throws IOException{
        readData(new File(dataDirectory, "time" + time));
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
       
        // read data 
        int l = mesh.nLevels-1;
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                            for(int c=0; c<= maxComponent; c++){
                                // note: when a cell is null, the stored data
                                // will be zero, but we have to read it anyway!
                                double value = in.readDoubleImpl(); // hmm, we kind of cheat here by not using readVector.
                                if(cell != null){
                                    cell.dataBuffer.setComponent(c, value);
                                }
                                // else: discard the zero data.
                            }
                    }
        in.close();
    }
    
    /*public void readData(File file) throws IOException{
        
        Message.debug("reading: " + file);
        
        // open data input stream
        DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
        
        // process header
        int header = in.readInt();
       
        int startLevel;
        if(Header.isFullMesh(header))
            startLevel = 0;
        else
            startLevel = getMesh().levels.length-1;
        vectorData = Header.isVector(header);
        double maxComponent = vectorData? Z: X;
        boolean doubleData = Header.isDouble(header);
        
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
       
        // read data 
        for(int l=startLevel; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null){
                            for(int c=0; c<= maxComponent; c++){
                                double value = doubleData? in.readDouble(): in.readFloat();
                                cell.dataBuffer.setComponent(c, value);
                            }
                        }
                    }
        in.close();
        Message.debug(": " + Header.toString(header));
    }*/

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
            }
            return isVector();
        }
    }

    @Override
    public boolean isTimeDependend() {
        return true;
    }

    @Override
    public boolean isSpaceDependend() {
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
