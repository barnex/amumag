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

//package amu.xdata;
//
//import amu.xdata.model.Extremum;
//import amu.xdata.model.AtTime;
//import amu.xdata.model.Component;
//import amu.xdata.model.Norm;
//import java.awt.event.ActionEvent;
//import java.io.File;
//import java.io.IOException;
//import refsh.Interpreter;
//import refsh.RefSh;
//import amu.xdata.model.SavedDataModel;
//import amu.xdata.model.Scalarizer;
//import amu.xdata.render.ColorMap;
//import amu.xdata.render.DR2DRenderer;
//import java.io.File;
//import java.io.IOException;
//import javax.swing.JFrame;
//import refsh.Interpreter;
//import refsh.RefSh;
//import amu.xdata.render.DR3DRenderer;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.event.AdjustmentEvent;
//import java.awt.event.AdjustmentListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.io.FileFilter;
//import java.util.Hashtable;
//import javax.swing.AbstractAction;
//import javax.swing.JFileChooser;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JScrollBar;
//import static amu.geom.Vector.*;
//        
//public class Main2D {
// 
//    private SavedDataModel originalData;
//    private AtTime timeProbedData;
//    private Scalarizer scalarTimeProbedData;
//    
//    private DR2DRenderer renderer;
//    private View view;
//    private JFrame frame;
//    
//    // base dir of the simulation
//    private File simDir = null; 
//    
//    // dir of the data being shown
//    private String dataDir = null;
//    
//    // the quantity being shown;
//    private String quantity = null;
//    
//    // used for interpreting commands from the menus (not command the
//    // command line.
//    private RefSh refsh;
//    
//    public Main2D() throws IOException{  
//        view = new View();
//        frame = new JFrame("3D view");
//        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(view, BorderLayout.CENTER);
//        frame.getContentPane().add(view.scroller, BorderLayout.EAST);
//        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//        frame.setSize(600, 600);
//        frame.setJMenuBar(new MenuBar());
//        refsh = new RefSh(new Interpreter(getClass(), this));
//    }
//    
//    public void slice(int slice){
//        renderer.setSlice(slice);
//        view.repaint();
//    }
//    
//    public void view(String v){
//        renderer.setView((int)(v.charAt(0)) - (int)('x'));
//        
//    }
//    
//    public void simulation(File simDir) throws IOException{
//        this.simDir = simDir;
//        frame.setTitle(simDir.getName());
//        frame.setJMenuBar(new MenuBar());
//        repaint();
//        //show mesh?
//    }
//    
//    public File simulation(){
//        return simDir;
//    }
//    
//    private JFileChooser fileChooser = null;
//    public void openSimulation() throws IOException{
//        if(fileChooser == null){
//            fileChooser = new JFileChooser(System.getProperty("user.dir"));
//            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        }
//        int result = fileChooser.showOpenDialog(view);
//        if(result == JFileChooser.APPROVE_OPTION)
//            simulation(fileChooser.getSelectedFile());
//    }
//    
//   public void data(String dataDir) throws IOException{
//        this.dataDir = dataDir;
//        frame.setTitle(simDir.getName() + '/' + dataDir);
//        
//        originalData = new SavedDataModel(simDir, dataDir);
//        timeProbedData = new AtTime(originalData, 0);
//        scalarTimeProbedData = new Component(timeProbedData, X);
//        
//        renderer = new DR2DRenderer(scalarTimeProbedData);        
//        //renderer.setData(scalarTimeProbedData);
//        //renderer.setColorMap(new ColorMap(-1, 1, Color.BLACK, Color.GRAY, Color.WHITE));
//        
//        frame.setJMenuBar(new MenuBar());
//        
//        view.scroller.setMinimum(0);
//        view.scroller.setMaximum(originalData.getTimeDomain());//extent=1 -> max = length-1
//        time(0);
//    }
//    
//    public String data(){
//        return dataDir;
//    }
//    
//    public void time(int time) throws IOException{
//        timeProbedData.setTime(time);
//        //renderer.updateColors();
//        repaint();
//    }
//    
//    public int time(){
//        return timeProbedData.getTime_();
//    }
//    
//    public void exit(){
//        System.exit(0);
//    }
//    
//    private void repaint() throws IOException {
//       frame.setVisible(true);
//       frame.getContentPane().repaint();
//    }
//    
//    private static final Hashtable<String, Color> colorHash = new Hashtable<String, Color>();
//    static{
//        colorHash.put("white", Color.WHITE);
//        colorHash.put("gray", Color.GRAY);
//        colorHash.put("black", Color.BLACK);
//        colorHash.put("red", Color.RED);
//        colorHash.put("green", Color.GREEN);
//        colorHash.put("blue", Color.BLUE);  
//    }
//    
//    private class MenuBar extends JMenuBar{
//                
//        public MenuBar(){
//            
//            // choose simulation
//            JMenu file = new JMenu("simulation");
//            JMenuItem sim = new JMenuItem(new RefAction("open", "openSimulation"));
//            file.add(sim);
//            add(file);
//            
//            // data
//            if(simDir != null){
//                JMenu data = new JMenu("data");
//                File[] dirs = simDir.listFiles(new FileFilter(){
//                    public boolean accept(File f) {
//                        return f.isDirectory();
//                    }   
//                });
//                for(int i=0; i<dirs.length; i++){
//                    data.add(new JMenuItem(
//                            new RefAction(dirs[i].getName(), "data " + dirs[i].getName())));
//                } 
//                add(data);
//            }
//            
//            //quantity
//            if(dataDir != null &&  originalData.isVector()){
//                JMenu quantity = new JMenu("quantity");
//                quantity.add(new JMenuItem(new RefAction("x", "quantity x")));
//                quantity.add(new JMenuItem(new RefAction("y", "quantity y")));
//                quantity.add(new JMenuItem(new RefAction("z", "quantity z")));
//                quantity.add(new JMenuItem(new RefAction("norm", "quantity norm")));
//                quantity.add(new JMenuItem(new RefAction("vector", "quantity vector")));
//                quantity.add(new JMenuItem(new RefAction("none", "quantity none")));
//                add(quantity);
//            }
//            
//            //colormap
//            if(dataDir != null && !renderer.getModel().isVector() && ! "none".equals(quantity)){
//                JMenu colormap = new JMenu("colormap");
//                colormap.add(new JMenuItem(new RefAction("black gray white", "colormap black gray white")));
//                colormap.add(new JMenuItem(new RefAction("blue black red", "colormap blue black red")));
//                colormap.add(new JMenuItem(new RefAction("black blue white", "colormap black blue white")));
//                add(colormap);
//                
//              //scale
//                JMenu s = new JMenu("scale");
//                s.add(new JMenuItem(new RefAction("autoscale", "autoscale")));
//                s.add(new JMenuItem(new RefAction("scale 0 1", "scale 0 1")));
//                s.add(new JMenuItem(new RefAction("scale -1 1", "scale -1 1")));
//                add(s);
//            }
//        }
//    }
//    
//    private class RefAction extends AbstractAction{
//
//        private String command;
//        
//        public RefAction(String name, String command){
//            super(name);
//            this.command = command;
//        }
//        
//        public void actionPerformed(ActionEvent e) {
//            refsh.interpretSafe(command);
//        }   
//    }
//    
//    private class View extends JPanel implements MouseListener, MouseMotionListener, KeyListener{
//        
//        private JScrollBar scroller;
//        public double mouseSensitivity = 0.01;
//    
//        public View() {
//            setFocusable(true);
//            addMouseListener(this);
//            addMouseMotionListener(this);
//            addKeyListener(this);
//            initScroller();
//        }   
//    
//        private void initScroller() {
//        scroller = new JScrollBar(JScrollBar.VERTICAL);
//        scroller.setMinimum(0);
//        scroller.setVisibleAmount(1);
//        scroller.setMaximum(1);
//        scroller.addAdjustmentListener(new AdjustmentListener(){
//            public void adjustmentValueChanged(AdjustmentEvent e){
//                    try {
//                        time(scroller.getValue());
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//        });
//    }
//        
//        @Override
//        public void paintComponent(Graphics g1){
//            Graphics2D g = (Graphics2D) g1;
//            // 08-05-15: disabling antialiasing and opting for render_speed makes
//            // a HUGE difference in speed on linux. This does not seem to be
//            // the case on macOSX. Also, sun-java6-sdk is a lot faster here than open-jdk.
//            //g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            Dimension d = getSize();
//            int w = d.width;
//            int h = d.height;
//            
//            if(renderer != null){
//                renderer.setSize(w, h);
//                renderer.paint(g, w, h);
//            }
//            else
//                super.paintComponent(g1);
//        }
//
//        private int downX, downY;
// 
//        public void mousePressed(MouseEvent e){
//	    downX = e.getX();
//	    downY = e.getY();
//	}
//	public void mouseReleased(MouseEvent e){}
//	public void mouseClicked(MouseEvent e){}
//	public void mouseEntered(MouseEvent e){}
//	public void mouseExited(MouseEvent e){}
//	public void mouseMoved(MouseEvent e){}
//
//        public void mouseDragged(MouseEvent e) {
//            
//        }
//
//        public void keyTyped(KeyEvent e) {
//           
//        }
//
//        public void keyPressed(KeyEvent e) {
//           
//        }
//
//        public void keyReleased(KeyEvent e) {
//           
//        }
//    }	
//	
//       
//    public static void main(String[] args) throws IOException{
//        Main2D main = new Main2D();
//        main.frame.setVisible(true);
//        //separate refsh for thread safety.
//        new RefSh(new Interpreter(main.getClass(), main)).interactive();
//    }
//
//        public void mouseDragged(MouseEvent e) {
//            
//        }
//
//        public void keyTyped(KeyEvent e) {
//            
//        }
//
//        public void keyPressed(KeyEvent e) {
//            
//        }
//
//        public void keyReleased(KeyEvent e) {
//           
//        }
//
//}
