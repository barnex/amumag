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
//import amu.x.View3D;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author arne
 */
public class Viewer extends JFrame{

   /* private final File baseDir;
    private final Mesh mesh;
    public final View3D view3d;
    
    private File fieldDir; 
    private Integer[] fieldNumbers;
    private int currentField;
    
    private JScrollBar scroller;
    private JLabel label;
    
    public Viewer(File baseDir) throws IOException, ClassNotFoundException{
        super(baseDir.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.baseDir = baseDir;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(baseDir, "mesh")));
        mesh = (Mesh) in.readObject();
        initVectors();
        in.close();
        
        view3d = new View3D(new Visualizer3D(mesh));
        
        initLayout();
    }

    private void initLayout() {
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        
        JPanel viewPane = new JPanel();
        viewPane.setLayout(new BorderLayout());
        viewPane.add(view3d, BorderLayout.CENTER);
        initScroller();
        viewPane.add(scroller, BorderLayout.EAST);
        
        JSplitPane split = new JSplitPane();
        split.setRightComponent(viewPane);
        split.setLeftComponent(new JScrollPane(new List()));
        
        pane.add(split, BorderLayout.CENTER);
        pane.add(new Toolbar(), BorderLayout.NORTH);

        setSize(500, 500);
    }

    private void initScroller() {
        scroller = new JScrollBar(JScrollBar.VERTICAL);
        scroller.setMinimum(0);
        scroller.setVisibleAmount(1);
        scroller.addAdjustmentListener(new AdjustmentListener(){
            public void adjustmentValueChanged(AdjustmentEvent e) {
                System.out.println("scroller:" + scroller.getValue());
                    loadInt(scroller.getValue());
                }
        });
    }
    
    private class Toolbar extends JPanel{
        public Toolbar(){
            setLayout(new FlowLayout(FlowLayout.LEFT));
            label = new JLabel("-----/-----");
            add(label);
            add(view3d.panel);
        }
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
    
    private class List extends JList{
    
        public List(){
            super(listFiles());
            addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    System.out.println("list:" + getSelectedValue());
                    loadDir(getSelectedValue().toString());     
                }
            });
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
   
    public void loadDir(String dir){
        File newfieldDir = new File(baseDir, dir);
        if(newfieldDir.equals(fieldDir))
            return;
        System.out.println("loadDir:" + dir);
        fieldDir = newfieldDir;
        String[] fieldStrings = fieldDir.list(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                try{
                    Integer.parseInt(name);
                    return true;
                }
                catch(NumberFormatException e){
                    return false;
                }
            }
        });
        fieldNumbers = new Integer[fieldStrings.length];
        for(int s=0; s<fieldStrings.length; s++)
            fieldNumbers[s] = Integer.parseInt(fieldStrings[s]);
        
        Arrays.sort(fieldNumbers);
        System.out.println("fieldNumbers:" + fieldNumbers.length);
        scroller.setMinimum(0);
        scroller.setMaximum(fieldNumbers.length);//extent=1 -> max = length-1
        currentField = -1;
        loadInt(scroller.getValue());
    }
    
    public void loadInt(int inode){
        if(inode != currentField){
            System.out.println("loadInt:" + inode);
            currentField = inode;
            if(scroller.getValue() != inode)
                scroller.setValue(inode);
            label.setText("loading");
            loadFile(new File(fieldDir, fieldNumbers[inode].toString()));
            label.setText(currentField + "/" + fieldNumbers.length);
        }
    }
    
    public void loadFile(File file){
        System.out.println("loadFile:" + file);
        try{
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int header = in.readInt();
        Cell[][][][] levels = mesh.levels;
        for(int l=0; l<levels.length; l++)
            for(int i=0; i<levels[l].length; i++)
                for(int j=0; j<levels[l][i].length; j++)
                    for(int k=0; k<levels[l][i][j].length; k++){
                        Cell cell = levels[l][i][j][k];
                        if(cell != null){
                            cell.display.x = in.readDouble();
                            cell.display.y = in.readDouble();
                            cell.display.z = in.readDouble();
                        }
                    }
        in.close();
        view3d.visualizer.updateColors();
        view3d.repaint();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        if(args.length == 0)
            args = new String[]{"/home/arne/Desktop/problem1.amu"};
        Viewer v = new Viewer(new File(args[0]));
        v.view3d.visualizer.setColor(new Magnetization3D());
        v.setVisible(true);
    }*/
}
