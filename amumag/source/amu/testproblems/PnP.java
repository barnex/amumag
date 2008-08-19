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

public class PnP extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/pnp.amu");
        setMs(860E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(300E-9);
        setBoxSizeY(300E-9);
        setBoxSizeZ(30E-9);
        setMaxCellSizeX(8E-9);
        setMaxCellSizeY(8E-9);
        setMaxCellSizeZ(8E-9);
        setFmmOrder(1);
        setFmmAlpha(1.5);
        setKernelIntegrationAccuracy(2);
        setMagnetization(new Vortex(1));
        setTargetMaxAbsError(1E-4);
        setTargetMaxDm(0.01);
    }
      
    
    @Override
    public void run() throws Exception{
        
        setExternalField(new StaticField(10E-3, 0, 0));
       
	save("m", 1);
        save("dt", 1);
        runTime(5E-9);

    }
}
