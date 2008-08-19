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
import amu.geom.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.time.*;
import amu.io.*;
import amu.geom.solid.*;
import amu.data.*;
//import static java.lang.Math.*;

public class Probl4 extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/probl4.amu");
        setMs(800E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(500E-9);
        setBoxSizeY(125E-9);
        setBoxSizeZ(3E-9);
        setMaxCellSizeX(8E-9);
        setMaxCellSizeY(8E-9);
        setMaxCellSizeZ(16E-9);
        setFmmOrder(2);
        setFmmAlpha(0.9);
        setKernelIntegrationAccuracy(3);
        setMagnetization(new Uniform(1, 1, 1));
        setTargetMaxAbsError(5E-5);
 
    }
      
    
    //@Override
    public void run() throws Exception{
        save("m", 100);
        //save(new SpaceAverage(getData("m")), 10);
	setPrecession(false);
	runTorque(1E-4);
      
	setExternalField(new StaticField(-35.5E-3, -6.3E-3, 0));
	setPrecession(true);
	setDt(1E-5);
        
      	save(new SpaceAverage(getData("m")), 10);
        runTime(1E-9);
    }
}
