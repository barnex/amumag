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

package amu.mag.config;

import amu.geom.Vector;

/**
 *
 * @author arne
 */
public class Uniform extends Configuration{

    private final Vector m;
    
    public Uniform(double x, double y, double z){
        m = new Vector(x, y, z);
    }
    
    @Override
    public void putM(double x, double y, double z, Vector target) {
        target.set(m);
    }

}
