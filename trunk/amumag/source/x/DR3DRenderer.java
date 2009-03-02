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
import amu.geom.Vector;
import amu.mag.Cell;
import amu.mag.Face;
import amu.core.Index;
import amu.io.Message;
import amu.io.Message;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.IOException;
import java.util.ArrayList;
import static java.lang.Math.*;
import java.util.Arrays;
import java.util.IdentityHashMap;
import amu.data.DataModel;
import java.awt.Stroke;
import java.io.File;
import static amu.geom.Vector.X;


public class DR3DRenderer extends Renderer{
      
    //(pooled) Vertices of the 3D Object.
    private Vector[] vertices;
    
    // Vertices after transformation for the camera position.
    private Vector[] transformedVertices;
    
    // Definition of the polygons (with color) to be drawn between the transformed vertices.
    private Poly[] polygons;
    
    //Perspective Parameter, bigger value = smaller looking objects.
    private double persp = 700;
    private double scale;
    
    //Camera position.
    private double camx, camy , camz=-2;
    public double phi, theta;			//0..2pi, -pi/2..pi/2
    
    //transformation matrix
    private double m11, m12, m13;
    private double m21, m22, m23;
    private double m31, m32, m33;
    
    //default colors when none specified
    public static final Color LINE_COLOR = Color.BLACK;
    public static final Color FILL_COLOR = null;
    
    //One polygon to be re-used many times for drawing each polygon of the 3D object
    private final Polygon POLYGON_BUFFER = new Polygon(new int[3], new int[3], 3);
    private final Stroke STROKE = new BasicStroke(1);
    
    //__________________________________________________________________________
    
    public DR3DRenderer(){
    
    }

    public DR3DRenderer(DataModel model) throws IOException {
        this.model = model;
        setPolys(model.getMesh());
        surface();
        //();
    }
    
    // sets the data but not the mesh.
    // rather poor performace seems to be caused by the overhead of the many
    // get / put invocations of the underlying data models...
    public void setModel(DataModel model) throws IOException{
        Message.debug("renderer.setModel " + model);
        this.model = model;
        updateColors();
    }
    
    public void setPolys(Mesh mesh){
       Message.debug("setPolys");
            
       scale = 1.0/mesh.boxSize.maxNorm();
	
       final IdentityHashMap<Vector, Vector> vertexPool = new IdentityHashMap<Vector, Vector>();
       final ArrayList<Poly> polys = new ArrayList<Poly>();
        
        for(int i=0; i<mesh.baseLevel.length; i++){
            for(int j=0; j<mesh.baseLevel[i].length; j++){
                for(int k=0; k<mesh.baseLevel[i][j].length; k++){
                    Cell cell = mesh.baseLevel[i][j][k];
                    if(cell != null){
                        // for all face vertices
			for(int f=0; f<cell.faces.length; f++){
			    Face face = cell.faces[f];
			    if(face.sideness != 0){
                                for(int triangle=0; triangle<4; triangle++){
                                    Poly p = new Poly(face, cell.normal[f], vertexPool, triangle);
                                    p.index = new Index(i, j, k);
                                    polys.add(p);
                                }
			    }
			}
                    }
                }
            }
        }
			
	vertices = vertexPool.keySet().toArray(new Vector[]{});
	transformedVertices = vertexPool.values().toArray(new Vector[]{});
	polygons = polys.toArray(new Poly[]{});
        
	System.gc();
	update();
    }
    
    @Override
    public void updateColors() throws IOException{      
        setColorMap(colorMap);
    }
    
    //incorporate colormap?
    public void setColorMap(ColorMap map) throws IOException {
        Vector buffer = new Vector();
        this.colorMap = map;
        if (map == null) {
            surface();
        }
        else{
            //this.colorMap = map;
            if (model.isVector())
                map = new ColorMap3D();

            for (Poly p : polygons) {
                model.put(-1, p.index, buffer);
                p.color = map.get(buffer);
                //p.lineColor = p.color;
                p.lineColor = null;
            } 
        }
    }

