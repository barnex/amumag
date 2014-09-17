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

import amu.geom.Vector;
import amu.mag.Cell;
import amu.io.Message;
import static java.lang.Math.*;

/**
 * Proximity criterion is the opening angle weighed by the norm of the cell's
 * multipole. As a safety measure, partner cells can be no closer than allowed
 * by TouchProximity.
 * @author arne
 */
public final class DynamicRewire extends ProximityModule {

    public final double alpha;
    private final int dimension;

    public DynamicRewire(double alpha, int dimension) {
        this.alpha = alpha;
        if (alpha == 0) {
            Message.warning("fmm alpha=0, using N^2 demag kernel.");
        }
        this.dimension = dimension;
    }

    /**
     * Checks if Cell a is near to Cell b
     */
    public boolean isNear(Cell field, Cell source) {
        //(step 1) make sure we do not go closer than touchproximity.

        //copied form TouchProximity
        for (Vector vertexA : field.vertex) {
            for (Vector vertexB : source.vertex) {
                if (vertexA == vertexB) {
                    return true;
                }
            }
        }

        // (step 2) use opening angle proximity, weighed by the source multipole
        // this is not commutative anymore in field and source

        double sourceWeight = abs(source.multipole.q[0]);

        //ad-hoc: todo: cache.
        for(int i=1; i<source.multipole.q.length; i++){
          if(abs(source.multipole.q[i]) > sourceWeight)
            sourceWeight = abs(source.multipole.q[i]);
        }


        System.out.println(sourceWeight);


        double diam2a = field.size.x * field.size.x + field.size.y * field.size.y;
        if (dimension == 3) {
            diam2a += field.size.z * field.size.z;
        }

        double dx = field.center.x - source.center.x;
        double dy = field.center.y - source.center.y;
        double dz = field.center.z - source.center.z;

        double distance2 = dx * dx + dy * dy + dz * dz;

        double openingAngleA = sqrt(diam2a / distance2);

        if (openingAngleA*sourceWeight > alpha) {
            return true;

        } else {

            double diam2b = source.size.x * source.size.x + source.size.y * source.size.y;
            if (dimension == 3) {
                diam2b += source.size.z * source.size.z;
            }

            double openingAngleB = sqrt(diam2b / distance2);

            if (openingAngleB*sourceWeight > alpha) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static double square(double a) {
        return a * a;
    }
}
