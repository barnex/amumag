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
import amu.geom.Mesh;
import amu.geom.Vector;

public class ToplayerHelix extends Helix{

    /*
    * ToplayerHelix-like deformation
    * @param stepSize height of the jump
    * @param phase start phase of the ToplayerHelix
    * @param steps number of steps to be made
    * @param hang width (in radians) of the step, avoids extremely steep edges.
    */
    public ToplayerHelix(double stepSize, double hang, double phase, double center, int steps){
        super(stepSize, hang, phase, center, steps);
    }
    
    public ToplayerHelix(double stepSize, double hang, double phase, double center){
        this(stepSize, hang, phase, center, 1);
    }
    
    public ToplayerHelix(double stepSize, double hang, double phase){
        this(stepSize, hang, phase, 0);
    }
    
    public ToplayerHelix(double stepSize, double hang){
        this(stepSize, hang, 0.0);
    }
    
    public ToplayerHelix(double stepSize){
        this(stepSize, 0.0);
    }
    
    
    @Override
    public void getMove(Vector r, Vector target) {
        throw new UnsupportedOperationException("Unused");
    }

    @Override
    public void getMove(Vector r, Vector target, Mesh mesh, Index cellIndex, Index vertexIndex) {
        int z = cellIndex.z + vertexIndex.z;
        
        int maxZ = mesh.baseLevel[0][0].length;
        if(z == 0 || z == maxZ)
            super.getMove(r, target);
        else
            target.set(0, 0, 0);
    }
    
    public String toString(){
       return "Toplayer" + super.toString();
    }
}