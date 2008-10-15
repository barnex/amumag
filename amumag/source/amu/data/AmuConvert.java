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

import amu.core.Index;
import amu.io.ArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import refsh.Interpreter;
import refsh.SyntaxException;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;

public final class AmuConvert{

    public static void main(String[] args){
        try {
            AmuConvert program = new AmuConvert();
            Interpreter interp = new Interpreter(program.getClass(), program);
            StringBuffer command = new StringBuffer();
            for (String arg : args) {
                command.append(arg);
                command.append(" ");
            }
            interp.run(command.toString());
        } catch (SyntaxException ex) {
            error(ex);
        } catch (InvocationTargetException ex) {
            error(ex);
        } catch (IllegalAccessException ex) {
            error(ex);
        } catch (IOException ex) {
            error(ex);
        }
    }

    private static void error(Exception ex) {
        ex.printStackTrace();
        System.exit(-1);
    }
    
    public void zaverage(File file) throws IOException{
        SavedDataModel data = new SavedDataModel(file);
        DataModel derived = new ZAverage(new Component(data, Z));
        derived.save(file.getParentFile());
    }
    
    public void component(File file, int comp) throws IOException{
        SavedDataModel data = new SavedDataModel(file);
        DataModel derived = new Component(data, comp);
        derived.save(file.getParentFile());
    }
    
    public void component(File file, String comp) throws IOException{
        if(comp.equalsIgnoreCase("x"))
            component(file, X);
         if(comp.equalsIgnoreCase("y"))
            component(file, Y);
         if(comp.equalsIgnoreCase("z"))
            component(file, Z);
    }
    
    public void diff(File file, int diff) throws IOException{
        DataModel derived = new Differential(file, diff);
        derived.save(file.getParentFile());
    }
    
    public void gradient(File file) throws IOException{
        SavedDataModel data = new SavedDataModel(file);
        DataModel derived = new Gradient(data);
        derived.save(file.getParentFile());
    }
}