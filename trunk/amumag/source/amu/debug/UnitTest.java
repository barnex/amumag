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

package amu.debug;

import amu.geom.Vector;
import amu.io.Message;
import amu.mag.time.Extrapolator;
import amu.mag.time.Extrapolator2;
import amu.mag.time.Extrapolator2Cached;
import amu.mag.time.SplineExtrapolator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UnitTest{

    public static void extrapolator2nd(){
        // , new SplineExtrapolator()
        for(Extrapolator ex :new Extrapolator[]{new Extrapolator2()}){

        ex.addPoint(0, new Vector(0, 0, 0));
        ex.addPoint(1, new Vector(1, 1, 1));
        ex.addPoint(1, new Vector(2, 4, -4));
        Vector target = new Vector();

        // 0.0 2.0 4.0 -4.0
        // -1.0 1.0 1.0 1.0
        //-2.0 0.0 0.0 0.0

        ex.extrapolate(0, target);
        assert target.equals(new Vector(2, 4, -4));

        ex.extrapolate(-1, target);
        assert target.equals(new Vector(1, 1, 1));

        ex.extrapolate(-2, target);
        assert target.equals(new Vector(0, 0, 0));
        }
    }

    //__________________________________________________________________________

    /**
     * Runs all static methods of this class and sees if anything is thrown.
     */
    public static void run() throws Bug{
        boolean fail = false;
        Message.title("unit testing");
        Message.hrule();
        Method[] methods = UnitTest.class.getDeclaredMethods();
        for(Method m: methods){
            if(!m.getName().equals("run"))// && Modifier.isStatic(m.getModifiers()))
                try {
                Message.print("\t" + m.getName() + ": \t ");
                m.invoke(null, new Object[]{});
                Message.println("OK");
            } catch (IllegalAccessException ex) {
                throw new Bug("Bug^2 (bug in debugging code)");
            } catch (IllegalArgumentException ex) {
                throw new Bug("Bug^2 (bug in debugging code)");
            } catch (InvocationTargetException ex) {
                Message.println("FAIL");
                fail = true;
            }
        }
        if(fail){
            Message.warning("Unit Tests failed.");
            System.exit(-2);
        }
    }
}