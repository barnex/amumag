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

 
public final class PerTime extends IoTab{

        private final double time;
        private int saveCount = 0;
        
        public PerTime(DataModel model, double time){
            super(model);
            this.time = time;
        }
  
        @Override
        public void update() throws IOException {
            if(outputManager.sim.totalTime / time >= saveCount){
                saveCount = (int) (outputManager.sim.totalTime / time)+1;
                model.incrementalSave(outputManager.getBaseDir());
            }
        }
    }