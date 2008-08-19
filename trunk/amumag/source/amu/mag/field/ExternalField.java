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


package amu.mag.field;

import amu.mag.*;
import amu.geom.Vector;

public abstract class ExternalField {

    private final Vector buffer = new Vector();
    
    // the time at which the ExternalField was set, in seconds
    // can be used for relative times.
    public double timeZero;
    
    public ExternalField(){
        timeZero = Main.sim.totalTime * Unit.TIME;
    }
    
    /**
     * Returns the applied field in SI units (A/m) as a function of time (s).
     * To be overridden.
     */
    protected abstract void put(double time, Vector r, Vector field);

    /**
     * Returns the applied field in simulation units, used by the solver.
     * @param time
     * @return
     */
    public final Vector get(double time, Vector r) {
        put((time)*Unit.TIME - timeZero, r ,buffer);
        buffer.divide(Unit.mu0 * Unit.FIELD);
        return buffer;
    }
    
    //________________________________________________________________operations
    
    public ExternalField add(ExternalField other){
        return new  Sum(this, other);
    }
    
     public ExternalField multiply(ExternalField other){
        return new  Product(this, other);
    }
}