    private static final Color softwhite = new Color(255, 255, 255);
    
    public void surface(){
        surface(Color.WHITE, 0.9);
    }
    
    public void litLines(){
        litLines(new Color(200, 200, 200), 1.0);
    }
    
    public void litLines(Color fill, double contrast){
        surface(fill, contrast);
        for (Poly p : polygons) {
            p.color = Color.WHITE;
        }
    }
    
    public void surface(Color fill, double contrast) {
        Vector lamp = new Vector(model.getMesh().boxSize);

        lamp.x = lamp.maxNorm() / 2;
        lamp.y = 0;
        lamp.z = lamp.maxNorm();
        lamp.multiply(2);

        Light light = new Light(lamp, fill, contrast);

        for (Poly p : polygons) {
            light.colorize(p);
            p.lineColor = p.color;
        }

    }
    
    public void surfaceWireframe(Color fill, double contrast){
        surface(fill, contrast);
         for (Poly p : polygons) {
            p.lineColor = new Color(p.color.getRed() / 2,
                    p.color.getGreen() / 2,
                    p.color.getBlue() / 2);
        }
    }
    
    public void wireframe() {
        wireframe(null, Color.BLACK);
    }

    public void wireframe(Color fill, Color line){
        for (Poly p : polygons) {
            p.color = fill;
            p.lineColor = line;
        }
    }
    
 
    
    public void save(File file) throws IOException{   
        width = 4096;
        height = 4096;
        update();
        
        // calc crop box here.
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for(Poly p: polygons)
            for(Vector v: p.vertex){
                if(v.x < xmin)
                    xmin = v.x;
                if(v.x > xmax)
                    xmax = v.x;
                if(v.y < ymin)
                    ymin = v.y;
                if(v.y > ymax)
                    ymax = v.y;
            }
            
       savePng(file, xmin, xmax, ymin, ymax);
       //todo: png.
    }
    
    public void paint(Graphics2D g, int width, int height){
    
        //clear
        if(background != null){
            g.setColor(background);
            g.fillRect(0, 0, width, height);
        }
                
        if(model == null)
            return;        
        
        if(width != this.width || height != this.height){
            this.width = width;
            this.height = height;
            update();
        }

	g.setStroke(STROKE);
        
	for(Poly p: polygons){    
            //System.out.print('*');
            //08-05-15
            if(Thread.currentThread().isInterrupted())
                break;
            
	    if(!p.inBack && p.transformedNormal.z > 0){
		for(int i=0; i<POLYGON_BUFFER.xpoints.length; i++){
		    POLYGON_BUFFER.xpoints[i] = (int) p.vertex[i].x;
		    POLYGON_BUFFER.ypoints[i] = (int) p.vertex[i].y;
		}
                if (p.color != null) {
                    g.setColor(p.color);
                    g.fill(POLYGON_BUFFER);
                }
                if (p.lineColor != null) {
                    g.setColor(p.lineColor);
                    g.draw(POLYGON_BUFFER);
                }
            }
        }
    }
    
    //updatecamera.
    public void update(){
        
        if(model == null)
            return;
        
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
	    transform(vertices[i], transformedVertices[i]);
	}
	
	for(Poly p: polygons){
            transform(p.normal, p.transformedNormal);
	    p.updateZ();
	}
	Arrays.sort(polygons);
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
	setCameraDirection(phi, theta);
    }
    
    public void setCameraDirection(double phi, double theta){
	this.phi = phi;
	this.theta = theta;
	update();
    }
    
    public void moveCamera(double dx, double dy, double dz){
	setCameraPosition(camx + dx, camy + dy, camz + dz);
    }
    
    public void setCameraPosition(double x, double y, double z){
	camx = x;
	camy = y;
	camz = z;
	update();
    }
}
