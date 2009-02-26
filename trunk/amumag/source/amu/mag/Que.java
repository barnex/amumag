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
package amu.mag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author arne
 */
public class Que {
    
    public static void main(String[] args) throws IOException{
        StringBuffer buffer = new StringBuffer();
        
        if(args.length > 0){
            for(int i=0; i<args.length; i++){
                buffer.append(args[i]);
                buffer.append(' ');
            }
        } else{
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line = in.readLine();
            while(line != null){
                buffer.append(line);
                if(!line.endsWith(";"))
                    buffer.append(';');
                line = in.readLine();
            }
        }
        String[] split = buffer.toString().split(";");
        for(int i=0; i<split.length; i++)
            split[i] = split[i].trim();
        new Que().run(split);
    }
    
    public void run(String[] args){
        TaskQue que = new TaskQue();
        for(int i=0; i<args.length; i++){
            final String arg = args[i];
            if(arg.length() != 0){
                que.add(new Task(arg){
                    public void run() throws Exception {
                        System.out.println("[started]: " + arg);
                        exec(arg);
                    }
                });
            }
        }
        System.out.println(que);
        que.run();
    }
    
    public void exec(String command) throws IOException, InterruptedException{
        Process p = Runtime.getRuntime().exec(command);
      
        
//        {
//          p.
//            InputStreamReader out = new InputStreamReader(p.getInputStream());
//            int chr = out.read();
//            //if(chr >= 0)
//            //    System.out.println("[" + command + "]:");
//            while(chr >= 0){
//                System.out.print((char)chr);
//                chr = out.read();
//            }
//        }
//
//        {
//            InputStreamReader err = new InputStreamReader(p.getErrorStream());
//            int chr = err.read();
//            if(chr >= 0)
//                System.err.println("[" + command + "]:");
//            while(chr >= 0){
//                System.err.print((char)chr);
//                chr = err.read();
//            }
//        }
          p.waitFor();
    }
}
