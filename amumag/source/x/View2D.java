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

//package amu.x;
//import amu.mag.Simulation;
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.event.ComponentListener;
//import java.awt.event.ComponentEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.border.TitledBorder;
//
///**
// * A Canvas that uses a Visualizer to display a cell geometry.
// */
//public final class View2D extends JPanel implements ComponentListener, MouseListener{
//    
//    private Visualizer2D visualizer;
//    
//    public View2D(Simulation sim){
//        this(new Visualizer2D(sim.mesh));
//    }
//    
//    public View2D(Visualizer2D v){
//        this.visualizer = v;
//        addComponentListener(this);
//	//addMouseListener(this);
//    }
//    
//    public JFrame showFrame(){
//        JFrame frame = new JFrame("2D view");
//        frame.setLayout(new BorderLayout());
//        frame.getContentPane().add(this, BorderLayout.CENTER);
//        Controller c = new Controller(this);
//        c.setBorder(new TitledBorder("controller"));
//        frame.getContentPane().add(c, BorderLayout.WEST);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setSize(800, 600);
//        frame.setVisible(true);
//        return frame;
//    }
//    
//    @Override
//    public void paintComponent(Graphics g1){
//        Graphics2D g = (Graphics2D) g1;
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        Dimension d = getSize();
//        int w = d.width;
//        int h = d.height;
//        visualizer.draw(g);
//    }
//
//    public void componentResized(ComponentEvent e){
//        Dimension d = getSize();
//        visualizer.setSize(d.width, d.height);
//    }
//
//    public Visualizer2D getVisualizer(){
//        return visualizer;
//    }
//    
//    public void componentMoved(ComponentEvent e){
//    }
//
//    public void componentShown(ComponentEvent e) {
//    }
//
//    public void componentHidden(ComponentEvent e) {
//    }
//
//    public void mouseClicked(MouseEvent e) {
//	//visualizer.setSelectedCell(e.getX(), e.getY());
//	//repaint();
//    }
//    
//    public void mousePressed(MouseEvent e) {
//    }
//    
//    public void mouseReleased(MouseEvent e) {
//    }
//    
//    public void mouseEntered(MouseEvent e) {
//    }
//    
//    public void mouseExited(MouseEvent e) {
//    }
//}
