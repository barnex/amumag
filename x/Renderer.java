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

import amu.geom.Mesh;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import static java.lang.Math.*;
import amu.data.DataModel;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import static amu.geom.Vector.X;

public abstract class Renderer {

    protected DataModel model;
    public ColorMap colorMap;
    protected Mesh mesh;
    /** Size (in pixels) of the drawing area. */
    protected int width, height;
    
    public Color background = Color.WHITE;
    protected String message = "";

    //public abstract void colorSurface();
    
    public final DataModel getModel() {
        return model;
    }

    public void moveCamera(double x, double y, double z) {
        //ignore (overridden by 3D renderer)
    }

    public void rotateCamera(double phi, double theta) {
       //ignore (overridden by 3D renderer)
    }

    public void setCameraDirection(double phi, double theta) {
        //ignore (overridden by 3D renderer)
    }
    
    public abstract void setModel(DataModel model) throws IOException;
    
    public abstract void paint(Graphics2D g, int width, int height) throws IOException;
            
    public abstract void setColorMap(ColorMap map) throws IOException;

    public void savePng(File file, double xmin, double xmax, double ymin, double ymax) throws IOException{
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D)(img.getGraphics());
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paint(graphics, width, height);
        ImageIO.write(img, "png", file);
    }
    
    public void savePng(File file) throws IOException{
        savePng(file, 0, 0, 0, 0);
    }

    public void updateColors() throws IOException {
        // overridden by 3D renderer.
    }



  void setMessage(String string) {
    this.message = string;
  }
}
