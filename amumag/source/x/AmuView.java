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

import amu.data.AtTime;
import amu.data.DataModel;
import amu.data.SavedDataModel;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import amu.data.AtTime;
import amu.data.Component;
import amu.data.DataModel;
import amu.data.Extremum;
import amu.data.Norm;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import refsh.Interpreter;
import refsh.RefSh;
import amu.data.SavedDataModel;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import refsh.Interpreter;
import refsh.RefSh;
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
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;


public final class AmuView {

    private SavedDataModel originalData;
    private AtTime timeProbedData;
    private DataModel displayedData;
    
    private Renderer renderer;
    private View view;
    private JFrame frame;
    private JLabel label;
    
    // used for interpreting commands from the menus (not command the
    // command line.
    protected RefSh refsh;

    public AmuView() {
        view = new View();
        frame = new JFrame("amuview");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view, BorderLayout.CENTER);
        frame.getContentPane().add(view.scroller, BorderLayout.EAST);
        label = new JLabel("amuview");
        frame.getContentPane().add(label, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(600, 600);
        refsh = new RefSh(new Interpreter(getClass(), this));
    }

    public void open(File file) throws IOException {
        msg("Opening: " + file);
        originalData = new SavedDataModel(file);
        
        if(originalData.hasData()){
            if(originalData.isTimeDependent()){
                timeProbedData = new AtTime(originalData, 0);
                displayedData = timeProbedData;  
            }
            else
                displayedData = originalData;
        }
        else{
            displayedData = originalData;
        }
        
        renderer = new DR3DRenderer(displayedData);
        renderer.setModel(displayedData);
        if(originalData.hasData())
            renderer.setColorMap(new ColorMap(-1, 1, Color.BLACK, Color.GRAY, Color.WHITE));
        
        frame.setTitle(file.getName());
        updateMenubar();
        repaint();
        if (originalData.isTimeDependent()) {
            view.scroller.setMinimum(0);
            view.scroller.setMaximum(originalData.getTimeDomain());//extent=1 -> max = length-1
            time(0);
        }
        msg("File: " + file);
    }

