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

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package debug;
//
//import amu.output.ScalarIoTab;
//import amu.output.TableIoTab;
//import amu.output.VectorIoTab;
//import amu.mag.*;
//import amu.mag.config.*;
//
///**
// *
// * @author arne
// */
//public class StandardProblem3 extends Problem{
//
//    @Override
//    public void run(String[] args) throws Exception {
//        output.setBaseDirectory(args[0] + ".amu");
//        double size = Double.parseDouble(args[0] + "E-9");   
//        setMaterial(860E3, 13E-12);
//        setMeshBox(size, size, size, 13, 3);
//        //intersectMesh(new Ellipsoid(size/2));
//        setSimulation(2, 0.9, 3);
//        setMagnetization(new Vortex(0, 0, 0, 1));
//        addMagnetization(new Random(0.001));
//        addMagnetization(new Uniform(0, 0, 0.05));
//        //setEvolver(0.5, 0.01);
//        
//        
//        output.setMainDivider(1);
//        /*output.add(new ScalarIoTab("energyDensity"));
//        output.add(new VectorIoTab("magnetization"));
//        output.add(new VectorIoTab("h"));
//        output.add(new VectorIoTab("torque"));
//        output.add(new VectorIoTab("hDemag"));
//        output.add(new VectorIoTab("hKernel"));
//        output.add(new VectorIoTab("hExchange"));
//        output.add(new TableIoTab("exchangeEnergy"));
//        output.add(new TableIoTab("demagEnergy"));
//        output.add(new TableIoTab("energy"));
//        output.add(new TableIoTab("maxTorque"));
//        output.add(new TableIoTab("stepError"));*/
//        runIterations(50);
//    }
//
//}
