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

import java.util.Random;

/**
 * Superclass for all homeomorphism using random numbers, always uses the same
 * seed so that geometries are reproducible.
 * @author arne
 */
abstract class RandomHomeomorphism extends Homeomorphism{

    protected final Random random;

    public RandomHomeomorphism(){
        random = new Random(0);
    }
    
    public RandomHomeomorphism(int seed){
        random = new Random(seed);
    }
    
    protected double random(){
        return random.nextDouble();
    }
}