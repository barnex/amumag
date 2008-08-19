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

import amu.mag.Cell;
import amu.io.Message;

public final class OpeningAngleProximity extends ProximityModule {

    public final double alpha2;
    private final int dimension;

    public OpeningAngleProximity(double alpha, int dimension) {
        this.alpha2 = square(alpha);
        if (alpha2 == 0) {
            Message.warning("fmm alpha=0, using N^2 demag kernel.");
        }
        this.dimension = dimension;
    }

    /**
     * Checks if Cell a is near to Cell b
     */
    public boolean isNear(Cell a, Cell b) {
        //return openingAngle2(a, b) > alpha2 || openingAngle2(b, a) > alpha2;

        double diam2a = a.size.x * a.size.x + a.size.y * a.size.y;
        if (dimension == 3) {
            diam2a += a.size.z * a.size.z;
        }

        double dx = a.center.x - b.center.x;
        double dy = a.center.y - b.center.y;
        double dz = a.center.z - b.center.z;

        double distance2 = dx * dx + dy * dy + dz * dz;

        double openingAngle2a = diam2a / distance2;

        if (openingAngle2a > alpha2) {
            return true;
            
        } else {

            double diam2b = b.size.x * b.size.x + b.size.y * b.size.y;
            if (dimension == 3) {
                diam2b += b.size.z * b.size.z;
            }

            double openingAngle2b = diam2b / distance2;

            if (openingAngle2b > alpha2) {
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
