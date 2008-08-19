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

package amu.testproblems;

import amu.geom.Vector;
import amu.mag.field.*;
import amu.geom.jelly.*;
import amu.geom.solid.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.field.ExponentialField;
import amu.data.*;

public class Test extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/test.amu");
        setMs(800E3);
        setA(13E-12);
        setAlpha(0.01);
        setBoxSizeX(250E-9);
        setBoxSizeY(250E-9);
        setBoxSizeZ(50E-9);
        setMaxCellSizeX(8E-9);
        setMaxCellSizeY(8E-9);
        setMaxCellSizeZ(1.0/0.0);
        setFmmOrder(1);
        setFmmAlpha(0.9);
        setKernelIntegrationAccuracy(1);
        setMagnetization(new Vortex(1));
        setTargetMaxAbsError(1E-5);
        //addShape(new Cylinder(50E-9));
        //addTransform(new Stripline(20E-9, 15E-9));
    }
      
    
    //@Override
    public void run() throws Exception{
        
        save("m", 10);
        save("hExt", 10);
        
        final double f=562.5E6;
        
        setPrecession(false);
	runSteps(1000);
      	
	setExternalField(new ExternalField(){
            protected void put(double time, Vector r, Vector field) {
                field.x = Math.sin(2*Math.PI*r.x/50E-9) * 1E-3 * Math.sin(2*Math.PI*500E6*time);
            }
        });
        
	setPrecession(true);
	setDt(1E-5);    

        runTime(60E-9);//*/
    }
}

