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

import amu.data.Extremum;
import amu.data.AtTime;
import amu.data.Component;
import amu.data.DataModel;
import amu.data.Norm;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import refsh.Interpreter;
import refsh.RefSh;
import amu.data.SavedDataModel;
import amu.data.Scalarizer;
import x.ColorMap;
import x.DR2DRenderer;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import refsh.Interpreter;
import refsh.RefSh;
import x.DR3DRenderer;
import x.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileFilter;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import static amu.geom.Vector.*;
        
public abstract class AbstractMain {
    
    protected AtTime timeProbedData;
    protected Scalarizer scalarTimeProbedData;
    protected DataModel model;
    
    protected Renderer renderer;
    protected View view;
    protected JFrame frame;
    
    // the quantity being shown;
    protected String quantity = null;
    
    // used for interpreting commands from the menus (not command the
    // command line.
    protected RefSh refsh;
    
    public AbstractMain() throws IOException{  
        view = new View();
        frame = new JFrame("amumag viewer");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view, BorderLayout.CENTER);
        frame.getContentPane().add(view.scroller, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(600, 600);
  
        refsh = new RefSh(new Interpreter(getClass(), this));
    }
    
    
    
    public void time(int time) throws IOException{
        timeProbedData.setTime(time);
        renderer.updateColors();
        repaint();
    }
    
    public int time(){
        return timeProbedData.getTime_();
    }
    
    public void quantity(String scalar) throws IOException{
        if(scalar.equals("x")){
            scalarTimeProbedData = (new Component(timeProbedData, X));
            model = scalarTimeProbedData;
            renderer.setModel(model);
        }
        else if(scalar.equals("y")){
            scalarTimeProbedData = (new Component(timeProbedData, Y));
             model = scalarTimeProbedData;
            renderer.setModel(model);
        }
        else if(scalar.equals("z")){
            scalarTimeProbedData = (new Component(timeProbedData, Z));
             model = scalarTimeProbedData;
            renderer.setModel(model);
        }
        else if(scalar.equals("norm")){
            scalarTimeProbedData = (new Norm(timeProbedData));
             model = scalarTimeProbedData;
            renderer.setModel(model);
        }
        else if(scalar.equals("vector")){
            model = timeProbedData;
            renderer.setModel(model);
            //renderer.setColorMap(null);
        }
        else if(scalar.equals("none")){
            renderer.colorSurface();
        }
        else
            throw new IllegalArgumentException("\"quantity\" must be one of: x, y, z, norm, vector, none.");
        this.quantity = scalar;
        updateMenubar();
        repaint();
    }
    
    public void colormap(String a, String b, String c) throws IOException{
        renderer.colorMap.setColors(color(a), color(b), color(c));
        renderer.updateColors();
        repaint();
    }
    
    public void type(String dim) throws IOException{
        if(dim.startsWith("2"))
            renderer = new DR2DRenderer(model);
        else if(dim.startsWith("3"))
            renderer = new DR3DRenderer(model);
        else
            throw new IllegalArgumentException("Type must be either 2D or 3D");
        repaint();
    }
    
    public void scale(double min, double max) throws IOException{
        renderer.colorMap.setValues(min, max);
        renderer.updateColors();
        repaint();
    }
    
    public String scale(){
        return renderer.colorMap.getMin() + ", " + renderer.colorMap.getMax();
    }
    
    public String autoscale() throws IOException{
        Extremum probe = new Extremum(scalarTimeProbedData, Extremum.MIN);
        double min = probe.getDouble( -1,null);//already time independend (t=-1) because of time probe
        probe = new Extremum(scalarTimeProbedData, Extremum.MAX);
        double max = probe.getDouble( -1,null);
        scale(min, max);
        return scale();
    }
    
    
    public void phi(double phi) throws IOException{
        renderer.setCameraDirection(phi, theta());
        repaint();
    }
    
    public double phi(){
        if(renderer instanceof DR3DRenderer)
            return ((DR3DRenderer)renderer).phi;
        else
            return 0.0;
    }
    
    public void theta(double theta) throws IOException{
        renderer.setCameraDirection(phi(), theta);
        repaint();
    }
    
