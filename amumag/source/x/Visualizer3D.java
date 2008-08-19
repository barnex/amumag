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
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Face;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import static java.lang.Math.*;
import java.util.Arrays;
import java.util.IdentityHashMap;
import static amu.geom.Vector.X;

public final class Visualizer3D {
      
    private Vector[] vertices;
    private Vector[] transformed;
    private Poly[] polygons;
    private Mesh mesh;
    
    private double scale;
    
    //Perspectief parameter: bepaalt hoe klein de dingen er uit zien.
    private double persp = 500;
    
    //Camera positie.
    private double camx, camy , camz=-1;
    private double phi, theta;							//0..2pi, -pi/2..pi/2
    
    //Huidige schermafmetingen
    public int width, height;
    
    //transformation matrix
    private double m11, m12, m13;
    private double m21, m22, m23;
    private double m31, m32, m33;
    
    private Colorizer colorizer;
    
    //__________________________________________________________________________
    
    public Visualizer3D(){
         width = 600;
        height = 600;
    }
    
    /**
     * Visualizer for the base level of mesh.
     */
    public Visualizer3D(Mesh mesh){
        this(mesh, mesh.nLevels - 1);
        //default settings
        //setColor(new Light(new Vector(10, 0, -100), Color.RED, 0.8));
       
    }
    
    /**
     * If lineColor == null, only outer faces will be drawn, else fillColor
     * should at least be semi transparent and all faces will be drawn.
     */
    public Visualizer3D(Mesh mesh, int level){
	setMesh(mesh, level);
    }
    
    public void setMesh(Mesh mesh){
        setMesh(mesh, mesh.nLevels-1);
    }
    
    public void setMesh(Mesh mesh, int level){
        this.mesh = mesh;
        this.scale = 1.0/mesh.boxSize.maxNorm();
	
        final IdentityHashMap<Vector, Vector> vertexPool = new IdentityHashMap<Vector, Vector>();
        final ArrayList<Poly> polys = new ArrayList<Poly>();
        
	for(Cell[][] levelI: mesh.levels[level])
	    for(Cell[] levelIJ: levelI)
		for(Cell cell: levelIJ)
		    if(cell != null)
			
			// for all face vertices
			for(int f=0; f<cell.faces.length; f++){
			    Face face = cell.faces[f];
			    if(face.sideness != 0){		// lineColor != null || 
				Poly p = new Poly(face, cell.normal[f], vertexPool);
				polys.add(p);
			    }
			}
	vertices = vertexPool.keySet().toArray(new Vector[]{});
	transformed = vertexPool.values().toArray(new Vector[]{});
	polygons = polys.toArray(new Poly[]{});

	System.gc();
	update();
    }
    
    public void setColor(Colorizer c){
	colorizer = c;
	updateColors();
    }
    
    public void paint(Graphics2D g){
        if(mesh == null)
            return;
        
	g.setColor(Color.WHITE);
	g.fillRect(0, 0, width, height);
	
	Polygon poly = new Polygon(new int[4], new int[4], 4);
	g.setStroke(new BasicStroke(1));
        
	for(Poly p: polygons){
	    
	    boolean inBack= false;
		for(int i=0; i<4; i++){
		    poly.xpoints[i] = (int) p.vertex[i].x;
		    poly.ypoints[i] = (int) p.vertex[i].y;
		    if(p.vertex[i].z < 0){
			inBack = true;
			break;
		    }
		}
		if(!inBack){
		    if(p.color != null){
			g.setColor(p.color);
			g.fill(poly);
		    }
		    if(p.lineColor != null){
			g.setColor(p.lineColor);
			g.draw(poly);
		    }
		}
	    }
    }
    
    public void update(){
	m11 = cos(phi);
	m12 = 0;
	m13 = -sin(phi);
	
	m21 = -sin(phi)*sin(theta);
	m22 = cos(theta);
	m23 = -cos(phi)*sin(theta);
	
	m31 = sin(phi)*cos(theta);
	m32 = sin(theta);
	m33 = cos(phi)*cos(theta);
	
	for(int i=0; i<vertices.length; i++){
	    transform(vertices[i], transformed[i]);
	}
	
	for(Poly p: polygons){
	    p.updateZ();
	    //p.updateLight();
	}
	Arrays.sort(polygons);
    }
    
    public void updateColors(){
        if(colorizer != null && mesh != null)
            for(Poly p: polygons)
                colorizer.colorize(p);
    }
    
    private void transform(Vector v, Vector t){
	//Translatie naar camera
	double x = scale * v.x;
	double y = scale * v.y;
	double z = scale * v.z;
	
	//Rotatie
	double xt = m11 * x + m12 * y + m13 * z - camx;
	double yt = (m21 * x + m22 * y + m23 * z) - camy;
	double zt = m31 * x + m32 * y + m33 * z - camz;
	
	//Aanpassen aan scherm + perspectief
	t.x = ((xt/zt)*persp) + width/2;
	t.y = ((yt/zt)*persp) + height/2;
	t.z = zt;
    }
    
    public void rotateCamera(double dPhi, double dTheta){
	phi += dPhi;
	phi %= 2*PI;
	theta += dTheta;
	/*if(theta > PI)
	    theta -= 2*PI;
	else if(theta < -PI)
	    theta += PI;*/
	setCameraDirection(phi, theta);
    }
    
    public void setCameraDirection(double phi, double theta){
	this.phi = phi;
	this.theta = theta;
	
	//System.out.println("theta=" + theta + ", phi=" + phi);
	update();
    }
    
    public void moveCamera(double dx, double dy, double dz){
	setCameraPosition(camx + dx, camy + dy, camz + dz);
	//System.out.println("movecam");
    }
    
    public void setCameraPosition(double x, double y, double z){
	camx = x;
	camy = y;
	camz = z;
	//System.out.println("camx=" + camx + ", camy=" + camy + ", camz=" + camz);
	update();
    }
    
     
}
