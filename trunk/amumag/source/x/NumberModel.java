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

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static java.lang.Math.*;

/**
 *
 */
public final class NumberModel extends SpinnerNumberModel implements ChangeListener{
    
    public NumberModel(){
        super(0.0, 0.0, Double.POSITIVE_INFINITY, 1.0);
        addChangeListener(this);
    }

    public void stateChanged(ChangeEvent e){
        double value = getNumber().doubleValue();
        int step = 1;
        while(step <= value && step < 1000000000)
            step *= 10;
        setStepSize(((double)step)/10.0);    
    }
}
