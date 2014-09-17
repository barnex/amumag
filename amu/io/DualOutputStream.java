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

import java.io.IOException;
import java.io.OutputStream;

public class DualOutputStream extends OutputStream{

    private final OutputStream a, b;
    
    public DualOutputStream(OutputStream a, OutputStream b){
        this.a = a;
        this.b = b;
    }
    
    @Override
    public void write(int byte_) throws IOException {
        a.write(byte_);
        b.write(byte_);
    }

    @Override
    public void flush() throws IOException{
        a.flush();
        b.flush();
    }
    
    @Override
    public void close() throws IOException{
        a.close();
        b.close();
    }
}
