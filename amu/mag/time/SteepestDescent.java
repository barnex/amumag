///*
// *  This file is part of amumag,
// *  a finite-element micromagnetic simulation program.
// *  Copyright (C) 2006-2008 Arne Vansteenkiste
// * 
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// * 
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details (licence.txt).
// */
//
//
//package amu.mag.time;
//
//import amu.mag.Cell;
//import amu.mag.Simulation;
//import amu.io.Message;
//import static java.lang.Math.*;
//
//public class SteepestDescent extends Evolver{
//
//    protected double maxStep;
//    protected double maxTorque;
//    protected double dt;
//    
//    public SteepestDescent(Simulation sim, double maxStep){
//        super(sim);
//        sim.precess = false;
//        this.maxStep = maxStep;
//    }
//    
//    @Override
//    public void init() {
//        sim.update();
//    }
//
//    @Override
//    public void stepImpl() {
//        //determine step size
//        double maxTorque2 = 0.0;
//        for(Cell c = sim.mesh.baseRoot; c != null; c = c.next){
//            if(c.torque.norm2() > maxTorque2)
//                maxTorque2 = c.torque.norm2();
//        }
//        maxTorque = sqrt(maxTorque2);
//        dt = maxStep / maxTorque;
//        for(Cell c = sim.mesh.baseRoot; c != null; c = c.next){
//            c.m.add(dt, c.torque);
//            c.m.normalizeSafe();
//        }
//        sim.totalTime += dt;
//        sim.update();
//    }
//
//    public double get_maxTorque() {
//        return maxTorque;
//    }
//
//    public double get_dt() {
//        return dt;
//    }
//
//    
//}
