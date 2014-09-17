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

package amu.io;

import amu.data.DataModel;
import java.io.IOException;

public final class PerIterations extends IoTab{

        private final int divider;
        
        public PerIterations(DataModel model, int divider){
            super(model);
            this.divider = divider;
        }
        
        @Override
        public void update() throws IOException {
            if(outputManager.sim.getTotalIteration() % divider == 0)
                model.incrementalSave(outputManager.getBaseDir());
        }
    
    }