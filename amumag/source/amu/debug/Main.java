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


package amu.debug;


public class Main {

    public static void main(String[] args) throws Exception{
        amu.mag.Main.main(new String[]{"amu.testproblems.Test"});
        
        /*DataModel diff = new Differential(new File("rftoplayerrough3d20.0nm50.0nm-ramp.amu"), "m", 32);
        AtTime at = new AtTime(new ZAverage(diff), 0);
        
        for(int i=0; i < diff.getTimeDomain(); i++){
            at.setTime(i);
            at.incrementalSaveDir(null, new File("diff"));
        }//*/
    }
}