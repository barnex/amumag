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

import x.Poly;
import java.awt.Color;

public final class Wireframe extends Colorizer{
    
    private Color LINE_COLOR = new Color(0, 0, 0, 150);
    private Color FILL_COLOR = new Color(255, 255, 255, 150);

    public void colorize(Poly poly) {
        poly.color = FILL_COLOR;
	poly.lineColor = LINE_COLOR;
    }

}
