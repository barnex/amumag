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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package amu.io;

import amu.mag.Simulation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author arne
 */
public final class OutputModule {

    //"base" fields
    public final Simulation sim;
    private final File baseDir; 
    
    //"mutable" fields
    private final ArrayList<IoTab> tabs;
   
    //__________________________________________________________________________
    
    public OutputModule(Simulation sim, File baseDir) throws IOException{
        this.sim = sim;
        this.baseDir = baseDir;
        createBaseDir();
        tabs = new ArrayList<IoTab>();      
    }
    
    //__________________________________________________________________________
    
    private void createBaseDir() throws IOException{
        boolean mkdirs = getBaseDir().mkdirs();
        
        // check if the base dir could be created. mkdirs() returns true if the
        // dir was created, but not if it already existed.
        if(!getBaseDir().exists()){
            throw new IOException("Unable to create directory: " + baseDir);
        }
        // purge dir
        for(File d:getBaseDir().listFiles()){
            if(d.listFiles() != null){
                deletionMsg( d + "/*");
                for(File f:d.listFiles())
                    f.delete();
            }
            deletionMsg("" + d);
            d.delete();
        }
        
        try {
            OutputStream out = new FileOutputStream(new File(getBaseDir(), "log.txt"));
            Message.out = new PrintStream(out);
            System.setErr(new PrintStream(new DualOutputStream(out, System.err)));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void add(IoTab tab){
        tab.outputManager = this;
        tabs.add(tab);
    }

    public void notifyStep() throws IOException{
        
        for (IoTab tab : tabs) {
            tab.update();
        }
    }
    
    public void remove(IoTab tab){
        if(!tabs.remove(tab))
            throw new IllegalArgumentException("Output tab does not exist.");
    }
    
    public void saveMesh() throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
                new File(getBaseDir(), "mesh.gz"))));
        out.writeObject(sim.mesh);        
        out.flush();
        out.close();
        Message.debug("saved mesh.");
    }

    public File getBaseDir() {
        return baseDir;
    }

    private static boolean initiated = false;
    private void deletionMsg(String string) {
        if(!initiated){
            Message.title("deleting files");
            Message.hrule();
            initiated = true;
        }
        Message.indent(string);
    }
}
