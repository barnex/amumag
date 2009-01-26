package amu.testproblems;


import amu.data.Integral;
import amu.data.Norm;
import amu.geom.solid.Cylinder;
import amu.mag.Problem;
import amu.mag.config.Random;
import amu.mag.config.Vortex;
import amu.mag.time.AmuSolver5;
import amu.mag.time.Relax5;

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

public class Test extends Problem{

    @Override
    public void init() throws Exception {
        setOutputDir("/home/arne/Desktop/test.amu");
        setMs(736E3);
        setA(13E-12);
        setBoxSize(200E-9, 200E-9, 40E-9);
        setMaxCellSize(5E-9, 5E-9, 1.0/0.0);
        //addShape(new Cylinder(100E-9));
        setMagnetization(new Vortex(1, 1));
        setSolver(new AmuSolver5(0.02, 2, 1));
        setKernelIntegrationAccuracy(4);
        setFmmAlpha(0.6);
        setDipoleCutoff(0.01);
        setAlpha(0.5);
    }

    @Override
    public void run() throws Exception {
       
        int save = 10;
        save(getData("m"), save);
        save(getData("charge"), save);
        save(getData("dipole"), save);
        save(new Norm(getData("dipole")), save);
        save(getData("chargefree"), save);
        save(new Integral(getData("energyDensity")), save);

         runSteps(1000);
    }

}