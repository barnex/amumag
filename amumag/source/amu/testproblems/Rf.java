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
//import amu.geom.*;
//import amu.mag.*;
//import amu.mag.config.*;
//import amu.mag.time.*;
//import amu.output.*;
//import amu.solid.*;
//import static java.lang.Math.*;
//
//public class Rf extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        
//        int a = 0;
//        output.setBaseDirectory("rf" + args[a++] + "MHz" +args[a++] + "mT/" 
//                + args[a++] + "nmx" + args[a++] + "nm"+
//                "/C" + args[a++] + "P" + args[a++] + "/" + 
//                "dz" + args[a++] + ".amu");
//  
//        a=0;
//        
//        final double freq = Double.parseDouble(args[a++] + "E6");
//        final double pulse = Double.parseDouble(args[a++] + "E-3");
//        final double length = Double.parseDouble(args[a++] + "E-9");
//        final double thick = Double.parseDouble(args[a++] + "E-9");
//        final int circ = Integer.parseInt(args[a++]);
//        final int pol = Integer.parseInt(args[a++]);
//        final double dz = Double.parseDouble(args[a++]);
//        
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 1*lEx;             //cell size 
//        
//        setMeshMaxCellSize(length, length, thick, c, c, 1.0/0.0);
//        setSimulation(3, 0.6, 3);
//        sim.setDzyaloshinsky(dz);
//        
//        setMagnetization(new Vortex(0, 0, 0, circ));
//        setMagnetization(new Region(new Vector(0, 0, 0), 2, new Vector(0, 0, pol)));
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
//        e.targetMaxAbsError = 1E-5;
//        sim.precess = false;
//        runTorque(1E-4);
//        
//        //reset solver
//        final double t0 = sim.totalTime*Unit.TIME;
//        sim.externalField = new ExternalField(){
//            @Override
//            protected void put(double time, Vector field) {
//                field.set(pulse*sin(2*PI*freq*(time-t0)), 0, 0);
//            }
//        };
//        sim.precess = true;
//        e.dt = 1E-5;
//        e.targetMaxAbsError = 1E-5;
//        sim.update();
//        runIterations(10000);
//    }    
//}