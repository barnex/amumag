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


package amu.debug;

import amu.geom.Vector;
import amu.mag.time.*;




public class Main {

    public static void main(String[] args) throws Exception{
        amu.mag.Main.main(new String[]{"amu.testproblems.Probl4"});
        
        /*Extrapolator ex = new Extrapolator2();
        ex.addPoint(0, new Vector());
        ex.addPoint(1, new Vector(-1, -1, -1));
        ex.addPoint(1, new Vector(-4, -4, -4));
        Vector target = new Vector();
        double x = 2;
        
        for(int i=0; i<1000; i++){
            double r = 0.5-Math.random();
            
            ex.extrapolate(r, target);
            ex.addPoint(r, target);
            x+=r;
            System.out.println(x + " " + target);
        }//*/
    }
}