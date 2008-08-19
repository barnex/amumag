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

package x;

import amu.core.Pool;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Face;
import amu.core.Index;
import java.awt.Color;
import java.util.IdentityHashMap;

/**
 *
 */
public final class Poly implements Comparable<Poly>{
	
        public Index index;
        
	// transformed vertices
	public final Vector[] vertex;
	public Color color;
	public Color lineColor;
	public double z;
        
	public final Vector normal;
        public final Vector transformedNormal;
	public boolean inBack;
        
	
	public Poly(Face face, Vector normal, IdentityHashMap<Vector, Vector> vertexPool){
            
            
            this.normal = new Vector(normal);
	    this.normal.normalizeSafe();
            this.transformedNormal = new Vector(this.normal);
	    vertex = new Vector[4];
	    for(int v=0; v<face.vertex.length; v++){
		Vector vertex = face.vertex[v];
		if(vertexPool.get(vertex) == null)
		    vertexPool.put(vertex, new Vector(vertex)); // checklist plus link to transformed vertex at the same time
		this.vertex[v] = vertexPool.get(vertex);
	    }
	}
	
	public void updateZ(){
	    z = 0.0;
	    for(Vector v: vertex)
		z += v.z;
            inBack = false;
            for(int i=0; i<vertex.length; i++)
                if(vertex[i].z < 0){
                    inBack = true;
                    break;
                }
	}
	
	public int compareTo(Poly other){
	    if(z < other.z)
		return 1;
	    else
		return -1;
	}
    }
