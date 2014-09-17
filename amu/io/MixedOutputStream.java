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

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream with the combined capabilities of PrintStream and DataOutputStream.
 */
public final class MixedOutputStream implements Closeable, Flushable{
    
    private final DataOutputStream out;
    
    public MixedOutputStream(OutputStream out){
        this.out = new DataOutputStream(out);
    }
    
    //________________________________________________________________print text
    
    public void println(String s) throws IOException{
        print(s + '\n');
    }
    
    public void print(String s) throws IOException{
        out.writeBytes(s);
    }
    
    public void print(double d) throws IOException{
        print("" + d);
    }
    
    public void print(float f) throws IOException{
        print ("" + f);
    }
    
    //________________________________________________________________write data
    
    public void writeDouble(double d) throws IOException{
        out.writeDouble(d);
    }

    public void writeFloat(float f) throws IOException{
        out.writeFloat(f);
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }
}