    public double theta(){
        if(renderer instanceof DR3DRenderer)
            return ((DR3DRenderer)renderer).theta;
        else
            return 0.0;
    }
    
    public void save(String file) throws IOException{
        renderer.save(new File(file));
    }
    
    public void exit(){
        System.exit(0);
    }
    
    
    public void repaint() throws IOException {
       frame.setVisible(true);
       frame.getContentPane().repaint();
    }
    
    protected void updateMenubar(){
        
    }
    
    protected static final Hashtable<String, Color> colorHash = new Hashtable<String, Color>();
    static{
        colorHash.put("white", Color.WHITE);
        colorHash.put("gray", Color.GRAY);
        colorHash.put("black", Color.BLACK);
        colorHash.put("red", Color.RED);
        colorHash.put("green", Color.GREEN);
        colorHash.put("blue", Color.BLUE);  
    }
    protected Color color(String name){
        if(!colorHash.containsKey(name))
                throw new IllegalArgumentException("Color not defined: " + name);
        else
            return colorHash.get(name);
    }
    
    
    
    protected class RefAction extends AbstractAction{

        protected String command;
        
        public RefAction(String name, String command){
            super(name);
            this.command = command;
        }
        
        public void actionPerformed(ActionEvent e) {
            refsh.interpretSafe(command);
        }   
    }
    
    protected class View extends JPanel implements MouseListener, MouseMotionListener, KeyListener{
        
        protected JScrollBar scroller;
        public double mouseSensitivity = 0.01;
    
        public View() {
            setFocusable(true);
            addMouseListener(this);
            addMouseMotionListener(this);
            addKeyListener(this);
            initScroller();
        }   
    
        protected void initScroller() {
        scroller = new JScrollBar(JScrollBar.VERTICAL);
        scroller.setMinimum(0);
        scroller.setVisibleAmount(1);
        scroller.setMaximum(1);
        scroller.addAdjustmentListener(new AdjustmentListener(){
            public void adjustmentValueChanged(AdjustmentEvent e){
                    try {
                        time(scroller.getValue());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        });
    }
        
        @Override
        public void paintComponent(Graphics g1) {

            Graphics2D g = (Graphics2D) g1;
            // 08-05-15: disabling antialiasing and opting for render_speed makes
            // a HUGE difference in speed on linux. This does not seem to be
            // the case on macOSX. Also, sun-java6-sdk is a lot faster here than open-jdk.
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize();
            int w = d.width;
            int h = d.height;
            if (renderer != null) {
                try {
                    renderer.paint(g, w, h);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                super.paintComponent(g1);
            }
        }

        protected int downX, downY;
 
        public void mousePressed(MouseEvent e){
	    downX = e.getX();
	    downY = e.getY();
	}
	public void mouseReleased(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}
	
	public void mouseDragged(MouseEvent e){
	    int upX = e.getX();
	    int upY = e.getY();
	    renderer.rotateCamera((upX-downX)*mouseSensitivity,
		    -(upY-downY)*mouseSensitivity);
	    downX = upX;
	    downY = upY;
	    repaint();
	}
	
	//Hoeveel bewogen wordt per key-press.
	protected double D = 0.2;
	
	public void keyPressed(KeyEvent e){
	    //System.out.println("key");
	    int key = e.getKeyCode();
	    switch(key){
		case KeyEvent.VK_LEFT: renderer.moveCamera(-D, 0, 0); repaint(); break;
		case KeyEvent.VK_RIGHT: renderer.moveCamera(D, 0, 0); repaint(); break; //rotateWorld(-PI/128); repaint(); break;
		case KeyEvent.VK_UP: renderer.moveCamera(0, 0, D); repaint(); break;
		case KeyEvent.VK_DOWN: renderer.moveCamera(0, 0, -D); repaint(); break;
		case KeyEvent.VK_C: renderer.moveCamera(0, -D, 0); repaint(); break;
		case KeyEvent.VK_SPACE: renderer.moveCamera(0, D, 0); repaint(); break;
		default: break;
	    }
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
    }
}
