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

import amu.core.Index;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.geom.solid.*;
import amu.mag.Cell;
import amu.mag.Problem;
import amu.mag.config.*;

public class SphereTest extends Problem{

    @Override
    public void init() throws Exception {
        setOutputDir("/home/arne/Desktop/sphereTest.amu");
        setMs(1000E3);
        setA(13E-11);
        double size = 1E-6;
        setBoxSizeX(size);
        setBoxSizeY(size);
        setBoxSizeZ(size);
        setMaxCellSizeX(50E-9);
        setMaxCellSizeY(50E-9);
        setMaxCellSizeZ(50E-9);
        setFmmOrder(1);
        addShape(new Ellipsoid(500E-9));
        setMagnetization(new Uniform(1, 0, 0));
        setKernelIntegrationAccuracy(2);
    }

    @Override
    public void run() throws Exception {
        Mesh mesh = sim.mesh;
        Vector h = new Vector(-1.0 / 3.0, 0, 0);
        int cells = mesh.baseLevel.length;
        int start = cells/4;
        int stop = 3*cells/4;
        for (int order = 1; order < 6; order++) {
            setFmmOrder(order);
            sim.update();
            double rms = 0.0;
            int count = 0;
            for(int i=start; i<stop; i++)
                for(int j=start; j<stop; j++)
                    for(int k=start; k<stop; k++){
                        Cell cell = mesh.getCell(new Index(i, j, k));
                double dx = cell.hDemag.x - h.x;
                double dy = cell.hDemag.y - h.y;
                double dz = cell.hDemag.z - h.z;
                rms += dx * dx + dy * dy + dz * dz;
                count++;
            }
            rms /= count;
            System.out.println(order + " " + rms);
        }
    }

}