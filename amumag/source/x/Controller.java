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
//import amu.mag.Cell;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import javax.swing.ButtonGroup;
//import javax.swing.JCheckBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JSlider;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import static amu.geom.Vector.X;
//import static amu.geom.Vector.Y;
//import static amu.geom.Vector.Z;
//
///**
// * Interactively controls a View object, setting its viewing direction, slice
// * and level.
// */
//public final class Controller extends JPanel{
//    
//    private View2D view;
//    
//    private JSlider[] slider = new JSlider[2];
//    private JLabel sliceLabel = new JLabel(), levelLabel = new JLabel();
//    private JRadioButton[] radio = new JRadioButton[3];
//    private NumberModel[] spinner = new NumberModel[2];
//    
//    /**
//     * Creates a Controller for the View.
//     **/
//    public Controller(View2D v){
//        this.view = v;
//        Cell[][][][] levels = view.getVisualizer().mesh.levels;
//        
//        slider[0] = new JSlider(JSlider.HORIZONTAL, 0, levels.length-1, levels.length-1);
//        slider[1] = new JSlider(JSlider.HORIZONTAL);
//        
//        for(int i=0; i<2; i++){
//            slider[i].setMinimum(0);
//            slider[i].setPaintTicks(true);
//            slider[i].setMajorTickSpacing(1);
//            slider[i].setSnapToTicks(true);
//        }
//        
//        setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(3, 3, 3, 3);
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.fill = GridBagConstraints.HORIZONTAL;  
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.gridx = 0; gbc.gridy = 0;
//        
//        add(new JLabel("View"), gbc);
//        // pane for choosing the viewing direction.
//        gbc.gridx = 1;
//        gbc.gridwidth = 2;
//        JPanel viewPane = new JPanel(new GridLayout(1, 3));
//        ButtonGroup group = new ButtonGroup();
//        for(int i=0; i<3; i++){
//            radio[i] = new JRadioButton((char)('X' + i) + "");
//            viewPane.add(radio[i]);
//            group.add(radio[i]);
//        }
//        radio[view.getVisualizer().getView()].setSelected(true);
//        for(int i=0; i<3; i++){
//            radio[i].addActionListener(new ActionListener(){
//                public void actionPerformed(ActionEvent e) {
//                    int i = 0;
//                    while(!radio[i].isSelected())
//                        i++;
//                    setView(i);
//                }
//            });
//        }
//        add(viewPane, gbc);
//        
//        //labels and sliders
//        gbc.gridwidth = 1;
//        gbc.gridx = 0; gbc.gridy = 1;
//        add(new JLabel("Level"), gbc);
//        
//        gbc.gridx = 1; gbc.gridy = 1;
//        add(levelLabel, gbc);
//        
//        gbc.gridx = 2; gbc.gridy = 1;
//        add(slider[0] , gbc);
//        
//        gbc.gridx = 0; gbc.gridy = 2;
//        add(new JLabel("Slice") , gbc);
//        
//        gbc.gridx = 1; gbc.gridy = 2;
//        add(sliceLabel, gbc);
//        
//        gbc.gridx = 2; gbc.gridy = 2;
//        add(slider[1] , gbc);
//        
//        slider[0].addChangeListener(new ChangeListener(){
//            public void stateChanged(ChangeEvent e) {
//                setLevel(slider[0].getValue());
//            }
//        });
//        
//        
//        slider[1].addChangeListener(new ChangeListener(){
//            public void stateChanged(ChangeEvent e) {
//                setSlice(slider[1].getValue());
//            }
//        });
//        
//        setView(view.getVisualizer().getView());
//        setLevel(view.getVisualizer().getLevel());
//        setSlice(view.getVisualizer().getSlice());
//        
//        // checkboxes
//        gbc.gridx = 0;
//        gbc.gridwidth = 3;
//        final boolean[] draw = view.getVisualizer().draw;
//        for(int i=0; i<draw.length; i++){
//            final int i_ = i;
//            final JCheckBox box = new JCheckBox(view.getVisualizer().description[i], draw[i]);
//            box.addActionListener(new ActionListener(){
//                public void actionPerformed(ActionEvent e){
//                    draw[i_] = box.isSelected();
//                    view.repaint();
//                }
//            });
//            gbc.gridy++;
//            /*if(gbc.gridy > 6)
//                gbc.gridy++;*/
//            add(box, gbc);
//        }
//        
//        //spinners
//        /*for(int i=0; i<spinner.length; i++)
//            spinner[i] = new NumberModel();
//        gbc.gridx = 1;
//        gbc.gridy = 7;
//        add(new JSpinner(spinner[0]), gbc);
//        gbc.gridy = 9;
//        add(new JSpinner(spinner[1]), gbc);  */      
//    }
//    
//    /** Updates the level label. */
//    private void updateLevel(){
//        levelLabel.setText(view.getVisualizer().getLevel() + "");
//    }
//    
//    /** Updates the slice label.*/
//    private void updateSlice(){
//        sliceLabel.setText(view.getVisualizer().getSlice() + "");
//    }
//    
//    /** Sets the level and updates labels and slider maxima. */
//    private void setLevel(int level){
//        view.getVisualizer().setLevel(level);
//        setMaxSliceForView(view.getVisualizer().getView());
//        updateLevel();
//        updateSlice();
//        view.repaint();
//    }
//    
//    /** Sets the maximum of the slice slider to the value determined by the view
//     * direction v. */
//    private void setMaxSliceForView(int v){
//        int level = view.getVisualizer().getLevel();
//        Cell[][][][] levels = view.getVisualizer().mesh.levels;
//        switch(v){
//            case X: slider[1].setMaximum(levels[level].length-1); break;
//            case Y: slider[1].setMaximum(levels[level][0].length-1); break;
//            case Z: slider[1].setMaximum(levels[level][0][0].length-1); break;
//        }
//    }
//    
//    /** Sets the slice value and updates. */
//    private void setSlice(int slice){
//        view.getVisualizer().setSlice(slice);
//        updateSlice();
//        view.repaint();
//    }
//    
//    /** Sets the view direction and updates labels and slider maxima. */
//    private void setView(int v){
//        setMaxSliceForView(v);
//        view.getVisualizer().setView(v);
//        view.componentResized(null); //not so nice, but a resize is needed.
//        view.repaint();
//    }
//}
