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
//import amu.geom.*;
//import amu.mag.*;
//import amu.mag.config.*;
//import amu.mag.time.*;
//import amu.output.*;
//import amu.solid.*;
//
//public class Dz extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        
//        //output.setBaseDirectory("test.amu");
//        output.setBaseDirectory("dz" + args[0] + "nmx" + args[1] + "nmC" + args[2] + "P" + args[3] + "dz" + args[4] + ".amu");
//        
//        final double length = Double.parseDouble(args[0] + "E-9");
//        final double thick = Double.parseDouble(args[1] + "E-9");
//        final int circ = Integer.parseInt(args[2]);
//        final int pol = Integer.parseInt(args[3]);
//        final double dz = Double.parseDouble(args[4]);
//        
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 1.5*lEx;             //cell size 
//        
//        setMeshMaxCellSize(length, length, thick, c, c, 2*c);
//        setSimulation(2, 0.9, 3);
//        sim.precess = false;
//        sim.setDzyaloshinsky(dz);
//        //setMagnetization(new Landau());
//        setMagnetization(new Vortex(0, 0, 0, circ));
//        //setMagnetization(new Uniform(1, 0, 0));
//        setMagnetization(new Region(new Vector(0, 0, 0), 2, new Vector(0, 0, pol)));
//        addMagnetization(new Random(0.01));
//        AdamsEvolver2 e = new AdamsEvolver2(sim, 0.01);
//        e.targetMaxAbsError = 1E-5;
//        setEvolver(e);
//        
//        
//        output.setMainDivider(1);
//        output.add(new ScalarIoTab("energyDensity"), 10);
//        output.add(new VectorIoTab("magnetization"), 10);
//        output.add(new VectorIoTab("h"), 10);
//        output.add(new VectorIoTab("hExchange"), 10);
//        output.add(new VectorIoTab("hDemag"), 10);
//        output.add(new VectorIoTab("hDzyaloshinsky"), 10);
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
//    
//}
