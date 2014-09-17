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

package amu.io;

import java.io.PrintStream;
import java.util.Hashtable;

public class Message{
    
    public static int MSG = 0, WARNING = 1, DEBUG = 2;
    public static final String ESC = ((char)27) + "[";
    public static String 
             RESET   = ESC + "0m",
             BOLD    = ESC + "1m",
             REGULAR = ESC + "22m",
             BLACK   = ESC + "30m",
             RED     = ESC + "31m",
             BLUE    = ESC + "34m";
    
    
    public static boolean color = true;
    public static boolean debug = true;
    
    private static String[] colorForType = new String[]{"", 
                                                        RED,
                                                        BLUE};
    private static String[] prefixForType = new String[]{"",
                                                         "WARNING: ", 
                                                         "[debug] "};
    
    public static PrintStream out;
    
    public static void print(String message, int type){
        //suppres debug messages when not wanted.
        if(type == DEBUG && !debug)
            return;
        
        if(color && type != MSG)
            System.out.print(colorForType[type]);
        System.out.print(prefixForType[type]);
        System.out.print(message);
        if(color && type != MSG)
            System.out.print(RESET);
        
        if(out != null){
            out.print(prefixForType[type]);
            out.print(message);
        }
    }
    
    public static void println(String message, int type){
        print(message + "\n", type);
    }
    
    public static void println(){
        print("\n", MSG);
    }
    
    public static void title(String s){
        println("\n" + s);
    }
    
    public static void indent(String s){
        println("\t" + s);
    }
 
    public static void print(String message){
        print(message, MSG);
    }
    
    public static void println(String message){
        println(message, MSG);
    }
    
    public static void debug(String message){
        println(message, DEBUG);
    }
    
    public static void debugnoln(String message){
        print(message, DEBUG);
    }

    private static double progressTotal = -1;
    private static double currentProgress = 0;
            
    public static void startProgress(int total){
        assert progressTotal == -1;
        progressTotal = total;
        //print("[");
    }
    
    public static void progress(int progress) {
        while((double)progress / (double)progressTotal >= currentProgress){
            print("-");
            currentProgress += 0.04;
        }
    }
    
    public static void stopProgress(){
        currentProgress = 0;
        progressTotal = -1;
        println();
    }

    public static void hrule(){
        startProgress(1);
        progress(1);
        stopProgress();
    }
    
    public static void warning(String message){
        println(message, WARNING);
    }

    private static final Hashtable<String, Boolean> issuedWarnings = new Hashtable<String, Boolean>();
    
    public static void warningOnce(String string) {
        if(!issuedWarnings.containsKey(string)){
            issuedWarnings.put(string, Boolean.TRUE);
            warning(string + " (further warnings suppressed)");
        }
    }
}