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

package amu.geom.solid;

import amu.geom.Vector;

/**
 *
 * @author arne
 */
public class ScaledShape extends Shape{

    private Shape shape;
    private double scale;
    
    public ScaledShape(Shape shape, double scale){
        this.shape = shape;
        this.scale = 1.0/scale;
    }
    
    @Override
    protected boolean inside(Vector r) {
        r.divide(scale);
        return shape.insideInternal(r);
    }

}
