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
// * A test implementation of the AmuSolver using larger steps for Hsmooth
// * than for Hkernel, Hexch.
// *
// * @author arne
// */
//public class AmuSolver4 extends AmuSolver{
//
//    // extrapolators are always up-to-date, ready to extrapolate.
//    // new field values have to be added directly after any simulation update.
//    /** Extrapolates the slowly varying part of the effective field (hSmooth). */
//    protected Extrapolator1[] hSlow;
//    /** Extrapolates the fast varying part of the effective field (hExch + hKernel) */
//    protected Extrapolator1[] hFast;
//    protected Extrapolator1[] hFastBackup;
//    // TODO: make the above 2nd order again.
//
//    // extrapolators are always up-to-date, ready to extrapolate.
//    // so new m values have to be added after every substep.
//    /** Extrapolates the magnetization to the next, smallest step. */
//    protected Extrapolator1[] m;
//
//    /** Backup of m and its history before doing a predictor root step, which
//     * uses the extrapolated slow field. */
//    protected Extrapolator1[] mBackupSlow;
//    /** Backup of m and its history before doing a predictor fast step, which
//     *  uses the extrapolated fast field. */
//    protected Extrapolator1[] mBackupFast;
//
//    /** Maximum precession angle per time step. */
//    protected double maxDphi;
//    /** Number of fast steps (using exact hExch & hKernel, but interpolated hSmooth)
//     *  per root step. */
//    protected int fastSteps;
//    /** Number of sub-steps per fast step (using exact torque (and later hAnis, hExt),
//     *  but interpolated hExch, hKernel and hSmooth) */
//    protected int subSteps;
//
//    // Should fastSteps/subSteps extrapolate their fields to the future (predictor step)
//    // or interpolate between the last field and the updated new field (corrector step)?
//    protected final static boolean INTERPOLATE = true, EXTRAPOLATE = false;
//
//    //cell # to probe for debug
//    public int probe = 0;
//
//
//    public AmuSolver4(double maxDphi, int fastSteps, int subSteps){
//        if(maxDphi <= 0.0)
//            throw new IllegalArgumentException();
//        if(subSteps < 1 || fastSteps < 1)
//            throw new IllegalArgumentException();
//
//        this.maxDphi = maxDphi;
//        this.fastSteps = fastSteps;
//        this.subSteps = subSteps;
//    }
//
//    @Override
//    public void init(Simulation sim){
//        super.init(sim);
//
//        hSlow = new Extrapolator1[sim.mesh.cells];
//        hFast = new Extrapolator1[sim.mesh.cells];
//        hFastBackup = new Extrapolator1[sim.mesh.cells];
//
//        m = new Extrapolator1[sim.mesh.cells];
//        mBackupSlow = new Extrapolator1[sim.mesh.cells];
//        mBackupFast = new Extrapolator1[sim.mesh.cells];
//
//        for(int i=0; i<hSlow.length; i++){
//            hSlow[i] = new Extrapolator1();
//            hFast[i] = new Extrapolator1();
//            hFastBackup[i] = new Extrapolator1();
//
//            m[i] = new Extrapolator1();
//            mBackupSlow[i] = new Extrapolator1();
//            mBackupFast[i] = new Extrapolator1();
//        }
//   }
//
//    /**
//     * Pushes the system state to the extrapolators, so they are ready for first use.
//     */
//    private void initExtrapolators(){
//        Vector cell_hFast = new Vector();
//        int i = 0;
//        for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//            hSlow[i].addPoint(Double.NaN, cell.hSmooth);  // time of first point should never be used, make it NaN to be sure...
//
//            cell_hFast.set(cell.hEx);
//            cell_hFast.add(cell.hKernel);
//            hFast[i].addPoint(Double.NaN, cell_hFast);
//
//            m[i].addPoint(Double.NaN, cell.m);
//            i++;
//        }
//    }
//
//    /**
//     * Make a root step, sub steps are invisible to the simulation output.
//     */
//    public void stepImpl(){
//
//        // (0) not used in this solver, but by some differentiating data models.
//        prevDt = dt;
//
//
//        // (1) determine the intrinsic time scale of the system in its present state
//        // and use it for the time step.
//        // adaptive time step = max precession angle per step.
//        // todo: multiply by the appropriate factor for large damping or no precession.
//
//
//        /////////////
//        ///////////////////////////////////// DEBUG !!!!!!!!!!!!!!!!!!!!!!!
//        ///////////////////////////////////
//        dt = maxDphi;// / maxH();
//        // debug.
//        /*if(totalSteps % 100 == 0)
//            dt /= 10000;//*/
//
//        // (2) If the solver has not been started yet, make the first (smaller) steps with lower order.
//        // Make normal steps otherwise.
//        if (totalSteps == 0) {
//            initExtrapolators();                        // first step: push current m,h to extrapolators
//            dt = dt/4;
//            slowStep();     // first step is of the euler type, make it cubically smaller
//                          // to get roughly the same accuracy as the next steps
//        }
//        else if (totalSteps == 1)
//        {
//            dt = dt /2; // second step is 2st order, make it quadratically smaller
//            slowStep();
//        }
//        else {
//            slowStep();    // finaly, the other steps are big and high-order.
//        }
//
//
//        // (3) bookkeeping
//        sim.mesh.rootCell.resyncH();
//        sim.totalTime += dt;
//        totalSteps++;
//    }
//
//    /**
//     * Make a large step on the time scale of the slowly varying part of the field.
//     * Smaller substeps will be made for the faster varying parts, using interpolated
//     * values for the slowly varying field.
//     */
//    private void slowStep(){
//        // (1) make backup copy of current m/hFast history
//        {int i = 0;for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                mBackupSlow[i].set(m[i]);
//                hFastBackup[i].set(hFast[i]);
//         i++;}}
//
//        // (2) predictor step in extrapolated hSlow (which is set at this moment)
//        fastSteps(EXTRAPOLATE);
//
//        // (3) update smooth field & push to interpolator, hKernel & hExch are already up-to-date
//        sim.updateHSmooth();
//        {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                hSlow[i].addPoint(dt, cell.hSmooth);
//         i++;}}
//
//        // (4) revert to backup copy of m/h history before the estimator step
//        {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                m[i].set(mBackupSlow[i]);
//                hFast[i].set(hFastBackup[i]);
//         i++;}}
//
//        // (5) corrector step in interpolated hSlow.
//        fastSteps(INTERPOLATE);
//    }
//
//    Vector hFastUpdated = new Vector();
//
//    private void fastSteps(boolean interpolateSlow) {
//        double fastDt = dt / (fastSteps);
//        for (int s = 0; s < fastSteps; s++) {
//
//            // (1) make backup copy of current m history
//            {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                mBackupFast[i].set(m[i]);
//            i++;}}
//
//            // (2) predictor step in extrapolated hSlow.
//            subSteps(interpolateSlow, EXTRAPOLATE, s*fastDt); //extrapolate fast
//
//            // (3) update smooth field & push to interpolator, hKernel & hExch are already up-to-date
//            sim.updateHFast();
//            {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                 hFastUpdated.set(cell.hKernel);
//                 hFastUpdated.add(cell.hEx);
//                 hFast[i].addPoint(fastDt, hFastUpdated);  // 2008.12.02: fix: add hFast, not h.
//             i++;}}
//
//            // (4) revert to backup copy of m history before the estimator step
//            {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//                m[i].set(mBackupFast[i]);
//            i++;}}
//
//            // (5) corrector step in interpolated hFast.
//            subSteps(interpolateSlow, INTERPOLATE, s*fastDt); //interpolate fast
//        }
//    }
//
//    // buffers
//    private final Vector hSlowNew = new Vector();
//    private final Vector hFastNew = new Vector();
//    private final Vector hNew = new Vector();
//    private final Vector mnew = new Vector();
//    private final Vector torquenew = new Vector();
//
//    private void subSteps(boolean interpolateSlow, boolean interpolateFast, double t0){
//        double subDt = dt / (subSteps * fastSteps);
//
//        for (int s = 0; s < subSteps; s++) {
//
//            {int i = 0; for (Cell cell = sim.mesh.baseRoot; cell != null; cell = cell.next) {
//
//                 if(probe == i) {
//                     // print at beginning of interval
//                     // extrapolate/interplate field to estimated value, halfway the sub interval
//                    if(!interpolateSlow)
//                        hSlow[i].extrapolate( (s)*subDt+t0, hSlowNew);
//                    else
//                        hSlow[i].extrapolate( (s)*subDt+t0-dt, hSlowNew);
//
//
//                    if(!interpolateFast)
//                        hFast[i].extrapolate( (s)*subDt, hFastNew);
//                    else
//                        hFast[i].extrapolate( (s-subSteps)*subDt, hFastNew);
//
//                    m[i].extrapolate(subDt, mnew);
//                                                                       //# column
//                        System.err.println(totalSteps                  // 1
//                                + " " + (sim.totalTime + (s*subDt) + t0)   // 2
//                                + " " + hSlowNew                       // 3  4  5
//                                + " " + hFastNew                       // 6  7  8
//                                + " " + mnew );//*/                    // 9 10 11
//                 }
//
//                    // extrapolate/interplate field to estimated value, halfway the sub interval
//                    if(!interpolateSlow)
//                        hSlow[i].extrapolate( (s+0.5)*subDt+t0, hSlowNew);
//                    else
//                        hSlow[i].extrapolate( (s+0.5)*subDt+t0-dt, hSlowNew);
//
//
//                    if(!interpolateFast)
//                        hFast[i].extrapolate( (s+0.5)*subDt, hFastNew);
//                    else
//                        hFast[i].extrapolate( (s+0.5-subSteps)*subDt, hFastNew);
//
//                    hNew.set(hSlowNew);
//                    hNew.add(hFastNew);
//
//                    m[i].extrapolate((0.5)*subDt, mnew);
//                    mnew.normalize();
//
//                    torque(mnew, hNew, torquenew);
//
//                    // put the last value of the extrapolator in cell.m, it will be
//                    // ahead-of-date when the step is a corrector step.
//                    m[i].extrapolate(0.0, cell.m); //todo: extrapolator.getLastValue();
//
//                    cell.m.add(subDt, torquenew);
//                    cell.m.normalize();
//
//                    // push m to update extrapolator
//                    // !! here and only here, not outside this loop or m at the beginning of each sub-loop will be pushed twice!
//                    m[i].addPoint(subDt, cell.m);
//                    // use same extrapolation for h, however.
//
//                    if(probe == i) {
//                     // print at beginning of interval
//                     // extrapolate/interplate field to estimated value, halfway the sub interval
//                    if(!interpolateSlow)
//                        hSlow[i].extrapolate( (s+1)*subDt+t0, hSlowNew);
//                    else
//                        hSlow[i].extrapolate( (s+1)*subDt+t0-dt, hSlowNew);
//
//
//                    if(!interpolateFast)
//                        hFast[i].extrapolate( (s+1)*subDt, hFastNew);
//                    else
//                        hFast[i].extrapolate( (s+1-subSteps)*subDt, hFastNew);
//
//
//                                                                       //# column
//                        System.err.println(totalSteps                  // 1
//                                + " " + (sim.totalTime + (s+1)*subDt + t0)   // 2
//                                + " " + hSlowNew                       // 3  4  5
//                                + " " + hFastNew                       // 6  7  8
//                                + " " + cell.m );//*/                    // 9 10 11
//                 }
//
//                    i++;}}
//
//                }
//    }
//
//    @Override
//    public String toString(){
//        return "AmuSolver4";
//    }
//}
