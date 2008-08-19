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

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 *
 * @author arne
 */
public class NestPanel extends JPanel{

    protected String[] choices;
    protected JPanel[] panels;
    protected int selectedIndex = 0;
    private JComboBox combo;

    public NestPanel() {      
    }

    public void setPanels(String[] choices, final JPanel[] panels){
        this.choices = choices;
        this.panels = panels;

        setLayout(new FlowLayout(FlowLayout.LEFT));
        combo = new JComboBox(choices);
        add(combo);

        add(panels[selectedIndex]);

        combo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                remove(panels[selectedIndex]);
                selectedIndex = combo.getSelectedIndex();
                add(panels[selectedIndex]);
                panels[selectedIndex].invalidate();
                superRepaint();
                itemChanged();
            }
        });
    }
    
    private void superRepaint() {
        invalidate();
        repaint();
    }

    // override me!
    protected void itemChanged() {

    }
}