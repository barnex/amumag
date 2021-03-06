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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package x;

import static java.lang.Math.*;
import amu.geom.Vector;
import x.Poly;
import java.awt.Color;

/**
 *
 * @author arne
 */
public class Magnetization3D extends Colorizer{

    @Override
    public void colorize(Poly poly) {
        /*Vector m = poly.cell.display;
        double invnorm = 1.0/m.norm();
        double x = invnorm * m.x;
        double y = invnorm * m.y;
        double z = invnorm * m.z;
        double h = atan2(y,x)/2.0/Math.PI;
        double s = 1.0 - abs(z);
        double b = z > 0.0? 1.0: 1.0+z;
        poly.color = Color.getHSBColor((float)h,(float)s,(float)b);
        poly.lineColor = poly.color;*/
    }

    @Override
    public String toString(){
        return "direction";
    }
}
