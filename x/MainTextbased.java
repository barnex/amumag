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

package x;

//package amu.xdata;
//
//import amu.core.Index;
//import amu.xdata.model.AtPosition;
//import amu.xdata.model.AtTime;
//import amu.xdata.model.DataModel;
//import amu.xdata.model.SavedDataModel;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintStream;
//import refsh.Interpreter;
//import refsh.RefSh;
//
//public final class MainTextbased{
//    
//    private SavedDataModel originalData;
//    private DataModel derivedData;
//    
//    public static void main(String[] args) throws IOException{
//    
//        MainTextbased program = new MainTextbased();
//        new RefSh(new Interpreter(program.getClass(), program)).interactive();
//    
//    }
//    
//    public void exit(){
//        System.exit(0);
//    }
//    
//    public void simulation(File simDir) throws IOException{
//        originalData = new SavedDataModel(simDir);
//        derivedData = null;
//    }
//    
//    public void data(String dataDir) throws IOException{
//        originalData.setData(dataDir);
//        derivedData = originalData;
//    }
//    
//    public void reset(){
//        derivedData = originalData;
//    }
//    
//    public void attime(int time){
//        derivedData = new AtTime(derivedData, time);
//    }
//    
//    public void atposition(int x, int y, int z){
//        derivedData = new AtPosition(derivedData, new Index(x, y, z));
//    }
//    
//    public String print(){
//        return derivedData.toString();
//    }
//    
//    public void save(File file) throws FileNotFoundException{
//        PrintStream out = new PrintStream(new FileOutputStream(file));
//        out.println(derivedData.toString());
//    }
//}