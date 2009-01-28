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

package amu.mag.fmm;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Simulation;
import amu.mag.config.*;
import amu.io.Message;
import static amu.mag.fmm.OpeningAngleProximity.square;
import static java.lang.Math.sqrt;

/**
 * Determines optimal FMM parameter (alpha, order) settings for maximal speed
 * at the desired accuracy.
 */
public class FmmSettingsModule{
    
    private final Simulation sim;
    private final Mesh mesh;
    
    // original settings to revert to after scan.
    private int originalOrder;
    private double originalAlpha;
    
    public FmmSettingsModule(Simulation sim){
        this.sim = sim;
        this.mesh = sim.mesh;
        originalOrder = sim.order;
        originalAlpha = sim.alphaFMM;
    }
    
    /**
     * The number of times the smoothField is updated to measure the needed time.
     */
    public static final int UPDATES = 10;
    
    public void scan(int startOrder, int stopOrder, 
            double startAlpha, double stopAlpha, double alphaStep,
            int referenceOrder, double referenceAlpha) throws Exception{
        
        Message.println("Scan optimal FMM settings: order=" + startOrder + "-" + stopOrder +
                        ", alpha=" + startAlpha + "-" + stopAlpha + "(" + alphaStep + " step)" + 
                        ", reference: order=" + referenceOrder + ", alpha=" + referenceAlpha
                );
        
        sim.setAlphaFMM(referenceAlpha);
        sim.setOrder(referenceOrder);
        sim.update();
        
        // reference field with accurate FMM settings.
        Vector[][][] hReference = copyHDemag();
        
        // to be overwritten with fields for different settings.
        Vector[][][] h = copyHDemag();
        
        // changing alpha is more expensive than changing order: outer loop.
        for(double alpha = startAlpha; alpha <= stopAlpha; alpha += alphaStep){
            sim.setAlphaFMM(alpha);
            for(int order = startOrder; order <= stopOrder; order++){
                sim.setOrder(order);
                
                int time = 0;
                for(int i = 0; i < UPDATES; i++){
                    long start = System.currentTimeMillis();
                    sim.update();
                    time += (int)(System.currentTimeMillis() - start);
                }
                
                
                copyHDemag(h);
                double rmsError = rmsDeviation(h, hReference);
                
                Message.println("order: " + order + ", alpha: " + alpha + 
                        ": error=" + rmsError + ", time=" + time + "ms");
                Message.println(alpha +  " " + order +  " " + rmsError + " " + time + " #scan");
            }
            Message.println("#scan");
        }
    }
    
    public void scan(int startOrder, int stopOrder,
            double startAlpha, double stopAlpha, double alphaStep) throws Exception{
            
        int referenceOrder = stopOrder + 1;
        double referenceAlpha = startAlpha;
        
        scan(startOrder, stopOrder, startAlpha, stopAlpha, alphaStep, referenceOrder, referenceAlpha);
    }
            
    public void scan(int stopOrder, double startAlpha, double stopAlpha) throws Exception{
        
        double alphaStep = (stopAlpha-startAlpha)/10;
        int referenceOrder = stopOrder + 1;
        double referenceAlpha = startAlpha;
        
        scan(1, stopOrder, startAlpha, stopAlpha, alphaStep, referenceOrder, referenceAlpha);
    }
    
        private Vector[][][] copyHDemag(){
        Vector[][][] hDemag = new Vector[mesh.baseLevel.length]
                                        [mesh.baseLevel[0].length]
                                        [mesh.baseLevel[0][0].length];
        copyHDemag(hDemag);
        return hDemag;
    }

    /**
     * Copy HDemag to a Vector array.
     */
    private void copyHDemag(Vector[][][] hDemag) {
        for(int i=0; i<mesh.baseLevel.length; i++)
            for(int j=0; j<mesh.baseLevel[i].length; j++)
                for(int k=0; k<mesh.baseLevel[i][j].length; k++){
                    
                    // if there is no cell at position ijk, hDemag is set to null
                    Cell cell = mesh.baseLevel[i][j][k];
                    if(cell == null)
                        hDemag[i][j][k] = null;
                    // otherwise we copy hDemag, but recycle existing vectors if present.
                    else{
                        if(hDemag[i][j][k] == null)
                            //copy to fresh Vector
                            hDemag[i][j][k] = new Vector(cell.hDemag);
                        else
                            //copy to recycled old vector.
                            hDemag[i][j][k].set(cell.hDemag);
                    }
                    
                }
    }

    private double rmsDeviation(Vector[][][] field1, Vector[][][] field2) {
        
        int count = 0;
        double deviation2 = 0.0; //bucket?
        
        for(int i=0; i<mesh.baseLevel.length; i++)
            for(int j=0; j<mesh.baseLevel[i].length; j++)
                for(int k=0; k<mesh.baseLevel[i][j].length; k++){
                    Vector v1 = field1[i][j][k];
                    if(v1 != null){
                        count++;
                        Vector v2 = field2[i][j][k];
                        deviation2 += square(v1.x - v2.x) + square(v1.y - v2.y) + square(v1.z - v2.z);
                    }
                }
            return sqrt(deviation2);
    }

}
