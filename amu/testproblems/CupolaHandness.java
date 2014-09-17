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
//import amu.geom.Vector;
//import amu.output.ScalarIoTab;
//import amu.output.TableIoTab;
//import amu.output.VectorIoTab;
//import amu.mag.Problem;
//import amu.mag.Unit;
//import amu.mag.config.Region;
//import amu.mag.config.Uniform;
//import amu.mag.config.Vortex;
//import amu.mag.time.AdamsEvolver2;
//import amu.solid.*;
//import static java.lang.Math.*;
//
//public class CupolaHandness extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception { 
//        output.setBaseDirectory("atan" + args[0] + "nmx" + args[1] + "nm+" + args[2]+ "nm" + args[3] + "P" + args[4] + ".amu");
//        final double length = Double.parseDouble(args[0] + "E-9");
//        final double thick = Double.parseDouble(args[1] + "E-9");
//        final double depth = Double.parseDouble(args[2] + "E-9");
//        //final double skew = Double.parseDouble(args[3] + "E-9");
//        final int circ = Integer.parseInt(args[3]);
//        final int pol = Integer.parseInt(args[4]);
//        
//        setMaterial(860E3, 13E-12);
//        double lEx = Unit.LENGTH;
//        double c = 1.5*lEx;             //cell size 
//        
//        setMeshMaxCellSize(length, length, thick+depth, c, c, 2*c);
//        
//        //intersectMesh(new Cylinder(length/2));
//        
//        intersectMesh(new Shape(){
//            @Override
//            protected boolean inside_(Vector r) {
//                return r.z <= 0.5*thick + 0.5*depth * atan(r.x/r.y) / PI
//                        && r.z >= -0.5*thick + 0.5*depth * atan(r.x/r.y)/PI;
//            }
//        });
//    
//        intersectMesh(new Cylinder(length/2));
//        
//        setSimulation(2, 0.9, 3);
//        sim.precess = false;
//        setMagnetization(new Vortex(0, 0, 0, circ));
//        addMagnetization(new Region(new Vector(0, 0, 0), 2, new Vector(0, 0, 10*pol)));
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
