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


package amu.mag.exchange;

import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Simulation;

public class DzyaloshinskyModule {

    private final Simulation sim;
    //dzyaloshinsky vector.
    private final Vector d;
    
    public DzyaloshinskyModule(Simulation sim, Vector d){
        this.sim = sim;
        this.d = d;
    }
    
    public void update(){
       
        Mesh mesh = sim.mesh;
        final Cell[][][] base = mesh.baseLevel;
        
        for(int x=0; x<base.length; x++){
            Cell[][] baseX = base[x];
            for(int y=0; y<baseX.length; y++){
                Cell[] baseXY = baseX[y];
                for(int z=0; z<baseXY.length; z++){  
                    
                    Cell cell = baseXY[z];
                    if(cell != null){
                        
                        final Vector m = cell.m;
                        final Vector center = cell.center;
                        
                        Cell x1 = x == 0            ? null: base[x-1][y][z];
                        Cell x2 = x == base.length-1? null: base[x+1][y][z];
                        
                        Cell y1 = y == 0             ? null: base[x][y-1][z];
                        Cell y2 = y == baseX.length-1? null: base[x][y+1][z];
                        
                        Cell z1 = z == 0              ? null: base[x][y][z-1];
                        Cell z2 = z == baseXY.length-1? null: base[x][y][z+1];
                        
                        double dmxdy1 = y1 == null? 0: (m.x - y1.m.x) / (center.y - y1.center.y);
                        double dmxdy2 = y2 == null? 0: (m.x - y2.m.x) / (center.y - y2.center.y);
                        
                        double dmxdz1 = z1 == null? 0: (m.x - z1.m.x) / (center.z - z1.center.z);
                        double dmxdz2 = z2 == null? 0: (m.x - z2.m.x) / (center.z - z2.center.z);
                        
                        
                        double dmydx1 = x1 == null? 0: (m.y - x1.m.y) / (center.x - x1.center.x);
                        double dmydx2 = x2 == null? 0: (m.y - x2.m.y) / (center.x - x2.center.x);
                        
                        double dmydz1 = z1 == null? 0: (m.y - z1.m.y) / (center.z - z1.center.z);
                        double dmydz2 = z2 == null? 0: (m.y - z2.m.y) / (center.z - z2.center.z);
                        
                        
                        double dmzdx1 = x1 == null? 0: (m.z - x1.m.z) / (center.x - x1.center.x);
                        double dmzdx2 = x2 == null? 0: (m.z - x2.m.z) / (center.x - x2.center.x);
                        
                        double dmzdy1 = y1 == null? 0: (m.z - y1.m.z) / (center.y - y1.center.y);
                        double dmzdy2 = y2 == null? 0: (m.z - y2.m.z) / (center.y - y2.center.y);
                        
                        double dmxdy = 0.5 * (dmxdy1 + dmxdy2);
                        double dmxdz = 0.5 * (dmxdz1 + dmxdz2);
                        double dmydx = 0.5 * (dmydx1 + dmydx2);
                        double dmydz = 0.5 * (dmydz1 + dmydz2);
                        double dmzdx = 0.5 * (dmzdx1 + dmzdx2);
                        double dmzdy = 0.5 * (dmzdy1 + dmzdy2);
          
                        cell.hDzyaloshinsky.x =  d.x *   (dmzdy - dmydz);
                        cell.hDzyaloshinsky.y =  d.y *  (-dmzdx + dmxdz);
                        cell.hDzyaloshinsky.z =  d.z *   (dmydx - dmxdy);
                    }
                }}}
    }
}
