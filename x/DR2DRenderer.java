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
import amu.core.Interpolator;
import amu.data.Scalarizer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.IOException;
import static java.lang.Math.*;
import amu.data.DataModel;
import java.awt.Stroke;
import amu.mag.Cell;
import amu.mag.Face;
import amu.mag.Face;
import amu.core.Interpolator;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.core.Index;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.Graphics2D;
import java.io.File;
import static java.lang.Math.*;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import static amu.core.Index.UNIT;

public final class DR2DRenderer extends Renderer{
    
    /** The view direction, can be X, Y or Z. */
    private int view;
    
    /** The selected Cell, whose properties are shown in more detail. */
    private Cell selectedCell = null;
    
    /** Index of the axis corresponding to the direction on the screen.
     * Can be X, Y or Z.
     */
    private int screenX, screenY, screenZ;
    private int[] screenXY = new int[2];
    
    /** Interpolator used to transform points to screen coordinates.*/
    private Interpolator xInt, yInt;
    
    /** The discretization level to be shown. */
    private int level;
    
    /** The position along the view direction to be shown.*/
    private int slice;
    private double sliceZ;
    
    public Color cellColor = new Color(210, 210, 210);
    public Stroke cellStroke = new BasicStroke();
    
    private static int ALPHA = 100;
    public Color partnerColor = new Color(0, 0, 255, ALPHA);
    public Color parentPartnerColor = new Color(0, 255, 0, ALPHA);
    public Color nearColor = new Color(255, 0, 0, ALPHA);
    public Color selectedColor = new Color(255, 255, 0, ALPHA);
    
