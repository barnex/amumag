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
import amu.data.Component;
import java.io.File;
import java.io.IOException;
import refsh.Interpreter;
import refsh.RefSh;
import amu.data.SavedDataModel;
import java.io.File;
import java.io.IOException;
import refsh.Interpreter;
import refsh.RefSh;
import java.awt.Color;
import java.io.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import static amu.geom.Vector.*;
import amu.data.AtTime;
import amu.data.Component;
import amu.data.SavedDataModel;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import refsh.Interpreter;
import refsh.RefSh;

public final class MainSaved extends AbstractMain{

    protected SavedDataModel originalData;
    // base dir of the simulation
    protected File simDir = null; 
    
    // dir of the data being shown
    protected String dataDir = null;
    
    public MainSaved() throws IOException{ 
        super();
        frame.setJMenuBar(new MenuBar());
    }
  
    protected JFileChooser fileChooser = null;
    public void openSimulation() throws IOException{
        if(fileChooser == null){
            fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int result = fileChooser.showOpenDialog(view);
        if(result == JFileChooser.APPROVE_OPTION)
            data(fileChooser.getSelectedFile().getParentFile(), fileChooser.getSelectedFile().getName());
    }
    
   public void data(File simDir, String dataDir) throws IOException{
       
        this.simDir = simDir;
        frame.setTitle(simDir.getName());
        frame.setJMenuBar(new MenuBar());
        repaint();
        
        this.dataDir = dataDir;
        frame.setTitle(simDir.getName() + '/' + dataDir);
        
        originalData = new SavedDataModel(simDir, dataDir);
        timeProbedData = new AtTime(originalData, 0);
        scalarTimeProbedData = new Component(timeProbedData, X);
        model = scalarTimeProbedData;
        
        renderer = new DR3DRenderer(model);        
        renderer.setModel(scalarTimeProbedData);
        renderer.setColorMap(new ColorMap(-1, 1, Color.BLACK, Color.GRAY, Color.WHITE));
        
        frame.setJMenuBar(new MenuBar());
        
        view.scroller.setMinimum(0);
        view.scroller.setMaximum(originalData.getTimeDomain());//extent=1 -> max = length-1
        time(0);
    }
    
    public String data(){
        return dataDir;
    }
    
    protected void updateMenubar(){
        frame.setJMenuBar(new MenuBar());
    }
    
    protected class MenuBar extends JMenuBar{
                
        public MenuBar(){
            
            // choose simulation
            JMenu file = new JMenu("simulation");
            JMenuItem sim = new JMenuItem(new RefAction("open", "openSimulation"));
            file.add(sim);
            add(file);
            
            // data
            if(simDir != null){
                JMenu data = new JMenu("data");
                File[] dirs = simDir.listFiles(new FileFilter(){
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }   
                });
                for(int i=0; i<dirs.length; i++){
                    data.add(new JMenuItem(
                            new RefAction(dirs[i].getName(), "data " + dirs[i].getName())));
                } 
                add(data);
            }
            
            //quantity
            if(dataDir != null &&  originalData.isVector()){
                JMenu quantity = new JMenu("quantity");
                quantity.add(new JMenuItem(new RefAction("x", "quantity x")));
                quantity.add(new JMenuItem(new RefAction("y", "quantity y")));
                quantity.add(new JMenuItem(new RefAction("z", "quantity z")));
                quantity.add(new JMenuItem(new RefAction("norm", "quantity norm")));
                quantity.add(new JMenuItem(new RefAction("vector", "quantity vector")));
                quantity.add(new JMenuItem(new RefAction("none", "quantity none")));
                add(quantity);
            }
            
            //colormap
            if(dataDir != null && !renderer.getModel().isVector() && ! "none".equals(quantity)){
                JMenu colormap = new JMenu("colormap");
                colormap.add(new JMenuItem(new RefAction("black gray white", "colormap black gray white")));
                colormap.add(new JMenuItem(new RefAction("blue black red", "colormap blue black red")));
                colormap.add(new JMenuItem(new RefAction("black blue white", "colormap black blue white")));
                add(colormap);
                
              //scale
                JMenu s = new JMenu("scale");
                s.add(new JMenuItem(new RefAction("autoscale", "autoscale")));
                s.add(new JMenuItem(new RefAction("scale 0 1", "scale 0 1")));
                s.add(new JMenuItem(new RefAction("scale -1 1", "scale -1 1")));
                add(s);
            }
        }
    }
    
    public static void main(String[] args) throws IOException{
        MainSaved main = new MainSaved();
        main.frame.setVisible(true);
        //separate refsh for thread safety.
        new RefSh(new Interpreter(main.getClass(), main)).interactive();
    }
}