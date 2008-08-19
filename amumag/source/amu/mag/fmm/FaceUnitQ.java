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

import amu.core.Recycler;
import amu.geom.GeomModule;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Face;
import amu.mag.kernel.Triangle;

/**
 * TODO: does only integrate with 4 points, implement recursive refinement.
 */
public final class FaceUnitQ extends UnitQModule{

    private final Recycler<Triangle> triangles = new Recycler<Triangle>(new Triangle[20]);
    
    public FaceUnitQ(Mesh mesh){
	super(mesh);
    }
	    
    protected double integrate(Vector center, Face face, IntVector n) {
        if(face.scalarArea != 0.0){
	    Vector[] vertex = face.vertex;
	    Vector r0 = GeomModule.barycentrumPlane(face.vertex);
	    double sum = 0.0;
	    
	    for(int i=0; i<4; i++){
		Triangle t = triangles.get();
		t.set(vertex[i], vertex[(i+1)%4], r0);
		sum += integrate(center, t, n, face.scalarArea);
		triangles.recycle(t);
	    }
	    return sum;
	}
	else
	    return 0.0;
    }

    private double integrate(Vector center, Triangle t, IntVector n, double faceArea){
	Vector r = new Vector(t.center);
	r.subtract(center);
	return r.pow(n) * t.area / faceArea;
    }
}
