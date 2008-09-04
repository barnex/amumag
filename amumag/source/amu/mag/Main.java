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

import amu.io.Message;
import amu.io.Message;
import java.lang.reflect.InvocationTargetException;


public final class Main {
            
    public static int CPUS = Runtime.getRuntime().availableProcessors();
    public static int LOG_CPUS = log2(CPUS);
    public static int THREADS = pow2(LOG_CPUS);
    
    public static final String WD = System.getProperty("user.dir");
    public static final String CP = System.getProperty("java.class.path");
    
    public static final String VM = System.getProperty("java.version");
    private static final String VERSION = "2b218"; //140+commit74
    private static final String MEMORY = Runtime.getRuntime().maxMemory() / (1024*1024) + "MB";
    
    private static final String BANNER = 
            Message.RED + "\n" + 
            "  --   amumag2 " + VERSION + "\n" +
            " |->|  on JVM " + VM + "\n" + 
            "  --   " + CPUS + " cpu's, " + MEMORY  + " mem.\n" + 
            " Copyright (C) 2006-2008  Arne Vansteenkiste\n" +
            " This program comes with ABSOLUTELY NO WARRANTY\n"  +
            " This is free software, and you are welcome to redistribute\n" + 
            " it under certain conditions; see licence.txt for details." + Message.RESET;
    
    public static Simulation sim;
    
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, Exception{
        System.out.println(BANNER);
        
        assert checkAssertionsEnabled();
        //amu.debug.Test.run();
        
        String arg = args[0];
        String[] args2 = new String[args.length - 1];
        for(int i=0; i<args2.length; i++)
            args2[i] = args[i+1];

        Problem probl = (Problem) Class.forName(arg).getConstructor(new Class[]{}).newInstance(new Object[]{});
       
        probl.args = args2;
        probl.init();
        probl.initImpl();
        probl.run();
    }

    // print a warning when assertions are enabled.
    private static boolean checkAssertionsEnabled() {
        Message.warning("Assertion checking is enabled, use java -da to disable assertions and improve performance.");
        return true;
    }
    
    /**
     * 2-base log, rounded up.
     * number of threads for n CPU's is 2^2log(n).
     * @param n
     * @return
     */
    private static int log2(int n){
        if(n < 1)
            throw new IllegalArgumentException();
        
        n = 2*n-1;
        int log = 0;
        while(n > 1){
            log++;
            n /= 2;
        }
        return log;
    }
    
    private static int pow2(int n){
        if(n < 0)
            throw new IllegalArgumentException();
        int pow = 1;
        while(n > 0){
            pow *= 2;
            n--;
        }
        return pow;
    }
    
    /*private static void runProblems(){
        for(int i=0; i<problems.length; i++){
            Message.exit();
            Message.enter("run");
            Message.println(problems[i].toString());
            try{
                problems[i].run(null);
            }
            catch(Exception e){
                System.out.println();
                e.printStackTrace();
            }
        }
    }*/
    
    /*private static void loadProblems(String[] args){
        problems = new Problem[args.length];
        for(int i=0; i<problems.length; i++){
            String arg = args[i];
            Message.enter("read");
            Message.print(arg);
            try {
                problems[i] = (Problem) Class.forName(arg).getConstructor(new Class[]{}).newInstance(new Object[]{});
            } 
            catch (IllegalArgumentException ex)
            {
                ex.printStackTrace();
                System.exit(-1);
            } 
            catch (SecurityException ex) 
            {
                ex.printStackTrace();
                System.exit(-1);
            } 
            catch (InvocationTargetException ex) 
            {
                ex.printStackTrace();
                System.exit(-1);
            } 
            catch (InstantiationException ex) 
            {
                ex.printStackTrace();
                System.exit(-1);
            } 
            catch (NoSuchMethodException ex) 
            {
                ex.printStackTrace();
                System.exit(-1);
            } 
            catch (ClassNotFoundException ex) {
                System.out.println(": class not found");
                System.out.println("Make sure this class is located in the classpath:\n"+CP);
                System.out.println();
                System.exit(-1);
                
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
            Message.println(": OK");
        }
    }*/
    
}
