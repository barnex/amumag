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
//package amu.mag.time;
//
//import amu.geom.Vector;
//import amu.io.Message;
//import amu.mag.Cell;
//import amu.mag.Simulation;
//
///**
// * A test implementation of the AmuSolver using the midpoint rule.
// *
// * @author arne
// */
//public class AmuSolver1 extends AmuSolver{
//
//    protected Extrapolator1[] field;
//    protected Extrapolator1[] m;
//
//    protected double maxDphi;
//    private boolean firstStep = true;
//
//    public AmuSolver1(double maxDphi){
//        this.maxDphi = maxDphi;
//    }
//
//   private final Vector hnew = new Vector();
//   private final Vector mnew = new Vector();
//   private final Vector torquenew = new Vector();
//
//    @Override
//   public void init(Simulation sim){
//        super.init(sim);
//        field = new Extrapolator1[sim.mesh.cells];
//        m = new Extrapolator1[sim.mesh.cells];
//
//        for(int i=0; i<field.length; i++){
//            field[i] = new Extrapolator1();
//            m[i] = new Extrapolator1();
//        }
//
//        //Message.indent("dt:\t " + dt);
//   }
//
//    public void stepImpl(){
//
//        // (0) backup the current state, which will soon be the previous one!
//        {
//            prevDt = dt;
//            // push current state to extrapolator
//            int i = 0;
//            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                field[i].addPoint(prevDt, cell.h);
//                m[i].addPoint(prevDt, cell.m);
//                i++;
//            }
//        }
//
//        // (1) determine the intrinsic time scale of the system in its present state
//        // and use for the time step.
//        {
//            double maxH2 = 0.0;
//            int i = 0;
//            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                if (cell.h.norm2() > maxH2) {
//                    maxH2 = cell.h.norm2();
//                }
//                i++;
//            }
//            double maxH = Math.sqrt(maxH2);
//            dt = maxDphi / maxH; // adaptive time step: max precession angle per step.
//                                 // todo: multiply by the appropriate factor for large damping
//                                 // or no precession.
//            if(firstStep){
//                dt = dt * dt;   // first step is of the euler type, make it quadratically smaller
//                                // to get roughly the same accuracy as the next steps
//                firstStep = false;
//            }
//        }
//
//        // (2) step
//        {
//            int i = 0;
//            for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                // extrapolate to estimated new state
//                field[i].extrapolate(dt / 2, hnew);
//                m[i].extrapolate(dt / 2, mnew);
//                mnew.normalize();// not necessary here
//
//                torque(mnew, hnew, torquenew);
//                cell.m.add(dt, torquenew);
//                cell.m.normalize();
//
//                i++;
//            }
//        }
//
//        // (3) bookkeeping
//        sim.totalTime += dt;
//        totalSteps++;
//
//        // (4) update for next step or output.
//        sim.update();
//    }
//
//
//
//    @Override
//    public String toString(){
//        return "AmuSolver1 (1st order)";
//    }
//}
//
//
//
//
//
//
//
//
////    protected Extrapolator1[] field;
////    protected Extrapolator1[] m;
////
////    public AmuSolver1(double dt){
////        this.dt = dt;
////    }
////
////    @Override
////    public void init(Simulation sim){
////        super.init(sim);
////
////        field = new Extrapolator1[sim.mesh.cells];
////        m = new Extrapolator1[sim.mesh.cells];
////        for(int i=0; i<field.length; i++){
////            field[i] = new Extrapolator1();
////            m[i] = new Extrapolator1();
////        }
////        Message.indent("dt:\t " + dt);
////    }
////
////   private final Vector hnew = new Vector();
////   private final Vector mnew = new Vector();
////   private final Vector torquenew = new Vector();
////
////    public void stepImpl(){
////
////
////        int i=0;
////        for(Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next){
////            // store previous state
////            field[i].setLastPoint(dt, cell.h);
////            m[i].setLastPoint(dt, cell.m);
////
////            // calculate new time step;
////            dt = dt;
////
////            // extrapolate to estimated new state
////            field[i].extrapolate(dt/2, hnew);
////            m[i].extrapolate(dt/2, mnew);
////            mnew.normalize();
////
////            //integrate
////            torque(mnew, hnew, torquenew);
////            cell.m.add(dt, torquenew);
////
////            i++;
////        }
////
////        sim.update();
////
////        totalTime += dt;
////        totalSteps++;
////    }
////
////    protected void torque(Vector m, Vector h, Vector torque){
////        // - m cross H
////        double _mxHx = -m.y*h.z + h.y*m.z;
////	double _mxHy =  m.x*h.z - h.x*m.z;
////	double _mxHz = -m.x*h.y + h.x*m.y;
////
////        // - m cross (m cross H)
////        double _mxmxHx =  m.y*_mxHz - _mxHy*m.z;
////        double _mxmxHy = -m.x*_mxHz + _mxHx*m.z;
////        double _mxmxHz =  m.x*_mxHy - _mxHx*m.y;
////
////        torque.x = _mxHx + _mxmxHx * Cell.alpha;
////        torque.y = _mxHy + _mxmxHy * Cell.alpha;
////        torque.z = _mxHz + _mxmxHz * Cell.alpha;
////    }
////
////    @Override
////    public String toString(){
////        return "AmuSolver1 (1st order extrapolation)";
////    }