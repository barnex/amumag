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

import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.Cell;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import refsh.Interpreter;
import refsh.RefSh;
import static amu.geom.Vector.*;

/**
 *
 * @author arne
 */
public class Plot3D {
   
    /*private final View3D view3d;
    private final JFrame frame;
    private final Visualizer3D visualizer;
    
    // (1) open base dir
    private File baseDir;
    private Mesh mesh;
    private String[] fields;
    
    // (2) show field dir
    private File fieldDir;
    private Integer[] fieldNumbers;
    private String currentDir;
    
    // (3) choose frame
    private int currentField;
    private JScrollBar scroller;
    
    // (anytime) choose colorizer
    private Colorizer[] colorizers = new Colorizer[]{
        new Light(new Vector(0, 0, -100), Color.YELLOW, 0.9),
        
    };
     
    public static void main(String[] args) throws IOException{
        Plot3D program = new Plot3D();
        RefSh refsh = new RefSh(new Interpreter(program.getClass(), program));
        refsh.interactive();
    }
    
    public Plot3D(){
        visualizer = new Visualizer3D();
        visualizer.setColor(colorizers[0]);
        view3d = new View3D(visualizer);
        frame = new JFrame();
        initLayout();
    }

     private void initLayout() {   
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        JPanel viewPane = new JPanel();
        viewPane.setLayout(new BorderLayout());
        viewPane.add(view3d, BorderLayout.CENTER);
        initScroller();
        viewPane.add(scroller, BorderLayout.EAST);

        frame.add(viewPane);
        frame.setSize(500, 500);
    }
     
    private void initScroller() {
        scroller = new JScrollBar(JScrollBar.VERTICAL);
        scroller.setMinimum(0);
        scroller.setVisibleAmount(1);
        scroller.setMaximum(1);
        scroller.addAdjustmentListener(new AdjustmentListener(){
            public void adjustmentValueChanged(AdjustmentEvent e) {
                System.out.println("scroller:" + scroller.getValue());
                    frame(scroller.getValue());
                }
        });
    }
    
    //(anytime) choose colorizer________________________________________________
    
    public void show(String tag){
        int i=0;
        while(i<colorizers.length && !colorizers[i].toString().equals(tag))
            i++;
        visualizer.setColor(colorizers[i]);
        frame.repaint();
    }
    
    //(1) open base dir ________________________________________________________
   
    public void open(File baseDir) throws IOException, ClassNotFoundException{
        this.baseDir = baseDir;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(baseDir, "mesh")));
        mesh = (Mesh) in.readObject();
        in.close(); 
        initVectors();
        
        visualizer.setMesh(mesh);
        //visualizer.setColor(colorizers[0]);
        //view3d.setVisualizer(visualizer);
        frame.setTitle(baseDir.getName());
        frame.setVisible(true);
        if(currentDir != null)
            load(currentDir);
    }
   
    private void initVectors() {
        Cell[][][][] levels = mesh.levels;
         for(int l=0; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null){
                            if(cell.display == null)
                                cell.display = new Vector();
                        }
                    }
    }
       
    private String[] listFiles(){
        String[] files = baseDir.list(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(".field");
            }
        });
        return files;
    }
   
    //(2) show field directory__________________________________________________
    
    public void load(String dir){
        File newfieldDir = new File(baseDir, dir);
        if(newfieldDir.equals(fieldDir))
            return;
        System.out.println("loadDir:" + dir);
        fieldDir = newfieldDir;
        String[] fieldStrings = fieldDir.list(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                if(name.endsWith(".gz")){
                    try{
                        Integer.parseInt(removeGZ(name)); //-".gz".length -1
                        return true;
                    }
                    catch(NumberFormatException e){
                        return false;
                    }
                }
                else
                    return false;
            }
        });
        fieldNumbers = new Integer[fieldStrings.length];
        for(int s=0; s<fieldStrings.length; s++)
            fieldNumbers[s] = Integer.parseInt(removeGZ(fieldStrings[s]));
        
        Arrays.sort(fieldNumbers);
        System.out.println("fieldNumbers:" + fieldNumbers.length);
        scroller.setMinimum(0);
        scroller.setMaximum(fieldNumbers.length);//extent=1 -> max = length-1
        currentField = -1;
        frame(scroller.getValue());
        currentDir = dir;
    }
    
    // removes ".gz"
    private static final String removeGZ(String s){
        return s.substring(0, s.length()-3);
    }
    
    // (3) choose frame_________________________________________________________
    
    public void frame(int inode){
        if(inode != currentField){
            System.out.println("loadInt:" + inode);
            currentField = inode;
            if(scroller.getValue() != inode)
                scroller.setValue(inode);
            loadFile(new File(fieldDir, fieldNumbers[inode].toString() + ".gz"));
        }
    }
    
    private void loadFile(File file){
        System.out.println("loadFile:" + file);
        try{
            MeshInput.read(file, mesh);
            view3d.visualizer.updateColors();
            view3d.repaint();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        frame.repaint();
    }*/
    
}
