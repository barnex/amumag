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

import amu.mag.field.StaticField;
import amu.geom.solid.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.data.*;

public class Skew extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/skew" + (getArg(0)==1?"Up":"Down") + ".amu");
        setMs(800E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(600E-9);
        setBoxSizeY(500E-9);
        setBoxSizeZ(50E-9);
        setMaxCellSizeX(16E-9);
        setMaxCellSizeY(18E-9);
        setMaxCellSizeZ(16E-9);
        setFmmOrder(2);
        setFmmAlpha(0.9);
        setKernelIntegrationAccuracy(1);
        setMagnetization(new Vortex());
        setTargetMaxAbsError(2E-5);
        addShape(new SemiSpace().rotateZ(Math.PI*10/180).translate(500E-9, 0.0));
    }
      
    
    //@Override
    public void run() throws Exception{
        
        save("m", 100);
      	save(new SpaceAverage(getData("m")), 10);
        
        setExternalField(new StaticField(10E-3, 0, 0));
	setPrecession(false);
	runTime(10E-9);
      
	setExternalField(new StaticField(0, 0, 0));
	setPrecession(true);
	setDt(1E-5);
	//setTime(0.0);
        
        runTime(1.5E-9);
    }
}
