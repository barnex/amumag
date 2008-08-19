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
import amu.geom.jelly.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.data.*;

public class Cupola extends Problem{

    public void init(){
        setOutputDir("/home/arne/Desktop/cupola.amu");
        setMs(800E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(300E-9);
        setBoxSizeY(300E-9);
        setBoxSizeZ(30E-9);
        setMaxCellSizeX(16E-9);
        setMaxCellSizeY(16E-9);
        setMaxCellSizeZ(16E-9);
        setFmmOrder(2);
        setFmmAlpha(0.9);
        setKernelIntegrationAccuracy(1);
        setMagnetization(new Vortex());
        setTargetMaxAbsError(2E-5);
        addTransform(new QuarterBend(30E-9, 100E-9).rotateZ(Math.PI*45/180));
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


//
//public class Cupola extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        output.setBaseDirectory("UpCyl" + args[0] + "nmx" + args[1] + "nm+" + args[2]+ "nm.amu");
//        final double l = Double.parseDouble(args[0] + "E-9");
//        final double d = Double.parseDouble(args[1] + "E-9");
//        final double t = Double.parseDouble(args[2] + "E-9");
//        
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 2*lEx;             //cell size 
//        
//        setMeshMaxCellSize(l, l, d+t, c, c, c);
//        
//        intersectMesh(new Shape(){
//
//            @Override
//            protected boolean inside_(Vector r) {
//                return r.z <= (d+t)/2.0 - 4*t*r.x*r.x/(l*l)
//                        && r.z >= -(d+t)/2.0 + t - 4*t*r.x*r.x/(l*l);
//            }
//        
//        });
//    
//        setSimulation(2, 0.9, 3);
//        setMagnetization(new Vortex(0, 0, 0));
//        addMagnetization(new Uniform(0, 0, 0.1));
//        AdamsEvolver2 e = new AdamsEvolver2(sim, 0.5);
//        e.targetMaxAbsError = 1E-5;
//        setEvolver(e);
//        
//        
//        output.setMainDivider(1);
//        output.add(new ScalarIoTab("energyDensity"));
//        output.add(new VectorIoTab("magnetization"));
//        output.add(new VectorIoTab("h"));
//        output.add(new TableIoTab(sim, "energy"));
//        output.add(new TableIoTab(sim, "maxTorque"));
//        output.add(new TableIoTab(sim.evolver, "dt"));
//        output.add(new TableIoTab(sim.evolver, "maxAbsStepError"));
//        output.add(new TableIoTab(sim.evolver, "maxRelStepError"));
//        output.add(new TableIoTab(sim.evolver, "rmsAbsStepError"));
//        output.add(new TableIoTab(sim.evolver, "rmsRelStepError"));
//        output.add(new TableIoTab(sim, "mx"));
//        output.add(new TableIoTab(sim, "my"));
//        output.add(new TableIoTab(sim, "mz"));
//        
//        runTorque(1E-4);
//    }
//}
