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
import amu.mag.Face;

/**
 * Debug class: Disable unit multipole expansions.
 */
public final class ZeroUnitQ extends UnitQModule{

    public ZeroUnitQ(Mesh mesh){
	super(mesh);
    }
    
    protected double integrate(Vector center, Face face, IntVector n) {
        return 0.0;
    }

}