    public int faceWidth = 1;
    public Stroke faceStroke = new BasicStroke(faceWidth*2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
    private ColorMap faceColorMap = new ColorMap(1.0, Color.BLUE, Color.BLACK, Color.RED);
    
    public Stroke magStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private ColorMap magColorMap = new ColorMap(1.0);
    
    private ColorMap magCompColorMap = new ColorMap(1.0, Color.BLACK, Color.GRAY, Color.WHITE);
    
    /** Inset (in pixels) around the image. */
    private static final int marge = 10;
    
    //__________________________________________________________________________
    
    /** Axis in the simulation to be used on the screen for a given view direction.*/
    private static final int[]
            dirForXAxis = new int[]{Y, X, X},
            dirForYAxis = new int[]{Z, Z, Y},
            dirForZAxis = new int[]{X, Y, Z};
        
    private int componentToColor = Z;
    private boolean drawCells = false;
    private boolean drawFaces = true;
    private boolean drawVector = true;
    
    //__________________________________________________________________________
    
    
    public DR2DRenderer(DataModel model) throws IOException{
        setModel(model);
        setSize(256, 256);
    }
    
     /** Sets the discretization level to be shown. */
    public void setLevel(int level){
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    /** Sets the viewing direction and the slice position along that direction. */
    public void setView(int view, int cut){
        if(view < 0 || view > 2)
            throw new IllegalArgumentException("view");
        if(!isInside(UNIT[view].multiply(cut)))
            throw new IllegalArgumentException("outside view");
        this.view = view;
        this.slice = cut;
        screenX = dirForXAxis[view];
        screenY = dirForYAxis[view];
        screenZ = dirForZAxis[view];
	
	screenXY[0] = screenX;
	screenXY[1] = screenY;
		
	Index start = UNIT[view].multiply(slice);
	// problem now that the mesh is not finite difference anymore: "prototype"
	// cell may not exist anymore (cut out), and its size may not represent
	// the thickness of a slice. a true 3D view is needed.
        //sliceZ = levels[level][start.x][start.y][start.z].position.getComponent(screenZ);
        sliceZ = (slice + 0.5) * mesh.rootCell.size.getComponent(screenZ);
    }
    
    /** Sets the slice position along the viewing direction. */
    public void setSlice(int slice) {
        setView(view, slice);
    }
    
    public int getSlice(){
        return slice;
    }
    
    /** Sets the viewing direction. */
    public void setView(int view){
        setView(view, slice);
    }
    
    public int getView() {
        return view;
    }
    
    //__________________________________________________________________________
    
    /** Sets the size of the drawing area. */
    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
        
        Vector size = mesh.rootCell.getSize();
        double sizeX = size.getComponent(screenX);
        double sizeY = size.getComponent(screenY);
        //double sizeZ = size.getComponent(screenZ);
        xInt = new Interpolator(-sizeX/2.0, marge, sizeX/2.0, width-2*marge);
        yInt = new Interpolator(-sizeY/2.0, marge, sizeY/2.0, height-2*marge);
        //zInt = new Interpolator(-sizeZ/2.0, marge, sizeZ/2.0,
        
        // preserve aspect ratio, also in Z-direction;
        if(xInt.a > yInt.a)
            xInt = new Interpolator(yInt.a, xInt.b);
        else if(yInt.a > xInt.a)
            yInt = new Interpolator(xInt.a, yInt.b);
    }
    
    public void setSelectedCell(int level, int x, int y, int z){
        selectedCell = mesh.levels[level][x][y][z];
    }
    
    public void setSelectedCell(int mouseX, int mouseY){
	Interpolator 
		iInt = xInt.inverse(),
		jInt = yInt.inverse();
	
	Vector size = mesh.levels[level][0][0][0].getSize();
	double x = iInt.transf(mouseX) / size.getComponent(screenX);
	double y = jInt.transf(mouseY) / size.getComponent(screenY);
	Index index = new Index();
	index.setComponent(screenX, (int)x);
	index.setComponent(screenY, (int)y);
	index.setComponent(screenZ, slice);
	
	try{
	    System.out.println("select " + level + " " + index.x + " " + index.y + " " + index.z);
	    selectedCell = mesh.levels[level][index.x][index.y][index.z];
	}
	catch(ArrayIndexOutOfBoundsException e){
	    System.out.println(e + ": " + index);
	}
    }
    
    //__________________________________________________________________________
    
    /** Draws the geometry. */
    public void paint(Graphics2D g, int width, int height) throws IOException{
        setSize(width, height);
        //make the Y-axis point upwards
        g.translate(0, height);
        g.scale(1, -1);
        
        if(background != null){
            g.setColor(background);
            g.fillRect(0, 0, width, height);
        }
        
        Index start = UNIT[view].multiply(slice);
        Index r1 = UNIT[screenX],
                  r2 = UNIT[screenY];
        
        Index j = new Index();
        if(drawCells){
            for(Index i = start; isInside(i); i=i.sum(r1)){
                for(j.set(i); isInside(j); j=j.sum(r2)){
                    Cell c = mesh.levels[level][j.x][j.y][j.z];
		    if(c != null)
			drawCell(g, c);
                }
            }
        }
      
        for(Index i = start; isInside(i); i=i.sum(r1)){
	    for(j.set(i); isInside(j); j=j.sum(r2)){
		Cell c = mesh.levels[level][j.x][j.y][j.z];
		if(c != null){
		    if(componentToColor > -1){
                        //todo:put
			g.setColor(magCompColorMap.get(model.get(-1, i).getComponent(componentToColor)));
			setCellShape(c);
			g.fill(cellShape);
                    }
		    if(drawVector)
			drawNormalizedArrow(g, c);
		    if(drawFaces)
			drawFaces(g, c);
		}
	    }
	}
	
	//drawSelection(g);
    }
    
    /*private void drawSelection(Graphics2D g){
	if(selectedCell != null){
	    if(paint[PARTNERS]){
		//paint partners
		g.setColor(partnerColor);
		for(Cell c: selectedCell.getPartners())
		    fillCell(g, c);
	    }
	    
	    if(paint[NEAR]){
		//paint nearCells
		g.setColor(nearColor);
		for(Cell c: selectedCell.getNearCells())
		    fillCell(g, c);
	    }
	    
	    if(paint[PARENTPARTNERS]){
		//paint parent's partners
		for(Cell cell = selectedCell.parent; cell != null; cell = cell.parent){
		    g.setColor(parentPartnerColor);
		    for(Cell c: cell.getPartners())
			fillCell(g, c);
		}
	    }
	    
	    g.setColor(selectedColor);
	    fillCell(g, selectedCell);
	}
    }*/
    
    //__________________________________________________________________________
    
    /** Checks if the indices lie inside the bounds of the level to be shown. */
    private boolean isInside(Index v){
        return v.x < mesh.levels[level].length
                && v.y < mesh.levels[level][0].length
                && v.z < mesh.levels[level][0][0].length;
    }
    
    //__________________________________________________________________________
    
    /** Shape for drawing cell outlines. */
    private final Rectangle2D.Double cellShape = new Rectangle2D.Double();
    private final Ellipse2D.Double vertexShape = new Ellipse2D.Double();
    private final RoundRectangle2D.Double roundCellShape = new RoundRectangle2D.Double();
    
    public Color getBackground() {
        return background;
    }
    
    /** Draws the cell "outline" */
    private void drawCell(Graphics2D g, Cell cell){
        /**g.setColor(cellColor);
        g.setStroke(cellStroke);
        setCellShape(cell);
        g.paint(cellShape);*/
	g.setColor(cellColor);//todo: move up
	g.setStroke(cellStroke);//idem
	int centerX = (int)x(cell.center);
	int centerY = (int)y(cell.center);
	g.drawLine(centerX-2, centerY, centerX+2, centerY);
	g.drawLine(centerX, centerY-2, centerX, centerY+2);
	for(Vector v: cell.vertex){
	    int x = (int)x(v);
	    int y = (int)y(v);
	    vertexShape.setFrame(x-2, y-2, 4, 4);
	    g.fill(vertexShape);
	}
    }
    
    /** Fills the cell. */
    private void fillCell(Graphics2D g, Cell cell){
	//double z = levels[level][0][0][0].getPosition().getComponent(screenZ);
	//if(abs(cell.position.getComponent(screenZ) - sliceZ) < cell.getSize().getComponent(screenZ)/2.0) {
	    setRoundCellShape(cell);
	    g.fill(roundCellShape);
	//}
    }
    
    private void setCellShape(Cell cell){
        Vector pos = cell.center;
        Vector size = cell.getSize();
        double x = pos.getComponent(screenX);
        double y = pos.getComponent(screenY);
        double w = size.getComponent(screenX)/2.0;
        double h = size.getComponent(screenY)/2.0;
        cellShape.setRect(x(x-w), y(y-h), x(x+w)-x(x-w)+1, +y(y+h)-y(y-h)+1);       //make efficient
    }
    
    private void setRoundCellShape(Cell cell){
        Vector pos = cell.center;
        Vector size = cell.getSize();
        double x = pos.getComponent(screenX);
        double y = pos.getComponent(screenY);
        double w = size.getComponent(screenX)/2.0;
        double h = size.getComponent(screenY)/2.0;
        roundCellShape.setRoundRect(x(x-w)+1, y(y-h)+1, x(x+w)-x(x-w)-2, +y(y+h)-y(y-h)-2, 4, 4);       //make efficient
    }
    
    //__________________________________________________________________________
    
    /** Shape for drawing faces. */
    private final int[] xPoints = new int[4], yPoints = new int[4];
    private final Polygon facePoly = new Polygon(xPoints, yPoints, 4);
    
    /** Draws the faces of the cell. */
    private void drawFaces(Graphics2D g, Cell cell){
        g.setStroke(faceStroke);
        for(int dir: screenXY){
	  for(int side = 0; side <= 1; side++){
	       Face face = cell.getFace(dir, side);
	       g.setColor(faceColorMap.get(face.getChargeDensity()));
	       for(int i=0; i<4; i++){
		    xPoints[i] = (int) (x(face.vertex[i].getComponent(screenX)) + 0.5);	    //nicely round off
		    yPoints[i] = (int) (y(face.vertex[i].getComponent(screenY)) + 0.5);
	       }
	       g.draw(new Polygon(xPoints, yPoints, 4)); //make efficient.
	   }
        }
    }
 
    
    //__________________________________________________________________________
    
    private final Line2D.Double arrow = new Line2D.Double();
    private final Line2D.Double head1 = new Line2D.Double(),
            head2 = new Line2D.Double();
    
    //private final Vector v = new Vector();
    
    /** Draws the vector given by type, normalized to unit length.
     * The unit length is given by the cell size.
     */
    private void drawNormalizedArrow(Graphics2D g, Cell cell){
        
        Vector v = cell.dataBuffer;
        v.multiply(1.0/v.norm());
        g.setColor(magColorMap.get(v.getComponent(screenZ)));
         
        Vector size = cell.getSize();
        Vector pos = cell.center;
        
        double x = pos.getComponent(screenX);
        double y = pos.getComponent(screenY);
        double w = size.getComponent(screenX)/2.0;
        double h = size.getComponent(screenY)/2.0;
        
        double size_ = 0.8 * min(w, h);      // * (v.getComponent(screenZ)) = bad;  //small (20%) margin around the arrow
        v.multiply(size_);
        
        double vx = v.getComponent(screenX);
        double vy = v.getComponent(screenY);
        double vz = v.getComponent(screenZ);
        
        arrow.setLine(x(x-vx), y(y-vy), x(x+vx), y(y+vy));
        head1.setLine(x(x+vx), y(y+vy), x(x+vy*0.2), y(y-vx*0.2));  //arrowheads
        head2.setLine(x(x+vx), y(y+vy), x(x-vy*0.2), y(y+vx*0.2));
        
        g.setStroke(magStroke);
        g.draw(arrow);
        g.draw(head1);
        g.draw(head2);
    }
    
    //__________________________________________________________________________
    
    /** Returns the component of the vector in the screenX direction, transformed
     * to screen coordinates.*/
    private final double x(Vector v){
        return xInt.transf(v.getComponent(screenX));
    }
    
    /** Returns the component of the vector in the screenY direction, transformed
     * to screen coordinates.*/
    private final double y(Vector v){
        return yInt.transf(v.getComponent(screenY));
    }
    
    /** Transforms an x coordinate to a screen coordiante.*/
    private final double x(double xSim){
        return xInt.transf(xSim);
    }
    
    /** Transforms an y coordinate to a screen coordiante. */
    private final double y(double ySim){
        return yInt.transf(ySim);
    }

    @Override
    public void setModel(DataModel model) throws IOException {
        this.model = model;
        this.mesh = model.getMesh();
        level = mesh.nLevels - 1;
        setView(Z, 0);
    }

    @Override
    public void setColorMap(ColorMap map) throws IOException {
        this.colorMap = map;
    }

    public void colorSurface() {
        //draw cells = false?
    }
}
        