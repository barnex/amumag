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
import amu.mag.field.StaticField;
import amu.geom.jelly.*;
import amu.geom.solid.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.data.*;

public class PhD extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/phd.amu");
        setMs(800E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(500E-9);
        setBoxSizeY(500E-9);
        setBoxSizeZ(50E-9);
        setMaxCellSizeX(8E-9);
        setMaxCellSizeY(8E-9);
        setMaxCellSizeZ(16E-9);
        setFmmOrder(1);
        setFmmAlpha(5.0);
        setKernelIntegrationAccuracy(1);
        setMagnetization(new Vortex());
        Homeomorphism bend = new Homeomorphism(){
            public void getMove(Vector r, Vector target) {
	    double height = 100E-9; // we move the vertices up by 100nm ...
	    double length = 250E-9; // ... at 250nm from the origin
            target.z = -height * (r.x*r.x) / (length*length);
        }};
        addTransform(bend);
        setTargetMaxAbsError(2E-5);
        
        //addTransform(new Bend(-100E-9, 320E-9, 600E-9).rotateZ(Math.PI*20/180));
    }
      
    
    //@Override
    public void run() throws Exception{
        save("m");
        
    }
}

