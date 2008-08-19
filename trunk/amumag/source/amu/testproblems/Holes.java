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

public class Holes extends Problem{

    public void init(){
        setOutputDir("holes.amu");
        setMs(860E3);
        setA(13E-12);
        setAlpha(0.02);
        setBoxSizeX(500E-9);
        setBoxSizeY(500E-9);
        setBoxSizeZ(50E-9);
        setMaxCellSizeX(8E-9);
        setMaxCellSizeY(8E-9);
        setMaxCellSizeZ(1.0 / 0.0);
        setFmmOrder(1);
        setFmmAlpha(0.9);
        setKernelIntegrationAccuracy(3);
        setMagnetization(new Vortex(1));
        setTargetMaxAbsError(1E-4);
        final double R = 15E-9;     
        
        final double holes = 150E-9;
        Shape fourHoles = new Cylinder(R).translate(holes, holes, 0)
                .join(new Cylinder(R).translate(holes, -holes, 0))
                .join(new Cylinder(R).translate(-holes, holes, 0))
                .join(new Cylinder(R).translate(-holes, -holes, 0))
                .inverse();
        addShape(fourHoles.intersect(new Cylinder(500E-9/2.0)));
    }
      
    
    @Override
    public void run() throws Exception{
        
        setExternalField(new StaticField(10E-3, 0, 0));
        
        save("m", 100E-12);
        
        runTime(10E-9);
        
        setAlpha(0.6);
        setFmmOrder(2);
        runTime(10E-9);

    }
}


//
//package debug;
//
//import amu.geom.*;
//import amu.mag.*;
//import amu.mag.config.*;
//import amu.mag.time.*;
//import amu.output.*;
//import amu.solid.*;
//
//public class Holes extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        
//        int a = 0;
//        output.setBaseDirectory("holes"
//                + args[a++] + "nmx" + args[a++] + "nm"+
//                args[a++] + "holes.amu");
//  
//        a=0;
//
//        final double length = Double.parseDouble(args[a++] + "E-9");
//        final double thick = Double.parseDouble(args[a++] + "E-9");
//        final double holes = Double.parseDouble(args[a++] + "E-9");
//      
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 1*lEx;                                                       //cell size 
//        
//        setMeshMaxCellSize(length, length, thick, c, c, 1.0/0.0);
//        final double R = 15E-9;      
//        Shape fourHoles = new Cylinder(R).translate(holes, holes, 0)
//                .join(new Cylinder(R).translate(holes, -holes, 0))
//                .join(new Cylinder(R).translate(-holes, holes, 0))
//                .join(new Cylinder(R).translate(-holes, -holes, 0))
//                .inverse();
//        intersectMesh(fourHoles.intersect(new Cylinder(length/2.0)));
//        
//        setSimulation(2, 1.1, 3);  
//        setMagnetization(new Vortex(0, 0, 0, 1));
//        setMagnetization(new Region(new Vector(0, 0, 0), 2, new Vector(0, 0, 1)));
//        
//        AdamsEvolver2 e = new AdamsEvolver2(sim, 0.02);
//        setEvolver(e);
//           
//        output.setMainDivider(1);
//        output.add(new ScalarIoTab("energyDensity"), 100);
//        output.add(new VectorIoTab("magnetization"), 100);
//        output.add(new VectorIoTab("h"), 100);
//        output.add(new VectorIoTab("hExchange"), 100);
//        output.add(new VectorIoTab("hDemag"), 100);
//        output.add(new VectorIoTab("hDzyaloshinsky"), 100);
//        output.add(new TableIoTab(sim, "energy"));
//        output.add(new TableIoTab(sim, "maxTorque"));
//        output.add(new TableIoTab(sim.evolver, "dt"));
//        output.add(new TableIoTab(sim.evolver, "maxAbsStepError"));
//        output.add(new TableIoTab(sim.evolver, "maxRelStepError"));
//        output.add(new TableIoTab(sim.evolver, "rmsAbsStepError"));
//        output.add(new TableIoTab(sim.evolver, "rmsRelStepError"));
//        output.add(new TableIoTab(sim, "mx"), 100);
//        output.add(new TableIoTab(sim, "my"), 100);
//        output.add(new TableIoTab(sim, "mz"), 100);
//        output.add(new TableIoTab(sim, "Bx"), 100);
//        output.add(new TableIoTab(sim, "By"), 100);
//        output.add(new TableIoTab(sim, "Bz"), 100);
//        
//        
//        e.targetMaxAbsError = 1E-4;
//        sim.precess = false;
//        sim.externalField = new ExternalField(){
//            @Override
//            protected void put(double time, Vector field) {
//                field.set(5E-3, 0, 0);
//            }
//        };
//        runTorque(1E-3);
//        
//        //reset solver
//        sim.precess = true;
//        e.dt = 1E-5;
//        e.targetMaxAbsError = 1E-5;
//        sim.externalField = null;
//        sim.update();
//        runIterations(1000);
//        runTorque(1E-4);
//    }    
//}