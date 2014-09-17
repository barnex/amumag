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

//package debug;
//
//import amu.geom.*;
//import amu.mag.*;
//import amu.mag.config.*;
//import amu.mag.time.*;
//import amu.output.*;
//import amu.solid.*;
//
//public class CupolaSpherePnP extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        
//        int a = 0;
//        output.setBaseDirectory("cupSpherePnP"
//                + args[a++] + "nmx" + args[a++] + "nm"+
//                args[a++] + "nmCupP" + args[a++] + ".amu");
//  
//        a=0;
//        final double l = Double.parseDouble(args[a++] + "E-9");
//        final double t = Double.parseDouble(args[a++] + "E-9");
//        final double d = Double.parseDouble(args[a++] + "E-9");
//        final double pol =Integer.parseInt(args[a++]);
//          
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 2*lEx;             //cell size 
//        
//        setMeshMaxCellSize(l, l, d+t, c, c, c);
//        
//        intersectMesh(new Shape(){ //cupola
//            @Override
//            protected boolean inside_(Vector r) {
//                return r.z <= (d+t)/2.0 - 2*t*(r.x*r.x+r.y*r.y)/(l*l)
//                        && r.z >= -(d+t)/2.0 + t - 2*t*(r.x*r.x+r.y*r.y)/(l*l);
//            }
//        
//        });
//       
//        
//        setSimulation(2, 0.9, 1);  
//        setMagnetization(new Vortex(0, 0, 0, 1));
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
//        e.targetMaxAbsError = 1E-4;
//        sim.precess = false;
//        sim.externalField = new ExternalField(){
//            @Override
//            protected void put(double time, Vector field) {
//                field.set(10E-3, 0, 0);
//            }
//        };
//        runTorque(1E-3);
//        
//        //reset solver
//        sim.precess = true;
//        e.dt = 1E-4;
//        e.targetMaxAbsError = 1E-5;
//        sim.externalField = null;
//        sim.update();
//        runIterations(10000);
//        runTorque(1E-4);
//    }    
//}
