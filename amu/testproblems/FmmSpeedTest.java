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
//import amu.mag.Problem;
//import amu.mag.Simulation;
//import amu.mag.config.*;
//import amu.mag.config.Vortex;
//import amu.mag.fmm.FmmSettingsModule;
//
//public class FmmSpeedTest extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception {
//        setMaterial(860E3, 13E-12);
//        double width = 100E-9;
//        double height = 100E-9;
//        double thickness = 20E-9;
//        double cellsize = 5E-9;
//                
//        setMeshMaxCellSize(width, height, thickness, cellsize, cellsize, cellsize);
//        setMagnetization(new Vortex(0, 0, 0, 1));
//        
//        sim = new Simulation();
//        sim.setMesh(temp_mesh);
//        sim.setKernelIntegrationAccuracy(1);
//        setMagnetization(new Vortex());
//        
//        new FmmSettingsModule(sim).scan(1, 5, 0.05, 0.5, 0.05);
//    }
//    
//}