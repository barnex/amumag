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

//
//package amu.input;
//
//import amu.output.*;
//import amu.geom.Mesh;
//import amu.mag.Cell;
//import java.io.DataInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.util.zip.GZIPInputStream;
//import static amu.geom.Vector.X;
//import static amu.geom.Vector.Z;
//
//public class MeshInput {
//
//    public static final Mesh readMesh(File file) throws IOException{
//        try { 
//            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
//            Mesh mesh = (Mesh) in.readObject();
//            in.close();
//            return mesh;
//        } catch (ClassNotFoundException ex) {
//            throw new IOException("Error reading file: " + file, ex);
//        }
//        
//    }
//    
//    public static final void read(File file, Mesh mesh) throws IOException{
//        
//        Message.debug("read: " + file);
//        
//        // open data input stream
//        DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
//        
//        // process header
//        int header = in.readInt();
//       
//        int startLevel;
//        if(Header.isFullMesh(header))
//            startLevel = 0;
//        else
//            startLevel = mesh.levels.length-1;
//        boolean vectorData = Header.isVector(header);
//        double maxComponent = vectorData? Z: X;
//        boolean doubleData = Header.isDouble(header);
//        
//        Cell[][][][] levels = mesh.levels;
//        // clear data
//        for(int l=0; l<levels.length; l++)
//            for(int i=0; i<levels[l].length; i++)
//                for(int j=0; j<levels[l][i].length; j++)
//                    for(int k=0; k<levels[l][i][j].length; k++){
//                        Cell cell = levels[l][i][j][k];
//                        if(cell != null)
//                            cell.dataBuffer.reset();
//                    }
//       
//        // read data 
//        for(int l=startLevel; l<levels.length; l++)
//            for(int i=0; i<levels[l].length; i++)
//                for(int j=0; j<levels[l][i].length; j++)
//                    for(int k=0; k<levels[l][i][j].length; k++){
//                        Cell cell = levels[l][i][j][k];
//                        if(cell != null){
//                            for(int c=0; c<= maxComponent; c++){
//                                double value = doubleData? in.readDouble(): in.readFloat();
//                                cell.dataBuffer.setComponent(c, value);
//                            }
//                        }
//                    }
//        in.close();
//        Message.debug(": " + Header.toString(header));
//    }
//    
//
//}
