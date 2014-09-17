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

//
//package debug;
//
//import amu.output.ScalarIoTab;
//import amu.output.TableIoTab;
//import amu.output.VectorIoTab;
//import amu.mag.Problem;
//import amu.mag.Unit;
//import amu.mag.config.Uniform;
//import amu.mag.config.Vortex;
//import amu.mag.time.AdamsEvolver2;
//import amu.mag.time.SteepestDescent;
//import amu.solid.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//
//public class StandardProblem2 extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        output.setBaseDirectory(args[0] + ".amu");
//        
//        setMaterial(860E3, 13E-12);
//        double x = Double.parseDouble(args[0]);
//        double lEx = Unit.LENGTH;
//        double d = lEx * x;
//        double c = 1*lEx;             //cell size 
//        
//        setMeshMaxCellSize(5*d, d, 0.2*d, c, c, 1.0/0.0);
//        //intersectMesh(new Cylinder(d/2));
//        setSimulation(2, 0.6, 2);
//        setMagnetization(new Uniform(1, 0.1, 0.01));
//        AdamsEvolver2 e = new AdamsEvolver2(sim, 0.5);
//        e.targetMaxAbsError = 0.001;
//        setEvolver(e);
//        sim.precess = false;
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
//        runTorque(0.001);
//        
//        PrintStream out = new PrintStream(new FileOutputStream(new File("result.txt"), true));
//        out.println(x + " " + sim.get_mx() + " " + sim.get_my() + " " + sim.get_mz());
//    }
//}
