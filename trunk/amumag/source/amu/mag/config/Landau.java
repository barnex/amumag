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


package amu.mag.config;

import amu.core.Index;
import amu.geom.Vector;
import static java.lang.Math.*;

public class Landau extends Configuration{

    private final int polarization;
    private final double CORE_RADIUS = 1.5;
    private final Vector center = new Vector();
    
    public Landau(int polarization){
      this.polarization = polarization;
    }
    
    @Override
    public void putM(double x, double y, double z, Vector target, Index index) {
        double angle = atan2(y, x);
        if(angle <= PI/4 && angle >= -PI/4)
            target.set(0, -1, 0);
        else if(angle >= PI/4 && angle <= 3*PI/4)
            target.set(1, 0, 0);
        else if(angle >= 3*PI/4 || angle <= -3*PI/4)
            target.set(0, 1, 0);
        else if(angle <= -PI/4 && angle >= -3*PI/4)
            target.set(-1, 0, 0);

        if(polarization != 0){
            double distance = sqrt(square(x-center.x) + square(y-center.y));
            if(distance <= CORE_RADIUS){
                target.add(0, 0, polarization);
                Configuration.normalizeVerySafe(target);
            }
        }
    }

    private double square(double x){
      return x*x;
    }

}
