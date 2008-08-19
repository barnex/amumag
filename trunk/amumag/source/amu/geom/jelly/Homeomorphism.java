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

package amu.geom.jelly;

import amu.core.Index;
import amu.geom.*;
import amu.mag.Cell;
import amu.mag.Unit;
import java.util.IdentityHashMap;
import static java.lang.Math.*;

/**
 *
 */
public abstract class Homeomorphism {
    
    private Vector position = new Vector(), target = new Vector();
    
    /**
     * Method implementing the homeomorphism: returns the translation
     * for each vertex.
     * @param r position of vertex to be moved.
     * @param target relative translation to be applied to the vertex should
     * be stored here.
     */
    public abstract void getMove(Vector r, Vector target);
    
    /**
     * May be overridden instead of getMove(r, target) if one also needs to
     * know the size of the mesh.
     * @param mesh
     */
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex){
        getMove(r, target);
    }
    
    public final void transform(Vector v, Mesh mesh, Index cellIndex, Index vertexIndex){
        position.set(v);
        position.multiply(Unit.LENGTH);
        target.reset();
        getMove(position, target, mesh, cellIndex, vertexIndex);
        target.divide(Unit.LENGTH);
        v.add(target);
    }
    
    public void apply(Mesh mesh) {
        //Vector rootSize = (mesh.rootCell.size);
        Cell[][][] base = mesh.baseLevel;
        IdentityHashMap<Vector, Boolean> checklist = new IdentityHashMap<Vector, Boolean>();
        Index index = new Index();
        Index side = new Index();
        
        // for all cells
        for (int i = 0; i < base.length; i++) {
            Cell[][] baseI = base[i];
            for (int j = 0; j < baseI.length; j++) {
                Cell[] baseIJ = baseI[j];
                for (int k = 0; k < baseIJ.length; k++) {
                    
                    Cell cell = baseIJ[k];
                    index.set(i, j, k);
                    if (cell != null) {

                        // for all vertices
                        for (int vi = 0; vi <= 1; vi++) {
                            for (int vj = 0; vj <= 1; vj++) {
                                for (int vk = 0; vk <= 1; vk++) {
                                    side.set(vi, vj, vk);
                                    
                                    Vector vertex = cell.getVertex(vi, vj, vk);
                                    //check if vertex has not yet been processed.
                                    if (checklist.get(vertex) == null) {
                                        this.transform(vertex, mesh, index, side);
                                        // check vertex as processed.
                                        checklist.put(vertex, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public final Homeomorphism translate(double x, double y, double z){
	return new TranslatedHomeomorphism(this, new Vector(x, y, z));
    }
    
    public final Homeomorphism translate(double x, double y){
	return new TranslatedHomeomorphism(this, new Vector(x, y, 0.0));
    }
    
    public final Homeomorphism scale(double r){
        return new TransformedHomeomorphism(this, new Vector[]{new Vector(r, 0.0, 0.0),
                                                       new Vector(0.0, r, 0.0),
                                                       new Vector(0.0, 0.0, r)});
    }
    
    public final Homeomorphism rotateZ(double angle){
	return new TransformedHomeomorphism(this, new Vector[]{new Vector(cos(angle), sin(angle), 0),
						       new Vector(-sin(angle), cos(angle), 0),
						       new Vector(0, 0, 1)});
    }

    public final Homeomorphism rotateY(double angle){
	return new TransformedHomeomorphism(this, new Vector[]{new Vector(cos(angle), 0, sin(angle)),
						       new Vector(0, 1, 0),
						       new Vector(-sin(angle), 0, cos(angle))});
    }
    
    public final Homeomorphism rotateX(double angle){
	return new TransformedHomeomorphism(this, new Vector[]{new Vector(1, 0, 0),
						       new Vector(0, cos(angle), sin(angle)),
						       new Vector(0, -sin(angle), cos(angle))});
    }
    
    public final Homeomorphism mirrorX(){
        return new TransformedHomeomorphism(this, new Vector[]{
                                                       new Vector(-1, 0, 0),
						       new Vector(0, 1, 0),
						       new Vector(0, 0, 1)});
    }
    
    public final Homeomorphism mirrorY(){
        return new TransformedHomeomorphism(this, new Vector[]{
                                                       new Vector(1, 0, 0),
						       new Vector(0, -1, 0),
						       new Vector(0, 0, 1)});
    }
    
    public final Homeomorphism mirrorZ(){
        return new TransformedHomeomorphism(this, new Vector[]{
                                                       new Vector(1, 0, 0),
						       new Vector(0, 1, 0),
						       new Vector(0, 0, -1)});
    }
    
    
    public final Homeomorphism combine(Homeomorphism other){
        return new CombinedHomeomorphism(this, other);
    }
}
