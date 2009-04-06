package amu.mag;


import amu.geom.Mesh;
import amu.geom.Vector;
import amu.io.Message;
import amu.mag.Cell;
import amu.mag.Simulation;


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

public final class DampingTensor{

  private static final double[][] D = new double[3][3];
 

  static{
    Message.warning("Damping tensor not compatible with adaptive mesh");

  }

  private static final Vector dmdx = new Vector(), dmdy = new Vector(), dmdz = new Vector(), buffer = new Vector();
  private static final Vector[] dmd = new Vector[]{dmdx, dmdy, dmdz};
  private static final Vector DdotDm_Dt = new Vector();

  public static void update(Simulation sim){

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

                        // neighbors in each direction

                        Cell x1 = x == 0            ? null: base[x-1][y][z];
                        Cell x2 = x == base.length-1? null: base[x+1][y][z];

                        Cell y1 = y == 0             ? null: base[x][y-1][z];
                        Cell y2 = y == baseX.length-1? null: base[x][y+1][z];

                        Cell z1 = z == 0              ? null: base[x][y][z-1];
                        Cell z2 = z == baseXY.length-1? null: base[x][y][z+1];

                        dmdx.reset();
                        dmdy.reset();
                        dmdz.reset();

                        if(x1 != null){
                          buffer.set(m);
                          buffer.subtract(x1.m);
                          buffer.divide(center.x - x1.center.x);
                          dmdx.set(buffer);
                        }
                        if(x2 != null){
                          buffer.set(m);
                          buffer.subtract(x2.m);
                          buffer.divide(center.x - x2.center.x);
                          if(x1 == null)
                            dmdx.set(buffer);
                          else{
                            dmdx.add(buffer);
                            dmdx.multiply(0.5);
                          }
                        }


                        if(y1 != null){
                          buffer.set(m);
                          buffer.subtract(y1.m);
                          buffer.divide(center.y - y1.center.y);
                          dmdy.set(buffer);
                        }
                        if(y2 != null){
                          buffer.set(m);
                          buffer.subtract(y2.m);
                          buffer.divide(center.y - y2.center.y);
                          if(y1 == null)
                            dmdy.set(buffer);
                          else{
                            dmdy.add(buffer);
                            dmdy.multiply(0.5);
                          }
                        }



                        if(z1 != null){
                          buffer.set(m);
                          buffer.subtract(z1.m);
                          buffer.divide(center.z - z1.center.z);
                          dmdz.set(buffer);
                        }
                        if(z2 != null){
                          buffer.set(m);
                          buffer.subtract(z2.m);
                          buffer.divide(center.z - z2.center.z);
                          if(z1 == null)
                            dmdz.set(buffer);
                          else{
                            dmdz.add(buffer);
                            dmdz.multiply(0.5);
                          }
                        }

//                        System.out.println("dm/dx " + dmdx);
//                        System.out.println("dm/dy " + dmdy);
//                        System.out.println("dm/dz " + dmdz);

                        for(int i=0; i<3; i++)
                          for(int j=0; j<3; j++)
                            D[i][j] = 0.0;

                        // normal damping on diagonal elements 
                        D[0][0] = D[1][1] = D[2][2] = sim.dampingTensorAlpha;

                        // nonlocal contributions:
                        for(int a = 0; a < 3; a++){
                          for(int b = 0; b < 3; b++){

                            double sum = 0.0;

                            for(int i=0; i<3; i++){

                              buffer.set(m);
                              buffer.cross(dmd[i]);

                              sum += buffer.getComponent(a) * buffer.getComponent(b);
                            }

                            sum *= sim.dampingTensorEta; // sign error somewhere?
                            //System.out.println(sum);
                            D[a][b] += sum;

                          }
                        }// end damping tensor loop

                        //add extra damping to dm/dt
                        DdotDm_Dt.x = dot(D[0], cell.dmdt);
                        DdotDm_Dt.y = dot(D[1], cell.dmdt);
                        DdotDm_Dt.z = dot(D[2], cell.dmdt);

                        //System.out.println(cell.dmdt + " -> " + DdotDm_Dt);

                        buffer.set(m);
                        buffer.cross(DdotDm_Dt);  // m x (D . dm/dt)

                        debug.set(m);
                        debug.cross(cell.dmdt);
                        //System.out.println(buffer + " .  " + debug + " = " + buffer.dot(debug) + (buffer.dot(debug)<0.0? "!!!!!!!!!!!!!!!":"") );

                        cell.dmdt.add(buffer);
                    }
                }}}
  }

  private static final Vector debug = new Vector();

  private static final double dot(double[] a, Vector b){
    return a[0] * b.x + a[1] * b.y + a[2] * b.z;
  }
}