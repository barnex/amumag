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
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream with the combined capabilities of DataInputStream and StreamTokenizer.
 */
public final class MixedInputStream extends InputStream implements Closeable{

    private final DataInputStream in;
    private char currentChar;
    private int currentByte;
    private final StringBuffer buffer;
    
    public MixedInputStream(InputStream in) throws IOException{
        this.in = new DataInputStream(in);
        this.buffer = new StringBuffer(20);
        nextChar();
    }

    public String readWord() throws IOException{
        skipWhitespace();
        buffer.setLength(0);
        while(!isWhiteSpace()){
            buffer.append(currentChar);
            nextChar();
        }
        return buffer.toString();
    }

    private void skipWhitespace() throws EOFException, IOException{
        if(currentByte == -1)
            throw new EOFException();
        while(isWhiteSpace())
            nextChar();
    }
    
    private boolean isWhiteSpace() {
        return currentByte == -1 
                || currentChar == ' ' 
                || currentChar == '\n' 
                || currentChar == '#' 
                || currentChar == ':'
                || currentChar == '\t';
    }
    
    private void nextChar() throws EOFException, IOException{
        if(currentByte == -1)
            throw new EOFException();
        currentByte = read();
        currentChar = (char) currentByte;
    }
    
    public double readDouble() throws IOException{
        return in.readDouble();
    }
    
    public double readFloat() throws IOException{
        return in.readFloat();
    }
    
     @Override
    public int read() throws IOException {
        return in.read();
    }
}