    protected JFileChooser fileChooser = null;
    public void open() {
        try {
            if (fileChooser == null) {
                fileChooser = new JFileChooser(System.getProperty("user.dir"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }
            int result = fileChooser.showOpenDialog(view);
            if (result == JFileChooser.APPROVE_OPTION) {
                open(fileChooser.getSelectedFile());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, ex.toString(), "IOException", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void show() {
        frame.setVisible(true);
        updateMenubar();
    }

    public void repaint() throws IOException {
        frame.setVisible(true);
        frame.getContentPane().repaint();
    }

 private static final Hashtable<String, Color> colorHash = new Hashtable<String, Color>();
    static{
        colorHash.put("white", Color.WHITE);
        colorHash.put("gray", Color.GRAY);
        colorHash.put("black", Color.BLACK);
        colorHash.put("red", Color.RED);
        colorHash.put("green", Color.GREEN);
        colorHash.put("blue", Color.BLUE);
    }
    private Color color(String name){
        if(!colorHash.containsKey(name))
                throw new IllegalArgumentException("Color not defined: " + name);
        else
            return colorHash.get(name);
    }

     public void colormap(String a, String b, String c) throws IOException{
        renderer.colorMap.setColors(color(a), color(b), color(c));
        renderer.updateColors();
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
        Extremum probe = new Extremum(timeProbedData, Extremum.MIN);
        double min = probe.getDouble( -1,null);//already time independend (t=-1) because of time probe
        probe = new Extremum(timeProbedData, Extremum.MAX);
        double max = probe.getDouble( -1,null);
        scale(min, max);
        return scale();
    }

    public void phi(double phi) throws IOException{
        renderer.setCameraDirection(phi, theta());
        repaint();
    }

    public double phi(){
        return ((DR3DRenderer)renderer).phi;
    }

    public void theta(double theta) throws IOException{
        renderer.setCameraDirection(phi(), theta);
        repaint();
    }

    public double theta(){
        return ((DR3DRenderer)renderer).theta;
    }

    public void exit(){
        System.exit(0);
    }
    
    public void time(int time) throws IOException {
        msg("Opening time: " + time);
        if(view.scroller.getValue() != time)
            view.scroller.setValue(time);
        timeProbedData.setTime(time);
        renderer.updateColors();
        repaint();
        msg("Time: " + originalData.getTime()[time] + " s");
    }

    public int time() {
        return timeProbedData.getTime_();
    }

    public void type(String dim) throws IOException{
        if(dim.startsWith("2")){
            renderer = new DR2DRenderer(displayedData);
            new Controller((DR2DRenderer)renderer).showInFrame();
        }
        else if(dim.startsWith("3"))
            renderer = new DR3DRenderer(displayedData);
        else
            throw new IllegalArgumentException("Type must be either 2D or 3D");
        repaint();
    }
        
//    public void view(String dir){
//        if(renderer instanceof DR2DRenderer){
//            DR2DRenderer renderer2D = (DR2DRender) renderer;
//        }
//        else{
//            //set phi, theta
//        }
//    }
    
    
    public void savePng(String file) throws IOException{
        renderer.savePng(new File(file));
    }
        
    public void quantity(String scalar) throws IOException{
        if(scalar.equals("x")){
            renderer.setModel(new Component(displayedData, X));
        }
        else if(scalar.equals("y")){
            renderer.setModel(new Component(displayedData, Y));
        }
        else if(scalar.equals("z")){
            renderer.setModel(new Component(displayedData, Z));
        }
        else if(scalar.equals("norm")){
            renderer.setModel(new Norm(displayedData));
        }
        else if(scalar.startsWith("dir")){
            renderer.setModel(displayedData);
        }
        /*else if(scalar.equals("none")){
            renderer.colorSurface();
        }*/
        else
            throw new IllegalArgumentException("\"quantity\" must be one of: x, y, z, norm, vector, none.");
        updateMenubar();
        repaint();
    }
    
    protected class RefAction extends AbstractAction {

        protected String command;

        public RefAction(String name, String command) {
            super(name);
            this.command = command;
        }

        public void actionPerformed(ActionEvent e) {
            refsh.interpretSafe(command);
        }
    }

    protected class View extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

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
            scroller.addAdjustmentListener(new AdjustmentListener() {

                public void adjustmentValueChanged(AdjustmentEvent e) {
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
        protected int downX,  downY;

        public void mousePressed(MouseEvent e) {
            downX = e.getX();
            downY = e.getY();
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (renderer != null) {
                int upX = e.getX();
                int upY = e.getY();
                renderer.rotateCamera((upX - downX) * mouseSensitivity,
                        -(upY - downY) * mouseSensitivity);
                downX = upX;
                downY = upY;
                repaint();
            }
        }
        //movement per keypress.
        protected double D = 0.2;

        public void keyPressed(KeyEvent e) {
            //System.out.println("key");
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT:
                    renderer.moveCamera(-D, 0, 0);
                    repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    renderer.moveCamera(D, 0, 0);
                    repaint();
                    break; //rotateWorld(-PI/128); repaint(); break;
                case KeyEvent.VK_UP:
                    renderer.moveCamera(0, 0, D);
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    renderer.moveCamera(0, 0, -D);
                    repaint();
                    break;
                case KeyEvent.VK_C:
                    renderer.moveCamera(0, -D, 0);
                    repaint();
                    break;
                case KeyEvent.VK_SPACE:
                    renderer.moveCamera(0, D, 0);
                    repaint();
                    break;
                default:
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
    }

    protected void updateMenubar() {
        frame.setJMenuBar(new MenuBar());
    }

    protected class MenuBar extends JMenuBar {

        public MenuBar() {

            // choose simulation
            JMenu file = new JMenu("File");
            JMenuItem sim = new JMenuItem(new RefAction("Open", "open"));
            file.add(sim);
            add(file);

            if (originalData != null) {
                
                if (originalData.hasData()) {
                    if (originalData.isVector()) {
                        JMenu quantity = new JMenu("Quantity");
                        quantity.add(new JMenuItem(new RefAction("X-component", "quantity x")));
                        quantity.add(new JMenuItem(new RefAction("Y-component", "quantity y")));
                        quantity.add(new JMenuItem(new RefAction("Z-component", "quantity z")));
                        quantity.add(new JMenuItem(new RefAction("Norm", "quantity norm")));
                        quantity.add(new JMenuItem(new RefAction("Direction", "quantity direction")));
                        //quantity.add(new JMenuItem(new RefAction("None (show mesh)", "quantity none")));
                        add(quantity);
                    }
                    //colormap
                    JMenu colormap = new JMenu("Colormap");
                    colormap.add(new JMenuItem(new RefAction("black gray white", "colormap black gray white")));
                    colormap.add(new JMenuItem(new RefAction("blue black red", "colormap blue black red")));
                    colormap.add(new JMenuItem(new RefAction("black blue white", "colormap black blue white")));
                    add(colormap);
                    //scale
                    JMenu s = new JMenu("Scale");
                    s.add(new JMenuItem(new RefAction("autoscale", "autoscale")));
                    s.add(new JMenuItem(new RefAction("scale 0 1", "scale 0 1")));
                    s.add(new JMenuItem(new RefAction("scale -1 1", "scale -1 1")));
                    add(s);
                }
                else {
                    JMenu mesh = new JMenu("Mesh");
                    mesh.add(new JMenuItem(new RefAction("Wireframe", "mesh wireframe")));
                    mesh.add(new JMenuItem(new RefAction("Black & White", "mesh blackandwhite")));
                    mesh.add(new JMenuItem(new RefAction("Shaded Frame", "mesh litlines")));
                    mesh.add(new JMenuItem(new RefAction("Surface", "mesh surface")));
                    add(mesh);
                }
            }
        }
    }

    public void mesh(String type) throws IOException{
        if("wireframe".equals(type))
            ((DR3DRenderer)renderer).wireframe();
        else if("blackandwhite".equals(type))
            ((DR3DRenderer)renderer).wireframe(Color.WHITE, Color.BLACK);
        else if("surface".equals(type))
            ((DR3DRenderer)renderer).surface();
        else if("litlines".equals(type))
            ((DR3DRenderer)renderer).litLines();
        else
            throw new IllegalArgumentException(type);
        repaint();
    }
    
    private void msg(String msg){
        label.setText(msg);
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            AmuView program = new AmuView();
            program.show();
            //separate refsh for thread safety.
            new RefSh(new Interpreter(program.getClass(), program)).interactive();
        } else if (args.length == 1) {
            AmuView program = new AmuView();
            program.show();
            program.open(new File(args[0]));
            //separate refsh for thread safety.
            new RefSh(new Interpreter(program.getClass(), program)).interactive();
        } else {
            System.err.println("Usage: amumag [one file].");
            System.exit(-1);
        }//*/
        //new AmuView().open(new File("/home/arne/Desktop/test.amu/m"));
    }
}
