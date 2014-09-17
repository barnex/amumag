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

package amu.data;

//
//package amu.xdata.model;
//
//import amu.geom.Vector;
//import amu.core.Index;
//import java.io.IOException;
//
///**
// * Model with 3 settable probes. Utility class.
// */
//public class ProbedDataModel extends DerivedDataModel{
//
//    private Scalarizer scalarizer;
//    private TimeProbe timeProbe;
//    private SpaceProbe spaceProbe;
//    
//    public ProbedDataModel(DataModel model){
//        super(model);
//    }
//    
//    @Override
//    public void put( int time,Index r, Vector v) throws IOException {
//         //R&T dependence
//         if(r != null && time > -1){
//            originalModel.put(time, r, v);
//         }
//         //R depencence only
//         else if(r!= null && time < 0){
//            timeProbe.put(r, v);
//         }
//         //t dependence only
//         else if(r == null && time > -1){
//            spaceProbe.put(time, v);
//         }
//         //constant (not very useful)
//         else{
//            throw new IllegalArgumentException();
//         } 
//         //scalarize?
//         if(scalarizer != null){
//            scalarizer.put(time, r, v);
//         }
//    }
//
//    public void setScalarizer(Scalarizer scalarizer) {
//        this.scalarizer = scalarizer;
//        if(scalarizer != null)
//            scalarizer.originalModel = this;
//    }
//
//    /*public TimeProbe getTimeProbe() {
//        return timeProbe;
//    }*/
//
//    public void setTimeProbe(TimeProbe timeProbe) {
//        this.timeProbe = timeProbe;
//        if(timeProbe != null){
//            timeProbe.originalModel = this;
//        }
//    }
//
//    /*public SpaceProbe getSpaceProbe() {
//        return spaceProbe;
//    }*/
//
//    public void setSpaceProbe(SpaceProbe spaceProbe) {
//        this.spaceProbe = spaceProbe;
//        if(spaceProbe != null){
//            spaceProbe.originalModel = this;
//        }
//    }
//    
//    @Override
//    public boolean isVector(){
//        return (scalarizer == null);
//    }
//    
//    @Override
//    public boolean isTimeDependend(){
//        return (timeProbe == null);
//    }
//    
//    @Override
//    public boolean isSpaceDependend(){
//        return (spaceProbe == null);
//    }
//
//    @Override
//    public Index getSpaceDomain() {
//        if(isSpaceDependend())
//            return originalModel.getSpaceDomain();
//        else
//            return null;
//    }
//
//    @Override
//    public int getTimeDomain() {
//        if(isTimeDependend())
//            return originalModel.getTimeDomain();
//        else
//            return -1;
//    }
//}
