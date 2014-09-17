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
import amu.mag.time.SplineExtrapolator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UnitTest{

    public static void vector() {
        Vector a = new Vector();
        a.set(1, 2, 3);
        Vector b = new Vector(1 + 1E-13, 2 - 1E-13, 3 + 1E-13);
        Vector c = new Vector(3, 4, 5);

        assert a.equals(b);
        assert a.hashCode() == b.hashCode();
        assert !a.equals(c);
    }

    //obsolete
    /*public static void splineExtrapolator(){
        Vector target = new Vector();
        SplineExtrapolator ex = new SplineExtrapolator();

        Vector b = new Vector(-2, 2, 5);
        ex.init(b);
        ex.extrapolate(0.0, target);
        assert target.equals(b);
        ex.extrapolate(1000, target);
        assert target.equals(b);
        
        Vector a = new Vector(1, 2, 3);
        ex.addPoint(2.0, a);
        ex.extrapolate(-2.0, target);
        assert target.equals(b);

        Vector c = new Vector(7, 2, -3);
        ex.addPoint(3.0, c);
        ex.extrapolate(0, target);
        assert target.equals(c);

        //test replaceLastPoint
        ex.replaceLastPoint(2.0, b);
        ex.extrapolate(0.0, target);
        assert target.equals(b);
        ex.extrapolate(-2.0, target);
        assert target.equals(a);
    }*/

    public static void extrapolator2nd() {
        for (Extrapolator ex : new Extrapolator[]{new Extrapolator2()}) {
            Vector target = new Vector();

            Vector b = new Vector(-2, 2, 5);
            ex.addPoint(0.0, b);
            ex.extrapolate(0.0, target);
            assert target.equals(b);
            ex.extrapolate(1000, target);
            assert target.equals(b);

            Vector a = new Vector(1, 2, 3);
            ex.addPoint(2.0, a);
            ex.extrapolate(-2.0, target);
            assert target.equals(b);

            Vector c = new Vector(7, 2, -3);
            ex.addPoint(3.0, c);
            ex.extrapolate(0, target);
            assert target.equals(c);

            //test replaceLastPoint
            ex.replaceLastPoint(2.0, b);
            ex.extrapolate(0.0, target);
            assert target.equals(b);
            ex.extrapolate(-2.0, target);
            assert target.equals(a);

            ex.addPoint(0, new Vector(0, 0, 0));
            ex.addPoint(1, new Vector(1, 1, 1));
            ex.addPoint(1, new Vector(2, 4, -4));

            // test some points on a parabola
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