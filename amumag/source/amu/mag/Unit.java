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

package amu.mag;
import amu.debug.InvalidProblemDescription;
import amu.io.Message;
import java.util.Hashtable;
import static java.lang.Math.*;

/**
 * Inside the simulation Ms = A = gamma = mu0 = 1.
 * To convert results back to physical (SI) units,
 * multiply by FIELD, LENGTH, TIME or ENERGY.
 * Thes simulation units are calculated from the 
 * material parameters.
 */
public final class Unit {

    // Material parameters
    
    /**
     * Saturation magnetization in A/m
     */
    public static double Ms;
    
    /**
     *  Exchange constant in J/m
     */
    public static double A;
    
    /**
     * Gyromagnetic ratio in m/As
     */
     public static double gamma = 2.211E5;
     
     /**
      * mu0 in N/A^2
      */
     public static final double mu0 = 4.0E-7 * PI;
     
     
     // Simulation units: Ms=1, A=1, gamma=1, mu0=1
	
     protected static void setMaterial(double ms, double a, double gamma) throws InvalidProblemDescription{
        if(ms <= 0.0)
            throw new InvalidProblemDescription("Ms (saturation magnetization) should be set > 0");
        if(a <= 0.0)
            throw new InvalidProblemDescription("A (exchange constant) should be set > 0");
        if(gamma == 0.0)
            throw new InvalidProblemDescription("gamma (gyromagnetic ratio) should not be 0");
        Unit.gamma = gamma;
        Unit.Ms = ms;
        Unit.A = a;
        Unit.init();
    }
    
    protected static void setMaterial(double ms, double a) throws InvalidProblemDescription{
        setMaterial(ms, a, Unit.gamma);
    }
    
     public static void init(){
        FIELD = Ms;
        LENGTH = sqrt(2.0*A/(mu0*Ms*Ms));   //2007-02-05: crucial fix: factor sqrt(2), LENGTH is now the exchange length, not just 'a' good length unit.
        TIME = 1.0 / (gamma * Ms);
        ENERGY = A * LENGTH;
        
        forField.put("m", FIELD + "Am-1");
        forField.put("h", FIELD + "Am-1");
        forField.put("hDemag", FIELD + "Am-1");
        forField.put("hKernel", FIELD + "Am-1");
        forField.put("hSmooth", FIELD + "Am-1");
        forField.put("hEx", FIELD + "Am-1");
        forField.put("hExt", "T");
        forField.put("dt", "s");
        
        Message.title("material parameters");
        Message.hrule();
        Message.indent("A    : \t" + A + " J/m");
        Message.indent("Ms   : \t" + Ms + " A/m");
        Message.indent("gamma: \t" + gamma + " m/As");
        Message.indent("mu0  : \t" + mu0 + " N/A^2");
        
        Message.indent("exchange length: \t" + LENGTH + " m");
        Message.indent("unit time      : \t" + TIME + " s");
        Message.indent("unit energy    : \t" + ENERGY + " J");
        
     }
     
     public static final String getUnit(String fieldName){
        String unit = forField.get(fieldName);
        if(unit == null)
            return "unspecified";
        else
            return unit;
     }
     
     private static final Hashtable<String, String> forField = new Hashtable<String, String>();
     
     /**
      * Simulation unit of H-fields (saturation magnetization).
      */
     public static double FIELD;
	
     /**
      * Simulation unit of length (exchange length).
      */
     public static double LENGTH;
     
     /**
      * Simulation unit of time (precession frequency of Ms in unit field)
      */
     public static double TIME;
     
     /**
      * Simulation unit of energy (energy of a featrue with excange length size?)
      */
     public static double ENERGY;
     
     
    // 1 US teaspoon per minute equals 1.22057858x10-8 cubic furlong per fortnight.
    // google calculator.
}
