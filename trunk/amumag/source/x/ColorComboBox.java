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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package x;

import java.awt.Color;
import javax.swing.JComboBox;
import static java.awt.Color.*;
/**
 *
 * @author arne
 */
public class ColorComboBox extends JComboBox{

    public static final Color[] colors = new Color[]{
        WHITE,
        GRAY,
        BLACK,
        RED,
        YELLOW,
        GREEN,
        CYAN,
        BLUE};
    
    public static final String[] names = new String[]{
        "white",
        "gray",
        "black",
        "red",
        "yellow",
        "green",
        "cyan",
        "blue"};
    
    public ColorComboBox(){
        super(names);
    }
    
    public Color getColor(){
        return colors[getSelectedIndex()];
    }
}
